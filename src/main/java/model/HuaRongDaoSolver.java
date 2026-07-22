package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/** A* solver. One single-cell piece translation counts as one move. */
public class HuaRongDaoSolver {
    public List<AIMovement> solve(int[][] initialBoard) {
        BoardRules.validateBoard(initialBoard);
        PriorityQueue<State> openSet = new PriorityQueue<>();
        Map<State, Integer> bestSteps = new HashMap<>();
        State start = new State(initialBoard, 0, null, 0, 0, null);
        openSet.add(start);
        bestSteps.put(start, 0);

        while (!openSet.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                return Collections.emptyList();
            }
            State current = openSet.poll();
            if (current.steps != bestSteps.getOrDefault(current, Integer.MAX_VALUE)) {
                continue; // A better route to the same board was queued later.
            }
            if (BoardRules.isSolved(current.board)) {
                return reconstructPath(current);
            }

            for (State neighbor : getNeighbors(current)) {
                int knownSteps = bestSteps.getOrDefault(neighbor, Integer.MAX_VALUE);
                if (neighbor.steps < knownSteps) {
                    bestSteps.put(neighbor, neighbor.steps);
                    openSet.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();
        for (BoardRules.Piece piece : BoardRules.pieces(state.board)) {
            for (Direction direction : Direction.values()) {
                if (Thread.currentThread().isInterrupted()) {
                    return Collections.emptyList();
                }
                int[][] moved = BoardRules.applyMove(state.board, piece.row(), piece.col(), direction);
                if (moved != null) {
                    neighbors.add(new State(moved, state.steps + 1, state,
                            piece.row(), piece.col(), direction));
                }
            }
        }
        return neighbors;
    }

    private List<AIMovement> reconstructPath(State state) {
        List<AIMovement> path = new ArrayList<>();
        while (state != null && state.direction != null) {
            path.add(new AIMovement(state.row, state.col, state.direction));
            state = state.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
