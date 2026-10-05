package visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import phylogenetics.PhylogeneticTree;
import java.util.IdentityHashMap;
import java.util.Map;

/** Radial tree renderer with a Newick display below the canvas. */
public class TreePane extends BorderPane {
    private record Point(double x, double y, double angle) {}
    private final Canvas canvas = new Canvas(580, 420);
    private final TextArea newick = new TextArea();
    private PhylogeneticTree currentTree;
    private int currentRf;

    @SuppressWarnings("this-escape")
    public TreePane() {
        getStyleClass().add("tree-pane");
        setPrefSize(600, 500); setMinSize(280, 230);
        setTop(new Label("Inferred UPGMA tree"));
        StackPane drawingArea = new StackPane(canvas);
        drawingArea.setMinSize(220, 150);
        canvas.widthProperty().bind(drawingArea.widthProperty());
        canvas.heightProperty().bind(drawingArea.heightProperty());
        drawingArea.widthProperty().addListener((obs, oldValue, newValue) -> redraw());
        drawingArea.heightProperty().addListener((obs, oldValue, newValue) -> redraw());
        setCenter(drawingArea);
        newick.setEditable(false); newick.setPrefRowCount(2); newick.setMaxHeight(74); newick.setWrapText(true);
        newick.getStyleClass().add("glass-text-area");
        setBottom(newick);
        drawBackground();
    }

    public void update(PhylogeneticTree tree, int rfDistance) {
        currentTree = tree;
        currentRf = rfDistance;
        redraw();
    }

    private void redraw() {
        if (currentTree == null) { drawBackground(); return; }
        PhylogeneticTree tree = currentTree;
        int rfDistance = currentRf;
        drawBackground();
        Map<PhylogeneticTree.TreeNode, Point> points = new IdentityHashMap<>();
        int leaves = Math.max(1, tree.getAllTaxa().size());
        int[] index = {0};
        layout(tree.getRoot(), 0, maxDepth(tree.getRoot()), leaves, index, points,
            canvas.getWidth() / 2.0, canvas.getHeight() / 2.0,
            Math.max(20, Math.min(canvas.getWidth(), canvas.getHeight()) * 0.42));
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#3e8e7e")); gc.setLineWidth(1.5);
        drawEdges(tree.getRoot(), points);
        gc.setFont(javafx.scene.text.Font.font(Math.max(8, Math.min(11, canvas.getWidth() / 60)))); gc.setFill(Color.web("#e5f0fa"));
        points.forEach((node, point) -> {
            if (node.isLeaf()) gc.fillText(node.getLabel(), point.x + (Math.cos(point.angle) >= 0 ? 5 : -38), point.y + 4);
        });
        newick.setText(tree.toNewick() + "\nRF vs maternal projection: " + rfDistance);
    }

    private void drawBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#142337")); gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private double layout(PhylogeneticTree.TreeNode node, int depth, int maxDepth, int leafCount,
                          int[] nextLeaf, Map<PhylogeneticTree.TreeNode, Point> points,
                          double centerX, double centerY, double maxRadius) {
        double angle;
        if (node.isLeaf()) angle = 2 * Math.PI * nextLeaf[0]++ / leafCount;
        else {
            double sine = 0, cosine = 0;
            for (var child : node.getChildren()) { double childAngle = layout(child, depth + 1, maxDepth, leafCount, nextLeaf, points, centerX, centerY, maxRadius); sine += Math.sin(childAngle); cosine += Math.cos(childAngle); }
            angle = Math.atan2(sine, cosine);
        }
        double radius = node.isLeaf() ? maxRadius : maxRadius * depth / Math.max(1, maxDepth);
        double x = centerX + radius * Math.cos(angle), y = centerY + radius * Math.sin(angle);
        points.put(node, new Point(x, y, angle));
        return angle;
    }

    private int maxDepth(PhylogeneticTree.TreeNode node) {
        if (node.isLeaf()) return 1;
        return 1 + node.getChildren().stream().mapToInt(this::maxDepth).max().orElse(0);
    }

    private void drawEdges(PhylogeneticTree.TreeNode node, Map<PhylogeneticTree.TreeNode, Point> points) {
        GraphicsContext gc = canvas.getGraphicsContext2D(); Point from = points.get(node);
        for (var child : node.getChildren()) {
            Point to = points.get(child); gc.strokeLine(from.x, from.y, to.x, to.y);
            gc.setFill(Color.DARKGOLDENROD); gc.fillOval(to.x - 2, to.y - 2, 4, 4);
            drawEdges(child, points);
        }
    }
}
