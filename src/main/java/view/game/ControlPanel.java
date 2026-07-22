package view.game;

import controller.GameController;
import data.LeaderboardRepository;
import data.LeaderboardRepository.ScoreEntry;
import model.BoardRules;
import model.AIMovement;
import model.Difficulty;
import model.HuaRongDaoSolver;
import model.MapModel;
import util.AppResources;
import view.MainFrame;
import view.ViewUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
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
    private SwingWorker<HuaRongDaoSolver.Result, Void> aiWorker;
    private final Timer focusTimer;
    private boolean disposed;
    private long aiTaskGeneration;

    // 控制面板构造函数
    public ControlPanel(MainFrame mainFrame, int width, int height, JButton last, JButton next, JButton sound, MapModel mapModel, String user, Difficulty difficulty) {
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
        ImageIcon originalIcon = AppResources.icon("resources/image/background.png");
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
        restartBtn.addActionListener(event -> controller.restartGame());
        loadBtn.addActionListener(event -> controller.loadGame());
        undoBtn.addActionListener(event -> controller.undo());
        logoutBtn.addActionListener(event -> showConfirmationDialog(user));
        AIBtn.addActionListener(event -> AISolve());
        showRankBtn.addActionListener(this::showLeaderboard);
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

    // AI求解器
    private void AISolve() {
        if (disposed) {
            return;
        }
        if (!AIEnabled && controller.isAnimating()) {
            JOptionPane.showMessageDialog(this, "请等待当前移动动画结束");
            return;
        }
        if (AIEnabled)  {// 现在是AI托管状态，本次点击应该停止AI
            stopAI();
        } else {
            AIBtn.setText("停止推演");
            AIEnabled = true;
            setButtons(false); // 由AI接管，应该禁用其他按钮
            int[][] boardSnapshot = BoardRules.copy(controller.model.getMatrix());
            long taskGeneration = ++aiTaskGeneration;
            aiWorker = new SwingWorker<>() {
                @Override
                protected HuaRongDaoSolver.Result doInBackground() {
                    return solver.solveDetailed(
                            boardSnapshot,
                            HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES,
                            (expanded, discovered) -> SwingUtilities.invokeLater(() -> {
                                if (!disposed && AIEnabled && taskGeneration == aiTaskGeneration) {
                                    AIBtn.setText(formatSearchCount(discovered));
                                    AIBtn.setToolTipText("已展开 " + expanded + "，已发现 " + discovered);
                                }
                            }));
                }

                @Override
                protected void done() {
                    if (disposed || isCancelled()) {
                        return;
                    }
                    try {
                        handleSolveResult(get());
                    } catch (CancellationException exception) {
                        stopAI();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        restoreAfterAiError("求解已中断");
                    } catch (ExecutionException exception) {
                        restoreAfterAiError("求解失败：" + exception.getCause().getMessage());
                    } finally {
                        aiWorker = null;
                    }
                }
            };
            aiWorker.execute();
        }
    }

    private void handleSolveResult(HuaRongDaoSolver.Result result) {
        String metrics = String.format("（展开 %,d，发现 %,d）",
                result.expandedStates(), result.discoveredStates());
        AIBtn.setToolTipText(metrics);
        switch (result.status()) {
            case SOLVED -> startAiPlayback(new ArrayList<>(result.moves()));
            case ALREADY_SOLVED -> restoreAfterAiError("当前棋局已经完成" + metrics);
            case NO_SOLUTION -> restoreAfterAiError("当前棋局没有可行解" + metrics);
            case CANCELLED -> stopAI();
            case STATE_LIMIT_REACHED -> restoreAfterAiError(
                    "搜索达到状态上限 " + HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES + metrics);
        }
    }

    private void startAiPlayback(List<AIMovement> solved) {
        if (disposed) {
            return;
        }
        if (solved.isEmpty()) {
            restoreAfterAiError("当前棋局已完成或没有可行解");
            return;
        }
        AIBtn.setEnabled(true);
        AIBtn.setText("停止献策");
        AIMoveTimer = new Timer(500, e -> {
            if (solved.isEmpty()) {
                AIBtn.setText("军师献策");
                AIBtn.setToolTipText(null);
                AIEnabled = false;
                setButtons(true);
                ((Timer) e.getSource()).stop();
                AIMoveTimer = null;
            } else {
                AIMovement currentMove = solved.removeFirst();
                gamePanel.AIMove(currentMove.getRow(), currentMove.getCol(), currentMove.getDirection());
            }
        });
        AIMoveTimer.start();
    }

    private void restoreAfterAiError(String message) {
        if (disposed) {
            return;
        }
        AIBtn.setEnabled(true);
        AIBtn.setText("军师献策");
        AIBtn.setToolTipText(null);
        AIEnabled = false;
        setButtons(true);
        JOptionPane.showMessageDialog(this, message);
    }

    private void stopAI() {
        aiTaskGeneration++;
        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
        }
        aiWorker = null;
        if (AIMoveTimer != null) {
            AIMoveTimer.stop();
            AIMoveTimer = null;
        }
        AIBtn.setText("军师献策");
        AIBtn.setToolTipText(null);
        AIEnabled = false;
        setButtons(true);
    }

    private static String formatSearchCount(int discoveredStates) {
        if (discoveredStates >= 100_000) {
            return "推演 " + discoveredStates / 1_000 + "k";
        }
        if (discoveredStates >= 1_000) {
            return String.format("推演 %.1fk", discoveredStates / 1_000.0);
        }
        return "推演 " + discoveredStates;
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
        disposed = true;
        focusTimer.stop();
        if (aiWorker != null && !aiWorker.isDone()) {
            aiWorker.cancel(true);
        }
        aiWorker = null;
        if (AIMoveTimer != null) {
            AIMoveTimer.stop();
            AIMoveTimer = null;
        }
        if (gamePanel.countdownTimer != null) {
            gamePanel.countdownTimer.stop();
        }
        controller.dispose();
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
            scores.sort(showSteps.get()
                    ? LeaderboardRepository.bySteps()
                    : LeaderboardRepository.byElapsedTime());
            for (int i = 0; i < Math.min(scores.size(), 100); i++) {
                ScoreEntry entry = scores.get(i);
                int timeUsed = 180 - entry.remainingTime(); // 计算用时
                model.addRow(new Object[]{i + 1, entry.user(), entry.steps() + " 步", String.format("%d分%02d秒", timeUsed / 60, timeUsed % 60)}); // 添加行
            }
        };
        toggleBtn.addActionListener(toggleEvent -> {
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
        try {
            return new LeaderboardRepository().load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取榜单时发生错误: " + e.getMessage()); // 异常处理
            return new ArrayList<>();
        }
    }

    // 显示确认对话框
    private void showConfirmationDialog(String user) {
        if (controller.isAnimating()) {
            JOptionPane.showMessageDialog(this, "请等待当前移动动画结束");
            return;
        }
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
