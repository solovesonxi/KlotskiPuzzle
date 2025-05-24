package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

// 游戏方块组件
public class BoxComponent extends JComponent {
    private final Image image; // 方块图像
    private int row; // 行坐标
    private int col; // 列坐标
    private boolean isSelected; // 是否被选中

    // 以该方块左上角的坐标为准
    public BoxComponent(String path, int row, int col) {
        this.image =  Toolkit.getDefaultToolkit().getImage("resources/image/"+path); // 加载图像
        this.row = row;
        this.col = col;
        isSelected = false; // 初始状态为未选择
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = (getWidth() - image.getWidth(null)) / 2;
        int y = (getHeight() - image.getHeight(null)) / 2;
        g.drawImage(image, x, y, this);
        Border border;
        if (isSelected) {
            border = BorderFactory.createLineBorder(Color.YELLOW, 4); // 选中时边框颜色
        } else {
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1); // 未选中时边框颜色
        }
        this.setBorder(border);
    }

    // 设置选中状态
    public void setSelected(boolean selected) {
        isSelected = selected;
        this.repaint(); // 刷新面板
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}