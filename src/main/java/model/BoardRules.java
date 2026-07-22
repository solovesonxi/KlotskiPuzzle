package model;

import java.util.ArrayList;
import java.util.List;

/** Pure rules for validating and applying moves on a Klotski board. */
public final class BoardRules {
    public static final int GAME_ROWS = 5;
    public static final int GAME_COLUMNS = 4;
    public static final int EMPTY = 0;
    public static final int SOLDIER = 1;
    public static final int HORIZONTAL = 2;
    public static final int VERTICAL = 3;
    public static final int CAO_CAO = 4;

    private BoardRules() {
    }

    public record Piece(int row, int col, int type) {
    }

    public static int[][] applyMove(int[][] board, int row, int col, Direction direction) {
        validateBoard(board);
        if (!inBounds(board, row, col) || direction == null) {
            return null;
        }

        int type = board[row][col];
        int height = heightOf(type);
        int width = widthOf(type);
        if (height == 0 || !pieceOccupies(board, row, col, type, height, width)) {
            return null;
        }

        int targetRow = row + direction.getRow();
        int targetCol = col + direction.getCol();
        if (!rectangleInBounds(board, targetRow, targetCol, height, width)) {
            return null;
        }

        int[][] moved = copy(board);
        clearRectangle(moved, row, col, height, width);
        if (!rectangleIsEmpty(moved, targetRow, targetCol, height, width)) {
            return null;
        }
        fillRectangle(moved, targetRow, targetCol, height, width, type);
        return moved;
    }

    public static List<Piece> pieces(int[][] board) {
        validateBoard(board);
        boolean[][] visited = new boolean[board.length][board[0].length];
        List<Piece> pieces = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                int type = board[row][col];
                if (type == EMPTY || visited[row][col]) {
                    continue;
                }
                int height = heightOf(type);
                int width = widthOf(type);
                if (!pieceOccupies(board, row, col, type, height, width)) {
                    throw new IllegalArgumentException("Invalid piece at " + row + "," + col);
                }
                pieces.add(new Piece(row, col, type));
                for (int r = row; r < row + height; r++) {
                    for (int c = col; c < col + width; c++) {
                        visited[r][c] = true;
                    }
                }
            }
        }
        return pieces;
    }

    public static boolean isSolved(int[][] board) {
        validateBoard(board);
        return board.length >= 5 && board[0].length >= 3
                && board[3][1] == CAO_CAO && board[3][2] == CAO_CAO
                && board[4][1] == CAO_CAO && board[4][2] == CAO_CAO;
    }

    public static int[][] copy(int[][] board) {
        int[][] result = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            result[i] = board[i].clone();
        }
        return result;
    }

    public static void validateBoard(int[][] board) {
        if (board == null || board.length == 0 || board[0] == null || board[0].length == 0) {
            throw new IllegalArgumentException("Board must not be empty");
        }
        int width = board[0].length;
        for (int[] row : board) {
            if (row == null || row.length != width) {
                throw new IllegalArgumentException("Board must be rectangular");
            }
            for (int cell : row) {
                if (cell < EMPTY || cell > CAO_CAO) {
                    throw new IllegalArgumentException("Unknown piece type: " + cell);
                }
            }
        }
    }

    /** Validates a board that can be shown, saved, or loaded by this game. */
    public static void validateGameBoard(int[][] board) {
        validateBoard(board);
        if (board.length != GAME_ROWS || board[0].length != GAME_COLUMNS) {
            throw new IllegalArgumentException("Game board must be 5x4");
        }

        List<Piece> foundPieces = pieces(board);
        int soldiers = 0;
        int horizontalPieces = 0;
        int verticalPieces = 0;
        int caoCaoPieces = 0;
        for (Piece piece : foundPieces) {
            switch (piece.type()) {
                case SOLDIER -> soldiers++;
                case HORIZONTAL -> horizontalPieces++;
                case VERTICAL -> verticalPieces++;
                case CAO_CAO -> caoCaoPieces++;
                default -> throw new IllegalArgumentException("Unknown piece type: " + piece.type());
            }
        }

        if (caoCaoPieces != 1 || horizontalPieces != 1 || verticalPieces != 4) {
            throw new IllegalArgumentException(
                    "Game board must contain one Cao Cao, one horizontal piece, and four vertical pieces");
        }
        if (soldiers < 2 || soldiers > 4) {
            throw new IllegalArgumentException("Game board must contain two to four soldiers");
        }
        int emptyCells = countCells(board, EMPTY);
        if (emptyCells != 6 - soldiers) {
            throw new IllegalArgumentException("Unexpected number of empty cells");
        }
    }

    private static boolean pieceOccupies(int[][] board, int row, int col, int type, int height, int width) {
        if (!rectangleInBounds(board, row, col, height, width)) {
            return false;
        }
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                if (board[r][c] != type) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean rectangleIsEmpty(int[][] board, int row, int col, int height, int width) {
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                if (board[r][c] != EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void clearRectangle(int[][] board, int row, int col, int height, int width) {
        fillRectangle(board, row, col, height, width, EMPTY);
    }

    private static void fillRectangle(int[][] board, int row, int col, int height, int width, int value) {
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                board[r][c] = value;
            }
        }
    }

    private static boolean rectangleInBounds(int[][] board, int row, int col, int height, int width) {
        return row >= 0 && col >= 0 && row + height <= board.length && col + width <= board[0].length;
    }

    private static boolean inBounds(int[][] board, int row, int col) {
        return row >= 0 && col >= 0 && row < board.length && col < board[0].length;
    }

    private static int countCells(int[][] board, int value) {
        int count = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == value) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int heightOf(int type) {
        return switch (type) {
            case SOLDIER, HORIZONTAL -> 1;
            case VERTICAL, CAO_CAO -> 2;
            default -> 0;
        };
    }

    private static int widthOf(int type) {
        return switch (type) {
            case SOLDIER, VERTICAL -> 1;
            case HORIZONTAL, CAO_CAO -> 2;
            default -> 0;
        };
    }
}
