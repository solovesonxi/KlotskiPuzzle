package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PuzzleStateTest {
    @Test
    void isAnImmutableValueSnapshot() {
        int[][] board = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP).initialBoard();
        PuzzleState first = PuzzleState.of(board);
        PuzzleState same = PuzzleState.of(board);
        board[0][0] = BoardRules.EMPTY;
        int[][] exposed = first.board();
        exposed[0][0] = BoardRules.EMPTY;

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(BoardRules.EMPTY, first.board()[0][0]);
        assertEquals(20, first.compact().length());
    }
}
