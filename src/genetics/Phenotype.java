package genetics;

import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable expressed traits computed from an organism's genotype. */
public final class Phenotype {
    private final Map<String, Double> traits;
    public Phenotype(Map<String, Double> traits) { this.traits = Map.copyOf(new LinkedHashMap<>(traits)); }
    public double getTrait(String name) { return traits.getOrDefault(name, 0.0); }
    public Map<String, Double> getTraits() { return traits; }
}
