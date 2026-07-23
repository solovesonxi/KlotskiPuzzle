package model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzleDefinitionTest {
    @Test
    void contentIdentityIsStableAndIncludesTheMovementRule() {
        PuzzleDefinition first = PuzzlePreset.HENG_DAO_LI_MA.definition(MovementRule.CELL_STEP);
        PuzzleDefinition same = PuzzlePreset.HENG_DAO_LI_MA.definition(MovementRule.CELL_STEP);
        PuzzleDefinition otherRule = PuzzlePreset.HENG_DAO_LI_MA.definition(MovementRule.PIECE_MOVE);

        assertEquals(first, same);
        assertEquals(first.contentId(), same.contentId());
        assertTrue(first.contentId().matches("sha256:[0-9a-f]{64}"));
        assertNotEquals(first.contentId(), otherRule.contentId());
    }

    @Test
    void ownsDefensiveCopiesOfPuzzleAndSuccessorStates() {
        int[][] source = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP).initialBoard();
        PuzzleDefinition definition = PuzzleDefinition.of(source, MovementRule.CELL_STEP);
        source[0][0] = BoardRules.EMPTY;
        int[][] exposed = definition.initialBoard();
        exposed[0][0] = BoardRules.EMPTY;

        assertEquals(BoardRules.VERTICAL, definition.initialBoard()[0][0]);

        PuzzleDefinition.Successor successor = definition.successors(definition.initialBoard()).getFirst();
        int[][] successorState = successor.state();
        successorState[0][0] = BoardRules.EMPTY;
        assertNotEquals(successorState[0][0], successor.state()[0][0]);
    }

    @Test
    void cellStepRejectsLongMoves() {
        PuzzleDefinition definition = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);

        assertTrue(definition.tryApply(definition.initialBoard(),
                new PuzzleMove(4, 0, Direction.RIGHT, 1)).isPresent());
        assertFalse(definition.tryApply(definition.initialBoard(),
                new PuzzleMove(4, 0, Direction.RIGHT, 2)).isPresent());
    }

    @Test
    void pieceMoveExposesEveryReachableDistanceAsOneSuccessor() {
        PuzzleDefinition definition = PuzzlePreset.TUTORIAL.definition(MovementRule.PIECE_MOVE);
        List<PuzzleDefinition.Successor> successors = definition.successors(definition.initialBoard());

        assertTrue(successors.stream().anyMatch(successor -> successor.move().distance() == 2));
        PuzzleDefinition.Successor longMove = successors.stream()
                .filter(successor -> successor.move().distance() == 2)
                .findFirst()
                .orElseThrow();
        assertArrayEquals(longMove.state(), definition.tryApply(definition.initialBoard(),
                longMove.move()).orElseThrow());
    }

    @Test
    void successorOrderIsDeterministic() {
        PuzzleDefinition definition = PuzzlePreset.INTERMEDIATE.definition(MovementRule.PIECE_MOVE);

        List<PuzzleMove> first = definition.successors(definition.initialBoard()).stream()
                .map(PuzzleDefinition.Successor::move)
                .toList();
        List<PuzzleMove> second = definition.successors(definition.initialBoard()).stream()
                .map(PuzzleDefinition.Successor::move)
                .toList();

        assertEquals(first, second);
    }

    @Test
    void rejectsMalformedDefinitionsAndForeignStates() {
        assertThrows(IllegalArgumentException.class, () -> PuzzleDefinition.of(
                new int[][]{{1, 0}, {0, 0}}, MovementRule.CELL_STEP));

        PuzzleDefinition tutorial = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        int[][] foreignState = PuzzlePreset.HENG_DAO_LI_MA
                .definition(MovementRule.CELL_STEP).initialBoard();
        assertThrows(IllegalArgumentException.class, () -> tutorial.successors(foreignState));
    }
}
