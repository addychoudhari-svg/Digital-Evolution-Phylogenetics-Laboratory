package core;

import environment.LifeStage;
import environment.Behavior;
import genetics.Genotype;
import genetics.Phenotype;
import java.util.concurrent.atomic.AtomicInteger;

public class Organism {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int id;
    private final int parent1Id;
    private final int parent2Id;
    private final int generation;
    private final String regionName;
    private final SpeciesProfile speciesProfile;
    private final Genotype genotype;
    private LifeStage lifeStage;
    private Behavior behavior;

    public Organism(Genotype genotype) {
        this(NEXT_ID.getAndIncrement(), 0, 0, 0, "World", genotype);
    }

    public Organism(int id, int parent1Id, int parent2Id, int generation, String regionName, Genotype genotype) {
        this(id, parent1Id, parent2Id, generation, regionName, genotype, SpeciesType.ANIMAL);
    }

    public Organism(int id, int parent1Id, int parent2Id, int generation, String regionName,
                    Genotype genotype, SpeciesType speciesType) {
        this(id, parent1Id, parent2Id, generation, regionName, genotype, SpeciesCatalog.defaultFor(speciesType));
    }

    public Organism(int id, int parent1Id, int parent2Id, int generation, String regionName,
                    Genotype genotype, SpeciesProfile speciesProfile) {
        this.id = id == 0 ? NEXT_ID.getAndIncrement() : id;
        this.parent1Id = parent1Id;
        this.parent2Id = parent2Id;
        this.generation = generation;
        this.regionName = regionName;
        this.genotype = genotype;
        this.speciesProfile = java.util.Objects.requireNonNull(speciesProfile, "speciesProfile");
        this.lifeStage = LifeStage.JUVENILE; // everyone starts as a baby
        NEXT_ID.accumulateAndGet(this.id + 1, Math::max);
    }

    public int getId() { return id; }
    public int getParent1Id() { return parent1Id; }
    public int getParent2Id() { return parent2Id; }
    public int getGeneration() { return generation; }
    public String getRegionName() { return regionName; }
    public SpeciesType getSpeciesType() { return speciesProfile.kingdom(); }
    public SpeciesProfile getSpeciesProfile() { return speciesProfile; }

    public void ageUp() {
        this.lifeStage = this.lifeStage.next();
    }

    public boolean canReproduce() {
        return lifeStage.canReproduce();
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public Phenotype getPhenotype() { return genotype.getPhenotype(); }

    public LifeStage getLifeStage() {
        return lifeStage;
    }

    public void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    @Override
    public String toString() {
        return "Organism#" + id;
    }
}
