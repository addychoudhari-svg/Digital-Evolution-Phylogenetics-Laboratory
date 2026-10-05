package phylogenetics;

/** Strategy boundary for reconstructing a tree from pairwise distances. */
public interface TreeBuildingAlgorithm {
    PhylogeneticTree buildTree(DistanceMatrix distanceMatrix);
}
