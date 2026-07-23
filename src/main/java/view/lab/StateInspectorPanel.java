package view.lab;

import lab.SearchDecision;
import lab.SearchExpansion;
import model.Direction;
import model.PuzzleMove;
import model.PuzzleState;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Objects;
import java.util.function.Consumer;

import static util.Messages.text;

/** Explains one expanded state and every accepted or rejected candidate move. */
final class StateInspectorPanel extends JPanel {
    private final JLabel titleLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final DefaultListModel<SearchExpansion.Candidate> candidateModel = new DefaultListModel<>();
    private final JList<SearchExpansion.Candidate> candidateList = new JList<>(candidateModel);
    private final JTextArea explanation = new JTextArea();
    private final JButton expandedStateButton = GameTheme.createButton("");
    private final Consumer<PuzzleState> stateSelected;
    private SearchExpansion expansion;

    StateInspectorPanel(Consumer<PuzzleState> stateSelected) {
        this.stateSelected = Objects.requireNonNull(stateSelected, "stateSelected");
        setOpaque(false);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);
        titleLabel.setFont(GameTheme.strongFont(16));
        titleLabel.setForeground(GameTheme.GOLD);
        scoreLabel.setFont(GameTheme.bodyFont(12));
        scoreLabel.setForeground(GameTheme.TEXT_MUTED);
        header.add(titleLabel, BorderLayout.NORTH);
        header.add(scoreLabel, BorderLayout.CENTER);
        header.add(expandedStateButton, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        candidateList.setBackground(new Color(28, 25, 27));
        candidateList.setForeground(GameTheme.TEXT);
        candidateList.setFixedCellHeight(34);
        candidateList.setCellRenderer(this::renderCandidate);
        candidateList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showCandidate(candidateList.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(candidateList);
        scroll.setBorder(BorderFactory.createLineBorder(GameTheme.GOLD_SOFT, 1));
        add(scroll, BorderLayout.CENTER);

        explanation.setEditable(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setRows(3);
        explanation.setFont(GameTheme.bodyFont(12));
        explanation.setForeground(GameTheme.TEXT_MUTED);
        explanation.setBackground(new Color(28, 25, 27));
        explanation.setBorder(BorderFactory.createEmptyBorder(7, 9, 7, 9));
        add(explanation, BorderLayout.SOUTH);
        expandedStateButton.addActionListener(event -> showExpandedState());
        applyLanguage();
    }

    void inspect(SearchExpansion expansion) {
        this.expansion = Objects.requireNonNull(expansion, "expansion");
        candidateModel.clear();
        expansion.candidates().forEach(candidateModel::addElement);
        titleLabel.setText(text("lab.inspector.expansion", expansion.index()));
        scoreLabel.setText(text("lab.inspector.scores", expansion.pathCost(),
                expansion.heuristic(), formatPriority(expansion.priority()),
                expansion.discoveredStates()));
        if (!candidateModel.isEmpty()) {
            candidateList.setSelectedIndex(0);
        } else {
            explanation.setText(expansion.goal()
                    ? text("lab.inspector.goal") : text("lab.inspector.no.candidates"));
        }
        showExpandedState();
    }

    void clear() {
        expansion = null;
        candidateModel.clear();
        titleLabel.setText(text("lab.inspector.empty"));
        scoreLabel.setText("");
        explanation.setText(text("lab.inspector.hint"));
        expandedStateButton.setEnabled(false);
    }

    void applyLanguage() {
        expandedStateButton.setText(text("lab.inspector.expanded.state"));
        candidateList.repaint();
        if (expansion == null) {
            clear();
        } else {
            inspect(expansion);
        }
    }

    private Component renderCandidate(JList<? extends SearchExpansion.Candidate> list,
                                      SearchExpansion.Candidate value, int index,
                                      boolean selected, boolean focused) {
        PuzzleMove move = value.move();
        JLabel label = new JLabel(text("lab.candidate.row", index + 1,
                directionText(move.direction()), move.distance(), value.pathCost(),
                value.heuristic(), decisionText(value.decision())));
        label.setOpaque(true);
        label.setFont(GameTheme.bodyFont(12));
        label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        label.setBackground(selected ? GameTheme.LACQUER : new Color(28, 25, 27));
        label.setForeground(value.accepted() ? GameTheme.TEXT : GameTheme.TEXT_MUTED);
        return label;
    }

    private void showCandidate(SearchExpansion.Candidate candidate) {
        if (candidate == null) {
            return;
        }
        stateSelected.accept(candidate.state());
        explanation.setText(decisionExplanation(candidate));
    }

    private void showExpandedState() {
        if (expansion == null) {
            return;
        }
        expandedStateButton.setEnabled(true);
        candidateList.clearSelection();
        stateSelected.accept(expansion.state());
        explanation.setText(text("lab.inspector.expanded.explanation",
                expansion.frontierBefore(), expansion.candidates().size()));
    }

    private String decisionExplanation(SearchExpansion.Candidate candidate) {
        return switch (candidate.decision()) {
            case DISCOVERED -> text("lab.decision.discovered");
            case IMPROVED -> text("lab.decision.improved", candidate.previousCost(),
                    candidate.pathCost());
            case REJECTED_NOT_BETTER -> text("lab.decision.rejected",
                    candidate.previousCost(), candidate.pathCost());
            case STATE_LIMIT_REACHED -> text("lab.decision.limit");
        };
    }

    private String decisionText(SearchDecision decision) {
        return switch (decision) {
            case DISCOVERED -> text("lab.decision.discovered.short");
            case IMPROVED -> text("lab.decision.improved.short");
            case REJECTED_NOT_BETTER -> text("lab.decision.rejected.short");
            case STATE_LIMIT_REACHED -> text("lab.decision.limit.short");
        };
    }

    private String directionText(Direction direction) {
        return switch (direction) {
            case LEFT -> text("lab.direction.left");
            case UP -> text("lab.direction.up");
            case RIGHT -> text("lab.direction.right");
            case DOWN -> text("lab.direction.down");
        };
    }

    private static String formatPriority(double priority) {
        return priority == Math.rint(priority)
                ? Long.toString(Math.round(priority)) : String.format("%.2f", priority);
    }
}
