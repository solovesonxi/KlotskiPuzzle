package model;

// AIMovement 类表示 AI 的移动信息
public class AIMovement{
    int row;
    int col;
    Direction direction;
    public AIMovement(int row, int col, Direction direction) {
        this.row = row;
        this.col = col;
        this.direction = direction;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Direction getDirection() {
        return direction;
    }
}
