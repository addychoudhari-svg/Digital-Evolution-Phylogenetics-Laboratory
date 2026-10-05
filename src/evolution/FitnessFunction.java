package evolution;

import core.Organism;
import environment.Region;

@FunctionalInterface
public interface FitnessFunction {
    double fitness(Organism organism, Region region);

    static FitnessFunction terrainMatch() {
        return (organism, region) -> {
            double target = region.getTerrainColor().equalsIgnoreCase("dark") ? 1.0 : 0.0;
            double phenotype = organism.getGenotype().getGenesInOrder().stream()
                .mapToDouble(organism.getGenotype()::getPhenotypeValue).findFirst().orElse(0.5);
            return Math.max(0.05, 1.0 - Math.min(1.0, Math.abs(target - phenotype)));
        };
    }

    static FitnessFunction combine(FitnessFunction... components) {
        FitnessFunction[] copy = components.clone();
        return (organism, region) -> {
            double total = 0;
            for (FitnessFunction component : copy) total += component.fitness(organism, region);
            return total;
        };
    }
}
