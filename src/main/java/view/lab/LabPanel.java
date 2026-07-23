package view.lab;

import lab.SearchExperiment;
import lab.SearchExperimentRunner;
import lab.SearchStrategy;
import model.BoardRules;
import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzlePreset;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static util.Messages.text;

/** First independent Lab Mode vertical slice backed by the deterministic experiment runner. */
public final class LabPanel extends JPanel {
    private final Runnable backAction;
    private final SearchExperimentRunner runner = new SearchExperimentRunner();
    private final JPanel toolbarHost = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JLabel presetLabel = createFieldLabel();
    private final JLabel movementLabel = createFieldLabel();
    private final JLabel strategyLabel = createFieldLabel();
    private final JLabel weightLabel = createFieldLabel();
    private final JLabel contentIdLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel expandedLabel = createMetricLabel();
    private final JLabel discoveredLabel = createMetricLabel();
    private final JLabel frontierLabel = createMetricLabel();
    private final JLabel solutionLabel = createMetricLabel();
    private final JLabel elapsedLabel = createMetricLabel();
    private final JComboBox<PuzzlePreset> presetBox = new JComboBox<>(PuzzlePreset.values());
    private final JComboBox<MovementRule> movementBox = new JComboBox<>(MovementRule.values());
    private final JComboBox<SearchStrategy> strategyBox = new JComboBox<>(SearchStrategy.values());
    private final JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(
            SearchExperiment.DEFAULT_WEIGHTED_A_STAR_WEIGHT, 1.0, 5.0, 0.1));
    private final JButton runButton = GameTheme.createButton("");
    private final JButton cancelButton = GameTheme.createButton("");
    private final JButton backButton = GameTheme.createButton("");
    private final JProgressBar progressBar = new JProgressBar();
    private final LabBoardPreview boardPreview = new LabBoardPreview();

    private SwingWorker<SearchExperimentRunner.Result, SearchExperimentRunner.Progress> worker;
    private SearchExperimentRunner.Progress latestProgress;
    private SearchExperimentRunner.Result latestResult;

    public LabPanel(Runnable backAction, JPanel toolbar) {
        this.backAction = Objects.requireNonNull(backAction, "backAction");
        setLayout(new BorderLayout(18, 18));
        setBackground(GameTheme.INK);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);
        attachToolbar(toolbar);
        installListeners();
        applyLanguage();
        refreshPuzzle();
        setRunning(false);
    }

    public void attachToolbar(JPanel toolbar) {
        toolbarHost.removeAll();
        toolbarHost.add(Objects.requireNonNull(toolbar, "toolbar"));
        toolbarHost.revalidate();
        toolbarHost.repaint();
    }

    public void onShown() {
        refreshPuzzle();
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void applyLanguage() {
        titleLabel.setText(text("lab.title"));
        subtitleLabel.setText(text("lab.subtitle"));
        presetLabel.setText(text("lab.preset"));
        movementLabel.setText(text("lab.movement"));
        strategyLabel.setText(text("lab.strategy"));
        weightLabel.setText(text("lab.weight"));
        runButton.setText(text("lab.run"));
        cancelButton.setText(text("lab.cancel"));
        backButton.setText(text("lab.back"));
        presetBox.repaint();
        movementBox.repaint();
        strategyBox.repaint();
        boardPreview.repaint();
        refreshPuzzleIdentity();
        refreshStatusAndMetrics();
        revalidate();
        repaint();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        backButton.setPreferredSize(new Dimension(160, 42));
        header.add(backButton, BorderLayout.WEST);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titleLabel.setFont(GameTheme.displayFont(30));
        titleLabel.setForeground(GameTheme.TEXT);
        subtitleLabel.setFont(GameTheme.bodyFont(14));
        subtitleLabel.setForeground(GameTheme.TEXT_MUTED);
        titles.add(titleLabel);
        titles.add(subtitleLabel);
        header.add(titles, BorderLayout.CENTER);

        toolbarHost.setOpaque(false);
        header.add(toolbarHost, BorderLayout.EAST);
        return header;
    }

    private JComponent buildWorkspace() {
        JPanel previewSurface = new SurfacePanel();
        previewSurface.setLayout(new BorderLayout());
        previewSurface.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        previewSurface.add(boardPreview, BorderLayout.CENTER);

        JPanel experimentSurface = new SurfacePanel();
        experimentSurface.setLayout(new BorderLayout(14, 14));
        experimentSurface.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        experimentSurface.add(buildControls(), BorderLayout.NORTH);
        experimentSurface.add(buildMetrics(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                previewSurface, experimentSurface);
        splitPane.setResizeWeight(0.42);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JComponent buildControls() {
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 6, 5, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;

        styleCombo(presetBox, this::presetText);
        styleCombo(movementBox, this::movementText);
        styleCombo(strategyBox, this::strategyText);
        weightSpinner.setFont(GameTheme.bodyFont(15));

        addField(controls, constraints, presetLabel, presetBox);
        addField(controls, constraints, movementLabel, movementBox);
        addField(controls, constraints, strategyLabel, strategyBox);
        addField(controls, constraints, weightLabel, weightSpinner);

        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        contentIdLabel.setFont(GameTheme.bodyFont(12));
        contentIdLabel.setForeground(GameTheme.TEXT_MUTED);
        controls.add(contentIdLabel, constraints);

        constraints.gridy++;
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.add(runButton);
        actions.add(cancelButton);
        controls.add(actions, constraints);
        return controls;
    }

    private JComponent buildMetrics() {
        JPanel metrics = new JPanel(new BorderLayout(12, 12));
        metrics.setOpaque(false);
        statusLabel.setFont(GameTheme.strongFont(17));
        statusLabel.setForeground(GameTheme.GOLD);
        metrics.add(statusLabel, BorderLayout.NORTH);

        JPanel values = new JPanel(new GridLayout(5, 1, 0, 8));
        values.setOpaque(false);
        values.add(expandedLabel);
        values.add(discoveredLabel);
        values.add(frontierLabel);
        values.add(solutionLabel);
        values.add(elapsedLabel);
        metrics.add(values, BorderLayout.CENTER);

        progressBar.setIndeterminate(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(GameTheme.GOLD);
        progressBar.setBackground(GameTheme.SURFACE_RAISED);
        metrics.add(progressBar, BorderLayout.SOUTH);
        return metrics;
    }

    private void installListeners() {
        presetBox.addActionListener(event -> refreshPuzzle());
        movementBox.addActionListener(event -> refreshPuzzle());
        strategyBox.addActionListener(event -> updateWeightAvailability());
        runButton.addActionListener(event -> startExperiment());
        cancelButton.addActionListener(event -> cancelExperiment());
        backButton.addActionListener(event -> {
            cancelExperiment();
            backAction.run();
        });
    }

    private void startExperiment() {
        if (worker != null && !worker.isDone()) {
            return;
        }
        PuzzleDefinition puzzle = selectedPuzzle();
        SearchStrategy strategy = selectedStrategy();
        double weight = strategy == SearchStrategy.WEIGHTED_A_STAR
                ? ((Number) weightSpinner.getValue()).doubleValue()
                : 1.0;
        SearchExperiment experiment = new SearchExperiment(puzzle, strategy, weight,
                SearchExperiment.DEFAULT_MAX_DISCOVERED_STATES);
        latestProgress = null;
        latestResult = null;
        setRunning(true);
        refreshStatusAndMetrics();

        worker = new SwingWorker<>() {
            @Override
            protected SearchExperimentRunner.Result doInBackground() {
                return runner.run(experiment, progress -> publish(progress));
            }

            @Override
            protected void process(List<SearchExperimentRunner.Progress> chunks) {
                latestProgress = chunks.getLast();
                refreshStatusAndMetrics();
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        statusLabel.setText(text("lab.cancelled"));
                    } else {
                        latestResult = get();
                        refreshStatusAndMetrics();
                    }
                } catch (CancellationException exception) {
                    statusLabel.setText(text("lab.cancelled"));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText(text("lab.cancelled"));
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    statusLabel.setText(text("lab.failed",
                            cause == null ? exception.getMessage() : cause.getMessage()));
                } finally {
                    setRunning(false);
                }
            }
        };
        worker.execute();
    }

    private void cancelExperiment() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    private void refreshPuzzle() {
        PuzzleDefinition puzzle = selectedPuzzle();
        boardPreview.setBoard(puzzle.initialBoard());
        latestProgress = null;
        latestResult = null;
        refreshPuzzleIdentity();
        refreshStatusAndMetrics();
    }

    private void refreshPuzzleIdentity() {
        if (presetBox.getSelectedItem() == null || movementBox.getSelectedItem() == null) {
            return;
        }
        String contentId = selectedPuzzle().contentId();
        String abbreviated = contentId.substring(0, Math.min(contentId.length(), 23)) + "…";
        contentIdLabel.setText(text("lab.content.id", abbreviated));
        contentIdLabel.setToolTipText(contentId);
    }

    private void refreshStatusAndMetrics() {
        boolean running = worker != null && !worker.isDone();
        if (running) {
            statusLabel.setText(text("lab.searching"));
        } else if (latestResult != null) {
            statusLabel.setText(resultStatusText(latestResult.status()));
        } else if (statusLabel.getText() == null || !statusLabel.getText().equals(text("lab.cancelled"))) {
            statusLabel.setText(text("lab.ready"));
        }

        int expanded = latestResult != null
                ? latestResult.metrics().expandedStates()
                : latestProgress == null ? 0 : latestProgress.expandedStates();
        int discovered = latestResult != null
                ? latestResult.metrics().discoveredStates()
                : latestProgress == null ? 0 : latestProgress.discoveredStates();
        int frontier = latestResult != null
                ? latestResult.metrics().maximumFrontier()
                : latestProgress == null ? 0 : latestProgress.frontierSize();
        int solution = latestResult == null ? 0 : latestResult.solution().size();
        long elapsedMillis = latestResult == null ? 0 : latestResult.elapsedNanos() / 1_000_000;
        expandedLabel.setText(text("lab.metric.expanded", expanded));
        discoveredLabel.setText(text("lab.metric.discovered", discovered));
        frontierLabel.setText(text("lab.metric.frontier", frontier));
        solutionLabel.setText(text("lab.metric.solution", solution));
        elapsedLabel.setText(text("lab.metric.elapsed", elapsedMillis));
    }

    private void setRunning(boolean running) {
        presetBox.setEnabled(!running);
        movementBox.setEnabled(!running);
        strategyBox.setEnabled(!running);
        weightSpinner.setEnabled(!running && selectedStrategy() == SearchStrategy.WEIGHTED_A_STAR);
        runButton.setEnabled(!running);
        cancelButton.setEnabled(running);
        backButton.setEnabled(!running);
        progressBar.setIndeterminate(running);
    }

    private void updateWeightAvailability() {
        boolean running = worker != null && !worker.isDone();
        weightSpinner.setEnabled(!running && selectedStrategy() == SearchStrategy.WEIGHTED_A_STAR);
    }

    private PuzzleDefinition selectedPuzzle() {
        PuzzlePreset preset = (PuzzlePreset) Objects.requireNonNull(presetBox.getSelectedItem());
        MovementRule rule = (MovementRule) Objects.requireNonNull(movementBox.getSelectedItem());
        return preset.definition(rule);
    }

    private SearchStrategy selectedStrategy() {
        return (SearchStrategy) Objects.requireNonNull(strategyBox.getSelectedItem());
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

    private static JLabel createFieldLabel() {
        JLabel label = new JLabel();
        label.setFont(GameTheme.strongFont(14));
        label.setForeground(GameTheme.TEXT_MUTED);
        return label;
    }

    private static JLabel createMetricLabel() {
        JLabel label = new JLabel();
        label.setFont(GameTheme.bodyFont(17));
        label.setForeground(GameTheme.TEXT);
        return label;
    }

    private static void addField(JPanel owner, GridBagConstraints constraints,
                                 JLabel label, JComponent field) {
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        owner.add(label, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        field.setPreferredSize(new Dimension(270, 36));
        owner.add(field, constraints);
        constraints.gridy++;
    }

    private static <T> void styleCombo(JComboBox<T> combo,
                                       java.util.function.Function<T, String> formatter) {
        combo.setFont(GameTheme.bodyFont(15));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focused) {
                Component component = super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value != null) {
                    setText(formatter.apply((T) value));
                }
                return component;
            }
        });
    }

    private static final class SurfacePanel extends JPanel {
        private SurfacePanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(GameTheme.SURFACE);
            graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class LabBoardPreview extends JComponent {
        private int[][] board = PuzzlePreset.TUTORIAL
                .definition(MovementRule.CELL_STEP).initialBoard();

        private LabBoardPreview() {
            setPreferredSize(new Dimension(420, 540));
            setMinimumSize(new Dimension(300, 380));
        }

        private void setBoard(int[][] board) {
            this.board = BoardRules.copy(board);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int cell = Math.max(40, Math.min((getWidth() - 32) / BoardRules.GAME_COLUMNS,
                    (getHeight() - 32) / BoardRules.GAME_ROWS));
            int boardWidth = cell * BoardRules.GAME_COLUMNS;
            int boardHeight = cell * BoardRules.GAME_ROWS;
            int originX = (getWidth() - boardWidth) / 2;
            int originY = (getHeight() - boardHeight) / 2;
            graphics2D.setColor(new Color(20, 18, 20));
            graphics2D.fillRoundRect(originX - 6, originY - 6,
                    boardWidth + 12, boardHeight + 12, 18, 18);
            graphics2D.setColor(new Color(214, 177, 105, 35));
            for (int column = 1; column < BoardRules.GAME_COLUMNS; column++) {
                graphics2D.drawLine(originX + column * cell, originY,
                        originX + column * cell, originY + boardHeight);
            }
            for (int row = 1; row < BoardRules.GAME_ROWS; row++) {
                graphics2D.drawLine(originX, originY + row * cell,
                        originX + boardWidth, originY + row * cell);
            }

            for (BoardRules.Piece piece : BoardRules.pieces(board)) {
                int pieceWidth = switch (piece.type()) {
                    case BoardRules.HORIZONTAL, BoardRules.CAO_CAO -> 2;
                    default -> 1;
                };
                int pieceHeight = switch (piece.type()) {
                    case BoardRules.VERTICAL, BoardRules.CAO_CAO -> 2;
                    default -> 1;
                };
                int x = originX + piece.col() * cell + 4;
                int y = originY + piece.row() * cell + 4;
                int width = pieceWidth * cell - 8;
                int height = pieceHeight * cell - 8;
                graphics2D.setColor(pieceColor(piece.type()));
                graphics2D.fillRoundRect(x, y, width, height, 16, 16);
                graphics2D.setColor(GameTheme.GOLD_SOFT);
                graphics2D.setStroke(new BasicStroke(1.4f));
                graphics2D.drawRoundRect(x, y, width, height, 16, 16);
                drawPieceLabel(graphics2D, pieceLabel(piece.type()), x, y, width, height);
            }

            graphics2D.setColor(GameTheme.GOLD);
            graphics2D.setStroke(new BasicStroke(4f));
            graphics2D.drawLine(originX + cell, originY + boardHeight + 5,
                    originX + cell, originY + boardHeight - 8);
            graphics2D.drawLine(originX + cell * 3, originY + boardHeight + 5,
                    originX + cell * 3, originY + boardHeight - 8);
            graphics2D.dispose();
        }

        private static Color pieceColor(int type) {
            return switch (type) {
                case BoardRules.CAO_CAO -> GameTheme.LACQUER_HOVER;
                case BoardRules.HORIZONTAL -> new Color(132, 91, 47);
                case BoardRules.VERTICAL -> new Color(72, 61, 56);
                case BoardRules.SOLDIER -> new Color(73, 83, 76);
                default -> GameTheme.SURFACE_RAISED;
            };
        }

        private static String pieceLabel(int type) {
            return switch (type) {
                case BoardRules.CAO_CAO -> text("lab.piece.target");
                case BoardRules.HORIZONTAL -> text("lab.piece.horizontal");
                case BoardRules.VERTICAL -> text("lab.piece.vertical");
                case BoardRules.SOLDIER -> text("lab.piece.soldier");
                default -> "";
            };
        }

        private static void drawPieceLabel(Graphics2D graphics, String label,
                                           int x, int y, int width, int height) {
            graphics.setFont(GameTheme.strongFont(Math.max(13, Math.min(20, width / 6))));
            FontMetrics metrics = graphics.getFontMetrics();
            int textX = x + (width - metrics.stringWidth(label)) / 2;
            int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
            graphics.setColor(GameTheme.TEXT);
            graphics.drawString(label, textX, textY);
        }
    }
}
