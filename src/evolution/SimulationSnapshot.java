package evolution;

import core.Organism;
import java.util.List;

public record SimulationSnapshot(int generation, List<Organism> population, double meanFitness,
                                 double geneticDiversity) {
    public SimulationSnapshot { population = List.copyOf(population); }
}
