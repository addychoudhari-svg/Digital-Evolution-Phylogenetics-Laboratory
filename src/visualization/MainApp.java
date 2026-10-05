package visualization;

import core.Organism;
import core.SpeciesType;
import core.SpeciesProfile;
import core.SpeciesCatalog;
import config.SimulationConfig;
import environment.Region;
import evolution.EvolutionEngine;
import evolution.ExperimentController;
import evolution.FitnessFunction;
import evolution.PopulationFactory;
import evolution.SimulationSnapshot;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import genetics.AncestryRecorder;
import phylogenetics.DistanceMatrix;
import phylogenetics.PhylogeneticTree;
import phylogenetics.PopulationGeneticsTracker;
import phylogenetics.TreeComparator;
import phylogenetics.UPGMAAlgorithm;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** JavaFX workbench connected to the live simulation and analysis modules. */
public class MainApp extends Application {
    private final AtomicBoolean autoRunning = new AtomicBoolean(false);
    private final AtomicInteger delayMillis = new AtomicInteger(300);
    private final AtomicInteger simulationEpoch = new AtomicInteger();
    private EvolutionEngine engine;
    private ExperimentController controller;
    private PopulationGeneticsTracker tracker;
    private EcosystemPane ecosystemPane;
    private TreePane treePane;
    private StatsPane statsPane;
    private GenerationComparisonPane comparisonPane;
    private ToggleButton autoButton;
    private final ComboBox<Integer> generationPicker = new ComboBox<>();
    private final Label liveGeneration = new Label("Live generation 0");
    private final Label viewingGeneration = new Label("Viewing live population");
    private ComboBox<SpeciesType> kingdomPicker;
    private ComboBox<String> categoryPicker;
    private ComboBox<SpeciesProfile> speciesPicker;
    private boolean updatingSpeciesSelectors;
    private Region region;
    private SimulationConfig config;
    private AncestryRecorder ancestry;
    private SimulationSnapshot currentSnapshot;
    private Map<Integer, SimulationSnapshot> generationHistory = new ConcurrentSkipListMap<>();
    private boolean viewingHistory;

    @Override
    public void start(Stage stage) {
        try {
            config = SimulationConfig.load(java.nio.file.Path.of("config", "simulation.properties"));
            SpeciesCatalog.load(java.nio.file.Path.of("config", "species-catalog.csv"));
        }
        catch (java.io.IOException ex) { throw new IllegalStateException("Could not read simulation configuration or species catalog", ex); }
        ecosystemPane = new EcosystemPane(); treePane = new TreePane(); statsPane = new StatsPane();
        comparisonPane = new GenerationComparisonPane();
        initializeSimulation(SpeciesCatalog.defaultFor(SpeciesType.ANIMAL));
        comparisonPane.setViewGenerationAction(generation -> {
            SimulationSnapshot selected = generationHistory.get(generation);
            if (selected != null) {
                viewingHistory = generation != currentSnapshot.generation();
                viewingGeneration.setText(viewingHistory ? "Viewing generation " + generation : "Viewing live population");
                render(selected);
            }
        });
        GridPane overview = createOverview();
        Tab overviewTab = createViewTab("Overview", overview);
        Tab comparisonTab = createViewTab("Generation comparison", comparisonPane);
        TabPane views = new TabPane(overviewTab, comparisonTab);
        views.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
            comparisonPane.setActive(selected == comparisonTab));
        views.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setCenter(views);
        root.setTop(createHeader());
        stage.setScene(new Scene(root, 1250, 850));
        var stylesheet = MainApp.class.getResource("evolab.css");
        if (stylesheet != null) stage.getScene().getStylesheets().add(stylesheet.toExternalForm());
        stage.setTitle("EvoLab — Digital Evolution & Phylogenetics Laboratory");
        stage.setMinWidth(1024);
        stage.setMinHeight(720);
        stage.show();
        stage.setMaximized(true);
    }

    private GridPane createOverview() {
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new javafx.geometry.Insets(12));
        ColumnConstraints left = new ColumnConstraints(); left.setPercentWidth(50); left.setHgrow(Priority.ALWAYS);
        ColumnConstraints right = new ColumnConstraints(); right.setPercentWidth(50); right.setHgrow(Priority.ALWAYS);
        RowConstraints visuals = new RowConstraints(); visuals.setPercentHeight(58); visuals.setVgrow(Priority.ALWAYS);
        RowConstraints analytics = new RowConstraints(); analytics.setPercentHeight(42); analytics.setVgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(left, right);
        grid.getRowConstraints().addAll(visuals, analytics);
        ecosystemPane.getStyleClass().add("glass-panel");
        treePane.getStyleClass().add("glass-panel");
        statsPane.getStyleClass().add("glass-panel");
        ecosystemPane.setMinSize(280, 230); treePane.setMinSize(280, 230); statsPane.setMinHeight(220);
        grid.add(ecosystemPane, 0, 0);
        grid.add(treePane, 1, 0);
        grid.add(statsPane, 0, 1, 2, 1);
        GridPane.setHgrow(ecosystemPane, Priority.ALWAYS); GridPane.setHgrow(treePane, Priority.ALWAYS);
        GridPane.setVgrow(ecosystemPane, Priority.ALWAYS); GridPane.setVgrow(treePane, Priority.ALWAYS);
        GridPane.setHgrow(statsPane, Priority.ALWAYS); GridPane.setVgrow(statsPane, Priority.ALWAYS);
        return grid;
    }

    private void initializeSimulation(SpeciesProfile species) {
        if (controller != null) controller.pause();
        autoRunning.set(false);
        simulationEpoch.incrementAndGet();
        if (autoButton != null) { autoButton.setSelected(false); autoButton.setText("Start auto"); }
        generationHistory = new ConcurrentSkipListMap<>();
        viewingHistory = false;
        region = new Region("World", config.terrain(), 1.0);
        ancestry = new AncestryRecorder();
        AncestryRecorder activeAncestry = ancestry;
        List<Organism> founders = PopulationFactory.create(config.populationSize(), config.initialDarkFrequency(),
            config.seed(), region.getName(), species);
        engine = new EvolutionEngine(founders, region, config.mutationRate(), config.seed(), ancestry);
        controller = new ExperimentController(engine);
        PopulationGeneticsTracker activeTracker = new PopulationGeneticsTracker();
        tracker = activeTracker;
        activeTracker.recordGeneration(0, founders.stream().map(Organism::getGenotype).toList());
        currentSnapshot = new SimulationSnapshot(0, founders, meanFitness(founders), 0);
        generationHistory.put(0, currentSnapshot);
        Map<Integer, SimulationSnapshot> activeHistory = generationHistory;
        EvolutionEngine activeEngine = engine;
        statsPane.clearHistory();
        engine.addListener(snapshot -> {
            activeTracker.recordGeneration(snapshot.generation(), snapshot.population().stream().map(Organism::getGenotype).toList());
            activeHistory.put(snapshot.generation(), snapshot);
            Platform.runLater(() -> {
                if (engine != activeEngine) return;
                currentSnapshot = snapshot;
                liveGeneration.setText("Live generation " + snapshot.generation());
                refreshGenerationPicker(snapshot.generation());
                if (!viewingHistory) { viewingGeneration.setText("Viewing live population"); render(snapshot); }
                comparisonPane.update(activeHistory, currentSnapshot, activeTracker, activeAncestry);
            });
        });
        if (ecosystemPane != null) render(currentSnapshot);
        liveGeneration.setText("Live generation 0");
        viewingGeneration.setText("Viewing live population");
        refreshGenerationPicker(0);
        if (comparisonPane != null) comparisonPane.update(activeHistory, currentSnapshot, activeTracker, activeAncestry);
    }

    private Tab createViewTab(String title, javafx.scene.Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Tab tab = new Tab(title, scroll);
        tab.setClosable(false);
        return tab;
    }

    private VBox createHeader() {
        kingdomPicker = new ComboBox<>(); kingdomPicker.getItems().addAll(SpeciesType.values());
        categoryPicker = new ComboBox<>(); speciesPicker = new ComboBox<>();
        kingdomPicker.setValue(SpeciesType.ANIMAL);
        populateCategories();
        populateSpecies();
        kingdomPicker.setOnAction(event -> {
            if (!updatingSpeciesSelectors) { populateCategories(); populateSpecies(); resetForSelectedSpecies(); }
        });
        categoryPicker.setOnAction(event -> {
            if (!updatingSpeciesSelectors) { populateSpecies(); resetForSelectedSpecies(); }
        });
        speciesPicker.setOnAction(event -> { if (!updatingSpeciesSelectors) resetForSelectedSpecies(); });
        kingdomPicker.setPrefWidth(100); categoryPicker.setPrefWidth(150); speciesPicker.setPrefWidth(260);
        generationPicker.setPrefWidth(115);
        generationPicker.setOnAction(event -> {});
        Button inspect = new Button("Inspect"); inspect.setOnAction(event -> {
            Integer generation = generationPicker.getValue();
            SimulationSnapshot selected = generation == null ? null : generationHistory.get(generation);
            if (selected != null) {
                viewingHistory = generation != currentSnapshot.generation();
                viewingGeneration.setText(viewingHistory ? "Viewing generation " + generation : "Viewing live population");
                render(selected);
            }
        });
        Button live = new Button("Return to live"); live.setOnAction(event -> {
            viewingHistory = false;
            viewingGeneration.setText("Viewing live population");
            if (currentSnapshot != null) render(currentSnapshot);
        });
        HBox speciesContext = new HBox(10, new Label("EVOLAB"), new Separator(), new Label("Kingdom"), kingdomPicker,
            new Label("Subcategory"), categoryPicker, new Label("Species"), speciesPicker);
        speciesContext.getStyleClass().add("glass-header-row");
        HBox generationContext = new HBox(10, liveGeneration, new Separator(), new Label("Inspect generation"), generationPicker,
            inspect, live, new Separator(), viewingGeneration);
        generationContext.getStyleClass().add("glass-header-row");
        HBox actions = createControls();
        actions.getStyleClass().add("glass-header-row");
        VBox header = new VBox(8, speciesContext, generationContext, actions);
        header.getStyleClass().add("glass-header");
        comparisonPane.getStyleClass().add("glass-panel");
        return header;
    }

    private void populateCategories() {
        updatingSpeciesSelectors = true;
        categoryPicker.getItems().setAll(SpeciesCatalog.subcategories(kingdomPicker.getValue()));
        if (!categoryPicker.getItems().isEmpty()) categoryPicker.setValue(categoryPicker.getItems().get(0));
        updatingSpeciesSelectors = false;
    }

    private void populateSpecies() {
        updatingSpeciesSelectors = true;
        speciesPicker.getItems().setAll(SpeciesCatalog.inCategory(kingdomPicker.getValue(), categoryPicker.getValue()));
        if (!speciesPicker.getItems().isEmpty()) speciesPicker.setValue(speciesPicker.getItems().get(0));
        updatingSpeciesSelectors = false;
    }

    private void resetForSelectedSpecies() {
        SpeciesProfile selected = speciesPicker == null ? null : speciesPicker.getValue();
        if (selected != null) initializeSimulation(selected);
    }

    private HBox createControls() {
        Button one = new Button("Run 1"); one.setOnAction(event -> { EvolutionEngine activeEngine = engine; activeEngine.step(); });
        Button ten = new Button("Run 10"); ten.setOnAction(event -> {
            ExperimentController activeController = controller;
            new Thread(() -> activeController.runGenerations(10), "evolab-run-10").start();
        });
        TextField count = new TextField("50"); count.setPrefColumnCount(5);
        Button runCount = new Button("Run N"); runCount.setOnAction(event -> {
            try {
                int n = Integer.parseInt(count.getText());
                ExperimentController activeController = controller;
                new Thread(() -> activeController.runGenerations(n), "evolab-run-n").start();
            }
            catch (NumberFormatException ex) { count.setText("50"); }
        });
        ComboBox<Integer> delay = new ComboBox<>(); delay.getItems().addAll(100, 300, 700); delay.setValue(delayMillis.get()); delay.setPrefWidth(90);
        delay.setOnAction(event -> delayMillis.set(delay.getValue()));
        ToggleButton auto = new ToggleButton("Start auto");
        autoButton = auto;
        auto.setOnAction(event -> {
            if (auto.isSelected()) {
                controller.resume(); auto.setText("Pause"); autoRunning.set(true);
                int epoch = simulationEpoch.get();
                ExperimentController activeController = controller;
                EvolutionEngine activeEngine = engine;
                Thread loop = new Thread(() -> { while (autoRunning.get() && epoch == simulationEpoch.get()) {
                    if (!activeController.isPaused()) activeEngine.step();
                    try { Thread.sleep(delayMillis.get()); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); break; }
                } }, "evolab-auto"); loop.setDaemon(true); loop.start();
            } else { autoRunning.set(false); controller.pause(); auto.setText("Start auto"); }
        });
        HBox controls = new HBox(8, one, ten, new Label("Run generations"), count, runCount, new Separator(), auto,
            new Label("Delay ms"), delay);
        controls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        count.setPrefWidth(72);
        return controls;
    }

    private void refreshGenerationPicker(int latest) {
        Integer selected = generationPicker.getValue();
        generationPicker.setOnAction(null);
        generationPicker.getItems().setAll(generationHistory.keySet());
        generationPicker.setValue(selected != null && generationHistory.containsKey(selected) ? selected : latest);
        generationPicker.setOnAction(event -> {});
    }

    private void render(SimulationSnapshot snapshot) {
        ecosystemPane.update(snapshot.population(), region.getTerrainColor());
        PhylogeneticTree inferred = new UPGMAAlgorithm().buildTree(DistanceMatrix.fromOrganisms(snapshot.population()));
        PhylogeneticTree truth = engine.getAncestryRecorder().maternalProjection(
            snapshot.population().stream().map(Organism::getId).toList());
        treePane.update(inferred, TreeComparator.robinsonFoulds(truth, inferred));
        statsPane.update(snapshot, tracker);
    }

    private double meanFitness(List<Organism> organisms) {
        FitnessFunction fitness = FitnessFunction.terrainMatch();
        return organisms.stream().mapToDouble(o -> fitness.fitness(o, region)).average().orElse(0);
    }

    @Override public void stop() { autoRunning.set(false); if (controller != null) controller.pause(); }
    public static void main(String[] args) { launch(args); }
}
