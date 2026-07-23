package view.lab;

import lab.ExperimentRecord;
import lab.ExperimentRecordJson;
import lab.SearchExpansion;
import lab.SearchExperiment;
import lab.SearchExperimentRunner;
import lab.SearchObserver;
import lab.SolutionReplay;
import model.PuzzleDefinition;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static util.Messages.text;

/** Lab Mode coordinator; domain behavior remains behind runner, replay, and record modules. */
public final class LabPanel extends JPanel {
    private static final String OVERVIEW = "overview";
    private static final String INSPECTOR = "inspector";
    private static final String REPLAY = "replay";
    private static final int DETAILED_EXPANSION_LIMIT = 150;
    private static final int EXPANSION_SAMPLE_INTERVAL = 500;

    private final Runnable backAction;
    private final SearchExperimentRunner runner = new SearchExperimentRunner();
    private final JPanel toolbarHost = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final JButton backButton = GameTheme.createButton("");
    private final ExperimentControlsPanel controls = new ExperimentControlsPanel();
    private final LabBoardView boardView = new LabBoardView();
    private final SearchOverviewPanel overview = new SearchOverviewPanel();
    private final StateInspectorPanel inspector = new StateInspectorPanel(boardView::setState);
    private final SolutionReplayPanel replay = new SolutionReplayPanel(boardView::setState);
    private final CardLayout explanationLayout = new CardLayout();
    private final JPanel explanationCards = new JPanel(explanationLayout);
    private final JButton overviewTab = GameTheme.createButton("");
    private final JButton inspectorTab = GameTheme.createButton("");
    private final JButton replayTab = GameTheme.createButton("");
    private final JProgressBar progressBar = new JProgressBar();

    private SwingWorker<Execution, SearchExperimentRunner.Progress> worker;
    private SearchExperiment latestExperiment;
    private SearchExperimentRunner.Result latestResult;
    private String activeCard = OVERVIEW;
    private boolean inspectorAvailable;
    private boolean replayAvailable;

    public LabPanel(Runnable backAction, JPanel toolbar) {
        this.backAction = Objects.requireNonNull(backAction, "backAction");
        setLayout(new BorderLayout(16, 16));
        setBackground(GameTheme.INK);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 20, 22));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);
        attachToolbar(toolbar);
        installListeners();
        applyLanguage();
        refreshPuzzle(controls.selectedPuzzle());
        setRunning(false);
    }

    public void attachToolbar(JPanel toolbar) {
        toolbarHost.removeAll();
        toolbarHost.add(Objects.requireNonNull(toolbar, "toolbar"));
        toolbarHost.revalidate();
        toolbarHost.repaint();
    }

    public void onShown() {
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    public void applyLanguage() {
        titleLabel.setText(text("lab.title"));
        subtitleLabel.setText(text("lab.subtitle"));
        backButton.setText(text("lab.back"));
        overviewTab.setText(text("lab.tab.overview"));
        inspectorTab.setText(text("lab.tab.inspector"));
        replayTab.setText(text("lab.tab.replay"));
        controls.applyLanguage();
        overview.applyLanguage();
        inspector.applyLanguage();
        replay.applyLanguage();
        boardView.repaint();
        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        backButton.setPreferredSize(new Dimension(152, 40));
        header.add(backButton, BorderLayout.WEST);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titleLabel.setFont(GameTheme.displayFont(28));
        titleLabel.setForeground(GameTheme.TEXT);
        subtitleLabel.setFont(GameTheme.bodyFont(13));
        subtitleLabel.setForeground(GameTheme.TEXT_MUTED);
        titles.add(titleLabel);
        titles.add(subtitleLabel);
        header.add(titles, BorderLayout.CENTER);
        toolbarHost.setOpaque(false);
        header.add(toolbarHost, BorderLayout.EAST);
        return header;
    }

    private JComponent buildWorkspace() {
        LabSurfacePanel boardSurface = new LabSurfacePanel();
        boardSurface.setLayout(new BorderLayout());
        boardSurface.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        boardSurface.add(boardView, BorderLayout.CENTER);
        boardSurface.setMinimumSize(new Dimension(340, 430));

        LabSurfacePanel workSurface = new LabSurfacePanel();
        workSurface.setLayout(new BorderLayout(8, 8));
        workSurface.add(controls, BorderLayout.NORTH);
        workSurface.add(buildExplanationWorkspace(), BorderLayout.CENTER);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(GameTheme.GOLD);
        progressBar.setBackground(GameTheme.SURFACE_RAISED);
        progressBar.setPreferredSize(new Dimension(10, 5));
        workSurface.add(progressBar, BorderLayout.SOUTH);
        workSurface.setMinimumSize(new Dimension(610, 430));
        return new LabSplitPane(boardSurface, workSurface);
    }

    private JPanel buildExplanationWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(8, 8));
        workspace.setOpaque(false);
        JPanel tabs = new JPanel(new GridLayout(1, 3, 8, 0));
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        overviewTab.setName("lab.tab.overview");
        inspectorTab.setName("lab.tab.inspector");
        replayTab.setName("lab.tab.replay");
        tabs.add(overviewTab);
        tabs.add(inspectorTab);
        tabs.add(replayTab);
        workspace.add(tabs, BorderLayout.NORTH);
        explanationCards.setOpaque(false);
        explanationCards.add(overview, OVERVIEW);
        explanationCards.add(inspector, INSPECTOR);
        explanationCards.add(replay, REPLAY);
        workspace.add(explanationCards, BorderLayout.CENTER);
        return workspace;
    }

    private void installListeners() {
        controls.onPuzzleChanged(this::refreshPuzzle);
        controls.onRun(this::startExperiment);
        controls.onCancel(this::cancelExperiment);
        controls.onExport(this::exportRecord);
        overview.onInspect(expansion -> {
            inspector.inspect(expansion);
            showExplanation(INSPECTOR);
        });
        overviewTab.addActionListener(event -> showExplanation(OVERVIEW));
        inspectorTab.addActionListener(event -> showExplanation(INSPECTOR));
        replayTab.addActionListener(event -> showExplanation(REPLAY));
        backButton.addActionListener(event -> {
            cancelExperiment();
            replay.stop();
            backAction.run();
        });
    }

    private void startExperiment() {
        if (worker != null && !worker.isDone()) {
            return;
        }
        SearchExperiment experiment = controls.selectedExperiment();
        latestExperiment = null;
        latestResult = null;
        inspectorAvailable = false;
        replayAvailable = false;
        inspector.clear();
        replay.clear();
        overview.started();
        controls.setRecordAvailable(false);
        boardView.setBoard(experiment.puzzle().initialBoard());
        showExplanation(OVERVIEW);
        setRunning(true);

        worker = new SwingWorker<>() {
            @Override
            protected Execution doInBackground() {
                List<SearchExpansion> expansions = new ArrayList<>();
                SearchExperimentRunner.Result result = runner.run(experiment,
                        new SearchObserver() {
                            @Override
                            public void onProgress(SearchExperimentRunner.Progress progress) {
                                publish(progress);
                            }

                            @Override
                            public void onExpansion(SearchExpansion expansion) {
                                expansions.add(expansion);
                            }

                            @Override
                            public boolean observesExpansion(int index) {
                                return index <= DETAILED_EXPANSION_LIMIT
                                        || index % EXPANSION_SAMPLE_INTERVAL == 0;
                            }
                        });
                return new Execution(experiment, result, List.copyOf(expansions));
            }

            @Override
            protected void process(List<SearchExperimentRunner.Progress> chunks) {
                overview.updateProgress(chunks.getLast());
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        overview.cancelled();
                    } else {
                        acceptExecution(get());
                    }
                } catch (CancellationException exception) {
                    overview.cancelled();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    overview.cancelled();
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    overview.failed(cause == null ? exception.getMessage() : cause.getMessage());
                } finally {
                    setRunning(false);
                }
            }
        };
        worker.execute();
    }

    private void acceptExecution(Execution execution) {
        latestExperiment = execution.experiment();
        latestResult = execution.result();
        overview.complete(latestResult, execution.expansions());
        inspectorAvailable = !execution.expansions().isEmpty();
        controls.setRecordAvailable(true);
        if (latestResult.status() == SearchExperimentRunner.Status.SOLVED
                || latestResult.status() == SearchExperimentRunner.Status.ALREADY_SOLVED) {
            replay.setReplay(SolutionReplay.of(latestExperiment.puzzle(), latestResult.solution()));
            replayAvailable = true;
            showExplanation(REPLAY);
        } else {
            showExplanation(OVERVIEW);
        }
    }

    private void cancelExperiment() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    private void refreshPuzzle(PuzzleDefinition puzzle) {
        if (worker != null && !worker.isDone()) {
            return;
        }
        boardView.setBoard(puzzle.initialBoard());
        latestExperiment = null;
        latestResult = null;
        inspectorAvailable = false;
        replayAvailable = false;
        overview.reset();
        inspector.clear();
        replay.clear();
        controls.setRecordAvailable(false);
        showExplanation(OVERVIEW);
    }

    private void exportRecord() {
        if (latestExperiment == null || latestResult == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(text("lab.export.title"));
        chooser.setFileFilter(new FileNameExtensionFilter(text("lab.export.filter"), "json"));
        String id = latestExperiment.puzzle().contentId().substring("sha256:".length(), 15);
        chooser.setSelectedFile(new java.io.File("klotski-experiment-"
                + latestExperiment.strategy().name().toLowerCase(Locale.ROOT)
                + "-" + id + ".json"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path output = chooser.getSelectedFile().toPath();
        if (!output.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
            output = output.resolveSibling(output.getFileName() + ".json");
        }
        try {
            ExperimentRecordJson.write(ExperimentRecord.capture(latestExperiment, latestResult), output);
            JOptionPane.showMessageDialog(this, text("lab.export.success", output.toAbsolutePath()));
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, text("lab.export.failed", exception.getMessage()),
                    text("lab.export.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showExplanation(String card) {
        activeCard = card;
        explanationLayout.show(explanationCards, card);
        overviewTab.setEnabled(true);
        inspectorTab.setEnabled(inspectorAvailable);
        replayTab.setEnabled(replayAvailable);
        markSelected(overviewTab, OVERVIEW.equals(card));
        markSelected(inspectorTab, INSPECTOR.equals(card));
        markSelected(replayTab, REPLAY.equals(card));
    }

    private static void markSelected(JButton button, boolean selected) {
        button.putClientProperty("selected", selected);
        button.repaint();
    }

    private void setRunning(boolean running) {
        controls.setRunning(running);
        progressBar.setIndeterminate(running);
        if (!running) {
            showExplanation(activeCard);
        }
    }

    private record Execution(SearchExperiment experiment, SearchExperimentRunner.Result result,
                             List<SearchExpansion> expansions) {
        private Execution {
            Objects.requireNonNull(experiment, "experiment");
            Objects.requireNonNull(result, "result");
            expansions = List.copyOf(expansions);
        }
    }
}
