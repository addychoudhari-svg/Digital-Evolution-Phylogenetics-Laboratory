# EvoLab — Digital Evolution & Phylogenetics Laboratory

Java 21/Maven/JavaFX application for evolving diploid populations, recording parentage, and comparing genome-distance phylogenies with a declared maternal-lineage projection.

## Build and run

From the repository root, with JDK 21 and Maven installed:

```powershell
mvn test
mvn javafx:run
```

On Windows, you can run `run-evolab.cmd` to compile and launch the app using JavaFX 21.0.2 already present in the local Maven cache. This works without Maven on `PATH` when those JavaFX dependencies are cached.

The application reads `config/simulation.properties`. In the JavaFX window, use **Run 1**, **Run 10**, **Run N**, or **Start auto / Pause**. The delay selector controls automatic simulation speed.

Run the deterministic example experiments from the repository root with:

```powershell
mvn -q -DskipTests compile
java -cp target/classes experiment.ExperimentReport
```

That writes `results/mutation-rate-sweep.csv` and `results/geographic-isolation.csv`. The checked-in example outputs are single-seed demonstrations, not replicated study conclusions. Geographic isolation reports coat-trait divergence; its RF field is intentionally blank.

## Existing repository layout

- `src/core`: `Organism` identity, genotype, parents, generation, region, and life stage.
- `src/genetics`: genes, alleles, expression strategies, inheritance/mutation, phenotype, and ancestry records.
- `src/environment`: life-stage and behavior strategies plus the composite `Region` model.
- `src/evolution`: factory, fitness, simulation engine, event snapshot/listener, and run controls.
- `src/phylogenetics`: genome distance matrix, UPGMA, tree representation/comparison, and population genetics tracker.
- `src/visualization`: JavaFX dashboard, responsive ecosystem/tree canvases, charts, generation comparison, and the glass-style theme.
- `src/experiment`: mutation-rate and geographic-isolation scenario runners plus CSV writer.
- `src/config`: validated settings loader for `config/simulation.properties`.
- `tests`: JUnit tests, using Maven's configured test source directory.
- `experiments`: experiment protocol notes and scenario folders; `results`: generated CSV summaries.
- `out`: legacy compiled `.class` files already tracked in the repository. Rebuild from `src`; these binaries are not used by Maven.
- `main.c`, `main1.c`, `doublyll.c`: empty legacy files at the repository root; the active application is Java.

## Architecture and status

See [system architecture and data flow](docs/architecture.md), [complete project breakdown](docs/project-breakdown.md), [roadmap status](docs/roadmap-status.md), [experiment protocols](experiments/README.md), and [timed demo script](docs/demo-script.md).

The core loop is implemented. Remaining scope includes multi-seed statistical analysis, export-ready figures, side-by-side true/inferred tree rendering, and presentation/video deliverables. Stretch features cut by the roadmap (linkage/crossover, sexual selection, parsimony, and additional interventions) are not implemented.
