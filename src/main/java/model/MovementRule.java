package model;

/** Declares how a puzzle translation becomes one search edge. */
public enum MovementRule {
    /** One piece translated by one cell has a cost of one. */
    CELL_STEP,
    /** One piece translated any positive distance in one direction has a cost of one. */
    PIECE_MOVE
}
