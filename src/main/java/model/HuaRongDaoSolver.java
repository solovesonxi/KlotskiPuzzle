package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/** A* solver. One single-cell piece translation counts as one move. */
public class HuaRongDaoSolver {
    public static final int DEFAULT_MAX_DISCOVERED_STATES = 250_000;
    private static final int PROGRESS_INTERVAL = 1_000;

    public enum Status {
        SOLVED,
        ALREADY_SOLVED,
        NO_SOLUTION,
        CANCELLED,
        STATE_LIMIT_REACHED
    }

    public record Result(Status status, List<AIMovement> moves,
                         int expandedStates, int discoveredStates) {
        public Result {
            Objects.requireNonNull(status, "status");
            moves = List.copyOf(moves);
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int expandedStates, int discoveredStates);
    }

    public List<AIMovement> solve(int[][] initialBoard) {
        return solveDetailed(initialBoard, DEFAULT_MAX_DISCOVERED_STATES, (expanded, discovered) -> {
        }).moves();
    }

    public Result solveDetailed(int[][] initialBoard, int maxDiscoveredStates,
                                ProgressListener progressListener) {
        BoardRules.validateBoard(initialBoard);
        if (maxDiscoveredStates < 1) {
            throw new IllegalArgumentException("maxDiscoveredStates must be positive");
        }
        Objects.requireNonNull(progressListener, "progressListener");

        PriorityQueue<State> openSet = new PriorityQueue<>();
        Map<State, Integer> bestSteps = new HashMap<>();
        State start = new State(initialBoard, 0, null, 0, 0, null);
        openSet.add(start);
        bestSteps.put(start, 0);
        progressListener.onProgress(0, 1);
        if (BoardRules.isSolved(initialBoard)) {
            return new Result(Status.ALREADY_SOLVED, List.of(), 0, 1);
        }

        int expandedStates = 0;

        while (!openSet.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                return new Result(Status.CANCELLED, List.of(), expandedStates, bestSteps.size());
            }
            State current = openSet.poll();
            if (current.steps() != bestSteps.getOrDefault(current, Integer.MAX_VALUE)) {
                continue; // A better route to the same board was queued later.
            }
            expandedStates++;
            if (expandedStates % PROGRESS_INTERVAL == 0) {
                progressListener.onProgress(expandedStates, bestSteps.size());
            }
            if (BoardRules.isSolved(current.board())) {
                return new Result(Status.SOLVED, reconstructPath(current),
                        expandedStates, bestSteps.size());
            }

            List<State> neighbors = getNeighbors(current);
            if (Thread.currentThread().isInterrupted()) {
                return new Result(Status.CANCELLED, List.of(), expandedStates, bestSteps.size());
            }
            for (State neighbor : neighbors) {
                Integer knownSteps = bestSteps.get(neighbor);
                if (knownSteps == null && bestSteps.size() >= maxDiscoveredStates) {
                    return new Result(Status.STATE_LIMIT_REACHED, List.of(),
                            expandedStates, bestSteps.size());
                }
                if (knownSteps == null || neighbor.steps() < knownSteps) {
                    bestSteps.put(neighbor, neighbor.steps());
                    openSet.add(neighbor);
                }
            }
        }
        return new Result(Status.NO_SOLUTION, List.of(), expandedStates, bestSteps.size());
    }

    private List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();
        for (BoardRules.Piece piece : BoardRules.pieces(state.board())) {
            for (Direction direction : Direction.values()) {
                if (Thread.currentThread().isInterrupted()) {
                    return Collections.emptyList();
                }
                int[][] moved = BoardRules.applyMove(state.board(), piece.row(), piece.col(), direction);
                if (moved != null) {
                    neighbors.add(new State(moved, state.steps() + 1, state,
                            piece.row(), piece.col(), direction));
                }
            }
        }
        return neighbors;
    }

    private List<AIMovement> reconstructPath(State state) {
        List<AIMovement> path = new ArrayList<>();
        while (state != null && state.direction() != null) {
            path.add(new AIMovement(state.movedPieceRow(), state.movedPieceColumn(), state.direction()));
            state = state.parent();
        }
        Collections.reverse(path);
        return path;
    }
}
