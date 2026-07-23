# Prefer reproducible search behavior over maximum throughput

KlotskiPuzzle treats stable search order, solution path, and state counts as part of the experiment contract for the same puzzle, strategy, parameters, and version. Implementations must use explicit deterministic tie-breaking, accepting a small throughput cost because walkthroughs, comparisons, documentation, and tests must be repeatable; elapsed time and memory remain environment-dependent observations.
