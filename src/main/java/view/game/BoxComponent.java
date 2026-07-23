package view.game;

import util.AppResources;
import view.GameTheme;

import javax.swing.*;
import java.awt.*;

import static util.Messages.text;

// 游戏方块组件
public class BoxComponent extends JComponent {
    private final Image image; // 方块图像
    private final String labelKey;
    private int row; // 行坐标
    private int col; // 列坐标
    private boolean isSelected; // 是否被选中

    // 以该方块左上角的坐标为准
    public BoxComponent(String resourcePath, String labelKey, int row, int col) {
        this.image = AppResources.icon(resourcePath).getImage();
        this.labelKey = labelKey;
        this.row = row;
        this.col = col;
        isSelected = false; // 初始状态为未选择
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        drawLabel(graphics2D, text(labelKey));
        if (isSelected) {
            graphics2D.setColor(new Color(255, 214, 102, 72));
            graphics2D.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 18, 18);
            graphics2D.setColor(new Color(255, 224, 128));
            graphics2D.setStroke(new BasicStroke(4f));
            graphics2D.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 18, 18);
        }
        graphics2D.dispose();
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

    private void drawLabel(Graphics2D graphics, String label) {
        String[] lines = label.contains(" ") && getWidth() <= 110
                ? label.split(" ")
                : splitChineseLabel(label);
        int fontSize = getWidth() <= 100 ? 22 : 32;
        if (lines.length > 1) {
            fontSize = Math.min(fontSize, 21);
        }
        Font font = GameTheme.strongFont(fontSize);
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getHeight() - 2;
        int startY = (getHeight() - lineHeight * lines.length) / 2 + metrics.getAscent();
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            int x = (getWidth() - metrics.stringWidth(line)) / 2;
            int y = startY + index * lineHeight;
            graphics.setColor(new Color(34, 20, 15, 170));
            graphics.drawString(line, x + 2, y + 2);
            graphics.setColor(new Color(255, 242, 207));
            graphics.drawString(line, x, y);
        }
    }

    private String[] splitChineseLabel(String label) {
        if (getHeight() > getWidth() && label.codePointCount(0, label.length()) == 2) {
            return label.codePoints()
                    .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                    .toArray(String[]::new);
        }
        return new String[]{label};
    }
}
