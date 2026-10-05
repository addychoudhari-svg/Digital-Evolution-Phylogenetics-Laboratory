package genetics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Genotype {
    private final Map<Gene, Allele[]> alleleMap = new HashMap<>();

    public void setAllelePair(Gene gene, Allele allele1, Allele allele2) {
        if (gene == null || allele1 == null || allele2 == null) {
            throw new IllegalArgumentException("Gene and both alleles are required");
        }
        Gene existing = getGeneByName(gene.getName());
        if (existing != null && existing != gene) throw new IllegalArgumentException("Duplicate locus name: " + gene.getName());
        alleleMap.put(gene, new Allele[]{allele1, allele2});
    }

    public double getPhenotypeValue(Gene gene) {
        Allele[] pair = alleleMap.get(gene);
        if (pair == null) {
            throw new IllegalArgumentException("Gene not present in genotype: " + gene.getName());
        }
        return gene.computePhenotype(pair[0], pair[1]);
    }

    public boolean hasGene(Gene gene) {
        return alleleMap.containsKey(gene);
    }

    public Allele[] getAllelePair(Gene gene) {
        Allele[] pair = alleleMap.get(gene);
        if (pair == null) {
            throw new IllegalArgumentException("Gene not present in genotype: " + gene.getName());
        }
        return pair.clone();
    }

    public Set<Gene> getGenes() {
        return Collections.unmodifiableSet(alleleMap.keySet());
    }

    public int getLocusCount() {
        return alleleMap.size();
    }

    public Phenotype getPhenotype() {
        Map<String, Double> traits = new HashMap<>();
        alleleMap.forEach((gene, pair) -> traits.put(gene.getName(), gene.computePhenotype(pair[0], pair[1])));
        return new Phenotype(traits);
    }

    public Gene getGeneByName(String name) {
        return alleleMap.keySet().stream()
            .filter(gene -> gene.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public String[] getAlleleVariantsAt(int locusIndex) {
        Gene gene = getGenesInOrder().get(locusIndex);
        Allele[] pair = alleleMap.get(gene);
        return new String[]{pair[0].getVariantName(), pair[1].getVariantName()};
    }

    public java.util.List<Gene> getGenesInOrder() {
        return alleleMap.keySet().stream().sorted(java.util.Comparator.comparing(Gene::getName)).toList();
    }

    public Genotype copy() {
        Genotype copy = new Genotype();
        alleleMap.forEach((gene, pair) -> copy.setAllelePair(gene, pair[0], pair[1]));
        return copy;
    }
}
