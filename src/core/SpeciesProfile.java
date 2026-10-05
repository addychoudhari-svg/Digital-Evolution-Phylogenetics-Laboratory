package core;

import java.util.Objects;

/** Taxonomic label and synthetic trait-locus choice for one selectable simulation profile. */
public record SpeciesProfile(SpeciesType kingdom, String subcategory, String commonName,
                             String scientificName, String traitGene) {
    public SpeciesProfile {
        Objects.requireNonNull(kingdom, "kingdom");
        if (subcategory == null || subcategory.isBlank() || commonName == null || commonName.isBlank()
            || scientificName == null || scientificName.isBlank() || traitGene == null || traitGene.isBlank())
            throw new IllegalArgumentException("Species profile fields are required");
    }

    public String displayName() { return commonName + " (" + scientificName + ")"; }
    @Override public String toString() { return displayName(); }
}
