package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


// 一个工具类，用于创建和设置 Swing 组件的样式
public class ViewUtil {
    public static JLabel createJLabel(JPanel frame, Point location, int width, int height, int size, String text) {
        JLabel jLabel = new JLabel(text);
        jLabel.setSize(width, height);
        jLabel.setLocation(location);
        jLabel.setForeground(new Color(255, 255, 224));
        jLabel.setFont(new Font("楷体", Font.PLAIN, size));
        frame.add(jLabel);
        return jLabel;
    }

    public static JTextField createJTextField(JPanel frame, Point location, int width, int height) {
        JTextField jTextField = new JTextField();
        jTextField.setSize(width, height);
        jTextField.setLocation(location);
        frame.add(jTextField);
        return jTextField;
    }

    public static JButton createMusicButton(String path, Point pos) {
        JButton btn = new JButton(new ImageIcon(path));
        btn.setBounds(pos.x, pos.y, 50, 50);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        return btn;
    }

    public static JButton createStyledButton(JPanel frame, String text, Point pos, int width, int height, Color bgColor, Font font) {
        JButton btn = new JButton(text);
        // 基础样式设置
        btn.setBounds(pos.x, pos.y, width, height);
        btn.setBackground(bgColor);
        btn.setForeground(new Color(255, 223, 186)); // 浅米色文字
        btn.setFont(font);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        btn.setOpaque(true);

        // 鼠标悬停效果
        btn.addMouseListener(new MouseAdapter() {
            private final Color originalBg = btn.getBackground();
            private final Color hoverBg = new Color((int) (originalBg.getRed() * 1.1), (int) (originalBg.getGreen() * 1.1), (int) (originalBg.getBlue() * 1.1), 200);

            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205, 133, 63, 220)), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalBg);
                btn.setForeground(new Color(255, 223, 186));
                btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(139, 69, 19, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(hoverBg.darker());
            }
        });
        frame.add(btn);
        return btn;
    }


    public static JButton createAncientButton(JPanel panel, String text, Point position, int width, int height) {
        JButton btn = ViewUtil.createStyledButton(panel, text, position, width, height, new Color(143, 86, 59), // 铜锈色
                new Font("华文隶书", Font.BOLD, 20));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205, 170, 125), 2), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return btn;
    }

    public static void addButtonMouseListener(JButton button, String path) {
        ImageIcon originalIcon = new ImageIcon(path);
        Image img = originalIcon.getImage();
        ImageIcon scaledIcon = new ImageIcon(img.getScaledInstance((int) (img.getWidth(null) * 0.8), (int) (img.getHeight(null) * 0.8), Image.SCALE_SMOOTH));
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
                    button.setIcon(originalIcon);
                }
            }
        });
    }
}
