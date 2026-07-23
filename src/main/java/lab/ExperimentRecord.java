package lab;

import model.PuzzleDefinition;
import model.PuzzleMove;
import model.PuzzleState;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Versioned, shareable summary of one reproducible search experiment. */
public record ExperimentRecord(int formatVersion, String createdAt, int puzzleFormatVersion,
                               String puzzleContentId, PuzzleState initialState,
                               String movementRule, String strategy, double heuristicWeight,
                               int maxDiscoveredStates, String status,
                               SearchExperimentRunner.Metrics metrics,
                               List<PuzzleMove> solution, long elapsedNanos,
                               RuntimeEnvironment environment) {
    public static final int FORMAT_VERSION = 1;

    public ExperimentRecord {
        if (formatVersion < 1 || puzzleFormatVersion < 1 || maxDiscoveredStates < 1
                || elapsedNanos < 0) {
            throw new IllegalArgumentException("Experiment record values are invalid");
        }
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(puzzleContentId, "puzzleContentId");
        Objects.requireNonNull(initialState, "initialState");
        Objects.requireNonNull(movementRule, "movementRule");
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(metrics, "metrics");
        solution = List.copyOf(solution);
        Objects.requireNonNull(environment, "environment");
    }

    public static ExperimentRecord capture(SearchExperiment experiment,
                                           SearchExperimentRunner.Result result) {
        return capture(experiment, result, Instant.now(), RuntimeEnvironment.current());
    }

    public static ExperimentRecord capture(SearchExperiment experiment,
                                           SearchExperimentRunner.Result result,
                                           Instant createdAt,
                                           RuntimeEnvironment environment) {
        Objects.requireNonNull(experiment, "experiment");
        Objects.requireNonNull(result, "result");
        PuzzleDefinition puzzle = experiment.puzzle();
        return new ExperimentRecord(FORMAT_VERSION, createdAt.toString(),
                PuzzleDefinition.FORMAT_VERSION, puzzle.contentId(),
                PuzzleState.of(puzzle.initialBoard()), puzzle.movementRule().name(),
                experiment.strategy().name(), experiment.heuristicWeight(),
                experiment.maxDiscoveredStates(), result.status().name(), result.metrics(),
                result.solution(), result.elapsedNanos(), environment);
    }
}
