package phylogenetics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Unweighted pair group method with arithmetic mean (UPGMA). */
public final class UPGMAAlgorithm implements TreeBuildingAlgorithm {
    private record Cluster(PhylogeneticTree.TreeNode node, double height, int size, Set<Integer> members) {}

    @Override
    public PhylogeneticTree buildTree(DistanceMatrix distances) {
        int n = distances.size();
        if (n == 0) throw new IllegalArgumentException("At least one taxon is required");
        List<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            var leaf = new PhylogeneticTree.TreeNode(distances.getTaxa().get(i));
            clusters.add(new Cluster(leaf, 0.0, 1, Set.of(i)));
        }

        while (clusters.size() > 1) {
            int bestI = 0, bestJ = 1;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double d = averageDistance(distances, clusters.get(i), clusters.get(j));
                    if (d < bestDistance) { bestDistance = d; bestI = i; bestJ = j; }
                }
            }

            Cluster a = clusters.get(bestI), b = clusters.get(bestJ);
            double height = bestDistance / 2.0;
            var parent = new PhylogeneticTree.TreeNode("node-" + clusters.size() + "-" + bestI + "-" + bestJ);
            setBranch(a.node(), Math.max(0.0, height - a.height()));
            setBranch(b.node(), Math.max(0.0, height - b.height()));
            parent.setHeight(height);
            parent.addChild(a.node());
            parent.addChild(b.node());
            Set<Integer> members = new HashSet<>(a.members());
            members.addAll(b.members());
            Cluster merged = new Cluster(parent, height, a.size() + b.size(), Set.copyOf(members));
            clusters.remove(bestJ);
            clusters.remove(bestI);
            clusters.add(merged);
        }
        return new PhylogeneticTree(clusters.get(0).node());
    }

    private static void setBranch(PhylogeneticTree.TreeNode node, double branchLength) {
        node.setBranchLength(branchLength);
    }

    private static double averageDistance(DistanceMatrix matrix, Cluster a, Cluster b) {
        double sum = 0;
        for (int i : a.members()) for (int j : b.members()) sum += matrix.getDistance(i, j);
        return sum / (a.size() * (double) b.size());
    }
}
