package genetics;

public class Allele {
    private final String variantName;
    private final double effectValue;
    private final boolean dominant;

    public Allele(String variantName, double effectValue, boolean dominant) {
        if (variantName == null || variantName.isBlank()) throw new IllegalArgumentException("Allele variant name is required");
        if (!Double.isFinite(effectValue)) throw new IllegalArgumentException("Allele effect must be finite");
        this.variantName = variantName;
        this.effectValue = effectValue;
        this.dominant = dominant;
    }

    public String getVariantName() { return variantName; }
    public double getEffectValue() { return effectValue; }
    public boolean isDominant() { return dominant; }
}
