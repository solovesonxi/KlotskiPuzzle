package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

// 游戏方块组件
public class BoxComponent extends JComponent {
    private final Image image;
    private int row;
    private int col;
    private boolean isSelected;

    // 以该方块左上角的坐标为准
    public BoxComponent(String path, int row, int col) {
        this.image =  Toolkit.getDefaultToolkit().getImage("resources/image/"+path);
        this.row = row;
        this.col = col;
        isSelected = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = (getWidth() - image.getWidth(null)) / 2;
        int y = (getHeight() - image.getHeight(null)) / 2;
        g.drawImage(image, x, y, this);
        Border border;
        if (isSelected) {
            border = BorderFactory.createLineBorder(Color.YELLOW, 4);
        } else {
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        }
        this.setBorder(border);
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        this.repaint();
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
