package visualization;

import core.Organism;
import evolution.SimulationSnapshot;
import genetics.AncestryRecorder;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import phylogenetics.PopulationGeneticsTracker;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

/** Historical generation, genotype, and recorded two-parent ancestry comparison. */
public final class GenerationComparisonPane extends VBox {
    private final ComboBox<Integer> baselineGeneration = new ComboBox<>();
    private final ComboBox<Integer> baselineOrganism = new ComboBox<>();
    private final ComboBox<Integer> currentOrganism = new ComboBox<>();
    private final TextArea report = new TextArea();
    private final TextArea familyReport = new TextArea();
    private Map<Integer, SimulationSnapshot> history = Map.of();
    private SimulationSnapshot current;
    private PopulationGeneticsTracker tracker;
    private AncestryRecorder ancestry;
    private IntConsumer viewGeneration = ignored -> {};
    private boolean active;

    public GenerationComparisonPane() {
        getStyleClass().add("comparison-pane");
        setPadding(new Insets(12));
        setSpacing(8);
        baselineGeneration.setPromptText("Earlier generation");
        baselineOrganism.setPromptText("Earlier individual");
        currentOrganism.setPromptText("Current individual");
        Button show = new Button("Inspect generation");
        show.setOnAction(event -> {
            Integer generation = baselineGeneration.getValue();
            if (generation != null) viewGeneration.accept(generation);
        });
        Button buildFamily = new Button("Build full family hierarchies");
        buildFamily.setOnAction(event -> buildFamilyHierarchies());
        baselineGeneration.setOnAction(event -> { refreshIndividuals(); refresh(); clearFamily(); });
        baselineOrganism.setOnAction(event -> { refresh(); clearFamily(); });
        currentOrganism.setOnAction(event -> { refresh(); clearFamily(); });
        HBox controls = new HBox(8, new Label("Earlier generation"), baselineGeneration,
            new Label("Earlier organism"), baselineOrganism, new Label("Current organism"), currentOrganism,
            show, buildFamily);
        report.setEditable(false);
        report.setWrapText(false);
        familyReport.setEditable(false);
        familyReport.setWrapText(false);
        report.getStyleClass().add("glass-text-area");
        familyReport.getStyleClass().add("glass-text-area");
        familyReport.setText("Choose organisms, then select Build full family hierarchies to expand their recorded ancestors.");
        VBox.setVgrow(report, Priority.ALWAYS);
        VBox.setVgrow(familyReport, Priority.ALWAYS);
        getChildren().addAll(new Label("Compare saved and live generations"), controls,
            new Label("Population and genotype comparison"), report,
            new Label("Recorded two-parent family hierarchy"), familyReport);
    }

    public void setViewGenerationAction(IntConsumer action) { viewGeneration = action; }

    public void update(Map<Integer, SimulationSnapshot> history, SimulationSnapshot current,
                       PopulationGeneticsTracker tracker, AncestryRecorder ancestry) {
        this.history = history;
        this.current = current;
        this.tracker = tracker;
        this.ancestry = ancestry;
        if (!active) return;
        refreshGenerationChoices();
        refreshIndividuals();
        refresh();
        clearFamily();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active && current != null) {
            refreshGenerationChoices();
            refreshIndividuals();
            refresh();
        }
    }

    private void refreshGenerationChoices() {
        Integer previous = baselineGeneration.getValue();
        baselineGeneration.setOnAction(null);
        baselineGeneration.setItems(FXCollections.observableArrayList(history.keySet().stream().sorted().toList()));
        if (previous != null && history.containsKey(previous)) baselineGeneration.setValue(previous);
        else baselineGeneration.setValue(current.generation() > 0 ? 0 : current.generation());
        baselineGeneration.setOnAction(event -> { refreshIndividuals(); refresh(); clearFamily(); });
    }

    private void refreshIndividuals() {
        if (!active || current == null) return;
        SimulationSnapshot earlier = history.get(baselineGeneration.getValue());
        if (earlier == null) return;
        Integer oldId = baselineOrganism.getValue();
        Integer newId = currentOrganism.getValue();
        baselineOrganism.setOnAction(null);
        currentOrganism.setOnAction(null);
        baselineOrganism.setItems(FXCollections.observableArrayList(earlier.population().stream().map(Organism::getId).toList()));
        currentOrganism.setItems(FXCollections.observableArrayList(current.population().stream().map(Organism::getId).toList()));
        baselineOrganism.setValue(oldId != null && baselineOrganism.getItems().contains(oldId)
            ? oldId : baselineOrganism.getItems().stream().findFirst().orElse(null));
        currentOrganism.setValue(newId != null && currentOrganism.getItems().contains(newId)
            ? newId : currentOrganism.getItems().stream().findFirst().orElse(null));
        baselineOrganism.setOnAction(event -> { refresh(); clearFamily(); });
        currentOrganism.setOnAction(event -> { refresh(); clearFamily(); });
    }

    private void refresh() {
        if (!active || current == null || tracker == null) return;
        SimulationSnapshot earlier = history.get(baselineGeneration.getValue());
        if (earlier == null) return;
        StringBuilder text = new StringBuilder();
        if (!earlier.population().isEmpty()) {
            var profile = earlier.population().get(0).getSpeciesProfile();
            text.append("Selected profile: ").append(profile.displayName()).append(" — ").append(profile.subcategory()).append("\n\n");
        }
        text.append("Generation ").append(earlier.generation()).append(" → ").append(current.generation()).append('\n');
        text.append(String.format(java.util.Locale.ROOT,
            "Mean fitness: %.3f → %.3f (Δ %+.3f)%nMean pairwise genetic distance: %.3f → %.3f (Δ %+.3f)%n",
            earlier.meanFitness(), current.meanFitness(), current.meanFitness() - earlier.meanFitness(),
            earlier.geneticDiversity(), current.geneticDiversity(), current.geneticDiversity() - earlier.geneticDiversity()));
        text.append("Dark-variant allele frequency: ")
            .append(String.format(java.util.Locale.ROOT, "%.3f → %.3f", frequency(earlier.generation()), frequency(current.generation())))
            .append("\n\n");
        Organism oldOrg = find(earlier, baselineOrganism.getValue());
        Organism newOrg = find(current, currentOrganism.getValue());
        if (oldOrg != null && newOrg != null) {
            text.append("INDIVIDUAL GENOTYPE COMPARISON\n")
                .append("Earlier #").append(oldOrg.getId()).append(": ").append(genotype(oldOrg)).append('\n')
                .append("Current #").append(newOrg.getId()).append(": ").append(genotype(newOrg)).append('\n');
        }
        report.setText(text.toString());
        report.positionCaret(0);
    }

    private void buildFamilyHierarchies() {
        SimulationSnapshot earlier = history.get(baselineGeneration.getValue());
        if (!active || earlier == null || current == null || ancestry == null) return;
        Organism oldOrg = find(earlier, baselineOrganism.getValue());
        Organism newOrg = find(current, currentOrganism.getValue());
        if (oldOrg == null || newOrg == null) return;
        StringBuilder family = new StringBuilder();
        family.append("EARLIER GENERATION — organism #").append(oldOrg.getId()).append('\n');
        appendFamily(family, oldOrg.getId(), 0, new HashSet<>());
        family.append("\nCURRENT GENERATION — organism #").append(newOrg.getId()).append('\n');
        appendFamily(family, newOrg.getId(), 0, new HashSet<>());
        familyReport.setText(family.toString());
        familyReport.positionCaret(0);
    }

    private void clearFamily() {
        familyReport.setText("Selection changed. Select Build full family hierarchies to refresh both pedigrees.");
    }

    private double frequency(int generation) {
        try {
            return tracker.getSnapshot(generation, 0).alleleFrequencies.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("Dark"))
                .mapToDouble(Map.Entry::getValue).sum();
        } catch (IllegalArgumentException missing) { return 0; }
    }

    private static Organism find(SimulationSnapshot snapshot, Integer id) {
        return id == null ? null : snapshot.population().stream().filter(org -> org.getId() == id).findFirst().orElse(null);
    }

    private static String genotype(Organism organism) {
        return organism.getGenotype().getGenesInOrder().stream().map(gene -> {
            var pair = organism.getGenotype().getAllelePair(gene);
            return gene.getName() + "=" + pair[0].getVariantName() + "/" + pair[1].getVariantName();
        }).reduce((first, second) -> first + ", " + second).orElse("(no loci)");
    }

    private void appendFamily(StringBuilder text, int id, int depth, Set<Integer> visited) {
        text.append("  ".repeat(Math.min(depth, 30))).append("Organism #").append(id);
        if (!visited.add(id)) { text.append(" (already shown)\n"); return; }
        List<Integer> parents = ancestry.getParentsOf(id);
        if (parents.isEmpty()) { text.append(" (founder)\n"); return; }
        text.append('\n');
        appendFamily(text, parents.get(0), depth + 1, visited);
        appendFamily(text, parents.get(1), depth + 1, visited);
    }
}
