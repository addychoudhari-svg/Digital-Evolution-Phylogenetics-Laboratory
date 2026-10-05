package phylogenetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Composite-pattern representation of a rooted phylogenetic tree.
 *
 * A single class plays both "leaf" and "composite" roles depending on whether
 * {@code children} is empty, which is the standard lightweight way to do
 * Composite in Java without a separate Leaf/Composite class split. This keeps
 * UPGMA's merge step simple: merging two nodes is just "new TreeNode with
 * these two children."
 *
 * This class is the shared boundary artifact between:
 *   - Module C (builds it via UPGMA/Neighbor-Joining from a DistanceMatrix)
 *   - Module B (owns the ground-truth AncestryRecorder tree used for RF scoring)
 * Both sides MUST agree on this shape before Week 3 integration.
 */
public class PhylogeneticTree {

    /** A single node — leaf (taxon) or internal (ancestor/merge point). */
    public static class TreeNode {
        private final String label;              // taxon id for leaves, synthetic id for internal nodes
        private final List<TreeNode> children;    // empty => leaf
        private double branchLength;              // distance from this node to its parent
        private double height;                    // UPGMA "age" of this node (0 for leaves)

        public TreeNode(String label) {
            this.label = label;
            this.children = new ArrayList<>();
            this.branchLength = 0.0;
            this.height = 0.0;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public String getLabel() {
            return label;
        }

        public List<TreeNode> getChildren() {
            return Collections.unmodifiableList(children);
        }

        public void addChild(TreeNode child) {
            children.add(child);
        }

        public double getBranchLength() {
            return branchLength;
        }

        public void setBranchLength(double branchLength) {
            this.branchLength = branchLength;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        /** All leaf labels in the subtree rooted at this node, left-to-right. */
        public List<String> getLeafLabels() {
            List<String> result = new ArrayList<>();
            collectLeafLabels(this, result);
            return result;
        }

        private static void collectLeafLabels(TreeNode node, List<String> out) {
            if (node.isLeaf()) {
                out.add(node.label);
                return;
            }
            for (TreeNode child : node.children) {
                collectLeafLabels(child, out);
            }
        }

        @Override
        public String toString() {
            return toNewick();
        }

        /** Newick format, e.g. ((A:1.0,B:1.0):0.5,C:1.5); — handy for debugging/logging. */
        public String toNewick() {
            StringBuilder sb = new StringBuilder();
            buildNewick(this, sb);
            return sb.append(";").toString();
        }

        private static void buildNewick(TreeNode node, StringBuilder sb) {
            if (node.isLeaf()) {
                sb.append(node.label);
            } else {
                sb.append("(");
                for (int i = 0; i < node.children.size(); i++) {
                    if (i > 0) sb.append(",");
                    buildNewick(node.children.get(i), sb);
                }
                sb.append(")");
            }
            if (node.branchLength > 0) {
                sb.append(":").append(String.format(Locale.ROOT, "%.4f", node.branchLength));
            }
        }
    }

    private final TreeNode root;

    public PhylogeneticTree(TreeNode root) {
        this.root = root;
    }

    public TreeNode getRoot() {
        return root;
    }

    public List<String> getAllTaxa() {
        return root.getLeafLabels();
    }

    public String toNewick() {
        return root.toNewick();
    }
}
