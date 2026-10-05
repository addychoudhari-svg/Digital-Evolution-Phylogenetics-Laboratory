package experiment;

import core.Organism;
import environment.Region;
import evolution.EvolutionEngine;
import evolution.PopulationFactory;
import genetics.AncestryRecorder;
import phylogenetics.DistanceMatrix;
import phylogenetics.TreeComparator;
import phylogenetics.UPGMAAlgorithm;
import java.util.ArrayList;
import java.util.List;

/** Reproducible mutation-rate and zero-migration geographic-isolation scenarios. */
public final class Experiments {
    public record Result(String experiment, double parameter, double meanGeneticDiversity,
                         int rfDistance, double phenotypeDivergence) {}
    private Experiments() {}

    public static List<Result> mutationRateSweep(int populationSize, int generations, long seed) {
        return mutationRateSweep(populationSize, generations, 0.5, seed);
    }

    public static List<Result> mutationRateSweep(int populationSize, int generations, double initialDarkFrequency, long seed) {
        List<Result> results = new ArrayList<>();
        for (double rate : new double[]{0.001, 0.02, 0.15}) {
            AncestryRecorder ancestry = new AncestryRecorder();
            List<Organism> initial = PopulationFactory.create(populationSize, initialDarkFrequency, seed, "World");
            EvolutionEngine engine = new EvolutionEngine(initial, new Region("World", "dark", 1.0), rate, seed, ancestry);
            for (int i = 0; i < generations; i++) engine.step();
            List<Organism> finalPopulation = engine.getPopulation();
            var inferred = new UPGMAAlgorithm().buildTree(DistanceMatrix.fromOrganisms(finalPopulation));
            var truth = ancestry.maternalProjection(finalPopulation.stream().map(Organism::getId).toList());
            results.add(new Result("mutation-rate-sweep", rate, meanPairwise(finalPopulation),
                TreeComparator.robinsonFoulds(truth, inferred), 0));
        }
        return List.copyOf(results);
    }

    public static List<Result> geographicIsolation(int populationSize, int generations, long seed) {
        return geographicIsolation(populationSize, generations, 0.5, 0.02, seed);
    }

    public static List<Result> geographicIsolation(int populationSize, int generations, double initialDarkFrequency,
                                                   double mutationRate, long seed) {
        if (populationSize < 4 || populationSize % 2 != 0) throw new IllegalArgumentException("Use an even population size of at least four");
        List<Organism> founders = PopulationFactory.create(populationSize, initialDarkFrequency, seed, "ancestral");
        int half = populationSize / 2;
        List<Organism> western = new ArrayList<>(), eastern = new ArrayList<>();
        for (int i = 0; i < founders.size(); i++) {
            Organism founder = founders.get(i);
            Organism copy = new Organism(0, 0, 0, 0, i < half ? "west" : "east", founder.getGenotype().copy());
            copy.ageUp(); (i < half ? western : eastern).add(copy);
        }
        AncestryRecorder westAncestry = new AncestryRecorder(), eastAncestry = new AncestryRecorder();
        EvolutionEngine west = new EvolutionEngine(western, new Region("West", "light", 1), mutationRate, seed, westAncestry);
        EvolutionEngine east = new EvolutionEngine(eastern, new Region("East", "dark", 1), mutationRate, seed + 1, eastAncestry);
        for (int i = 0; i < generations; i++) { west.step(); east.step(); }
        double divergence = Math.abs(meanCoat(west.getPopulation()) - meanCoat(east.getPopulation()));
        double diversity = (meanPairwise(west.getPopulation()) + meanPairwise(east.getPopulation())) / 2;
        return List.of(new Result("geographic-isolation", 0, diversity, -1, divergence));
    }

    private static double meanPairwise(List<Organism> population) {
        var matrix = DistanceMatrix.fromOrganisms(population); double sum = 0; int pairs = 0;
        for (int i = 0; i < population.size(); i++) for (int j = i + 1; j < population.size(); j++) { sum += matrix.getDistance(i, j); pairs++; }
        return pairs == 0 ? 0 : sum / pairs;
    }
    private static double meanCoat(List<Organism> population) {
        return population.stream().mapToDouble(o -> o.getGenotype().getGenesInOrder().stream()
            .filter(g -> g.getName().equalsIgnoreCase("coat"))
            .mapToDouble(o.getGenotype()::getPhenotypeValue).findFirst().orElse(0.5)).average().orElse(0);
    }
}
