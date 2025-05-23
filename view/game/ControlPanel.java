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
    private final MainFrame mainFrame;
    private final GameController controller;
    private final GamePanel gamePanel;
    private final HuaRongDaoSolver solver;
    private final JButton restartBtn;
    private final JButton loadBtn;
    private final JButton undoBtn;
    private final JButton logoutBtn;
    private final JButton showRankBtn;
    private final JButton AIBtn;
    private final JButton upBtn;
    private final JButton downBtn;
    private final JButton leftBtn;
    private final JButton rightBtn;
    private boolean AIEnabled = false;
    private Timer AIMoveTimer;

    public ControlPanel(MainFrame mainFrame, int width, int height, JButton last, JButton next, JButton sound, MapModel mapModel, String user, String difficulty) {
        this.setLayout(null);
        this.setSize(width, height);
        this.setVisible(true);
        this.mainFrame = mainFrame;
        this.solver = new HuaRongDaoSolver();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, width, height);
        contentPanel.setOpaque(false);
        this.add(contentPanel);

        contentPanel.add(last);
        contentPanel.add(next);
        contentPanel.add(sound);

        ImageIcon originalIcon = new ImageIcon("resources/image/login_bg.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        JLabel backgroundLabel = new JLabel(new ImageIcon(scaledImage));
        backgroundLabel.setBounds(0, 0, width, height);
        this.add(backgroundLabel);

        // 修改按钮文字和布局部分
        Color btnColor = new Color(94, 38, 18); // 改为深褐色更符合古战场风格
        Font btnFont = new Font("隶书", Font.PLAIN, 22);
        int buttonWidth = 120; // 适当加宽按钮
        int buttonHeight = 50; // 统一按钮高度
        // 右侧功能按钮组
        restartBtn = ViewUtil.createStyledButton(contentPanel, "重整旗鼓", new Point(width / 2 + 380, height / 2 - 200), buttonWidth, buttonHeight, btnColor, btnFont);
        loadBtn = ViewUtil.createStyledButton(contentPanel, "切换战场", new Point(width / 2 + 380, height / 2 - 100), buttonWidth, buttonHeight, btnColor, btnFont);
        undoBtn = ViewUtil.createStyledButton(contentPanel, "撤军回防", new Point(width / 2 + 380, height / 2), buttonWidth, buttonHeight, btnColor, btnFont);
        AIBtn = ViewUtil.createStyledButton(contentPanel, "军师献策", new Point(width / 2 + 380, height / 2 + 100), buttonWidth, buttonHeight, btnColor, btnFont);
        logoutBtn = ViewUtil.createStyledButton(contentPanel, "退出战场", new Point(width / 2 + 380, height / 2 + 200), buttonWidth, buttonHeight, btnColor, btnFont);
        showRankBtn = ViewUtil.createStyledButton(contentPanel, "<html>战<br>功<br>榜</html>", new Point(0, height / 2 - 100), 80, 200, new Color(54, 35, 28), // 深古铜色
                new Font("华文行楷", Font.PLAIN, 28));
        // 方向控制按钮组（增加古风边框）
        upBtn = ViewUtil.createAncientButton(contentPanel, "↑ 进", new Point(width / 2 - 470, height / 2 - 100), 80, 60);
        downBtn = ViewUtil.createAncientButton(contentPanel, "↓ 退", new Point(width / 2 - 470, height / 2 + 40), 80, 60);
        leftBtn = ViewUtil.createAncientButton(contentPanel, "← 左翼", new Point(width / 2 - 590, height / 2 - 30), 100, 50);
        rightBtn = ViewUtil.createAncientButton(contentPanel, "右翼 →", new Point(width / 2 - 370, height / 2 - 30), 100, 50);
        // 状态标签优化
        ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 370), 400, 120, 36, user == null ? "游侠身份" : "将军：" + user);
        JLabel stepLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 280), 240, 100, 28, "行军步数：0");
        JLabel countdownLabel = ViewUtil.createJLabel(contentPanel, new Point(width / 2 - 520, height / 2 - 200), 240, 100, 28, "剩余时限：120息");

        gamePanel = new GamePanel(mapModel);
        gamePanel.setLabel(stepLabel, countdownLabel);
        gamePanel.setLocation(width / 2 - gamePanel.getWidth() / 2, height / 2 - gamePanel.getHeight() / 2);
        this.controller = new GameController(gamePanel, mapModel, user, difficulty);
        this.add(gamePanel);
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
        Timer timer = new Timer(500, _ -> {
            if (gamePanel.isVisible()) {
                gamePanel.requestFocusInWindow(); //确保键盘被监听
            }
        });
        timer.start();
    }

    private void AISolve() {
        if (AIEnabled) {
            AIBtn.setText("军师献策");
            AIEnabled = false;
            setButtons(true);
            AIMoveTimer.stop();
        } else {
            AIBtn.setText("停止献策");
            setButtons(false);
            List<AIMovement> solved = solver.solve(controller.model.getMatrix());
            int totalSteps = solved.size();
            System.out.println("AI需走" + totalSteps + "步, 预计花费" + (totalSteps * 0.5) + "秒");
            if (solved.isEmpty()) {
                JOptionPane.showMessageDialog(this, "无解");
            } else {
                AIEnabled = true;
                AIMoveTimer = new Timer(500, e -> {
                    if (solved.isEmpty()) {
                        AIBtn.setText("军师献策");
                        AIEnabled = false;
                        setButtons(true);
                        ((Timer) e.getSource()).stop();
                    } else {
                        AIMovement currentMove = solved.removeFirst();
                        System.out.println("AI:" + (totalSteps - solved.size()) + " row=" + currentMove.getRow() + ", col=" + currentMove.getCol() + ", dir=" + currentMove.getDirection());
                        gamePanel.AIMove(currentMove.getRow(), currentMove.getCol(), currentMove.getDirection());
                    }
                });
                AIMoveTimer.start();
            }
        }
    }

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

    // 读取排行榜数据
    private void showLeaderboard(ActionEvent event) {
        List<ScoreEntry> scores = readScoresFromFile();
        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(this, "排行榜为空。");
            return;
        }
        DefaultTableModel model = new DefaultTableModel(new Object[]{"排名", "用户名", "步数", "耗时"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("楷体", Font.BOLD, 16));
        table.setFont(new Font("宋体", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JButton toggleBtn = new JButton("切换为时间榜");
        AtomicBoolean showSteps = new AtomicBoolean(true);

        Runnable updateTable = () -> {
            model.setRowCount(0);
            scores.sort((a, b) -> showSteps.get() ? Integer.compare(a.steps, b.steps) : Integer.compare(180 - a.remainingTime, 180 - b.remainingTime));
            for (int i = 0; i < Math.min(scores.size(), 100); i++) {
                ScoreEntry entry = scores.get(i);
                int timeUsed = 180 - entry.remainingTime;
                model.addRow(new Object[]{i + 1, entry.user, entry.steps + " 步", String.format("%d分%02d秒", timeUsed / 60, timeUsed % 60)});
            }
        };
        toggleBtn.addActionListener(_ -> {
            showSteps.set(!showSteps.get());
            toggleBtn.setText(showSteps.get() ? "切换为时间榜" : "切换为步数榜");
            updateTable.run();
        });
        updateTable.run();
        JDialog dialog = new JDialog(mainFrame, "排行榜", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(toggleBtn);
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private List<ScoreEntry> readScoresFromFile() {
        List<ScoreEntry> scores = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("resources/leaderboard.txt"));
            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length >= 3) {
                    String user = parts[0];
                    int steps = Integer.parseInt(parts[1]);
                    int remainingTime = Integer.parseInt(parts[2]);
                    scores.add(new ScoreEntry(user, steps, remainingTime));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取榜单时发生错误: " + e.getMessage());
        }
        return scores;
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

    private void showConfirmationDialog(String user) {
        gamePanel.countdownTimer.stop();
        if (user == null) {
            mainFrame.showLogin();
        } else {
            int response = JOptionPane.showConfirmDialog(this, "是否保存游戏进度？", "确认", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.CANCEL_OPTION) {
                if (response == JOptionPane.YES_OPTION) {
                    controller.saveGame();
                }
                mainFrame.showLogin();
            }
        }
    }
}
