package visualization;

import evolution.SimulationSnapshot;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import phylogenetics.PopulationGeneticsTracker;
import java.util.Map;

/** Live fitness, diversity, frequency, Hardy-Weinberg and genotype-distribution panels. */
public class StatsPane extends VBox {
    private final Label summary = new Label("Generation 0");
    private final XYChart.Series<Number, Number> allele = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> expectedHet = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> observedHet = new XYChart.Series<>();
    private final XYChart.Series<String, Number> genotypes = new XYChart.Series<>();
    private final LineChart<Number, Number> lineChart;
    private final BarChart<String, Number> barChart;

    @SuppressWarnings("this-escape")
    public StatsPane() {
        getStyleClass().add("stats-pane");
        setPadding(new Insets(10)); setSpacing(6);
        NumberAxis x = new NumberAxis(); NumberAxis y = new NumberAxis(0, 1, 0.1);
        lineChart = new LineChart<>(x, y); lineChart.setTitle("Allele frequency / heterozygosity (H-W expected and observed)");
        lineChart.setAnimated(false); lineChart.setLegendVisible(true);
        allele.setName("Dark variant frequency"); expectedHet.setName("Expected heterozygosity"); observedHet.setName("Observed heterozygosity");
        lineChart.getData().add(allele); lineChart.getData().add(expectedHet); lineChart.getData().add(observedHet);
        barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        barChart.setTitle("Genotype distribution"); barChart.setAnimated(false); barChart.setLegendVisible(false); barChart.getData().add(genotypes);
        HBox charts = new HBox(8, lineChart, barChart); HBox.setHgrow(lineChart, Priority.ALWAYS); HBox.setHgrow(barChart, Priority.ALWAYS);
        setMinHeight(210); setPrefHeight(250);
        charts.setMinHeight(170);
        VBox.setVgrow(charts, Priority.ALWAYS);
        lineChart.setMinHeight(160); barChart.setMinHeight(160);
        lineChart.setPrefHeight(205); barChart.setPrefHeight(205);
        lineChart.getStyleClass().add("glass-chart");
        barChart.getStyleClass().add("glass-chart");
        getChildren().addAll(summary, charts);
    }

    public void update(SimulationSnapshot snapshot, PopulationGeneticsTracker tracker) {
        summary.setText(String.format(java.util.Locale.ROOT,
            "Generation %d    population %d    mean fitness %.3f    mean pairwise distance %.3f",
            snapshot.generation(), snapshot.population().size(), snapshot.meanFitness(), snapshot.geneticDiversity()));
        if (tracker.getLocusCount() == 0) return;
        var stats = tracker.getSnapshot(snapshot.generation(), 0);
        double darkFrequency = stats.alleleFrequencies.entrySet().stream()
            .filter(e -> e.getKey().startsWith("Dark")).mapToDouble(Map.Entry::getValue).sum();
        double observed = stats.observedGenotypeRatios.entrySet().stream()
            .filter(e -> !isHomozygous(e.getKey())).mapToDouble(Map.Entry::getValue).sum();
        double expected = stats.expectedGenotypeRatios.entrySet().stream()
            .filter(e -> !isHomozygous(e.getKey())).mapToDouble(Map.Entry::getValue).sum();
        allele.getData().add(new XYChart.Data<>(snapshot.generation(), darkFrequency));
        expectedHet.getData().add(new XYChart.Data<>(snapshot.generation(), expected));
        observedHet.getData().add(new XYChart.Data<>(snapshot.generation(), observed));
        trim(allele); trim(expectedHet); trim(observedHet);
        genotypes.getData().clear();
        stats.observedGenotypeRatios.forEach((key, value) -> genotypes.getData().add(new XYChart.Data<>(key, value * snapshot.population().size())));
    }

    public void clearHistory() {
        allele.getData().clear();
        expectedHet.getData().clear();
        observedHet.getData().clear();
        genotypes.getData().clear();
    }

    private static boolean isHomozygous(String genotype) {
        int slash = genotype.indexOf('/'); return slash >= 0 && genotype.substring(0, slash).equals(genotype.substring(slash + 1));
    }
    private static void trim(XYChart.Series<Number, Number> series) { if (series.getData().size() > 100) series.getData().remove(0); }
}
