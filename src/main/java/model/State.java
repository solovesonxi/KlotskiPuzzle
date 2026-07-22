package model;

import java.util.Arrays;

/** Immutable search node used by the A* frontier. */
final class State implements Comparable<State> {
    private static final int TARGET_ROW = 3;
    private static final int TARGET_COLUMN = 1;

    private final int[][] board;
    private final int steps;
    private final int estimatedRemainingSteps;
    private final int priority;
    private final State parent;
    private final int movedPieceRow;
    private final int movedPieceColumn;
    private final Direction direction;
    private final int hashCode;

    State(int[][] board, int steps, State parent, int movedPieceRow,
          int movedPieceColumn, Direction direction) {
        BoardRules.validateBoard(board);
        if (steps < 0) {
            throw new IllegalArgumentException("steps must not be negative");
        }
        this.board = BoardRules.copy(board);
        this.steps = steps;
        this.estimatedRemainingSteps = estimateRemainingSteps(this.board);
        this.priority = Math.addExact(steps, estimatedRemainingSteps);
        this.parent = parent;
        this.movedPieceRow = movedPieceRow;
        this.movedPieceColumn = movedPieceColumn;
        this.direction = direction;
        this.hashCode = Arrays.deepHashCode(this.board);
    }

    int[][] board() {
        return board;
    }

    int steps() {
        return steps;
    }

    int estimatedRemainingSteps() {
        return estimatedRemainingSteps;
    }

    int priority() {
        return priority;
    }

    State parent() {
        return parent;
    }

    int movedPieceRow() {
        return movedPieceRow;
    }

    int movedPieceColumn() {
        return movedPieceColumn;
    }

    Direction direction() {
        return direction;
    }

    private static int estimateRemainingSteps(int[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[0].length; column++) {
                if (board[row][column] == BoardRules.CAO_CAO) {
                    return Math.abs(row - TARGET_ROW) + Math.abs(column - TARGET_COLUMN);
                }
            }
        }
        throw new IllegalArgumentException("Board does not contain the target piece");
    }

    @Override
    public int compareTo(State other) {
        int byPriority = Integer.compare(priority, other.priority);
        if (byPriority != 0) {
            return byPriority;
        }
        int byEstimate = Integer.compare(estimatedRemainingSteps, other.estimatedRemainingSteps);
        if (byEstimate != 0) {
            return byEstimate;
        }
        return Integer.compare(other.steps, steps);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof State other
                && hashCode == other.hashCode
                && Arrays.deepEquals(board, other.board);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
