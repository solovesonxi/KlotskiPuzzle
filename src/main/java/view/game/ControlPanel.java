package view.game;

import controller.AiSolveCoordinator;
import controller.GameController;
import model.Difficulty;
import model.MapModel;
import util.AppResources;
import view.GameTheme;
import view.MainFrame;

import javax.swing.*;
import java.awt.*;

import static util.Messages.text;

// 游戏控制面板，包含游戏操作按钮和状态信息
public class ControlPanel extends JPanel {
    private final MainFrame mainFrame; // 主窗口引用
    private final String user;
    public final GameController controller; // 游戏控制器
    private final GamePanel gamePanel; // 游戏面板
    private final AiSolveCoordinator aiCoordinator;
    private final JButton restartBtn; // 重启按钮
    private final JButton loadBtn; // 加载按钮
    private final JButton undoBtn; // 撤销按钮
    private final JButton logoutBtn; // 退出按钮
    private final JButton showRankBtn; // 显示排行榜按钮
    private final JButton AIBtn; // AI按钮
    private final JLabel playerLabel;
    private final JLabel stepLabel;
    private final JLabel countdownLabel;
    private final JLabel interactionHint;

    // 控制面板构造函数
    public ControlPanel(MainFrame mainFrame, int width, int height, JPanel musicControls,
                        MapModel mapModel, String user, Difficulty difficulty) {
        this.setLayout(null);
        this.setSize(width, height);
        this.setVisible(true);
        this.mainFrame = mainFrame;
        this.user = user;

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false);
        this.add(contentPanel); // 添加内容面板

        musicControls.setBounds(width - 326, 10, 306, 52);
        contentPanel.add(musicControls);

        // 设置背景
        ImageIcon originalIcon = AppResources.icon("resources/original/image/game-background.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        JLabel backgroundLabel = new JLabel(new ImageIcon(scaledImage));
        backgroundLabel.setBounds(0, 0, width, height);
        this.add(backgroundLabel);

        JPanel statusPanel = new SidePanel();
        statusPanel.setLayout(null);
        statusPanel.setBounds(width / 2 - 610, height / 2 - 300, 330, 360);
        contentPanel.add(statusPanel);
        playerLabel = createStatusLabel(statusPanel, 24, 24, 282, 58, 25,
                user == null ? text("control.guest") : text("control.player", user));
        stepLabel = createStatusLabel(statusPanel, 24, 94, 282, 42, 20, text("status.steps", 0));
        countdownLabel = createStatusLabel(statusPanel, 24, 142, 282, 42, 20,
                text("status.countdown", 180));
        interactionHint = createStatusLabel(statusPanel, 24, 216, 282, 112, 16,
                text("control.drag.hint"));
        interactionHint.setVerticalAlignment(SwingConstants.TOP);

        JPanel actionPanel = new SidePanel();
        actionPanel.setLayout(new GridLayout(6, 1, 0, 12));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        actionPanel.setBounds(width / 2 + 360, height / 2 - 286, 190, 390);
        contentPanel.add(actionPanel);
        restartBtn = GameTheme.createButton(text("control.restart"));
        loadBtn = GameTheme.createButton(text("control.load"));
        undoBtn = GameTheme.createButton(text("control.undo"));
        showRankBtn = GameTheme.createButton(text("control.leaderboard"));
        AIBtn = GameTheme.createButton(text("control.ai"));
        logoutBtn = GameTheme.createButton(text("control.logout"));
        for (JButton button : new JButton[]{restartBtn, loadBtn, undoBtn,
                showRankBtn, AIBtn, logoutBtn}) {
            actionPanel.add(button);
        }

        gamePanel = new GamePanel(mapModel);
        gamePanel.setLabel(stepLabel, countdownLabel);
        gamePanel.setLocation(width / 2 - gamePanel.getWidth() / 2, height / 2 - gamePanel.getHeight() / 2-50);
        this.controller = new GameController(gamePanel, mapModel, user, difficulty);
        this.aiCoordinator = new AiSolveCoordinator(
                this, AIBtn, gamePanel, controller, this::setButtons);
        this.add(gamePanel);
        // Swing's default insertion order is not a stable layering contract.
        // Keep the HUD above the board and the background below both.
        this.setComponentZOrder(contentPanel, 0);
        this.setComponentZOrder(gamePanel, 1);
        this.setComponentZOrder(backgroundLabel, 2);

        // 添加按钮事件
        restartBtn.addActionListener(event -> controller.restartGame());
        loadBtn.addActionListener(event -> controller.loadGame());
        undoBtn.addActionListener(event -> controller.undo());
        logoutBtn.addActionListener(event -> showConfirmationDialog(user));
        AIBtn.addActionListener(event -> aiCoordinator.toggle());
        showRankBtn.addActionListener(event -> LeaderboardDialog.show(this));
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }

    // 设置按钮状态
    private void setButtons(boolean enabled) {
        gamePanel.setInputEnabled(enabled);
        restartBtn.setEnabled(enabled);
        loadBtn.setEnabled(enabled);
        undoBtn.setEnabled(enabled);
        logoutBtn.setEnabled(enabled);
        showRankBtn.setEnabled(enabled);
    }

    /** Stops timers owned by this panel before it is replaced or hidden. */
    public void disposePanel() {
        aiCoordinator.close();
        if (gamePanel.countdownTimer != null) {
            gamePanel.countdownTimer.stop();
        }
        controller.dispose();
    }

    // 显示确认对话框
    private void showConfirmationDialog(String user) {
        if (controller.isAnimating()) {
            JOptionPane.showMessageDialog(this, text("common.animation.wait"));
            return;
        }
        gamePanel.countdownTimer.stop(); // 停止计时器
        if (user == null) {
            mainFrame.showLogin();
        } else {
            int response = JOptionPane.showConfirmDialog(this, text("control.save.prompt"),
                    text("common.confirm"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.CANCEL_OPTION) {
                if (response == JOptionPane.YES_OPTION) { // 确认保存
                    controller.saveGame();
                }
                mainFrame.showLogin();
            }
        }
    }

    public void applyLanguage() {
        restartBtn.setText(text("control.restart"));
        loadBtn.setText(text("control.load"));
        undoBtn.setText(text("control.undo"));
        logoutBtn.setText(text("control.logout"));
        showRankBtn.setText(text("control.leaderboard"));
        interactionHint.setText(text("control.drag.hint"));
        playerLabel.setText(user == null ? text("control.guest") : text("control.player", user));
        gamePanel.applyLanguage();
        aiCoordinator.applyLanguage();
    }

    private static JLabel createStatusLabel(JPanel owner, int x, int y, int width, int height,
                                            int fontSize, String value) {
        JLabel label = new JLabel(value);
        label.setBounds(x, y, width, height);
        label.setForeground(GameTheme.TEXT);
        label.setFont(GameTheme.bodyFont(fontSize));
        owner.add(label);
        return label;
    }

    private static final class SidePanel extends JPanel {
        private SidePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(24, 23, 27, 205));
            graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }
}
