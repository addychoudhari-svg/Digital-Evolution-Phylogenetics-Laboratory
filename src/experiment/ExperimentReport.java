package experiment;

import config.SimulationConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Runs configured scenarios and writes CSV summaries to results/. */
public final class ExperimentReport {
    private ExperimentReport() {}
    public static void main(String[] args) throws IOException {
        SimulationConfig config = SimulationConfig.load(Path.of("config", "simulation.properties"));
        Path results = Path.of("results"); Files.createDirectories(results);
        write(results.resolve("mutation-rate-sweep.csv"), Experiments.mutationRateSweep(config.populationSize(), config.generations(), config.initialDarkFrequency(), config.seed()));
        write(results.resolve("geographic-isolation.csv"), Experiments.geographicIsolation(config.populationSize(), config.generations(),
            config.initialDarkFrequency(), config.mutationRate(), config.seed()));
    }
    private static void write(Path path, List<Experiments.Result> results) throws IOException {
        var lines = new java.util.ArrayList<String>();
        lines.add("experiment,parameter,mean_genetic_diversity,rf_distance,phenotype_divergence");
        results.forEach(r -> lines.add(r.experiment() + "," + r.parameter() + "," + r.meanGeneticDiversity() + "," +
            (r.rfDistance() < 0 ? "" : r.rfDistance()) + "," + r.phenotypeDivergence()));
        Files.write(path, lines);
    }
}
