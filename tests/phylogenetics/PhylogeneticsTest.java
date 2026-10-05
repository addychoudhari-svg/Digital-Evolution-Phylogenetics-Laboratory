package phylogenetics;

import genetics.AdditiveRule;
import genetics.Allele;
import genetics.Gene;
import genetics.Genotype;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PhylogeneticsTest {
    private static Genotype genotype(Gene gene, String first, String second) {
        Genotype genotype = new Genotype();
        genotype.setAllelePair(gene, new Allele(first, first.equals("Dark") ? 1 : 0, false),
            new Allele(second, second.equals("Dark") ? 1 : 0, false));
        return genotype;
    }

    private static PhylogeneticTree tree(String[] left, String[] right) {
        var root = new PhylogeneticTree.TreeNode("root");
        for (String[] pair : List.of(left, right)) {
            var clade = new PhylogeneticTree.TreeNode("internal");
            for (String tip : pair) { var leaf = new PhylogeneticTree.TreeNode(tip); leaf.setBranchLength(1); clade.addChild(leaf); }
            clade.setBranchLength(1); root.addChild(clade);
        }
        return new PhylogeneticTree(root);
    }

    @Test void distanceMatrixValidatesSymmetryAndKeepsTaxa() {
        DistanceMatrix matrix = new DistanceMatrix(new double[][]{{0, 2}, {2, 0}}, List.of("A", "B"));
        assertEquals(2, matrix.getDistance(0, 1));
        assertEquals(matrix.getDistance(0, 1), matrix.getDistance(1, 0));
        assertEquals(0, matrix.getDistance(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(new double[][]{{0, 1}, {2, 0}}, List.of("A", "B")));
    }

    @Test void upgmaClustersNearestPairsAndRfIsZeroAgainstSelf() {
        DistanceMatrix matrix = new DistanceMatrix(new double[][]{
            {0, 2, 8, 8}, {2, 0, 8, 8}, {8, 8, 0, 2}, {8, 8, 2, 0}
        }, List.of("A", "B", "C", "D"));
        PhylogeneticTree tree = new UPGMAAlgorithm().buildTree(matrix);
        assertEquals(Set.of("A", "B", "C", "D"), Set.copyOf(tree.getAllTaxa()));
        assertEquals(0, TreeComparator.robinsonFoulds(tree, tree));
        assertEquals(Set.of(Set.of("A", "B"), Set.of("C", "D")),
            tree.getRoot().getChildren().stream().map(n -> Set.copyOf(n.getLeafLabels())).collect(java.util.stream.Collectors.toSet()));
        assertTrue(tree.toNewick().endsWith(";"));
    }

    @Test void rfDistanceCountsDifferentUnrootedSplits() {
        assertEquals(2, TreeComparator.robinsonFoulds(tree(new String[]{"A", "B"}, new String[]{"C", "D"}),
            tree(new String[]{"A", "C"}, new String[]{"B", "D"})));
    }

    @Test void genotypeDistanceIgnoresAlleleOrdering() {
        Gene coat = new Gene("coat", new AdditiveRule());
        DistanceMatrix matrix = new DistanceMatrix(List.of(genotype(coat, "Light", "Dark"), genotype(coat, "Dark", "Light")));
        assertEquals(0, matrix.getDistance(0, 1));
    }

    @Test void trackerComputesFrequenciesAndExpectedHeterozygosity() {
        Gene coat = new Gene("coat", new AdditiveRule());
        List<Genotype> population = List.of(genotype(coat, "Dark", "Dark"), genotype(coat, "Light", "Light"));
        PopulationGeneticsTracker tracker = new PopulationGeneticsTracker();
        tracker.recordGeneration(0, population);
        var snapshot = tracker.getSnapshot(0, 0);
        assertEquals(0.5, snapshot.alleleFrequencies.get("Dark"));
        assertEquals(0.5, snapshot.expectedGenotypeRatios.get("Dark/Light"));
        assertEquals(1.0, tracker.computeDeviationFromEquilibrium(0, 0));
    }
}
