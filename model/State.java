package model;

import java.util.Arrays;

import static controller.GameController.deepCopy;

// 表示棋盘状态
class State implements Comparable<State> {
    int[][] board;    // 当前棋盘布局
    int steps;        // 已走步数
    int heuristic;    // 启发式估值
    State parent;     // 父状态（用于回溯路径）
    int row, col; // 当前空格位置
    Direction direction;

    public State(int[][] board, int steps, State parent, int row, int col, Direction direction) {
        this.board = deepCopy(board);
        this.steps = steps;
        this.parent = parent;
        this.heuristic = calculateHeuristic();
        this.row = row;
        this.col = col;
        this.direction = direction;
    }



    // 计算启发式估值（曼哈顿距离）
    private int calculateHeuristic() {
        int targetRow = 3, targetCol = 1; // 目标位置（出口）
        int distance = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 4) {
                    distance = Math.abs(i - targetRow) + Math.abs(j - targetCol);
                    break;
                }
            }
        }
        return steps + distance; // A*的评估函数
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.heuristic, other.heuristic);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(this.getClass()) && Arrays.deepEquals(this.board, ((State) obj).board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
