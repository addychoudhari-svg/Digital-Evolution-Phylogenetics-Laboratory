package genetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import phylogenetics.PhylogeneticTree;

public class AncestryRecorder {

    public static class BirthRecord {
        public final int childId;
        public final int parent1Id;
        public final int parent2Id;
        public final int generation;

        public BirthRecord(int childId, int parent1Id, int parent2Id, int generation) {
            this.childId = childId;
            this.parent1Id = parent1Id;
            this.parent2Id = parent2Id;
            this.generation = generation;
        }
    }

    private final List<BirthRecord> records = new ArrayList<>();

    public synchronized void recordBirth(int childId, int parent1Id, int parent2Id, int generation) {
        records.add(new BirthRecord(childId, parent1Id, parent2Id, generation));
    }

    public synchronized List<BirthRecord> getRecords() {
        return List.copyOf(records);
    }

    public synchronized List<Integer> getParentsOf(int childId) {
        for (BirthRecord r : records) {
            if (r.childId == childId) {
                return List.of(r.parent1Id, r.parent2Id);
            }
        }
        return List.of();
    }

    public synchronized BirthRecord getBirthRecord(int childId) {
        return records.stream().filter(record -> record.childId == childId).findFirst().orElse(null);
    }

    /** Creates a strict maternal-lineage tree for RF comparison; parent2 ancestry is omitted. */
    public synchronized PhylogeneticTree maternalProjection(List<Integer> sampledTips) {
        if (sampledTips == null || sampledTips.isEmpty()) throw new IllegalArgumentException("At least one tip is required");
        Set<Integer> selected = new HashSet<>(sampledTips);
        Map<Integer, List<Integer>> maternalChildren = new HashMap<>();
        Set<Integer> recordedChildren = new HashSet<>();
        Set<Integer> possibleParents = new TreeSet<>();
        for (BirthRecord record : records) {
            recordedChildren.add(record.childId);
            possibleParents.add(record.parent1Id);
            maternalChildren.computeIfAbsent(record.parent1Id, ignored -> new ArrayList<>()).add(record.childId);
        }
        List<Integer> roots = possibleParents.stream().filter(id -> !recordedChildren.contains(id)).toList();
        List<PhylogeneticTree.TreeNode> rootChildren = new ArrayList<>();
        for (int root : roots) {
            PhylogeneticTree.TreeNode projected = project(root, selected, maternalChildren);
            if (projected != null) rootChildren.add(projected);
        }
        if (rootChildren.isEmpty()) {
            for (int tip : sampledTips) rootChildren.add(new PhylogeneticTree.TreeNode(Integer.toString(tip)));
        }
        if (rootChildren.size() == 1) return new PhylogeneticTree(rootChildren.get(0));
        PhylogeneticTree.TreeNode root = new PhylogeneticTree.TreeNode("root");
        for (var child : rootChildren) { child.setBranchLength(1.0); root.addChild(child); }
        return new PhylogeneticTree(root);
    }

    private static PhylogeneticTree.TreeNode project(int id, Set<Integer> selected,
                                                       Map<Integer, List<Integer>> children) {
        if (selected.contains(id)) return new PhylogeneticTree.TreeNode(Integer.toString(id));
        List<PhylogeneticTree.TreeNode> descendants = new ArrayList<>();
        for (int child : children.getOrDefault(id, List.of())) {
            PhylogeneticTree.TreeNode projected = project(child, selected, children);
            if (projected != null) { projected.setBranchLength(1.0); descendants.add(projected); }
        }
        if (descendants.isEmpty()) return null;
        PhylogeneticTree.TreeNode node = new PhylogeneticTree.TreeNode("ancestor-" + id);
        descendants.forEach(node::addChild);
        return node;
    }
}
