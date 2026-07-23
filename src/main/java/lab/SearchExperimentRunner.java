package lab;

import model.BoardRules;
import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzleMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Runs deterministic search experiments behind one small interface.
 *
 * <p>Strategy scoring, stable tie-breaking, cancellation, state limits, metrics, and path
 * reconstruction remain inside this module so Lab Mode never coordinates them itself.</p>
 */
public final class SearchExperimentRunner {
    private static final int PROGRESS_INTERVAL = 500;
    private static final int TARGET_ROW = 3;
    private static final int TARGET_COLUMN = 1;

    public enum Status {
        SOLVED,
        ALREADY_SOLVED,
        NO_SOLUTION,
        CANCELLED,
        STATE_LIMIT_REACHED
    }

    public record Metrics(int expandedStates, int discoveredStates, int maximumFrontier) {
        public Metrics {
            if (expandedStates < 0 || discoveredStates < 0 || maximumFrontier < 0) {
                throw new IllegalArgumentException("Search metrics must not be negative");
            }
        }
    }

    public record Progress(int expandedStates, int discoveredStates, int frontierSize,
                           int pathCost, int heuristic, double priority) {
    }

    public record Result(Status status, List<PuzzleMove> solution, Metrics metrics,
                         long elapsedNanos) {
        public Result {
            Objects.requireNonNull(status, "status");
            solution = List.copyOf(solution);
            Objects.requireNonNull(metrics, "metrics");
            if (elapsedNanos < 0) {
                throw new IllegalArgumentException("elapsedNanos must not be negative");
            }
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(Progress progress);
    }

    public Result run(SearchExperiment experiment) {
        return run(experiment, progress -> {
        });
    }

    public Result run(SearchExperiment experiment, ProgressListener progressListener) {
        Objects.requireNonNull(experiment, "experiment");
        Objects.requireNonNull(progressListener, "progressListener");
        long started = System.nanoTime();
        PuzzleDefinition puzzle = experiment.puzzle();
        int[][] initialBoard = puzzle.initialBoard();
        if (puzzle.isSolved(initialBoard)) {
            return result(Status.ALREADY_SOLVED, List.of(), 0, 1, 1, started);
        }

        long nextSequence = 0;
        Node start = new Node(initialBoard, 0, estimate(initialBoard, puzzle.movementRule()),
                nextSequence++, null, null);
        PriorityQueue<Node> frontier = new PriorityQueue<>(comparator(experiment));
        Map<BoardKey, Integer> bestCosts = new HashMap<>();
        frontier.add(start);
        bestCosts.put(start.key, 0);
        int expandedStates = 0;
        int maximumFrontier = 1;
        progressListener.onProgress(progress(start, expandedStates, bestCosts.size(), frontier.size(),
                experiment));

        while (!frontier.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                return result(Status.CANCELLED, List.of(), expandedStates, bestCosts.size(),
                        maximumFrontier, started);
            }
            Node current = frontier.poll();
            if (current.pathCost != bestCosts.getOrDefault(current.key, Integer.MAX_VALUE)) {
                continue;
            }
            expandedStates++;
            if (puzzle.isSolved(current.board)) {
                progressListener.onProgress(progress(current, expandedStates, bestCosts.size(),
                        frontier.size(), experiment));
                return result(Status.SOLVED, reconstruct(current), expandedStates, bestCosts.size(),
                        maximumFrontier, started);
            }

            for (PuzzleDefinition.Successor successor : puzzle.successors(current.board)) {
                if (Thread.currentThread().isInterrupted()) {
                    return result(Status.CANCELLED, List.of(), expandedStates, bestCosts.size(),
                            maximumFrontier, started);
                }
                int nextCost = current.pathCost + 1;
                BoardKey key = new BoardKey(successor.state());
                Integer knownCost = bestCosts.get(key);
                if (knownCost == null && bestCosts.size() >= experiment.maxDiscoveredStates()) {
                    return result(Status.STATE_LIMIT_REACHED, List.of(), expandedStates,
                            bestCosts.size(), maximumFrontier, started);
                }
                if (knownCost == null || nextCost < knownCost) {
                    int[][] board = successor.state();
                    Node next = new Node(board, nextCost,
                            estimate(board, puzzle.movementRule()), nextSequence++, current,
                            successor.move());
                    bestCosts.put(next.key, nextCost);
                    frontier.add(next);
                    maximumFrontier = Math.max(maximumFrontier, frontier.size());
                }
            }

            if (expandedStates % PROGRESS_INTERVAL == 0) {
                progressListener.onProgress(progress(current, expandedStates, bestCosts.size(),
                        frontier.size(), experiment));
            }
        }

        return result(Status.NO_SOLUTION, List.of(), expandedStates, bestCosts.size(),
                maximumFrontier, started);
    }

    private static Comparator<Node> comparator(SearchExperiment experiment) {
        return switch (experiment.strategy()) {
            case BFS -> Comparator.comparingInt((Node node) -> node.pathCost)
                    .thenComparingLong(node -> node.sequence);
            case GREEDY_BEST_FIRST -> Comparator.comparingInt((Node node) -> node.heuristic)
                    .thenComparingInt(node -> node.pathCost)
                    .thenComparingLong(node -> node.sequence);
            case A_STAR, WEIGHTED_A_STAR -> Comparator
                    .comparingDouble((Node node) -> score(
                            node.pathCost, node.heuristic, experiment))
                    .thenComparingInt(node -> node.heuristic)
                    .thenComparingInt(node -> node.pathCost)
                    .thenComparingLong(node -> node.sequence);
        };
    }

    private static Progress progress(Node node, int expandedStates, int discoveredStates,
                                     int frontierSize, SearchExperiment experiment) {
        return new Progress(expandedStates, discoveredStates, frontierSize, node.pathCost,
                node.heuristic, score(node.pathCost, node.heuristic, experiment));
    }

    private static double score(int pathCost, int heuristic, SearchExperiment experiment) {
        return switch (experiment.strategy()) {
            case BFS -> pathCost;
            case GREEDY_BEST_FIRST -> heuristic;
            case A_STAR -> pathCost + heuristic;
            case WEIGHTED_A_STAR -> pathCost + experiment.heuristicWeight() * heuristic;
        };
    }

    private static int estimate(int[][] board, MovementRule movementRule) {
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[0].length; column++) {
                if (board[row][column] == BoardRules.CAO_CAO) {
                    int verticalDistance = Math.abs(row - TARGET_ROW);
                    int horizontalDistance = Math.abs(column - TARGET_COLUMN);
                    return movementRule == MovementRule.CELL_STEP
                            ? verticalDistance + horizontalDistance
                            : (verticalDistance == 0 ? 0 : 1) + (horizontalDistance == 0 ? 0 : 1);
                }
            }
        }
        throw new IllegalArgumentException("Board does not contain the target piece");
    }

    private static List<PuzzleMove> reconstruct(Node goal) {
        List<PuzzleMove> path = new ArrayList<>();
        for (Node node = goal; node != null && node.move != null; node = node.parent) {
            path.add(node.move);
        }
        Collections.reverse(path);
        return List.copyOf(path);
    }

    private static Result result(Status status, List<PuzzleMove> path, int expandedStates,
                                 int discoveredStates, int maximumFrontier, long started) {
        return new Result(status, path,
                new Metrics(expandedStates, discoveredStates, maximumFrontier),
                Math.max(0, System.nanoTime() - started));
    }

    private static final class Node {
        private final int[][] board;
        private final int pathCost;
        private final int heuristic;
        private final long sequence;
        private final Node parent;
        private final PuzzleMove move;
        private final BoardKey key;

        private Node(int[][] board, int pathCost, int heuristic, long sequence,
                     Node parent, PuzzleMove move) {
            this.board = BoardRules.copy(board);
            this.pathCost = pathCost;
            this.heuristic = heuristic;
            this.sequence = sequence;
            this.parent = parent;
            this.move = move;
            this.key = new BoardKey(board);
        }
    }

    private static final class BoardKey {
        private final int[][] board;
        private final int hashCode;

        private BoardKey(int[][] board) {
            this.board = BoardRules.copy(board);
            this.hashCode = Arrays.deepHashCode(this.board);
        }

        @Override
        public boolean equals(Object object) {
            return this == object || object instanceof BoardKey other
                    && hashCode == other.hashCode
                    && Arrays.deepEquals(board, other.board);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
