package model;

// 地图模型类，储存棋盘的状态和操作
public class MapModel {
    int[][] matrix;

    public MapModel(int[][] matrix) {
        this.matrix = matrix;
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
        return matrix;
    }

    public void updateMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public void set(int row, int col, int type) {
        matrix[row][col] = type;
    }

    public boolean isEmpty(int row, int col) {
        return matrix[row][col] == 0;
    }

    public boolean checkInSize(int row, int col) {
        return row >= 0 && row < matrix.length && col >= 0 && col < matrix[0].length;
    }
}
