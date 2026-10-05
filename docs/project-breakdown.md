# Complete project breakdown

## Purpose

EvoLab is an object-oriented digital evolution lab and teaching visualization. It lets a user evolve a diploid population under a simple terrain-selection model, observe genetic summaries, infer a tree from terminal genomes, and compare that inference against a declared projection of recorded ancestry.

## Package-by-package structure

| Path | Main files / classes | Role |
|---|---|---|
| `src/core` | `Organism`, `SpeciesType`, `SpeciesProfile`, `SpeciesCatalog` | Stable organism ID, parents, generation, region, species teaching profile, mutable life stage, genotype, and computed phenotype. |
| `src/genetics` | `Allele`, `Gene`, `Genotype`, `Phenotype` | Diploid genotype data contract and expression of trait values. |
| `src/genetics` | `ExpressionRule`, `AdditiveRule`, `DominantRecessiveRule` | Swappable phenotype-expression strategies. |
| `src/genetics` | `ReproductionStrategy`, `MeiosisReproduction` | Independent segregation and configurable per-allele mutation; no crossover/linkage. |
| `src/genetics` | `AncestryRecorder` | Append-only parent1/parent2 birth records and a maternal-lineage tree projection for RF. |
| `src/environment` | `LifeStage` | Juvenile → adult → senescent; only adults reproduce. |
| `src/environment` | `Behavior`, `ForagingBehavior`, `FleeingBehavior` | Behavior strategy contract and Week 1 stubs. |
| `src/environment` | `Region` | Composite terrain/resource hierarchy. |
| `src/evolution` | `PopulationFactory` | Seeded founder population construction using one of the editable animal/plant catalog profiles and its synthetic trait locus. |
| `src/evolution` | `FitnessFunction`, `EvolutionEngine` | Terrain-match fitness, distinct mature-parent selection, birth processing, and observer ticks. |
| `src/evolution` | `SimulationSnapshot`, `SimulationListener`, `ExperimentController` | Observer event payload and command-like run controls. |
| `src/phylogenetics` | `DistanceMatrix` | Symmetric diploid variant Hamming distance and taxon labels. |
| `src/phylogenetics` | `PhylogeneticTree`, `TreeBuildingAlgorithm`, `UPGMAAlgorithm` | Tree representation, strategy boundary, Newick serialization, and average-linkage reconstruction. |
| `src/phylogenetics` | `TreeComparator` | Robinson–Foulds split distance for matching unrooted tip sets. |
| `src/phylogenetics` | `PopulationGeneticsTracker` | Allele frequencies, observed/H-W genotype ratios, and deviation per generation. |
| `src/visualization` | `MainApp`, `EcosystemPane`, `TreePane`, `StatsPane`, `GenerationComparisonPane` | Tabbed JavaFX views, species selection, saved generation browsing, population/genotype comparisons, and two-parent pedigree display. |
| `src/config` and `config/` | `SimulationConfig`, `species-catalog.csv` | Validated simulation settings plus 18 editable named profiles grouped into animal/plant subcategories. |
| `src/experiment` | `Experiments`, `ExperimentReport` | Mutation-rate sweep, geographic isolation, CSV generation. |
| `tests` | Existing genetics tests plus `EvolutionEngineTest` and `PhylogeneticsTest` | Regression coverage for expression, inheritance, ancestry, the engine, distances, UPGMA, RF, and H-W statistics. |

## One simulation generation

1. Fitness is evaluated from the selected species profile's trait phenotype and region terrain.
2. Two distinct fertile adults are sampled with fitness-weighted probability.
3. Each parent transmits one randomly chosen allele per locus; each transmitted allele independently mutates with the configured probability.
4. The offspring receives a new ID and references both parent IDs; its birth record is appended to ancestry.
5. After filling the next cohort, the engine emits an immutable snapshot.
6. The analytics tracker and JavaFX canvases/charts consume that snapshot.
7. `MainApp` retains the snapshot by generation so a user can inspect earlier generations while the engine continues forward.

## End-of-run analysis

Final organisms provide taxon IDs and genotypes to `DistanceMatrix`; UPGMA creates the inferred tree. The ancestry recorder retains the full two-parent graph and creates a parent-1 projection with the same terminal IDs. RF compares the two tree split sets. This metric has a meaningful and explicit limitation: the projection excludes parent-2 ancestry.

## Experiments and outputs

- **Mutation-rate sweep:** rates 0.001, 0.02, and 0.15; reports mean pairwise variant distance and RF per setting.
- **Geographic isolation:** splits a shared founder pool into light and dark regions, evolves the populations independently with no migration, and reports within-region diversity plus difference in mean coat phenotype. RF is blank because the experiment currently reports phenotype divergence, not a two-region tree score.
- Both outputs use one deterministic seed per run. Treat them as demonstrative output only; replicate over seeds and report uncertainty before making scientific claims.

## Deliberate scope and remaining work

The model currently has one diploid trait locus per selected teaching profile, independent segregation, simple mutation, and terrain-match selection. Animal and plant profiles share the same simplified life cycle; they are not detailed biological models. The full two-parent pedigree is recorded and viewable for selected organisms, while RF uses the parent-1 tree projection. Export-ready publication figures, multi-seed/statistical aggregation, true and inferred trees side by side, slides, and a recorded demo remain. Roadmap stretch items not attempted are gene linkage/crossover, sexual selection, maximum parsimony, and additional interactive interventions.
