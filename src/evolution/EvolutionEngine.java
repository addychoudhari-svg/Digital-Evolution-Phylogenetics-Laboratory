package evolution;

import core.Organism;
import environment.Region;
import genetics.AncestryRecorder;
import genetics.Genotype;
import genetics.MeiosisReproduction;
import genetics.ReproductionStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/** Discrete-generation engine with fitness-weighted parent sampling and observer snapshots. */
public final class EvolutionEngine {
    private List<Organism> population;
    private final Region region;
    private final double mutationRate;
    private final Random random;
    private final ReproductionStrategy reproduction;
    private final FitnessFunction fitness = FitnessFunction.terrainMatch();
    private final AncestryRecorder ancestry;
    private final List<SimulationListener> listeners = new CopyOnWriteArrayList<>();
    private int generation;

    public EvolutionEngine(List<Organism> founders, Region region, double mutationRate, long seed,
                           AncestryRecorder ancestry) {
        if (founders == null || founders.size() < 2) throw new IllegalArgumentException("At least two founders are required");
        if (mutationRate < 0 || mutationRate > 1) throw new IllegalArgumentException("Mutation rate must be in [0,1]");
        this.population = new ArrayList<>(founders);
        this.region = region;
        this.mutationRate = mutationRate;
        this.random = new Random(seed);
        this.reproduction = new MeiosisReproduction(mutationRate, 0.1, seed ^ 0x5DEECE66DL);
        this.ancestry = ancestry;
    }

    public synchronized SimulationSnapshot step() {
        double[] weights = new double[population.size()];
        double totalFitness = 0;
        for (int i = 0; i < population.size(); i++) {
            weights[i] = population.get(i).canReproduce() ? fitness.fitness(population.get(i), region) : 0;
            totalFitness += weights[i];
        }
        if (totalFitness <= 0) throw new IllegalStateException("No reproductively mature organisms in the population");
        double meanFitness = totalFitness / population.size();
        List<Organism> next = new ArrayList<>(population.size());
        int nextGeneration = generation + 1;
        while (next.size() < population.size()) {
            Organism first = select(weights, totalFitness, -1);
            Organism second = select(weights, totalFitness, first.getId());
            Genotype childGenotype = reproduction.reproduce(first.getGenotype(), second.getGenotype());
            Organism child = new Organism(0, first.getId(), second.getId(), nextGeneration,
                region.getName(), childGenotype, first.getSpeciesProfile());
            child.ageUp();
            ancestry.recordBirth(child.getId(), first.getId(), second.getId(), nextGeneration);
            next.add(child);
        }
        population = next;
        generation = nextGeneration;
        SimulationSnapshot snapshot = new SimulationSnapshot(generation, population, meanFitness, diversity(population));
        listeners.forEach(listener -> listener.onGeneration(snapshot));
        return snapshot;
    }

    private Organism select(double[] weights, double total, int excludedId) {
        double eligibleTotal = total;
        if (excludedId >= 0) for (int i = 0; i < weights.length; i++) if (population.get(i).getId() == excludedId) eligibleTotal -= weights[i];
        if (eligibleTotal <= 0) throw new IllegalStateException("At least two fertile organisms are required");
        double choice = random.nextDouble() * eligibleTotal;
        for (int i = 0; i < weights.length; i++) {
            if (population.get(i).getId() == excludedId) continue;
            choice -= weights[i];
            if (choice <= 0) return population.get(i);
        }
        for (int i = population.size() - 1; i >= 0; i--) {
            if (population.get(i).getId() != excludedId && weights[i] > 0) return population.get(i);
        }
        throw new IllegalStateException("No eligible parent available");
    }

    private static double diversity(List<Organism> organisms) {
        if (organisms.size() < 2) return 0;
        phylogenetics.DistanceMatrix distances = phylogenetics.DistanceMatrix.fromOrganisms(organisms);
        double sum = 0; int pairs = 0;
        for (int i = 0; i < organisms.size(); i++) for (int j = i + 1; j < organisms.size(); j++) {
            sum += distances.getDistance(i, j);
            pairs++;
        }
        return sum / pairs;
    }

    public void addListener(SimulationListener listener) { listeners.add(listener); }
    public synchronized List<Organism> getPopulation() { return List.copyOf(population); }
    public synchronized int getGeneration() { return generation; }
    public AncestryRecorder getAncestryRecorder() { return ancestry; }
    public double getMutationRate() { return mutationRate; }
}
