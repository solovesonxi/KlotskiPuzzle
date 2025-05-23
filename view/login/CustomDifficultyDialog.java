package view.login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

public class CustomDifficultyDialog extends JDialog {
    private String selectedDifficulty; // 选择的难度
    private final Color DARK_BROWN = new Color(54, 35, 23); // 深棕色
    private final Color GOLD = new Color(205, 170, 109); // 金色

    public CustomDifficultyDialog(JFrame parent) {
        super(parent, "开始游戏", true);
        setupUI();
        setSize(553, 300);
        setLocationRelativeTo(parent);
        setResizable(false); // 不允许调整大小
    }

    // 设置用户界面
    private void setupUI() {
        JPanel contentPane = getPanel();
        JLabel title = new JLabel("请选择难度", SwingConstants.CENTER);
        title.setFont(new Font("篆体", Font.BOLD, 24));
        title.setForeground(DARK_BROWN);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // 设置边界
        contentPane.add(title, BorderLayout.NORTH); // 添加标题到北部

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 80, 30)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿设置
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // 设置合成模式
                g2d.setColor(new Color(240, 230, 210));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // 绘制圆角矩形
                g2d.dispose();
            }
        };
        buttonPanel.setOpaque(false);
        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS)); // 设置为垂直布局
        buttonContainer.setOpaque(false);
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50)); // 设置边界

        // 创建难度选择按钮
        String[] difficulties = {"初出茅庐", "刮目相待", "运筹帷幄"};
        for (String diff : difficulties) {
            JButton btn = createAncientButton(diff);
            btn.addActionListener(this::handleSelection);
            buttonPanel.add(btn);
        }
        buttonContainer.add(buttonPanel);
        contentPane.add(buttonContainer, BorderLayout.CENTER);
        setContentPane(contentPane); // 设置对话框内容面板
    }

    // 获取面板
    private static JPanel getPanel() {
        JPanel contentPane = new JPanel(new BorderLayout()) { // 创建面板
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                Image parchmentTexture = new ImageIcon("resources/image/parchment.png").getImage(); // 加载背景纹理
                g2d.setPaint(new TexturePaint(new BufferedImage(parchmentTexture.getWidth(null), parchmentTexture.getHeight(null), BufferedImage.TYPE_INT_ARGB) {{
                    Graphics2D g = createGraphics();
                    g.drawImage(parchmentTexture, 0, 0, null);
                    g.dispose();
                }}, new Rectangle(0, 0, parchmentTexture.getWidth(null), parchmentTexture.getHeight(null))));
                g2d.fillRect(0, 0, getWidth(), getHeight()); // 填充背景
                g2d.dispose();
            }
        };
        contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // 设置边界
        return contentPane;
    }

    // 创建古风按钮
    private JButton createAncientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿设置
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f)); // 设置合成模式
                g2d.setColor(getModel().isRollover() ? new Color(180, 150, 100) : GOLD); // 设置按钮颜色
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); // 绘制圆角矩形
                g2d.setColor(DARK_BROWN); // 设置边框颜色
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25); // 绘制边框
                g2d.setFont(new Font("楷体", Font.BOLD, 18)); // 设置字体
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2 + 25;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 40;
                for (int i = 0; i < text.length(); i++) {
                    String charStr = String.valueOf(text.charAt(i));
                    g2d.drawString(charStr, x, y + (i * fm.getHeight() * 1.2f)); // 绘制字符
                }
                g2d.dispose(); // 释放图形上下文
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 180);
            }
        };
        btn.setContentAreaFilled(false); // 不填充内容区域
        btn.setBorderPainted(false); // 不绘制边框
        btn.setFocusPainted(false); // 不绘制焦点
        return btn;
    }

    // 处理难度选择事件
    private void handleSelection(ActionEvent e) {
        selectedDifficulty = ((JButton) e.getSource()).getText();
        dispose(); // 关闭对话框
    }

    // 获取选择的难度
    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }
}