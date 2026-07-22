package model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HuaRongDaoSolverTest {
    @Test
    void reportsAlreadySolvedBoard() {
        int[][] solved = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 4, 4, 0},
                {0, 4, 4, 0}
        };

        HuaRongDaoSolver.Result result = new HuaRongDaoSolver().solveDetailed(
                solved, HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES, (expanded, discovered) -> {
                });

        assertEquals(HuaRongDaoSolver.Status.ALREADY_SOLVED, result.status());
        assertTrue(result.moves().isEmpty());
        assertEquals(0, result.expandedStates());
        assertEquals(1, result.discoveredStates());
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

        AtomicInteger progressEvents = new AtomicInteger();
        HuaRongDaoSolver.Result result = new HuaRongDaoSolver().solveDetailed(
                board, HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES,
                (expanded, discovered) -> progressEvents.incrementAndGet());
        List<AIMovement> path = result.moves();

        assertEquals(HuaRongDaoSolver.Status.SOLVED, result.status());
        assertEquals(1, path.size());
        assertEquals(Direction.DOWN, path.getFirst().getDirection());
        assertTrue(result.expandedStates() > 0);
        assertTrue(result.discoveredStates() > 0);
        assertTrue(progressEvents.get() > 0);
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
            HuaRongDaoSolver.Result result = new HuaRongDaoSolver().solveDetailed(
                    board, HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES, (expanded, discovered) -> {
                    });
            assertEquals(HuaRongDaoSolver.Status.CANCELLED, result.status());
            assertTrue(result.moves().isEmpty());
        } finally {
            Thread.interrupted(); // Clear the flag for the JUnit worker thread.
        }
    }

    @Test
    void stopsAtConfiguredStateLimit() {
        HuaRongDaoSolver.Result result = new HuaRongDaoSolver().solveDetailed(
                Difficulty.EXPERT.initialBoard(), 1, (expanded, discovered) -> {
                });

        assertEquals(HuaRongDaoSolver.Status.STATE_LIMIT_REACHED, result.status());
        assertEquals(1, result.discoveredStates());
        assertTrue(result.moves().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new HuaRongDaoSolver().solveDetailed(
                Difficulty.EXPERT.initialBoard(), 0, (expanded, discovered) -> {
                }));
    }

    @Test
    void reportsExhaustedSearchAsNoSolution() {
        int[][] immovableBoard = {
                {4, 4},
                {4, 4}
        };

        HuaRongDaoSolver.Result result = new HuaRongDaoSolver().solveDetailed(
                immovableBoard, 10, (expanded, discovered) -> {
                });

        assertEquals(HuaRongDaoSolver.Status.NO_SOLUTION, result.status());
        assertEquals(1, result.expandedStates());
        assertEquals(1, result.discoveredStates());
        assertTrue(result.moves().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("builtInLayouts")
    void solvesBuiltInLayouts(int[][] initialBoard) {
        HuaRongDaoSolver.Result result = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> new HuaRongDaoSolver().solveDetailed(
                        initialBoard,
                        HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES,
                        (expanded, discovered) -> {
                        }));
        List<AIMovement> path = result.moves();

        assertEquals(HuaRongDaoSolver.Status.SOLVED, result.status());
        assertTrue(result.discoveredStates() <= HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES);
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
