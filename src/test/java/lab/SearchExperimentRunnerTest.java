package lab;

import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzleMove;
import model.PuzzlePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchExperimentRunnerTest {
    private final SearchExperimentRunner runner = new SearchExperimentRunner();

    @ParameterizedTest
    @EnumSource(SearchStrategy.class)
    void everyStrategyProducesALegalSolution(SearchStrategy strategy) {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        SearchExperimentRunner.Result result = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> runner.run(SearchExperiment.of(puzzle, strategy)));

        assertEquals(SearchExperimentRunner.Status.SOLVED, result.status());
        assertFalse(result.solution().isEmpty());
        assertTrue(result.metrics().expandedStates() > 0);
        assertTrue(result.metrics().discoveredStates() > 0);
        assertTrue(result.metrics().maximumFrontier() > 0);
        assertTrue(puzzle.isSolved(replay(puzzle, result)));
    }

    @Test
    void identicalExperimentsProduceIdenticalPathsAndMetrics() {
        PuzzleDefinition puzzle = PuzzlePreset.INTERMEDIATE.definition(MovementRule.CELL_STEP);
        SearchExperiment experiment = SearchExperiment.of(puzzle, SearchStrategy.A_STAR);

        SearchExperimentRunner.Result first = runner.run(experiment);
        SearchExperimentRunner.Result second = runner.run(experiment);

        assertEquals(first.status(), second.status());
        assertEquals(first.solution(), second.solution());
        assertEquals(first.metrics(), second.metrics());
    }

    @Test
    void pieceMoveUsesTheSameRunnerAndProducesALegalSolution() {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.PIECE_MOVE);
        SearchExperimentRunner.Result result = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> runner.run(SearchExperiment.of(puzzle, SearchStrategy.A_STAR)));

        assertEquals(SearchExperimentRunner.Status.SOLVED, result.status());
        assertTrue(puzzle.isSolved(replay(puzzle, result)));
        assertTrue(result.solution().stream().anyMatch(move -> move.distance() > 1));
    }

    @Test
    void reportsProgressAndHonorsTheDiscoveredStateLimit() {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        SearchExperiment limited = new SearchExperiment(
                puzzle, SearchStrategy.A_STAR, 1.0, 1);
        AtomicInteger progressEvents = new AtomicInteger();

        SearchExperimentRunner.Result result = runner.run(
                limited, progress -> progressEvents.incrementAndGet());

        assertEquals(SearchExperimentRunner.Status.STATE_LIMIT_REACHED, result.status());
        assertEquals(1, result.metrics().discoveredStates());
        assertTrue(progressEvents.get() > 0);
    }

    @Test
    void validatesExperimentParameters() {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);

        assertThrows(IllegalArgumentException.class, () -> new SearchExperiment(
                puzzle, SearchStrategy.WEIGHTED_A_STAR, 0.5, 10));
        assertThrows(IllegalArgumentException.class, () -> new SearchExperiment(
                puzzle, SearchStrategy.A_STAR, 1.0, 0));
    }

    private static int[][] replay(PuzzleDefinition puzzle,
                                  SearchExperimentRunner.Result result) {
        int[][] board = puzzle.initialBoard();
        for (PuzzleMove move : result.solution()) {
            board = puzzle.tryApply(board, move).orElseThrow();
        }
        return board;
    }
}
