package view.lab;

import lab.SearchExperiment;
import lab.SearchStrategy;
import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzlePreset;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Objects;
import java.util.function.Consumer;

import static util.Messages.text;

/** Owns experiment configuration and actions without coordinating search execution. */
final class ExperimentControlsPanel extends JPanel {
    private final JLabel presetLabel = fieldLabel();
    private final JLabel movementLabel = fieldLabel();
    private final JLabel strategyLabel = fieldLabel();
    private final JLabel weightLabel = fieldLabel();
    private final JLabel contentIdLabel = new JLabel();
    private final JComboBox<PuzzlePreset> presetBox = new JComboBox<>(PuzzlePreset.values());
    private final JComboBox<MovementRule> movementBox = new JComboBox<>(MovementRule.values());
    private final JComboBox<SearchStrategy> strategyBox = new JComboBox<>(SearchStrategy.values());
    private final JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(
            SearchExperiment.DEFAULT_WEIGHTED_A_STAR_WEIGHT, 1.0, 5.0, 0.1));
    private final JButton runButton = GameTheme.createButton("");
    private final JButton cancelButton = GameTheme.createButton("");
    private final JButton exportButton = GameTheme.createButton("");
    private Consumer<PuzzleDefinition> puzzleChanged = puzzle -> { };

    ExperimentControlsPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        GameTheme.styleComboBox(presetBox, this::presetText);
        GameTheme.styleComboBox(movementBox, this::movementText);
        GameTheme.styleComboBox(strategyBox, this::strategyText);
        GameTheme.styleSpinner(weightSpinner);
        runButton.setName("lab.run");
        cancelButton.setName("lab.cancel");
        exportButton.setName("experimentRecord.export");
        add(buildFields(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        installListeners();
        applyLanguage();
        refreshIdentity();
        setRunning(false);
        setRecordAvailable(false);
    }

    SearchExperiment selectedExperiment() {
        SearchStrategy strategy = selectedStrategy();
        double weight = strategy == SearchStrategy.WEIGHTED_A_STAR
                ? ((Number) weightSpinner.getValue()).doubleValue() : 1.0;
        return new SearchExperiment(selectedPuzzle(), strategy, weight,
                SearchExperiment.DEFAULT_MAX_DISCOVERED_STATES);
    }

    PuzzleDefinition selectedPuzzle() {
        PuzzlePreset preset = (PuzzlePreset) Objects.requireNonNull(presetBox.getSelectedItem());
        MovementRule rule = (MovementRule) Objects.requireNonNull(movementBox.getSelectedItem());
        return preset.definition(rule);
    }

    void onRun(Runnable action) {
        runButton.addActionListener(event -> action.run());
    }

    void onCancel(Runnable action) {
        cancelButton.addActionListener(event -> action.run());
    }

    void onExport(Runnable action) {
        exportButton.addActionListener(event -> action.run());
    }

    void onPuzzleChanged(Consumer<PuzzleDefinition> action) {
        puzzleChanged = Objects.requireNonNull(action, "action");
        puzzleChanged.accept(selectedPuzzle());
    }

    void setRunning(boolean running) {
        presetBox.setEnabled(!running);
        movementBox.setEnabled(!running);
        strategyBox.setEnabled(!running);
        weightSpinner.setEnabled(!running && selectedStrategy() == SearchStrategy.WEIGHTED_A_STAR);
        runButton.setEnabled(!running);
        cancelButton.setEnabled(running);
    }

    void setRecordAvailable(boolean available) {
        exportButton.setEnabled(available);
    }

    void applyLanguage() {
        presetLabel.setText(text("lab.preset"));
        movementLabel.setText(text("lab.movement"));
        strategyLabel.setText(text("lab.strategy"));
        weightLabel.setText(text("lab.weight"));
        runButton.setText(text("lab.run"));
        cancelButton.setText(text("lab.cancel"));
        exportButton.setText(text("lab.export"));
        presetBox.repaint();
        movementBox.repaint();
        strategyBox.repaint();
        refreshIdentity();
    }

    private JComponent buildFields() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        constraints.insets = new Insets(3, 5, 3, 5);
        addField(fields, constraints, 0, 0, presetLabel, presetBox);
        addField(fields, constraints, 1, 0, movementLabel, movementBox);
        addField(fields, constraints, 0, 1, strategyLabel, strategyBox);
        addField(fields, constraints, 1, 1, weightLabel, weightSpinner);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        contentIdLabel.setFont(GameTheme.bodyFont(11));
        contentIdLabel.setForeground(GameTheme.TEXT_MUTED);
        fields.add(contentIdLabel, constraints);
        return fields;
    }

    private JComponent buildActions() {
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);
        actions.add(runButton);
        actions.add(cancelButton);
        actions.add(exportButton);
        return actions;
    }

    private void installListeners() {
        presetBox.addActionListener(event -> selectionChanged());
        movementBox.addActionListener(event -> selectionChanged());
        strategyBox.addActionListener(event -> updateWeightAvailability());
    }

    private void selectionChanged() {
        refreshIdentity();
        puzzleChanged.accept(selectedPuzzle());
    }

    private void updateWeightAvailability() {
        weightSpinner.setEnabled(strategyBox.isEnabled()
                && selectedStrategy() == SearchStrategy.WEIGHTED_A_STAR);
    }

    private void refreshIdentity() {
        if (presetBox.getSelectedItem() == null || movementBox.getSelectedItem() == null) {
            return;
        }
        String id = selectedPuzzle().contentId();
        String shortId = id.substring(0, Math.min(id.length(), 23)) + "…";
        contentIdLabel.setText(text("lab.content.id", shortId));
        contentIdLabel.setToolTipText(id);
    }

    private SearchStrategy selectedStrategy() {
        return (SearchStrategy) Objects.requireNonNull(strategyBox.getSelectedItem());
    }

    private static void addField(JPanel owner, GridBagConstraints constraints, int x, int y,
                                 JLabel label, JComponent input) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setOpaque(false);
        block.add(label, BorderLayout.NORTH);
        input.setPreferredSize(new Dimension(190, 36));
        block.add(input, BorderLayout.CENTER);
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = 1;
        owner.add(block, constraints);
    }

    private static JLabel fieldLabel() {
        JLabel label = new JLabel();
        label.setFont(GameTheme.strongFont(12));
        label.setForeground(GameTheme.TEXT_MUTED);
        return label;
    }

    private String presetText(PuzzlePreset preset) {
        return switch (preset) {
            case TUTORIAL -> text("lab.preset.tutorial");
            case INTERMEDIATE -> text("lab.preset.intermediate");
            case HENG_DAO_LI_MA -> text("lab.preset.heng");
        };
    }

    private String movementText(MovementRule rule) {
        return switch (rule) {
            case CELL_STEP -> text("lab.rule.cell");
            case PIECE_MOVE -> text("lab.rule.piece");
        };
    }

    private String strategyText(SearchStrategy strategy) {
        return switch (strategy) {
            case BFS -> text("lab.strategy.bfs");
            case GREEDY_BEST_FIRST -> text("lab.strategy.greedy");
            case A_STAR -> text("lab.strategy.astar");
            case WEIGHTED_A_STAR -> text("lab.strategy.weighted");
        };
    }
}
