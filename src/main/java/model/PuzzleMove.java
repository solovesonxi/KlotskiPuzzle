package model;

import java.util.Objects;

/** A move from the current top-left cell of a piece. */
public record PuzzleMove(int row, int column, Direction direction, int distance) {
    public PuzzleMove {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Move origin must be on the board");
        }
        Objects.requireNonNull(direction, "direction");
        if (distance < 1) {
            throw new IllegalArgumentException("Move distance must be positive");
        }
    }
}
