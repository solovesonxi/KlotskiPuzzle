package lab;

import model.PuzzleDefinition;

import java.util.Objects;

/** Immutable configuration for one reproducible search experiment. */
public record SearchExperiment(PuzzleDefinition puzzle, SearchStrategy strategy,
                               double heuristicWeight, int maxDiscoveredStates) {
    public static final int DEFAULT_MAX_DISCOVERED_STATES = 250_000;
    public static final double DEFAULT_WEIGHTED_A_STAR_WEIGHT = 1.5;

    public SearchExperiment {
        Objects.requireNonNull(puzzle, "puzzle");
        Objects.requireNonNull(strategy, "strategy");
        if (!Double.isFinite(heuristicWeight) || heuristicWeight <= 0.0) {
            throw new IllegalArgumentException("heuristicWeight must be finite and positive");
        }
        if (strategy == SearchStrategy.WEIGHTED_A_STAR && heuristicWeight < 1.0) {
            throw new IllegalArgumentException("Weighted A* requires a weight of at least 1.0");
        }
        if (maxDiscoveredStates < 1) {
            throw new IllegalArgumentException("maxDiscoveredStates must be positive");
        }
    }

    public static SearchExperiment of(PuzzleDefinition puzzle, SearchStrategy strategy) {
        double weight = strategy == SearchStrategy.WEIGHTED_A_STAR
                ? DEFAULT_WEIGHTED_A_STAR_WEIGHT
                : 1.0;
        return new SearchExperiment(puzzle, strategy, weight, DEFAULT_MAX_DISCOVERED_STATES);
    }
}
