package evolution;

/** Command-style run, pause, and speed controls for a simulation engine. */
public final class ExperimentController {
    private final EvolutionEngine engine;
    private volatile boolean paused;
    private int generationsPerAction = 1;
    public ExperimentController(EvolutionEngine engine) { this.engine = engine; }
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public boolean isPaused() { return paused; }
    public void setSpeed(int generationsPerAction) {
        if (generationsPerAction < 1) throw new IllegalArgumentException("Speed must be positive");
        this.generationsPerAction = generationsPerAction;
    }
    public void runGenerations(int count) {
        if (count < 0) throw new IllegalArgumentException("Generation count cannot be negative");
        for (int i = 0; i < count && !paused; i++) engine.step();
    }
    public void runAction() { runGenerations(generationsPerAction); }
}
