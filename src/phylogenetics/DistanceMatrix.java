package phylogenetics;

import core.Organism;
import genetics.Allele;
import genetics.Gene;
import genetics.Genotype;
import java.util.ArrayList;
import java.util.List;

/** Symmetric Hamming distance matrix over unordered diploid genotypes. */
public final class DistanceMatrix {
    private final double[][] matrix;
    private final List<String> taxa;

    public DistanceMatrix(List<Genotype> genotypes) {
        this(genotypes, defaultLabels(genotypes.size()));
    }

    public DistanceMatrix(List<Genotype> genotypes, List<String> taxa) {
        if (genotypes.size() != taxa.size()) throw new IllegalArgumentException("Taxon count must match genotype count");
        validateTaxa(taxa);
        this.taxa = List.copyOf(taxa);
        this.matrix = new double[genotypes.size()][genotypes.size()];
        for (int i = 0; i < genotypes.size(); i++) {
            for (int j = i + 1; j < genotypes.size(); j++) {
                double distance = distance(genotypes.get(i), genotypes.get(j));
                matrix[i][j] = distance;
                matrix[j][i] = distance;
            }
        }
    }

    public DistanceMatrix(double[][] distances, List<String> taxa) {
        if (distances.length != taxa.size()) throw new IllegalArgumentException("Taxon count must match matrix size");
        validateTaxa(taxa);
        for (double[] row : distances) if (row.length != distances.length) throw new IllegalArgumentException("Distance matrix must be square");
        this.matrix = new double[distances.length][distances.length];
        this.taxa = List.copyOf(taxa);
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances.length; j++) {
                double value = distances[i][j];
                if (!Double.isFinite(value) || value < 0) throw new IllegalArgumentException("Distances must be finite and non-negative");
                if (Math.abs(value - distances[j][i]) > 1e-9) throw new IllegalArgumentException("Distance matrix must be symmetric");
                if (i == j && value != 0) throw new IllegalArgumentException("Distance matrix diagonal must be zero");
                matrix[i][j] = value;
            }
        }
    }

    public static DistanceMatrix fromOrganisms(List<Organism> organisms) {
        return new DistanceMatrix(organisms.stream().map(Organism::getGenotype).toList(),
            organisms.stream().map(o -> Integer.toString(o.getId())).toList());
    }

    private static List<String> defaultLabels(int count) {
        var labels = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) labels.add("taxon-" + (i + 1));
        return labels;
    }

    private static void validateTaxa(List<String> taxa) {
        if (taxa.stream().anyMatch(label -> label == null || label.isBlank()) || taxa.stream().distinct().count() != taxa.size()) {
            throw new IllegalArgumentException("Taxon labels must be non-empty and unique");
        }
    }

    private static double distance(Genotype left, Genotype right) {
        List<Gene> genes = left.getGenesInOrder();
        if (genes.size() != right.getLocusCount()) throw new IllegalArgumentException("Genotypes must contain matching loci");
        int differences = 0;
        for (Gene gene : genes) {
            Gene otherGene = right.getGeneByName(gene.getName());
            if (otherGene == null) throw new IllegalArgumentException("Genotypes have different loci");
            Allele[] a = left.getAllelePair(gene), b = right.getAllelePair(otherGene);
            int straight = mismatch(a[0], b[0]) + mismatch(a[1], b[1]);
            int crossed = mismatch(a[0], b[1]) + mismatch(a[1], b[0]);
            differences += Math.min(straight, crossed);
        }
        return differences;
    }

    private static int mismatch(Allele a, Allele b) {
        return a.getVariantName().equals(b.getVariantName()) ? 0 : 1;
    }

    public double getDistance(int i, int j) { return matrix[i][j]; }
    public int size() { return matrix.length; }
    public List<String> getTaxa() { return taxa; }
    public double[][] getRawMatrix() { return java.util.Arrays.stream(matrix).map(double[]::clone).toArray(double[][]::new); }
}
