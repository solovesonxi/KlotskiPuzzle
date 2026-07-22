package model;

/** Supported game presets and their initial boards. */
public enum Difficulty {
    BEGINNER("初出茅庐", false, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 0, 0, 3},
            {1, 0, 0, 1}
    }),
    INTERMEDIATE("刮目相待", false, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 0, 3},
            {1, 0, 0, 1}
    }),
    EXPERT("运筹帷幄", true, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 1, 3},
            {1, 0, 0, 1}
    });

    private final String displayName;
    private final boolean ranked;
    private final int[][] initialBoard;

    Difficulty(String displayName, boolean ranked, int[][] initialBoard) {
        BoardRules.validateGameBoard(initialBoard);
        this.displayName = displayName;
        this.ranked = ranked;
        this.initialBoard = BoardRules.copy(initialBoard);
    }

    public String displayName() {
        return displayName;
    }

    public boolean isRanked() {
        return ranked;
    }

    public int[][] initialBoard() {
        return BoardRules.copy(initialBoard);
    }
}
