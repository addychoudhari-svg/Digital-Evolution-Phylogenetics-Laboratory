package core;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Small, curated taxonomic label list; genomes generated from these entries are synthetic. */
public final class SpeciesCatalog {
    private static final List<SpeciesProfile> BUILT_IN = List.of(
        new SpeciesProfile(SpeciesType.ANIMAL, "Mammals", "Gray wolf", "Canis lupus", "coat"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Mammals", "Red fox", "Vulpes vulpes", "coat"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Mammals", "Snowshoe hare", "Lepus americanus", "coat"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Birds", "Rock pigeon", "Columba livia", "plumage"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Birds", "Emperor penguin", "Aptenodytes forsteri", "plumage"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Reptiles", "Green sea turtle", "Chelonia mydas", "scalePigment"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Insects", "Western honey bee", "Apis mellifera", "bodyColor"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Mammals", "African savanna elephant", "Loxodonta africana", "coat"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Amphibians", "Axolotl", "Ambystoma mexicanum", "skinPigment"),
        new SpeciesProfile(SpeciesType.ANIMAL, "Fish", "Zebrafish", "Danio rerio", "scalePigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Flowering plants", "Thale cress", "Arabidopsis thaliana", "leafPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Flowering plants", "Maize", "Zea mays", "leafPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Flowering plants", "Sunflower", "Helianthus annuus", "leafPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Flowering plants", "Asian rice", "Oryza sativa", "leafPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Flowering plants", "Pedunculate oak", "Quercus robur", "leafPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Conifers", "Scots pine", "Pinus sylvestris", "needlePigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Ferns", "Bracken fern", "Pteridium aquilinum", "frondPigment"),
        new SpeciesProfile(SpeciesType.PLANT, "Mosses", "Ceratodon moss", "Ceratodon purpureus", "leafPigment")
    );
    private static volatile List<SpeciesProfile> profiles = BUILT_IN;

    private SpeciesCatalog() {}
    public static List<SpeciesProfile> all() { return profiles; }
    public static void load(Path csvPath) throws IOException {
        if (!Files.exists(csvPath)) return;
        List<SpeciesProfile> loaded = new ArrayList<>();
        List<String> lines = Files.readAllLines(csvPath);
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = line.split(",", -1);
            if (fields.length != 5) throw new IOException("Invalid species catalog row " + (index + 1) + ": expected 5 columns");
            try {
                loaded.add(new SpeciesProfile(SpeciesType.valueOf(fields[0].trim().toUpperCase()), fields[1].trim(),
                    fields[2].trim(), fields[3].trim(), fields[4].trim()));
            } catch (IllegalArgumentException invalidRow) {
                throw new IOException("Invalid species catalog row " + (index + 1), invalidRow);
            }
        }
        if (loaded.isEmpty()) throw new IOException("Species catalog contains no profiles: " + csvPath);
        profiles = List.copyOf(loaded);
    }
    public static List<String> subcategories(SpeciesType kingdom) {
        return profiles.stream().filter(profile -> profile.kingdom() == kingdom)
            .map(SpeciesProfile::subcategory).distinct().toList();
    }
    public static List<SpeciesProfile> inCategory(SpeciesType kingdom, String subcategory) {
        return profiles.stream().filter(profile -> profile.kingdom() == kingdom)
            .filter(profile -> profile.subcategory().equals(subcategory)).toList();
    }
    public static SpeciesProfile defaultFor(SpeciesType kingdom) {
        return profiles.stream().filter(profile -> profile.kingdom() == kingdom).findFirst().orElseThrow();
    }
}
