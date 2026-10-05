# Roadmap status

This table maps the supplied EvoLab roadmap to the implementation in this checkout. “Implemented” means code is present; it does not imply a validated scientific result.

| Milestone | Status |
|---|---|
| Day 0: Java/Maven contracts and shared model | Genetics/environment contracts now exist in the established source tree; package and tree stubs were repaired. |
| Week 1 A: genes, alleles, genotype, expression strategies | Implemented for additive and dominant/recessive rules, with an explicit phenotype value object. |
| Week 1 B: organism, life stages, behavior, composite region | Implemented; adults alone are eligible to reproduce. Region supports nested structure and total resources. |
| Week 1 C: distance matrix and tree strategy | Implemented; unordered diploid mismatch and an UPGMA strategy interface. |
| Week 1 D: JavaFX shell and observer | Implemented and connected to generation snapshots. |
| Week 2 A: meiosis, mutation, ancestry | Implemented for independent segregation and mutation; both parent IDs are retained. |
| Week 2 B: fitness and evolution tick loop | Implemented with terrain-match fitness, fitness-weighted selection, a fixed cohort size, and observer events. |
| Week 2 C: UPGMA and population genetics | Implemented with expected/observed ratios and H-W deviation; tests cover a hand-specified four-taxon matrix. |
| Week 2 D: ecosystem renderer | Implemented with phenotype-colored organisms on a terrain grid. |
| Week 3 A/B: config, factory, end-to-end run | Implemented; settings load from `config/simulation.properties`, and 50-generation experiment runs are configurable. |
| Week 3 C: RF distance | Implemented against the explicit parent-1 ancestry projection; the second parent is not represented in this tree metric. |
| Week 3 D: radial inferred tree and live stats | Implemented. Side-by-side true-tree rendering is not implemented. |
| Week 4 A: mutation sweep | Implemented for three rates, currently one deterministic seed per setting. |
| Week 4 B: geographic isolation | Implemented as independently evolved light/dark subpopulations with no migration; reports coat phenotype divergence. |
| Week 4 C/D: Hardy-Weinberg and genotype analytics | Live allele-frequency/heterozygosity lines and genotype histogram are implemented. Export-ready figure files remain. |
| Week 5: integration tests, final report/demo | 17 unit tests passed using a local reflection runner; architecture, breakdown, exploratory report, protocols, and demo script are documented. Full Maven/Surefire run, multi-seed validation, slides, and recording remain. |

Roadmap stretch items not implemented: linkage/crossover, sexual selection, Neighbor Joining, maximum parsimony, god-mode interventions, and fitness-landscape heatmap. Existing root `out/*.class` files are legacy compiled artifacts tracked before these changes; Maven uses `src/`, not `out/`.
