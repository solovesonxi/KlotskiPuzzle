package lab;

import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzleMove;
import model.PuzzlePreset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolutionReplayTest {
    @Test
    void exposesEveryStateFromInitialPuzzleToSolvedBoard() {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        SearchExperimentRunner.Result result = new SearchExperimentRunner().run(
                SearchExperiment.of(puzzle, SearchStrategy.A_STAR));
        SolutionReplay replay = SolutionReplay.of(puzzle, result.solution());

        assertEquals(result.solution().size(), replay.lastStep());
        assertEquals(puzzle.initialBoard()[0][0], replay.stateAt(0).board()[0][0]);
        assertTrue(puzzle.isSolved(replay.stateAt(replay.lastStep()).board()));
        assertEquals(result.solution().getFirst(), replay.moveInto(1));
    }

    @Test
    void rejectsAnIncompletePath() {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        List<PuzzleMove> fullSolution = new SearchExperimentRunner().run(
                SearchExperiment.of(puzzle, SearchStrategy.A_STAR)).solution();

        assertThrows(IllegalArgumentException.class,
                () -> SolutionReplay.of(puzzle, fullSolution.subList(0, 1)));
    }
}
