package visualization;

import core.Organism;
import core.SpeciesType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import java.util.List;

/** Canvas renderer for phenotype-colored organisms on a terrain grid. */
public class EcosystemPane extends StackPane {
    private final Canvas canvas = new Canvas(600, 500);
    private List<Organism> population = List.of();
    private String terrain = "dark";

    @SuppressWarnings("this-escape")
    public EcosystemPane() {
        setPrefSize(600, 500); setMinSize(280, 230);
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        widthProperty().addListener((obs, oldValue, newValue) -> draw(population, terrain));
        heightProperty().addListener((obs, oldValue, newValue) -> draw(population, terrain));
        draw(population, terrain);
    }

    public void update(List<Organism> population, String terrain) {
        this.population = List.copyOf(population);
        this.terrain = terrain;
        draw(this.population, terrain);
    }

    private void draw(List<Organism> population, String terrain) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = Math.max(1, canvas.getWidth()), height = Math.max(1, canvas.getHeight());
        double sx = width / 600.0, sy = height / 500.0, marker = 14 * Math.min(sx, sy);
        gc.setFill(terrain.equalsIgnoreCase("dark") ? Color.web("#263b48") : Color.web("#d9dfd1"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.color(0.55, 0.76, 0.85, 0.18));
        for (double x = 0; x <= width; x += 50 * sx) gc.strokeLine(x, 0, x, height);
        for (double y = 0; y <= height; y += 50 * sy) gc.strokeLine(0, y, width, y);
        gc.setFont(javafx.scene.text.Font.font(10));
        SpeciesType species = population.isEmpty() ? SpeciesType.ANIMAL : population.get(0).getSpeciesType();
        String traitGene = population.isEmpty() ? species.traitGene() : population.get(0).getSpeciesProfile().traitGene();
        for (Organism organism : population) {
            double x = (12 + ((organism.getId() * 97L) % 570)) * sx;
            double y = (12 + ((organism.getId() * 193L) % 470)) * sy;
            double coat = organism.getGenotype().getGenesInOrder().stream()
                .filter(g -> g.getName().equalsIgnoreCase(traitGene))
                .mapToDouble(organism.getGenotype()::getPhenotypeValue).findFirst().orElse(0.5);
            gc.setFill(species == SpeciesType.PLANT
                ? Color.color(0.2 + coat * 0.55, 0.65 - coat * 0.3, 0.34, 0.9)
                : Color.gray(Math.max(0.08, Math.min(0.95, 1.0 - coat * 0.8))));
            gc.setStroke(Color.web("#d8efff"));
            if (species == SpeciesType.PLANT) {
                gc.fillOval(x + marker * 0.3, y, marker * 0.4, marker);
                gc.strokeOval(x + marker * 0.3, y, marker * 0.4, marker);
                gc.strokeLine(x + marker * 0.5, y + marker * 0.5, x + marker * 0.15, y + marker * 0.28);
                gc.strokeLine(x + marker * 0.5, y + marker * 0.7, x + marker * 0.85, y + marker * 0.48);
            } else {
                gc.fillOval(x, y, marker, marker);
                gc.strokeOval(x, y, marker, marker);
            }
        }
    }
}
