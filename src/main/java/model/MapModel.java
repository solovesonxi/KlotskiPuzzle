package model;

// 地图模型类，储存棋盘的状态和操作
public class MapModel {
    private int[][] matrix;

    public MapModel(int[][] matrix) {
        updateMatrix(matrix);
    }

    public int getWidth() {
        return this.matrix[0].length;
    }

    public int getHeight() {
        return this.matrix.length;
    }

    public int getId(int row, int col) {
        return matrix[row][col];
    }

    public int[][] getMatrix() {
        return BoardRules.copy(matrix);
    }

    public void updateMatrix(int[][] matrix) {
        BoardRules.validateGameBoard(matrix);
        this.matrix = BoardRules.copy(matrix);
    }

    public boolean isEmpty(int row, int col) {
        return matrix[row][col] == 0;
    }

    public boolean checkInSize(int row, int col) {
        return row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length;
    }
}
