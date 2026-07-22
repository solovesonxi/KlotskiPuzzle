package model;

import java.util.Arrays;

// 表示棋盘状态
class State implements Comparable<State> {
    int[][] board;    // 当前棋盘布局
    int steps;        // 已走步数
    int heuristic;    // 启发式估值
    State parent;     // 父状态（用于回溯路径）
    int row, col; // 当前空格位置
    Direction direction;

    public State(int[][] board, int steps, State parent, int row, int col, Direction direction) {
        this.board = BoardRules.copy(board);
        this.steps = steps;
        this.parent = parent;
        this.heuristic = calculateHeuristic();
        this.row = row;
        this.col = col;
        this.direction = direction;
    }

    // 计算启发式估值（曼哈顿距离）
    private int calculateHeuristic() {
        int targetRow = 3, targetCol = 1; // 曹操左上角的目标位置
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 4) {
                    return steps + Math.abs(i - targetRow) + Math.abs(j - targetCol);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.heuristic, other.heuristic);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof State other && Arrays.deepEquals(this.board, other.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
