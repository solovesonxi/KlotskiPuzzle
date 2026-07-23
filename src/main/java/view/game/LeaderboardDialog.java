package view.game;

import data.LeaderboardRepository;
import data.LeaderboardRepository.ScoreEntry;
import util.AppResources;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static util.Messages.text;

/** Displays the local leaderboard on a fully opaque themed surface. */
public final class LeaderboardDialog {
    private LeaderboardDialog() {
    }

    public static void show(JComponent owner) {
        List<ScoreEntry> scores;
        try {
            scores = new LeaderboardRepository().load();
        } catch (IOException exception) {
            scores = List.of();
        }

        Window ownerWindow = SwingUtilities.getWindowAncestor(owner);
        JDialog dialog = new JDialog(ownerWindow, text("leaderboard.title"),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(createContent(scores));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(620, 520);
        dialog.setMinimumSize(new Dimension(560, 460));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    static JPanel createContent(List<ScoreEntry> sourceScores) {
        List<ScoreEntry> scores = new ArrayList<>(sourceScores);
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{text("leaderboard.rank"), text("leaderboard.username"),
                        text("leaderboard.moves"), text("leaderboard.time")}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = createTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(true);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(new Color(243, 224, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(112, 72, 45), 2));

        JButton toggleButton = createToggleButton();
        AtomicBoolean showSteps = new AtomicBoolean(true);
        Runnable updateTable = () -> {
            model.setRowCount(0);
            scores.sort(showSteps.get()
                    ? LeaderboardRepository.bySteps()
                    : LeaderboardRepository.byElapsedTime());
            for (int index = 0; index < Math.min(scores.size(), 100); index++) {
                ScoreEntry entry = scores.get(index);
                int elapsedSeconds = 180 - entry.remainingTime();
                model.addRow(new Object[]{index + 1, entry.user(),
                        text("leaderboard.moves.value", entry.steps()),
                        text("leaderboard.time.value", elapsedSeconds / 60,
                                String.format("%02d", elapsedSeconds % 60))});
            }
        };
        toggleButton.addActionListener(event -> {
            showSteps.set(!showSteps.get());
            toggleButton.setText(showSteps.get()
                    ? text("leaderboard.show.time")
                    : text("leaderboard.show.moves"));
            updateTable.run();
        });
        updateTable.run();

        ParchmentPanel root = new ParchmentPanel();
        root.setLayout(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        JLabel subtitle = new JLabel(text("leaderboard.subtitle"));
        subtitle.setFont(GameTheme.displayFont(24));
        subtitle.setForeground(GameTheme.PAPER_TEXT);
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setOpaque(false);
        topPanel.add(subtitle, BorderLayout.WEST);
        topPanel.add(toggleButton, BorderLayout.EAST);
        root.add(topPanel, BorderLayout.NORTH);

        if (scores.isEmpty()) {
            toggleButton.setEnabled(false);
            JLabel empty = new JLabel(text("leaderboard.empty"), SwingConstants.CENTER);
            empty.setOpaque(true);
            empty.setBackground(GameTheme.PAPER);
            empty.setForeground(GameTheme.PAPER_TEXT);
            empty.setFont(GameTheme.bodyFont(20));
            empty.setBorder(BorderFactory.createLineBorder(new Color(112, 72, 45), 2));
            root.add(empty, BorderLayout.CENTER);
        } else {
            root.add(scrollPane, BorderLayout.CENTER);
        }
        return root;
    }

    private static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setOpaque(true);
        table.setRowHeight(34);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setFont(GameTheme.strongFont(15));
        table.getTableHeader().setBackground(new Color(96, 60, 38));
        table.getTableHeader().setForeground(new Color(255, 238, 199));
        table.setFont(GameTheme.bodyFont(14));
        table.setBackground(new Color(248, 232, 190));
        table.setForeground(new Color(55, 35, 24));
        table.setGridColor(new Color(161, 119, 75));
        table.setSelectionBackground(new Color(205, 166, 104));
        return table;
    }

    private static JButton createToggleButton() {
        return GameTheme.createButton(text("leaderboard.show.time"));
    }

    private static final class ParchmentPanel extends JPanel {
        private final Image parchment = AppResources.icon(
                "resources/original/image/parchment.png").getImage();

        private ParchmentPanel() {
            setOpaque(true);
            setBackground(GameTheme.PAPER);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(parchment, 0, 0, getWidth(), getHeight(), this);
            graphics2D.dispose();
        }
    }
}
