package lab;

import model.PuzzleDefinition;
import model.PuzzleMove;
import model.PuzzleState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Validates a solution once and exposes every replay state through a small immutable interface. */
public final class SolutionReplay {
    private final List<PuzzleMove> moves;
    private final List<PuzzleState> states;

    private SolutionReplay(PuzzleDefinition puzzle, List<PuzzleMove> moves) {
        Objects.requireNonNull(puzzle, "puzzle");
        this.moves = List.copyOf(moves);
        List<PuzzleState> replayStates = new ArrayList<>(moves.size() + 1);
        int[][] board = puzzle.initialBoard();
        replayStates.add(PuzzleState.of(board));
        for (PuzzleMove move : moves) {
            board = puzzle.tryApply(board, move)
                    .orElseThrow(() -> new IllegalArgumentException("Solution contains an illegal move: " + move));
            replayStates.add(PuzzleState.of(board));
        }
        if (!moves.isEmpty() && !puzzle.isSolved(board)) {
            throw new IllegalArgumentException("Solution path does not reach the puzzle goal");
        }
        states = List.copyOf(replayStates);
    }

    public static SolutionReplay of(PuzzleDefinition puzzle, List<PuzzleMove> moves) {
        return new SolutionReplay(puzzle, moves);
    }

    public int lastStep() {
        return moves.size();
    }

    public PuzzleState stateAt(int step) {
        return states.get(requireStep(step));
    }

    /** Returns the move that produced this step; step zero has no incoming move. */
    public PuzzleMove moveInto(int step) {
        if (step < 1 || step > moves.size()) {
            throw new IndexOutOfBoundsException("Move step must be between 1 and " + moves.size());
        }
        return moves.get(step - 1);
    }

    public List<PuzzleMove> moves() {
        return moves;
    }

    private int requireStep(int step) {
        if (step < 0 || step >= states.size()) {
            throw new IndexOutOfBoundsException("Replay step must be between 0 and " + moves.size());
        }
        return step;
    }
}
