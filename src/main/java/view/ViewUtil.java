package view;

import util.AppResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 一个工具类，用于创建和设置 Swing 组件的样式
public class ViewUtil {
    // 创建音乐按钮
    public static JButton createMusicButton(String path, Point pos,
                                            String accessibleName, String toolTipText) {
        JButton btn = new JButton(AppResources.icon(path)); // 设置按钮图标
        btn.setBounds(pos.x, pos.y, 50, 50); // 设置按钮位置和大小
        btn.setContentAreaFilled(false); // 不填充内容区域
        btn.setBorderPainted(false); // 不绘制边框
        configureButtonAccessibility(btn, accessibleName, toolTipText);
        return btn;
    }

    static void configureButtonAccessibility(JButton button, String accessibleName,
                                             String toolTipText) {
        button.getAccessibleContext().setAccessibleName(accessibleName);
        button.getAccessibleContext().setAccessibleDescription(toolTipText);
        button.setToolTipText(toolTipText);
    }

    // 为按钮添加鼠标监听器
    public static void addButtonMouseListener(JButton button, String path) {
        ImageIcon originalIcon = AppResources.icon(path); // 原始图标
        Image img = originalIcon.getImage(); // 获取图标
        ImageIcon scaledIcon = new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 0.8), (int) (img.getHeight(null) * 0.8), Image.SCALE_SMOOTH)); // 缩小后的图标
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (button.getIcon() != null) {
                    button.setIcon(scaledIcon); // 设置缩小后的图标
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.getIcon() != null) {
                    button.setIcon(originalIcon); // 抬起时恢复原图标
                }
            }
        });
    }
}
