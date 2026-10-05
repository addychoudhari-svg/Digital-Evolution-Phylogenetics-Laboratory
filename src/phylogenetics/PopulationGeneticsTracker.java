package phylogenetics;

import genetics.Genotype;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Tracks allele frequencies and Hardy-Weinberg genotype frequencies per locus. */
public final class PopulationGeneticsTracker {
    public static final class GenerationSnapshot {
        public final int generation;
        public final int locusIndex;
        public final Map<String, Double> alleleFrequencies;
        public final Map<String, Double> observedGenotypeRatios;
        public final Map<String, Double> expectedGenotypeRatios;

        GenerationSnapshot(int generation, int locusIndex, Map<String, Double> alleleFrequencies,
                          Map<String, Double> observed, Map<String, Double> expected) {
            this.generation = generation;
            this.locusIndex = locusIndex;
            this.alleleFrequencies = Map.copyOf(alleleFrequencies);
            this.observedGenotypeRatios = Map.copyOf(observed);
            this.expectedGenotypeRatios = Map.copyOf(expected);
        }
    }

    private final Map<Integer, Map<Integer, GenerationSnapshot>> history = new LinkedHashMap<>();

    public int getLocusCount() {
        return history.isEmpty() ? 0 : history.values().iterator().next().size();
    }

    public void recordGeneration(int generation, List<Genotype> population) {
        if (population == null || population.isEmpty()) throw new IllegalArgumentException("Population must not be empty");
        int loci = population.get(0).getLocusCount();
        if (population.stream().anyMatch(g -> g.getLocusCount() != loci)) throw new IllegalArgumentException("All genotypes need the same loci");
        List<String> locusNames = population.get(0).getGenesInOrder().stream().map(g -> g.getName()).toList();
        if (population.stream().anyMatch(g -> !g.getGenesInOrder().stream().map(locus -> locus.getName()).toList().equals(locusNames))) {
            throw new IllegalArgumentException("All genotypes must have identical loci");
        }
        Map<Integer, GenerationSnapshot> perLocus = new LinkedHashMap<>();
        for (int locus = 0; locus < loci; locus++) {
            Map<String, Double> alleleFreq = alleleFrequencies(population, locus);
            Map<String, Double> observed = observedRatios(population, locus);
            Map<String, Double> expected = expectedRatios(alleleFreq);
            perLocus.put(locus, new GenerationSnapshot(generation, locus, alleleFreq, observed, expected));
        }
        history.put(generation, Map.copyOf(perLocus));
    }

    private static Map<String, Double> alleleFrequencies(List<Genotype> population, int locus) {
        Map<String, Integer> counts = new TreeMap<>();
        for (Genotype genotype : population) for (String allele : genotype.getAlleleVariantsAt(locus)) counts.merge(allele, 1, Integer::sum);
        int total = population.size() * 2;
        Map<String, Double> frequencies = new LinkedHashMap<>();
        counts.forEach((allele, count) -> frequencies.put(allele, count / (double) total));
        return frequencies;
    }

    private static Map<String, Double> observedRatios(List<Genotype> population, int locus) {
        Map<String, Integer> counts = new TreeMap<>();
        for (Genotype genotype : population) {
            String[] pair = genotype.getAlleleVariantsAt(locus);
            counts.merge(key(pair[0], pair[1]), 1, Integer::sum);
        }
        Map<String, Double> ratios = new LinkedHashMap<>();
        counts.forEach((genotype, count) -> ratios.put(genotype, count / (double) population.size()));
        return ratios;
    }

    private static Map<String, Double> expectedRatios(Map<String, Double> frequencies) {
        List<String> alleles = new ArrayList<>(frequencies.keySet());
        Map<String, Double> expected = new LinkedHashMap<>();
        for (int i = 0; i < alleles.size(); i++) {
            String a = alleles.get(i); double p = frequencies.get(a);
            expected.put(key(a, a), p * p);
            for (int j = i + 1; j < alleles.size(); j++) {
                String b = alleles.get(j);
                expected.put(key(a, b), 2 * p * frequencies.get(b));
            }
        }
        return expected;
    }

    private static String key(String a, String b) { return a.compareTo(b) <= 0 ? a + "/" + b : b + "/" + a; }
    public GenerationSnapshot getSnapshot(int generation, int locus) {
        Map<Integer, GenerationSnapshot> perLocus = history.get(generation);
        if (perLocus == null || !perLocus.containsKey(locus)) throw new IllegalArgumentException("No data for generation/locus");
        return perLocus.get(locus);
    }
    public List<Integer> getRecordedGenerations() { return List.copyOf(history.keySet()); }
    public double computeDeviationFromEquilibrium(int generation, int locus) {
        GenerationSnapshot snapshot = getSnapshot(generation, locus);
        return snapshot.expectedGenotypeRatios.entrySet().stream().mapToDouble(e -> Math.abs(
            e.getValue() - snapshot.observedGenotypeRatios.getOrDefault(e.getKey(), 0.0))).sum();
    }
}
