package view.game;

import controller.AiSolveCoordinator;
import controller.GameController;
import model.Difficulty;
import model.MapModel;
import util.AppResources;
import view.MainFrame;
import view.ViewUtil;

import javax.swing.*;
import java.awt.*;

import static util.Messages.text;

// 游戏控制面板，包含游戏操作按钮和状态信息
public class ControlPanel extends JPanel {
    private final MainFrame mainFrame; // 主窗口引用
    public final GameController controller; // 游戏控制器
    private final GamePanel gamePanel; // 游戏面板
    private final AiSolveCoordinator aiCoordinator;
    private final JButton restartBtn; // 重启按钮
    private final JButton loadBtn; // 加载按钮
    private final JButton undoBtn; // 撤销按钮
    private final JButton logoutBtn; // 退出按钮
    private final JButton showRankBtn; // 显示排行榜按钮
    private final JButton AIBtn; // AI按钮
    private final JButton upBtn; // 上按钮
    private final JButton downBtn; // 下按钮
    private final JButton leftBtn; // 左按钮
    private final JButton rightBtn; // 右按钮
    private final Timer focusTimer;

    // 控制面板构造函数
    public ControlPanel(MainFrame mainFrame, int width, int height, JButton last, JButton next, JButton sound, MapModel mapModel, String user, Difficulty difficulty) {
        this.setLayout(null);
        this.setSize(width, height);
        this.setVisible(true);
        this.mainFrame = mainFrame;

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false);
        this.add(contentPanel); // 添加内容面板

        contentPanel.add(last);
        contentPanel.add(next);
        contentPanel.add(sound);

        // 设置背景
        ImageIcon originalIcon = AppResources.icon("resources/original/image/game-background.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        JLabel backgroundLabel = new JLabel(new ImageIcon(scaledImage));
        backgroundLabel.setBounds(0, 0, width, height);
        this.add(backgroundLabel);

        // 修改按钮文字和布局
        Color btnColor = new Color(94, 38, 18); // 改为深褐色
        Font btnFont = new Font("隶书", Font.PLAIN, 22); // 按钮字体
        int buttonWidth = 120; // 按钮宽度
        int buttonHeight = 50; // 按钮高度
        // 右侧功能按钮组
        restartBtn = ViewUtil.createStyledButton(contentPanel, text("control.restart"), new Point(width / 2 + 380, height / 2 - 250), buttonWidth, buttonHeight, btnColor, btnFont);
        loadBtn = ViewUtil.createStyledButton(contentPanel, text("control.load"), new Point(width / 2 + 380, height / 2 - 150), buttonWidth, buttonHeight, btnColor, btnFont);
        undoBtn = ViewUtil.createStyledButton(contentPanel, text("control.undo"), new Point(width / 2 + 380, height / 2-50), buttonWidth, buttonHeight, btnColor, btnFont);
        AIBtn = ViewUtil.createStyledButton(contentPanel, text("control.ai"), new Point(width / 2 + 380, height / 2 + 50), buttonWidth, buttonHeight, btnColor, btnFont);
        logoutBtn = ViewUtil.createStyledButton(contentPanel, text("control.logout"), new Point(width / 2 + 380, height / 2 + 150), buttonWidth, buttonHeight, btnColor, btnFont);
        showRankBtn = ViewUtil.createStyledButton(contentPanel, text("control.leaderboard"), new Point(0, height / 2 - 100), 100, 200, new Color(54, 35, 28),new Font("华文行楷", Font.PLAIN, 24));
        ViewUtil.createExitButton(contentPanel, text("control.exit"), new Point(width / 2 -100, height - 230), 200, 80, new Color(72, 60, 50, 200), new Font("华文行楷", Font.PLAIN, 30));
        // 方向控制按钮组
        upBtn = ViewUtil.createAncientButton(contentPanel, text("control.up"), new Point(width / 2 - 470, height / 2 - 100), 80, 60);
        downBtn = ViewUtil.createAncientButton(contentPanel, text("control.down"), new Point(width / 2 - 470, height / 2 + 40), 80, 60);
        leftBtn = ViewUtil.createAncientButton(contentPanel, text("control.left"), new Point(width / 2 - 590, height / 2 - 30), 100, 50);
        rightBtn = ViewUtil.createAncientButton(contentPanel, text("control.right"), new Point(width / 2 - 370, height / 2 - 30), 100, 50);
        // 状态标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 370), 400, 120, 32,
                user == null ? text("control.guest") : text("control.player", user));
        JLabel stepLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 280), 300, 100, 26, text("status.steps", 0));
        JLabel countdownLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 200), 300, 100, 26, text("status.countdown", 180));

        gamePanel = new GamePanel(mapModel);
        gamePanel.setLabel(stepLabel, countdownLabel);
        gamePanel.setLocation(width / 2 - gamePanel.getWidth() / 2, height / 2 - gamePanel.getHeight() / 2-50);
        this.controller = new GameController(gamePanel, mapModel, user, difficulty);
        this.aiCoordinator = new AiSolveCoordinator(
                this, AIBtn, gamePanel, controller, this::setButtons);
        this.add(gamePanel);

        // 添加按钮事件
        restartBtn.addActionListener(event -> controller.restartGame());
        loadBtn.addActionListener(event -> controller.loadGame());
        undoBtn.addActionListener(event -> controller.undo());
        logoutBtn.addActionListener(event -> showConfirmationDialog(user));
        AIBtn.addActionListener(event -> aiCoordinator.toggle());
        showRankBtn.addActionListener(event -> LeaderboardDialog.show(this));
        upBtn.addActionListener(event -> gamePanel.doMoveUp());
        downBtn.addActionListener(event -> gamePanel.doMoveDown());
        leftBtn.addActionListener(event -> gamePanel.doMoveLeft());
        rightBtn.addActionListener(event -> gamePanel.doMoveRight());

        JLabel exitLabel = new JLabel("") {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                // 动态居中定位
                super.setBounds((getParent().getWidth()-200)/2, getParent().getHeight()-50, 200, 30);
            }
        };

        // 样式配置
        exitLabel.setForeground(new Color(178, 34, 34)); // 火焰红
        exitLabel.setFont(new Font("华文隶书", Font.BOLD, 22));
        exitLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 添加半透明背景
        exitLabel.setOpaque(true);
        exitLabel.setBackground(new Color(255, 255, 255, 80)); // 半透明白色底

        this.add(exitLabel); // 添加到内容面板

        focusTimer = new Timer(500, event -> {
            if (gamePanel.isShowing()) {
                gamePanel.requestFocusInWindow(); //确保键盘被监听
            }
        });
        focusTimer.start();
    }

    // 设置按钮状态
    private void setButtons(boolean enabled) {
        gamePanel.setInputEnabled(enabled);
        restartBtn.setEnabled(enabled);
        loadBtn.setEnabled(enabled);
        undoBtn.setEnabled(enabled);
        logoutBtn.setEnabled(enabled);
        showRankBtn.setEnabled(enabled);
        upBtn.setEnabled(enabled);
        downBtn.setEnabled(enabled);
        leftBtn.setEnabled(enabled);
        rightBtn.setEnabled(enabled);
    }

    /** Stops timers owned by this panel before it is replaced or hidden. */
    public void disposePanel() {
        focusTimer.stop();
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
}
