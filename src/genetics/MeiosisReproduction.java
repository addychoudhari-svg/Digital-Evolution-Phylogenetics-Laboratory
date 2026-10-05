package genetics;

import java.util.Random;
import java.util.Set;

public class MeiosisReproduction implements ReproductionStrategy {
    private final double mutationRate;
    private final double mutationMagnitude;
    private final Random random;

    public MeiosisReproduction(double mutationRate, double mutationMagnitude) {
        this(mutationRate, mutationMagnitude, new Random());
    }

    public MeiosisReproduction(double mutationRate, double mutationMagnitude, long seed) {
        this(mutationRate, mutationMagnitude, new Random(seed));
    }

    private MeiosisReproduction(double mutationRate, double mutationMagnitude, Random random) {
        if (mutationRate < 0 || mutationRate > 1) throw new IllegalArgumentException("Mutation rate must be in [0,1]");
        if (mutationMagnitude < 0) throw new IllegalArgumentException("Mutation magnitude must be non-negative");
        this.mutationRate = mutationRate;
        this.mutationMagnitude = mutationMagnitude;
        this.random = random;
    }

    @Override
    public Genotype reproduce(Genotype parent1, Genotype parent2) {
        Genotype offspring = new Genotype();
        Set<Gene> genes = parent1.getGenes();

        for (Gene gene : genes) {
            Allele[] p1Pair = parent1.getAllelePair(gene);
            Allele[] p2Pair = parent2.getAllelePair(gene);

            Allele fromParent1 = p1Pair[random.nextBoolean() ? 0 : 1];
            Allele fromParent2 = p2Pair[random.nextBoolean() ? 0 : 1];

            offspring.setAllelePair(gene, maybeMutate(fromParent1), maybeMutate(fromParent2));
        }

        return offspring;
    }

    private Allele maybeMutate(Allele allele) {
        if (random.nextDouble() >= mutationRate) {
            return allele;
        }
        double delta = (random.nextDouble() * 2 - 1) * mutationMagnitude;
        double mutatedValue = allele.getEffectValue() + delta;
        return new Allele(allele.getVariantName() + "*", mutatedValue, allele.isDominant());
    }
}
