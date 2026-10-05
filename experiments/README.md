# Experiment protocols

## Mutation-rate sweep

Run `experiment.ExperimentReport` at rates 0.001, 0.02, and 0.15, holding population size and generation count constant in `config/simulation.properties`. The current CSV reports mean pairwise allele-variant distance and RF distance per run. It uses one seed for each setting; use multiple seeds per rate and summarize uncertainty before interpreting differences.

## Geographic isolation

The runner samples one shared founder pool and divides it between two regions. The two groups evolve in separate engines, so there is zero gene flow after the split. The current outcome is within-group genetic diversity and the difference in mean coat phenotype. RF is omitted for this comparison. Add replicate seeds and report distributions to support a scientific conclusion.
