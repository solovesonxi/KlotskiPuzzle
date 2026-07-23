package model;

import static util.Messages.text;

/** Supported game presets and their initial boards. */
public enum Difficulty {
    BEGINNER("difficulty.beginner", false, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 0, 0, 3},
            {1, 0, 0, 1}
    }),
    INTERMEDIATE("difficulty.intermediate", false, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 0, 3},
            {1, 0, 0, 1}
    }),
    EXPERT("difficulty.expert", true, new int[][]{
            {3, 4, 4, 3},
            {3, 4, 4, 3},
            {3, 2, 2, 3},
            {3, 1, 1, 3},
            {1, 0, 0, 1}
    });

    private final String messageKey;
    private final boolean ranked;
    private final int[][] initialBoard;

    Difficulty(String messageKey, boolean ranked, int[][] initialBoard) {
        BoardRules.validateGameBoard(initialBoard);
        this.messageKey = messageKey;
        this.ranked = ranked;
        this.initialBoard = BoardRules.copy(initialBoard);
    }

    public String displayName() {
        return text(messageKey);
    }

    public boolean isRanked() {
        return ranked;
    }

    public int[][] initialBoard() {
        return BoardRules.copy(initialBoard);
    }
}
