package view.game;

import data.LeaderboardRepository;
import data.LeaderboardRepository.ScoreEntry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Builds and displays the local leaderboard without bloating the control panel. */
public final class LeaderboardDialog {
    private LeaderboardDialog() {
    }

    public static void show(JComponent owner) {
        List<ScoreEntry> scores;
        try {
            scores = new LeaderboardRepository().load();
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(owner, "读取榜单时发生错误: " + exception.getMessage());
            return;
        }
        if (scores.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "排行榜为空。");
            return;
        }

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"排名", "用户名", "步数", "耗时"}, 0) {
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
        JButton toggleButton = new JButton("切换为时间榜");
        AtomicBoolean showSteps = new AtomicBoolean(true);

        Runnable updateTable = () -> {
            model.setRowCount(0);
            scores.sort(showSteps.get()
                    ? LeaderboardRepository.bySteps()
                    : LeaderboardRepository.byElapsedTime());
            for (int index = 0; index < Math.min(scores.size(), 100); index++) {
                ScoreEntry entry = scores.get(index);
                int elapsedSeconds = 180 - entry.remainingTime();
                model.addRow(new Object[]{index + 1, entry.user(), entry.steps() + " 步",
                        String.format("%d分%02d秒", elapsedSeconds / 60, elapsedSeconds % 60)});
            }
        };
        toggleButton.addActionListener(event -> {
            showSteps.set(!showSteps.get());
            toggleButton.setText(showSteps.get() ? "切换为时间榜" : "切换为步数榜");
            updateTable.run();
        });
        updateTable.run();

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(owner);
        JDialog dialog = new JDialog(frame, "排行榜", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(toggleButton);
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
