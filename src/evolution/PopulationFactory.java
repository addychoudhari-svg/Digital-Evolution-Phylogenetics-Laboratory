package evolution;

import core.Organism;
import core.SpeciesType;
import core.SpeciesProfile;
import core.SpeciesCatalog;
import genetics.AdditiveRule;
import genetics.Allele;
import genetics.Gene;
import genetics.Genotype;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Seeded construction of diploid founder organisms. */
public final class PopulationFactory {
    private PopulationFactory() {}

    public static List<Organism> create(int size, double darkAlleleFrequency, long seed, String regionName) {
        return create(size, darkAlleleFrequency, seed, regionName, SpeciesType.ANIMAL);
    }

    public static List<Organism> create(int size, double darkAlleleFrequency, long seed, String regionName,
                                        SpeciesType speciesType) {
        return create(size, darkAlleleFrequency, seed, regionName, SpeciesCatalog.defaultFor(speciesType));
    }

    public static List<Organism> create(int size, double darkAlleleFrequency, long seed, String regionName,
                                        SpeciesProfile speciesProfile) {
        if (size < 2) throw new IllegalArgumentException("Population size must be at least two");
        if (darkAlleleFrequency < 0 || darkAlleleFrequency > 1) throw new IllegalArgumentException("Allele frequency must be in [0,1]");
        Gene trait = new Gene(speciesProfile.traitGene(), new AdditiveRule());
        Random random = new Random(seed);
        List<Organism> population = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Allele first = allele(random.nextDouble() < darkAlleleFrequency);
            Allele second = allele(random.nextDouble() < darkAlleleFrequency);
            Genotype genotype = new Genotype();
            genotype.setAllelePair(trait, first, second);
            Organism founder = new Organism(0, 0, 0, 0, regionName, genotype, speciesProfile);
            founder.ageUp();
            population.add(founder);
        }
        return List.copyOf(population);
    }

    private static Allele allele(boolean dark) {
        return new Allele(dark ? "Dark" : "Light", dark ? 1.0 : 0.0, false);
    }
}
