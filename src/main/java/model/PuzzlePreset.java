package model;

/** Bundled puzzle definitions with stable identities. */
public enum PuzzlePreset {
    TUTORIAL("tutorial", new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 0, 0, 3},
            {1, 0, 0, 1}
    }),
    INTERMEDIATE("intermediate", new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 0, 3},
            {1, 0, 0, 1}
    }),
    HENG_DAO_LI_MA("heng-dao-li-ma", new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 1, 3},
            {1, 0, 0, 1}
    });

    private final String id;
    private final int[][] initialBoard;

    PuzzlePreset(String id, int[][] initialBoard) {
        this.id = id;
        BoardRules.validateGameBoard(initialBoard);
        this.initialBoard = BoardRules.copy(initialBoard);
    }

    public String id() {
        return id;
    }

    public PuzzleDefinition definition(MovementRule movementRule) {
        return PuzzleDefinition.of(initialBoard, movementRule);
    }
}
