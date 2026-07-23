package view.lab;

import lab.SearchExpansion;
import lab.SearchExperimentRunner;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static util.Messages.text;

/** Aggregate search progress plus a bounded, deterministic expansion timeline. */
final class SearchOverviewPanel extends JPanel {
    private enum ViewStatus { READY, RUNNING, CANCELLED, FAILED, RESULT }

    private final JLabel statusLabel = new JLabel();
    private final JLabel expandedLabel = metricLabel();
    private final JLabel discoveredLabel = metricLabel();
    private final JLabel frontierLabel = metricLabel();
    private final JLabel solutionLabel = metricLabel();
    private final JLabel elapsedLabel = metricLabel();
    private final DefaultListModel<SearchExpansion> timelineModel = new DefaultListModel<>();
    private final JList<SearchExpansion> timeline = new JList<>(timelineModel);
    private final JButton inspectButton = GameTheme.createButton("");
    private Consumer<SearchExpansion> inspectAction = expansion -> { };
    private ViewStatus viewStatus = ViewStatus.READY;
    private SearchExperimentRunner.Progress progress;
    private SearchExperimentRunner.Result result;
    private String failureMessage = "";

    SearchOverviewPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        statusLabel.setFont(GameTheme.strongFont(16));
        statusLabel.setForeground(GameTheme.GOLD);
        add(statusLabel, BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        inspectButton.setName("stateInspector.open");
        inspectButton.addActionListener(event -> {
            SearchExpansion selected = timeline.getSelectedValue();
            if (selected != null) {
                inspectAction.accept(selected);
            }
        });
        add(inspectButton, BorderLayout.SOUTH);
        timeline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timeline.setBackground(new Color(28, 25, 27));
        timeline.setForeground(GameTheme.TEXT);
        timeline.setFixedCellHeight(30);
        timeline.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        timeline.setCellRenderer(this::renderExpansion);
        reset();
    }

    void onInspect(Consumer<SearchExpansion> action) {
        inspectAction = Objects.requireNonNull(action, "action");
    }

    void reset() {
        viewStatus = ViewStatus.READY;
        progress = null;
        result = null;
        failureMessage = "";
        timelineModel.clear();
        inspectButton.setEnabled(false);
        refresh();
    }

    void started() {
        reset();
        viewStatus = ViewStatus.RUNNING;
        refresh();
    }

    void updateProgress(SearchExperimentRunner.Progress progress) {
        this.progress = Objects.requireNonNull(progress, "progress");
        refreshMetrics();
    }

    void complete(SearchExperimentRunner.Result result, List<SearchExpansion> expansions) {
        this.result = Objects.requireNonNull(result, "result");
        viewStatus = ViewStatus.RESULT;
        timelineModel.clear();
        expansions.forEach(timelineModel::addElement);
        if (!timelineModel.isEmpty()) {
            timeline.setSelectedIndex(0);
        }
        inspectButton.setEnabled(!timelineModel.isEmpty());
        refresh();
    }

    void cancelled() {
        viewStatus = ViewStatus.CANCELLED;
        refresh();
    }

    void failed(String message) {
        viewStatus = ViewStatus.FAILED;
        failureMessage = message == null ? "" : message;
        refresh();
    }

    void applyLanguage() {
        inspectButton.setText(text("lab.inspect.selected"));
        timeline.repaint();
        refresh();
    }

    private Component buildCenter() {
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 5, 6, 0));
        metrics.setOpaque(false);
        metrics.add(expandedLabel);
        metrics.add(discoveredLabel);
        metrics.add(frontierLabel);
        metrics.add(solutionLabel);
        metrics.add(elapsedLabel);
        center.add(metrics, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(BorderFactory.createLineBorder(GameTheme.GOLD_SOFT, 1));
        scroll.getViewport().setBackground(new Color(28, 25, 27));
        center.add(scroll, BorderLayout.CENTER);
        return center;
    }

    private Component renderExpansion(JList<? extends SearchExpansion> list,
                                      SearchExpansion value, int index, boolean selected,
                                      boolean focused) {
        JLabel label = new JLabel(text("lab.expansion.row", value.index(), value.pathCost(),
                value.heuristic(), formatPriority(value.priority()), value.frontierAfter()));
        label.setOpaque(true);
        label.setFont(GameTheme.bodyFont(12));
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        label.setBackground(selected ? GameTheme.LACQUER : new Color(28, 25, 27));
        label.setForeground(selected ? GameTheme.TEXT : GameTheme.TEXT_MUTED);
        return label;
    }

    private void refresh() {
        statusLabel.setText(switch (viewStatus) {
            case READY -> text("lab.ready");
            case RUNNING -> text("lab.searching");
            case CANCELLED -> text("lab.cancelled");
            case FAILED -> text("lab.failed", failureMessage);
            case RESULT -> resultStatusText(result.status());
        });
        refreshMetrics();
    }

    private void refreshMetrics() {
        int expanded = result != null ? result.metrics().expandedStates()
                : progress == null ? 0 : progress.expandedStates();
        int discovered = result != null ? result.metrics().discoveredStates()
                : progress == null ? 0 : progress.discoveredStates();
        int frontier = result != null ? result.metrics().maximumFrontier()
                : progress == null ? 0 : progress.frontierSize();
        int solution = result == null ? 0 : result.solution().size();
        long elapsedMillis = result == null ? 0 : result.elapsedNanos() / 1_000_000;
        expandedLabel.setText(text("lab.metric.expanded.short", expanded));
        discoveredLabel.setText(text("lab.metric.discovered.short", discovered));
        frontierLabel.setText(text("lab.metric.frontier.short", frontier));
        solutionLabel.setText(text("lab.metric.solution.short", solution));
        elapsedLabel.setText(text("lab.metric.elapsed.short", elapsedMillis));
    }

    private String resultStatusText(SearchExperimentRunner.Status status) {
        return switch (status) {
            case SOLVED -> text("lab.status.solved");
            case ALREADY_SOLVED -> text("lab.status.already");
            case NO_SOLUTION -> text("lab.status.no.solution");
            case CANCELLED -> text("lab.cancelled");
            case STATE_LIMIT_REACHED -> text("lab.status.limit");
        };
    }

    private static String formatPriority(double priority) {
        return priority == Math.rint(priority)
                ? Long.toString(Math.round(priority)) : String.format("%.2f", priority);
    }

    private static JLabel metricLabel() {
        JLabel label = new JLabel();
        label.setFont(GameTheme.bodyFont(12));
        label.setForeground(GameTheme.TEXT);
        return label;
    }
}
