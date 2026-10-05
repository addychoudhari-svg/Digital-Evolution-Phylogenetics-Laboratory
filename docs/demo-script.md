# Five-minute demo script

**0:00–0:45 — Question and model.** Introduce the question: how does mutation and selection shape genome-based tree reconstruction? State that the current model uses one diploid coat-color locus and a terrain-match fitness rule.

**0:45–1:45 — Evolving population.** Open the JavaFX app, point out terrain and organisms, then run ten generations. Explain fitness-weighted sampling, distinct parents, independent allele segregation, mutation, and ancestry capture.

**1:45–2:45 — Analytics.** Show dark-allele frequency, expected and observed heterozygosity, genotype counts, mean fitness, and genetic diversity. Describe the Hardy–Weinberg curves as model diagnostics, not a statistical significance test.

**2:45–3:45 — Tree inference.** Show the radial UPGMA tree and Newick string. Explain RF comparison uses the parent-1 projection of a two-parent ancestry graph, so parent-2 ancestry is excluded.

**3:45–4:30 — Experiments.** Run `experiment.ExperimentReport` from the repository root. Open the two CSVs. Explain the sweep uses one deterministic seed per rate and geographic isolation compares separate no-migration regions; these are illustrative outputs pending multi-seed analysis.

**4:30–5:00 — Architecture and limits.** Walk through `docs/architecture.md`, then name the current limits: one locus, no crossover/linkage, no sexual selection, no NJ/parsimony, and no side-by-side true/inferred tree panel.
