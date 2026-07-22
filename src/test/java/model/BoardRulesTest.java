package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardRulesTest {
    @Test
    void movesSingleCellWithoutMutatingInput() {
        int[][] board = {{1, 0}, {0, 0}};

        int[][] moved = BoardRules.applyMove(board, 0, 0, Direction.RIGHT);

        assertArrayEquals(new int[][]{{0, 1}, {0, 0}}, moved);
        assertArrayEquals(new int[][]{{1, 0}, {0, 0}}, board);
    }

    @Test
    void movesHorizontalAndVerticalPieces() {
        int[][] horizontal = {{2, 2, 0}, {0, 0, 0}};
        int[][] vertical = {{3, 0}, {3, 0}, {0, 0}};

        assertArrayEquals(new int[][]{{0, 2, 2}, {0, 0, 0}},
                BoardRules.applyMove(horizontal, 0, 0, Direction.RIGHT));
        assertArrayEquals(new int[][]{{0, 0}, {3, 0}, {3, 0}},
                BoardRules.applyMove(vertical, 0, 0, Direction.DOWN));
    }

    @Test
    void rejectsBlockedAndOutOfBoundsMoves() {
        int[][] board = {{2, 2, 1}, {0, 0, 0}};

        assertNull(BoardRules.applyMove(board, 0, 0, Direction.RIGHT));
        assertNull(BoardRules.applyMove(board, 0, 0, Direction.UP));
    }

    @Test
    void detectsSolvedBoard() {
        int[][] board = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0}
        };

        assertTrue(BoardRules.isSolved(board));
    }

    @Test
    void movesCaoCaoIntoTheExit() {
        int[][] board = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0},
                {0, 0, 0, 0}
        };

        int[][] moved = BoardRules.applyMove(board, 2, 1, Direction.DOWN);

        assertNotNull(moved);
        assertTrue(BoardRules.isSolved(moved));
        assertArrayEquals(new int[]{0, 0, 0, 0}, moved[2]);
    }

    @Test
    void acceptsEveryBuiltInLayout() {
        for (Difficulty difficulty : Difficulty.values()) {
            assertDoesNotThrow(() -> BoardRules.validateGameBoard(difficulty.initialBoard()));
        }
    }

    @Test
    void rejectsMalformedGameBoards() {
        int[][] wrongSize = {{1, 0}, {0, 0}};
        int[][] missingCaoCao = {
                {3, 0, 0, 3},
                {3, 0, 0, 3},
                {3, 2, 2, 3},
                {3, 1, 1, 3},
                {1, 0, 0, 1}
        };
        int[][] brokenVerticalPiece = Difficulty.EXPERT.initialBoard();
        brokenVerticalPiece[1][0] = BoardRules.EMPTY;

        assertThrows(IllegalArgumentException.class, () -> BoardRules.validateGameBoard(wrongSize));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.validateGameBoard(missingCaoCao));
        assertThrows(IllegalArgumentException.class, () -> BoardRules.validateGameBoard(brokenVerticalPiece));
    }

    @Test
    void mapModelDefensivelyCopiesBoards() {
        int[][] source = Difficulty.EXPERT.initialBoard();
        MapModel model = new MapModel(source);
        source[0][0] = BoardRules.EMPTY;
        int[][] exposed = model.getMatrix();
        exposed[0][0] = BoardRules.EMPTY;

        assertEquals(BoardRules.VERTICAL, model.getId(0, 0));
    }
}
