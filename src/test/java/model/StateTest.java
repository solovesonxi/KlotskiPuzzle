package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StateTest {
    @Test
    void keepsPathCostEstimateAndPrioritySeparate() {
        int[][] board = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0},
                {0, 0, 0, 0}
        };

        State state = new State(board, 5, null, 0, 0, null);

        assertEquals(5, state.steps());
        assertEquals(1, state.estimatedRemainingSteps());
        assertEquals(6, state.priority());
    }

    @Test
    void identityDependsOnBoardRatherThanSearchMetadata() {
        int[][] board = Difficulty.BEGINNER.initialBoard();
        State first = new State(board, 2, null, 0, 0, Direction.DOWN);
        State second = new State(board, 7, first, 3, 1, Direction.LEFT);
        int[][] differentBoard = BoardRules.applyMove(board, 2, 1, Direction.DOWN);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new State(differentBoard, 3, first, 2, 1, Direction.DOWN));
    }
}
