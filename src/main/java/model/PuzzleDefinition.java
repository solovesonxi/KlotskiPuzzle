package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A validated 5x4 puzzle and its movement contract.
 *
 * <p>This is the shared seam for play sessions and search experiments: callers do not need to
 * duplicate movement-rule branching, state validation, successor ordering, or content identity.</p>
 */
public final class PuzzleDefinition {
    public static final int FORMAT_VERSION = 1;

    private final int[][] initialBoard;
    private final MovementRule movementRule;
    private final int[] pieceCounts;
    private final String contentId;

    private PuzzleDefinition(int[][] initialBoard, MovementRule movementRule) {
        BoardRules.validateGameBoard(initialBoard);
        this.initialBoard = BoardRules.copy(initialBoard);
        this.movementRule = Objects.requireNonNull(movementRule, "movementRule");
        this.pieceCounts = countPieces(this.initialBoard);
        this.contentId = calculateContentId();
    }

    public static PuzzleDefinition of(int[][] initialBoard, MovementRule movementRule) {
        return new PuzzleDefinition(initialBoard, movementRule);
    }

    public int[][] initialBoard() {
        return BoardRules.copy(initialBoard);
    }

    public MovementRule movementRule() {
        return movementRule;
    }

    /** A stable SHA-256 identity derived only from versioned puzzle content. */
    public String contentId() {
        return contentId;
    }

    public boolean isSolved(int[][] state) {
        validateState(state);
        return BoardRules.isSolved(state);
    }

    /** Attempts one move without mutating the supplied state. */
    public Optional<int[][]> tryApply(int[][] state, PuzzleMove move) {
        validateState(state);
        Objects.requireNonNull(move, "move");
        if (movementRule == MovementRule.CELL_STEP && move.distance() != 1) {
            return Optional.empty();
        }

        int[][] current = BoardRules.copy(state);
        int row = move.row();
        int column = move.column();
        for (int step = 0; step < move.distance(); step++) {
            int[][] moved = BoardRules.applyMove(current, row, column, move.direction());
            if (moved == null) {
                return Optional.empty();
            }
            current = moved;
            row += move.direction().getRow();
            column += move.direction().getCol();
        }
        return Optional.of(current);
    }

    /**
     * Expands legal successors in a stable order: board scan, direction enum order, then distance.
     */
    public List<Successor> successors(int[][] state) {
        validateState(state);
        List<Successor> result = new ArrayList<>();
        for (BoardRules.Piece piece : BoardRules.pieces(state)) {
            for (Direction direction : Direction.values()) {
                int[][] current = state;
                int row = piece.row();
                int column = piece.col();
                for (int distance = 1; ; distance++) {
                    PuzzleMove move = new PuzzleMove(piece.row(), piece.col(), direction, distance);
                    int[][] moved = BoardRules.applyMove(current, row, column, direction);
                    if (moved == null) {
                        break;
                    }
                    result.add(new Successor(move, moved));
                    if (movementRule == MovementRule.CELL_STEP) {
                        break;
                    }
                    current = moved;
                    row += direction.getRow();
                    column += direction.getCol();
                }
            }
        }
        return List.copyOf(result);
    }

    private void validateState(int[][] state) {
        BoardRules.validateGameBoard(state);
        if (!Arrays.equals(pieceCounts, countPieces(state))) {
            throw new IllegalArgumentException("State piece classes do not match the puzzle definition");
        }
    }

    private String calculateContentId() {
        StringBuilder canonical = new StringBuilder()
                .append("klotski-puzzle-definition\n")
                .append("version=").append(FORMAT_VERSION).append('\n')
                .append("movement=").append(movementRule.name()).append('\n');
        for (int[] row : initialBoard) {
            for (int cell : row) {
                canonical.append(cell);
            }
            canonical.append('\n');
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
        }
    }

    private static int[] countPieces(int[][] board) {
        int[] counts = new int[BoardRules.CAO_CAO + 1];
        for (BoardRules.Piece piece : BoardRules.pieces(board)) {
            counts[piece.type()]++;
        }
        return counts;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof PuzzleDefinition other
                && movementRule == other.movementRule
                && Arrays.deepEquals(initialBoard, other.initialBoard);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.deepHashCode(initialBoard) + movementRule.hashCode();
    }

    /** One legal edge and its immutable resulting state. */
    public record Successor(PuzzleMove move, int[][] state) {
        public Successor {
            Objects.requireNonNull(move, "move");
            state = BoardRules.copy(Objects.requireNonNull(state, "state"));
        }

        @Override
        public int[][] state() {
            return BoardRules.copy(state);
        }
    }
}
