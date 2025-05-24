package model;

import java.util.*;

import static controller.GameController.deepCopy;

// 华容道AI求解器
public class HuaRongDaoSolver {
    public List<AIMovement> solve(int[][] initialBoard) {
        PriorityQueue<State> openSet = new PriorityQueue<>();
        Set<State> closedSet = new HashSet<>();
        State start = new State(initialBoard, 0, null, 0, 0, null);
        openSet.add(start);
        System.out.println("开始搜索");
        while (!openSet.isEmpty()) {
            State current = openSet.poll();
            if (closedSet.size() % 1000 == 0) {
                System.out.println("当前步数: " + current.steps + " 当前检查过的状态数: " + closedSet.size() + " 当前打开的状态数: " + openSet.size());
            }
            if (current.board[4][1] == 4 && current.board[4][2] == 4) {
                System.out.println("找到解");
                return reconstructPath(current);
            }
            closedSet.add(current);
            for (State neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;
                if (openSet.contains(neighbor)) {
                    for (State s : openSet) {
                        if (s.equals(neighbor)) {
                            if (s.heuristic > neighbor.heuristic) {
                                openSet.remove(s);
                                openSet.add(neighbor);
                            }
                            break;
                        }
                    }
                } else {
                    openSet.add(neighbor);
                }
            }
        }
        System.out.println("无解");
        return Collections.emptyList(); // 无解
    }

    // 检查是否在矩阵范围内
    private boolean checkInSize(int[][] matrix, int row, int col) {
        return row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length;
    }

    // 生成所有可能的下一步状态
    private List<State> getNeighbors(State state) {
        List<State> neighbors = new ArrayList<>();
        int[][] board = state.board;
        int rows = board.length;
        int cols = board[0].length;

        // 找到所有空格位置（用0表示）
        List<int[]> emptySpaces = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 0) {
                    emptySpaces.add(new int[]{i, j});
                }
            }
        }

        // 对每个空格尝试移动相邻块
        for (int[] space : emptySpaces) {
            int x = space[0], y = space[1];
            for (Direction dir : Direction.values()) {
                int nx = x - dir.getRow();
                int ny = y - dir.getCol();
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && board[nx][ny] != 0) {
                    int[][] newBoard = deepCopy(board);
                    int[][] newBoardIndex = indexBoard(deepCopy(board));
                    int index = newBoardIndex[nx][ny];
                    int row = index / newBoardIndex[0].length;
                    int col = index % newBoardIndex[0].length;
                    if (checkMove(newBoard, row, col, dir)) {
                        State neighbor = new State(newBoard, state.steps + 1, state, row, col, dir);
                        neighbors.add(neighbor);
                    }
                }
            }
        }
        return neighbors;
    }

    // 检查移动是否合法，类似于GameController.doMove()
    public boolean checkMove(int[][] board, int row, int col, Direction direction) {
        int nextRow = row + direction.getRow();
        int nextCol = col + direction.getCol();
        boolean result;
        if (board[row][col] == 1) {
            if (checkInSize(board, nextRow, nextCol)) {
                if (board[nextRow][nextCol] == 0) {
                    board[row][col] = 0;
                    board[nextRow][nextCol] = 1;
                    return true;
                }
            }
        } else if (board[row][col] == 2) {
            if (checkInSize(board, nextRow, nextCol) && checkInSize(board, nextRow, nextCol + 1)) {
                if (direction == Direction.UP || direction == Direction.DOWN) {
                    result = board[nextRow][nextCol] == 0 && board[nextRow][nextCol + 1] == 0;
                } else {
                    result = (direction == Direction.LEFT && board[nextRow][nextCol] == 0) || (direction == Direction.RIGHT && board[nextRow][nextCol + 1] == 0);
                }
                if (result) {
                    board[row][col] = 0;
                    board[row][col + 1] = 0;
                    board[nextRow][nextCol] = 2;
                    board[nextRow][nextCol + 1] = 2;
                    return true;
                }
            }
        } else if (board[row][col] == 3) {
            if (checkInSize(board, nextRow, nextCol) && checkInSize(board, nextRow + 1, nextCol)) {
                if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                    result = board[nextRow][nextCol] == 0 && board[nextRow + 1][nextCol] == 0;
                } else {
                    result = (direction == Direction.UP && board[nextRow][nextCol] == 0) || (direction == Direction.DOWN && board[nextRow + 1][nextCol] == 0);
                }
                if (result) {
                    board[row][col] = 0;
                    board[row + 1][col] = 0;
                    board[nextRow][nextCol] = 3;
                    board[nextRow + 1][nextCol] = 3;
                    return true;
                }
            }
        } else if (board[row][col] == 4) {
            if (checkInSize(board, nextRow, nextCol) && checkInSize(board, nextRow + 1, nextCol + 1)) {
                if (direction == Direction.UP) {
                    result = board[row - 1][col] == 0 && board[row - 1][col + 1] == 0;
                } else if (direction == Direction.DOWN) {
                    result = board[row + 2][col] == 0 && board[row + 2][col + 1] == 0;
                } else if (direction == Direction.LEFT) {
                    result = board[row][col - 1] == 0 && board[row + 1][col - 1] == 0;
                } else {
                    result = board[row][col + 2] == 0 && board[row + 1][col + 2] == 0;
                }
                if (result) {
                    board[row][col] = 0;
                    board[row + 1][col] = 0;
                    board[row][col + 1] = 0;
                    board[row + 1][col + 1] = 0;
                    board[nextRow][nextCol] = 4;
                    board[nextRow + 1][nextCol] = 4;
                    board[nextRow][nextCol + 1] = 4;
                    board[nextRow + 1][nextCol + 1] = 4;
                    return true;
                }
            }
        }
        return false;
    }

    // 索引化棋盘，将每个点所在块的左上角坐标作为索引存储在一个新的二维数组中
    private int[][] indexBoard(int[][] board) {
        int[][] indexBoard = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != 0) {
                    int index = i * board[0].length + j;
                    if (board[i][j] == 1) {
                        board[i][j] = 0;
                        indexBoard[i][j] = index;
                    } else if (board[i][j] == 2) {
                        board[i][j] = 0;
                        board[i][j + 1] = 0;
                        indexBoard[i][j] = index;
                        indexBoard[i][j + 1] = index;
                    } else if (board[i][j] == 3) {
                        board[i][j] = 0;
                        board[i + 1][j] = 0;
                        indexBoard[i][j] = index;
                        indexBoard[i + 1][j] = index;
                    } else if (board[i][j] == 4) {
                        board[i][j] = 0;
                        board[i + 1][j] = 0;
                        board[i][j + 1] = 0;
                        board[i + 1][j + 1] = 0;
                        indexBoard[i][j] = index;
                        indexBoard[i + 1][j] = index;
                        indexBoard[i][j + 1] = index;
                        indexBoard[i + 1][j + 1] = index;
                    }
                }
            }
        }
        return indexBoard;
    }

    // 回溯路径，返回一个包含所有移动的列表
    private List<AIMovement> reconstructPath(State state) {
        LinkedList<AIMovement> path = new LinkedList<>();
        while (state != null && state.direction != null) {
            path.addFirst(new AIMovement(state.row, state.col, state.direction));
            state = state.parent;
        }
        return path;
    }
}
