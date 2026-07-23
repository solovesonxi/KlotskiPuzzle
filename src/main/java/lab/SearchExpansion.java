package lab;

import model.PuzzleMove;
import model.PuzzleState;

import java.util.List;
import java.util.Objects;

/** One inspectable, deterministic state expansion and all candidate decisions. */
public record SearchExpansion(int index, PuzzleState state, int pathCost, int heuristic,
                              double priority, int frontierBefore, int frontierAfter,
                              int discoveredStates, boolean goal, List<Candidate> candidates) {
    public SearchExpansion {
        if (index < 1 || pathCost < 0 || heuristic < 0 || frontierBefore < 0
                || frontierAfter < 0 || discoveredStates < 1) {
            throw new IllegalArgumentException("Search expansion values must not be negative");
        }
        Objects.requireNonNull(state, "state");
        candidates = List.copyOf(candidates);
    }

    /** One candidate considered while expanding a state. */
    public record Candidate(PuzzleMove move, PuzzleState state, int pathCost, int heuristic,
                            double priority, SearchDecision decision, Integer previousCost) {
        public Candidate {
            Objects.requireNonNull(move, "move");
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(decision, "decision");
            if (pathCost < 1 || heuristic < 0 || previousCost != null && previousCost < 0) {
                throw new IllegalArgumentException("Candidate costs must not be negative");
            }
        }

        public boolean accepted() {
            return decision == SearchDecision.DISCOVERED || decision == SearchDecision.IMPROVED;
        }
    }
}
