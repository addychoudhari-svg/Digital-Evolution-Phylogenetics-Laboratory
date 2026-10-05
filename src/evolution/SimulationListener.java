package evolution;

@FunctionalInterface
public interface SimulationListener {
    void onGeneration(SimulationSnapshot snapshot);
}
