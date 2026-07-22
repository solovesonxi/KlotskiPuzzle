package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuaRongDaoSolverTest {
    @Test
    void returnsNoMovesForSolvedBoard() {
        int[][] solved = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0}
        };

        assertTrue(new HuaRongDaoSolver().solve(solved).isEmpty());
    }

    @Test
    void solvesOneMoveBoard() {
        int[][] board = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0},
                {0, 0, 0, 0}
        };

        List<AIMovement> path = new HuaRongDaoSolver().solve(board);

        assertEquals(1, path.size());
        assertEquals(Direction.DOWN, path.getFirst().getDirection());
    }

    @Test
    void stopsWhenWorkerThreadIsInterrupted() {
        int[][] board = {
                {3, 4, 4, 3},
                {3, 4, 4, 3},
                {3, 2, 2, 3},
                {3, 1, 1, 3},
                {1, 0, 0, 1}
        };

        Thread.currentThread().interrupt();
        try {
            assertTrue(new HuaRongDaoSolver().solve(board).isEmpty());
        } finally {
            Thread.interrupted(); // Clear the flag for the JUnit worker thread.
        }
    }

    @ParameterizedTest
    @MethodSource("builtInLayouts")
    void solvesBuiltInLayouts(int[][] initialBoard) {
        List<AIMovement> path = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> new HuaRongDaoSolver().solve(initialBoard));

        int[][] board = BoardRules.copy(initialBoard);
        for (AIMovement movement : path) {
            board = BoardRules.applyMove(board, movement.getRow(), movement.getCol(), movement.getDirection());
            assertNotNull(board, "Solver returned an illegal move");
        }
        assertTrue(BoardRules.isSolved(board));
    }

    private static Stream<int[][]> builtInLayouts() {
        return Stream.of(Difficulty.values()).map(Difficulty::initialBoard);
    }
}
