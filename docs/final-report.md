# EvoLab implementation report (exploratory baseline)

## Question

How do mutation rate and geographic isolation affect genetic diversity and the ability of genome-distance methods to recover ancestry in a small digital population?

## Model and methods

The current implementation models one diploid coat-color locus with additive expression, independent allele segregation, per-allele mutation, and fitness weighted by match between expressed coat value and region terrain. Each engine generation retains a fixed cohort size. The mutation sweep uses UPGMA over pairwise unphased allele-variant Hamming distances. RF compares UPGMA against the ancestry graph projected onto parent 1 only. Geographic isolation splits shared founders into light and dark regions and evolves each region independently with no migration.

## Single-seed output

Generated from `config/simulation.properties` using seed 2026, 30 organisms, and 50 generations. Values are retained in `results/mutation-rate-sweep.csv` and `results/geographic-isolation.csv`.

| Experiment | Parameter | Mean pairwise genetic distance | RF distance | Phenotype divergence |
|---|---:|---:|---:|---:|
| Mutation-rate sweep | 0.001 | 0.0000 | 51 | — |
| Mutation-rate sweep | 0.02 | 0.8598 | 49 | — |
| Mutation-rate sweep | 0.15 | 1.1701 | 48 | — |
| Geographic isolation | — | 0.5238 | — | 1.0350 |

## Interpretation and limits

This is a software demonstration, not a supported scientific conclusion. Each mutation setting has one seed; the values do not estimate variance or establish a trend. A one-locus model has low resolution, and the RF number compares with a parent-1 projection that discards parent-2 links. The geographic isolation result is an endpoint difference in mean coat phenotype, not a tree-accuracy measure. Repeat each setting across many seeds, save per-generation data, estimate uncertainty, and compare additional loci before drawing research conclusions.

## Engineering deliverables

The codebase now contains the documented JavaFX core loop, phenotype/genotype strategies, composite environment, ancestry recorder, UPGMA and RF tools, live Hardy–Weinberg and genotype views, CSV experiment runners, configuration, and regression tests. See `docs/architecture.md`, `docs/project-breakdown.md`, and `docs/roadmap-status.md` for module responsibilities and remaining roadmap items.
