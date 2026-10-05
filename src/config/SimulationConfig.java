package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Runtime settings loaded from config/simulation.properties with safe defaults. */
public record SimulationConfig(int populationSize, double initialDarkFrequency, double mutationRate,
                               int generations, long seed, String terrain) {
    public SimulationConfig {
        if (populationSize < 2) throw new IllegalArgumentException("Population size must be at least two");
        if (!Double.isFinite(initialDarkFrequency) || initialDarkFrequency < 0 || initialDarkFrequency > 1) throw new IllegalArgumentException("Starting allele frequency must be in [0,1]");
        if (!Double.isFinite(mutationRate) || mutationRate < 0 || mutationRate > 1) throw new IllegalArgumentException("Mutation rate must be in [0,1]");
        if (generations < 1) throw new IllegalArgumentException("Generation count must be positive");
        if (terrain == null || terrain.isBlank()) throw new IllegalArgumentException("Terrain is required");
    }
    public static SimulationConfig load(Path path) throws IOException {
        Properties p = new Properties();
        if (Files.exists(path)) try (InputStream input = Files.newInputStream(path)) { p.load(input); }
        return new SimulationConfig(Integer.parseInt(p.getProperty("population.size", "30")),
            Double.parseDouble(p.getProperty("starting.dark.allele.frequency", "0.5")),
            Double.parseDouble(p.getProperty("mutation.rate", "0.02")),
            Integer.parseInt(p.getProperty("generations", "50")),
            Long.parseLong(p.getProperty("random.seed", "2026")), p.getProperty("environment.terrain", "dark"));
    }
}
