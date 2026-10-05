package phylogenetics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Robinson-Foulds symmetric difference on unrooted, non-trivial bipartitions. */
public final class TreeComparator {
    private TreeComparator() {}

    public static int robinsonFoulds(PhylogeneticTree first, PhylogeneticTree second) {
        Set<String> firstTips = new TreeSet<>(first.getAllTaxa());
        Set<String> secondTips = new TreeSet<>(second.getAllTaxa());
        if (!firstTips.equals(secondTips)) throw new IllegalArgumentException("Trees must have identical tip labels");
        Set<String> a = splits(first.getRoot(), firstTips.size());
        Set<String> b = splits(second.getRoot(), secondTips.size());
        Set<String> aOnly = new HashSet<>(a); aOnly.removeAll(b);
        Set<String> bOnly = new HashSet<>(b); bOnly.removeAll(a);
        return aOnly.size() + bOnly.size();
    }

    private static Set<String> splits(PhylogeneticTree.TreeNode root, int totalTips) {
        Set<String> result = new HashSet<>();
        collect(root, root, totalTips, result);
        return result;
    }

    private static Set<String> collect(PhylogeneticTree.TreeNode node, PhylogeneticTree.TreeNode root,
                                       int totalTips, Set<String> splits) {
        Set<String> descendants = new TreeSet<>();
        if (node.isLeaf()) descendants.add(node.getLabel());
        else for (var child : node.getChildren()) descendants.addAll(collect(child, root, totalTips, splits));
        if (node != root && descendants.size() >= 2 && totalTips - descendants.size() >= 2) {
            Set<String> complement = new TreeSet<>(root.getLeafLabels());
            complement.removeAll(descendants);
            splits.add(canonical(descendants, complement));
        }
        return descendants;
    }

    private static String canonical(Set<String> a, Set<String> b) {
        List<String> left = List.copyOf(a), right = List.copyOf(b);
        List<String> chosen;
        if (left.size() != right.size()) chosen = left.size() < right.size() ? left : right;
        else chosen = String.join("\u0000", left).compareTo(String.join("\u0000", right)) <= 0 ? left : right;
        return String.join("\u0000", chosen);
    }
}
