package view.game;

import controller.GameController;
import model.AIMovement;
import model.HuaRongDaoSolver;
import model.MapModel;
import view.MainFrame;
import view.ViewUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// 游戏控制面板，包含游戏操作按钮和状态信息
public class ControlPanel extends JPanel {
    private final MainFrame mainFrame; // 主窗口引用
    public final GameController controller; // 游戏控制器
    private final GamePanel gamePanel; // 游戏面板
    private final HuaRongDaoSolver solver; // AI求解器
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
    private boolean AIEnabled = false; // AI启用状态
    private Timer AIMoveTimer; // AI移动计时器

    // 控制面板构造函数
    public ControlPanel(MainFrame mainFrame, int width, int height, JButton last, JButton next, JButton sound, MapModel mapModel, String user, String difficulty) {
        this.setLayout(null);
        this.setSize(width, height);
        this.setVisible(true);
        this.mainFrame = mainFrame;
        this.solver = new HuaRongDaoSolver(); // 初始化AI求解器

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false);
        this.add(contentPanel); // 添加内容面板

        contentPanel.add(last);
        contentPanel.add(next);
        contentPanel.add(sound);

        // 设置背景
        ImageIcon originalIcon = new ImageIcon("resources/image/background.png");
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
        restartBtn = ViewUtil.createStyledButton(contentPanel, "重整旗鼓", new Point(width / 2 + 380, height / 2 - 250), buttonWidth, buttonHeight, btnColor, btnFont);
        loadBtn = ViewUtil.createStyledButton(contentPanel, "切换战场", new Point(width / 2 + 380, height / 2 - 150), buttonWidth, buttonHeight, btnColor, btnFont);
        undoBtn = ViewUtil.createStyledButton(contentPanel, "撤军回防", new Point(width / 2 + 380, height / 2-50), buttonWidth, buttonHeight, btnColor, btnFont);
        AIBtn = ViewUtil.createStyledButton(contentPanel, "军师献策", new Point(width / 2 + 380, height / 2 + 50), buttonWidth, buttonHeight, btnColor, btnFont);
        logoutBtn = ViewUtil.createStyledButton(contentPanel, "退出战场", new Point(width / 2 + 380, height / 2 + 150), buttonWidth, buttonHeight, btnColor, btnFont);
        showRankBtn = ViewUtil.createStyledButton(contentPanel, "<html>战<br>功<br>榜</html>", new Point(0, height / 2 - 100), 80, 200, new Color(54, 35, 28),new Font("华文行楷", Font.PLAIN, 28));
        ViewUtil.createExitButton(contentPanel, "华容道出口", new Point(width / 2 -100, height - 230), 200, 80, new Color(72, 60, 50, 200), new Font("华文行楷", Font.PLAIN, 30));
        // 方向控制按钮组
        upBtn = ViewUtil.createAncientButton(contentPanel, "↑ 进", new Point(width / 2 - 470, height / 2 - 100), 80, 60);
        downBtn = ViewUtil.createAncientButton(contentPanel, "↓ 退", new Point(width / 2 - 470, height / 2 + 40), 80, 60);
        leftBtn = ViewUtil.createAncientButton(contentPanel, "← 左翼", new Point(width / 2 - 590, height / 2 - 30), 100, 50);
        rightBtn = ViewUtil.createAncientButton(contentPanel, "右翼 →", new Point(width / 2 - 370, height / 2 - 30), 100, 50);
        // 状态标签
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 370), 400, 120, 36, user == null ? "游侠身份" : "将军：" + user);
        JLabel stepLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 280), 240, 100, 28, "行军步数：0");
        JLabel countdownLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 200), 240, 100, 28, "剩余时限：120息");

        gamePanel = new GamePanel(mapModel);
        gamePanel.setLabel(stepLabel, countdownLabel);
        gamePanel.setLocation(width / 2 - gamePanel.getWidth() / 2, height / 2 - gamePanel.getHeight() / 2-50);
        this.controller = new GameController(gamePanel, mapModel, user, difficulty);
        this.add(gamePanel);

        // 添加按钮事件
        restartBtn.addActionListener(_ -> controller.restartGame());
        loadBtn.addActionListener(_ -> controller.loadGame());
        undoBtn.addActionListener(_ -> controller.undo());
        logoutBtn.addActionListener(_ -> showConfirmationDialog(user));
        AIBtn.addActionListener(_ -> AISolve());
        showRankBtn.addActionListener(this::showLeaderboard);
        upBtn.addActionListener(_ -> gamePanel.doMoveUp());
        downBtn.addActionListener(_ -> gamePanel.doMoveDown());
        leftBtn.addActionListener(_ -> gamePanel.doMoveLeft());
        rightBtn.addActionListener(_ -> gamePanel.doMoveRight());

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

        Timer timer = new Timer(500, _ -> {
            if (gamePanel.isVisible()) {
                gamePanel.requestFocusInWindow(); //确保键盘被监听
            }
        });
        timer.start();
    }

    // AI求解器
    private void AISolve() {
        if (AIEnabled)  {// 现在是AI托管状态，本次点击应该停止AI
            AIBtn.setText("军师献策");
            AIEnabled = false;
            setButtons(true); // 启用其他按钮
            AIMoveTimer.stop();
        } else {
            AIBtn.setText("停止献策");
            setButtons(false); // 由AI接管，应该禁用其他按钮
            List<AIMovement> solved = solver.solve(controller.model.getMatrix()); // 求解
            int totalSteps = solved.size();
            System.out.println("AI需走" + totalSteps + "步, 预计花费" + (totalSteps * 0.5) + "秒");
            if (solved.isEmpty()) {
                JOptionPane.showMessageDialog(this, "无解"); // 提示无解
            } else {
                AIEnabled = true;
                AIMoveTimer = new Timer(500, e -> {
                    if (solved.isEmpty()) {
                        AIBtn.setText("军师献策");
                        AIEnabled = false; // AI已经完成所有步骤，取消AI状态
                        setButtons(true);
                        ((Timer) e.getSource()).stop();
                    } else {
                        AIMovement currentMove = solved.removeFirst(); // 获取当前移动
                        gamePanel.AIMove(currentMove.getRow(), currentMove.getCol(), currentMove.getDirection());
                    }
                });
                AIMoveTimer.start();
            }
        }
    }

    // 设置按钮状态
    private void setButtons(boolean enabled) {
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

    // 显示排行榜数据
    private void showLeaderboard(ActionEvent event) {
        List<ScoreEntry> scores = readScoresFromFile(); // 读取排行榜
        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "排行榜为空。");
            return;
        }
        DefaultTableModel model = new DefaultTableModel(new Object[]{"排名", "用户名", "步数", "耗时"}, 0) { // 创建表格模型
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30); // 设置行高
        table.getTableHeader().setFont(new Font("楷体", Font.BOLD, 16)); // 设置表头字体
        table.setFont(new Font("宋体", Font.PLAIN, 14)); // 设置表格字体
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 400)); // 设置滚动面板大小
        JButton toggleBtn = new JButton("切换为时间榜");
        AtomicBoolean showSteps = new AtomicBoolean(true); // 切换状态

        Runnable updateTable = () -> {
            model.setRowCount(0); // 清空表格
            scores.sort((a, b) -> showSteps.get() ? Integer.compare(a.steps, b.steps) : Integer.compare(180 - a.remainingTime, 180 - b.remainingTime)); // 根据选择排序
            for (int i = 0; i < Math.min(scores.size(), 100); i++) {
                ScoreEntry entry = scores.get(i);
                int timeUsed = 180 - entry.remainingTime; // 计算用时
                model.addRow(new Object[]{i + 1, entry.user, entry.steps + " 步", String.format("%d分%02d秒", timeUsed / 60, timeUsed % 60)}); // 添加行
            }
        };
        toggleBtn.addActionListener(_ -> {
            showSteps.set(!showSteps.get()); // 切换显示
            toggleBtn.setText(showSteps.get() ? "切换为时间榜" : "切换为步数榜");
            updateTable.run(); // 更新表格
        });
        updateTable.run(); // 初次更新
        JDialog dialog = new JDialog(mainFrame, "排行榜", true); // 创建对话框
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 顶部面板
        topPanel.add(toggleBtn);
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER); // 添加滚动面板
        dialog.pack();
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this); // 中心定位
        dialog.setVisible(true); // 显示对话框
    }

    // 从文件读取排行榜数据
    private List<ScoreEntry> readScoresFromFile() {
        List<ScoreEntry> scores = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("resources/leaderboard.txt")); // 读取文件
            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    String user = parts[0];
                    int steps = Integer.parseInt(parts[1]);
                    int remainingTime = Integer.parseInt(parts[2]);
                    scores.add(new ScoreEntry(user, steps, remainingTime)); // 添加分数条目
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取榜单时发生错误: " + e.getMessage()); // 异常处理
        }
        return scores; // 返回分数数据
    }

    // 分数条目类
    private static class ScoreEntry {
        String user;
        int steps;
        int remainingTime;

        ScoreEntry(String user, int steps, int remainingTime) {
            this.user = user;
            this.steps = steps;
            this.remainingTime = remainingTime;
        }
    }

    // 显示确认对话框
    private void showConfirmationDialog(String user) {
        gamePanel.countdownTimer.stop(); // 停止计时器
        if (user == null) {
            mainFrame.showLogin();
        } else {
            int response = JOptionPane.showConfirmDialog(this, "是否保存游戏进度？", "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.CANCEL_OPTION) {
                if (response == JOptionPane.YES_OPTION) { // 确认保存
                    controller.saveGame();
                }
                mainFrame.showLogin();
            }
        }
    }
}
