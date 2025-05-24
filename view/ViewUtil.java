package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// 一个工具类，用于创建和设置 Swing 组件的样式
public class ViewUtil {
    // 创建 JLabel 组件
    public static JLabel createJLabel(JPanel frame, Point location, int width, int height, int size, String text) {
        JLabel jLabel = new JLabel(text);
        jLabel.setSize(width, height);
        jLabel.setLocation(location);
        jLabel.setForeground(new Color(255, 255, 224)); // 设置前景色
        jLabel.setFont(new Font("楷体", Font.PLAIN, size)); // 设置字体
        frame.add(jLabel); // 加入面板
        return jLabel;
    }

    // 创建 JTextField 组件
    public static JTextField createJTextField(JPanel frame, Point location, int width, int height) {
        JTextField jTextField = new JTextField();
        jTextField.setSize(width, height);
        jTextField.setLocation(location);
        frame.add(jTextField); // 加入面板
        return jTextField;
    }

    // 创建音乐按钮
    public static JButton createMusicButton(String path, Point pos) {
        JButton btn = new JButton(new ImageIcon(path)); // 设置按钮图标
        btn.setBounds(pos.x, pos.y, 50, 50); // 设置按钮位置和大小
        btn.setContentAreaFilled(false); // 不填充内容区域
        btn.setBorderPainted(false); // 不绘制边框
        return btn;
    }

    // 创建样式化按钮
    public static JButton createStyledButton(JPanel frame, String text, Point pos, int width, int height, Color bgColor, Font font) {
        JButton btn = new JButton(text);
        // 基础样式设置
        btn.setBounds(pos.x, pos.y, width, height); // 设置按钮位置和大小
        btn.setBackground(bgColor); // 设置背景颜色
        btn.setForeground(new Color(255, 223, 186)); // 浅米色文字
        btn.setFont(font); // 设置字体
        btn.setFocusPainted(false); // 不绘制焦点
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); // 设置边框
        btn.setOpaque(true); // 使组件不透明

        // 鼠标悬停效果
        btn.addMouseListener(new MouseAdapter() {
            private final Color originalBg = btn.getBackground(); // 记录原始背景色
            private final Color hoverBg = new Color((int) (originalBg.getRed() * 1.1), (int) (originalBg.getGreen() * 1.1), (int) (originalBg.getBlue() * 1.1), 200); // 悬停背景色

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg); // 设置悬停时背景
                btn.setForeground(Color.WHITE); // 设置悬停时文字颜色
                btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205, 133, 63, 220)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); // 设置悬停边框
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg); // 恢复原背景
                btn.setForeground(new Color(255, 223, 186)); // 恢复原文字颜色
                btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); // 恢复原边框
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(hoverBg.darker()); // 按下时背景变暗
            }
        });
        frame.add(btn); // 加入面板
        return btn;
    }

    // 创建古风按钮
    public static JButton createAncientButton(JPanel panel, String text, Point position, int width, int height) {
        JButton btn = ViewUtil.createStyledButton(panel, text, position, width, height, new Color(143, 86, 59), // 铜锈色
                new Font("华文隶书", Font.BOLD, 20));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205, 170, 125), 2), BorderFactory.createEmptyBorder(5, 10, 5, 10))); // 设置合成边框
        return btn;
    }

    // 创建退出按钮
    public static void createExitButton(JPanel frame, String text, Point pos, int width, int height, Color bgColor, Font font) {
        JButton btn = new JButton(text);
        // 基础样式设置
        btn.setBounds(pos.x, pos.y, width, height); // 设置按钮位置和大小
        btn.setBackground(bgColor); // 设置背景颜色
        btn.setForeground(new Color(255, 223, 0)); // 浅米色文字
        btn.setFont(font); // 设置字体
        btn.setFocusPainted(false); // 不绘制焦点
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15))); // 设置边框
        btn.setOpaque(true); // 使组件不透明
        btn.setEnabled(false); // 禁用按钮
        frame.add(btn); // 加入面板
    }

    // 为按钮添加鼠标监听器
    public static void addButtonMouseListener(JButton button, String path) {
        ImageIcon originalIcon = new ImageIcon(path); // 原始图标
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
