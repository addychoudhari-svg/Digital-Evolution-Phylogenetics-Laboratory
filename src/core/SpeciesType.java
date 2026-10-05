package core;

/** Simplified visual/genetic profiles offered by the teaching simulation. */
public enum SpeciesType {
    ANIMAL("Animal", "coat"),
    PLANT("Plant", "leafPigment");

    private final String label;
    private final String traitGene;

    SpeciesType(String label, String traitGene) {
        this.label = label;
        this.traitGene = traitGene;
    }

    public String label() { return label; }
    public String traitGene() { return traitGene; }
    @Override public String toString() { return label; }
}
