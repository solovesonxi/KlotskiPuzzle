package model;

import java.util.Arrays;

/** Immutable value representation of one validated 5x4 Klotski state. */
public final class PuzzleState {
    private final byte[] cells;
    private final int hashCode;

    private PuzzleState(int[][] board) {
        BoardRules.validateGameBoard(board);
        cells = new byte[BoardRules.GAME_ROWS * BoardRules.GAME_COLUMNS];
        int index = 0;
        for (int[] row : board) {
            for (int cell : row) {
                cells[index++] = (byte) cell;
            }
        }
        hashCode = Arrays.hashCode(cells);
    }

    public static PuzzleState of(int[][] board) {
        return new PuzzleState(board);
    }

    public int[][] board() {
        int[][] board = new int[BoardRules.GAME_ROWS][BoardRules.GAME_COLUMNS];
        for (int index = 0; index < cells.length; index++) {
            board[index / BoardRules.GAME_COLUMNS][index % BoardRules.GAME_COLUMNS] = cells[index];
        }
        return board;
    }

    /** Compact stable representation useful in diagnostics and exported records. */
    public String compact() {
        StringBuilder value = new StringBuilder(cells.length);
        for (byte cell : cells) {
            value.append(cell);
        }
        return value.toString();
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof PuzzleState other
                && Arrays.equals(cells, other.cells);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return compact();
    }
}
