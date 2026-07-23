package controller;

import model.AIMovement;
import model.BoardRules;
import model.HuaRongDaoSolver;
import view.game.GamePanel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static util.Messages.text;

/** Owns the background-solve and EDT-playback lifecycle for the AI action. */
public final class AiSolveCoordinator implements AutoCloseable {
    private final JComponent owner;
    private final JButton actionButton;
    private final GamePanel gamePanel;
    private final GameController controller;
    private final Consumer<Boolean> inputState;
    private final HuaRongDaoSolver solver = new HuaRongDaoSolver();

    private SwingWorker<HuaRongDaoSolver.Result, Void> worker;
    private Timer playbackTimer;
    private boolean enabled;
    private boolean disposed;
    private long taskGeneration;

    public AiSolveCoordinator(JComponent owner, JButton actionButton, GamePanel gamePanel,
                              GameController controller, Consumer<Boolean> inputState) {
        this.owner = owner;
        this.actionButton = actionButton;
        this.gamePanel = gamePanel;
        this.controller = controller;
        this.inputState = inputState;
    }

    public void toggle() {
        if (disposed) {
            return;
        }
        if (enabled) {
            stop();
            return;
        }
        if (controller.isAnimating()) {
            JOptionPane.showMessageDialog(owner, text("common.animation.wait"));
            return;
        }

        enabled = true;
        actionButton.setText(text("ai.stop.search"));
        inputState.accept(false);
        int[][] boardSnapshot = BoardRules.copy(controller.model.getMatrix());
        long generation = ++taskGeneration;
        worker = new SwingWorker<>() {
            @Override
            protected HuaRongDaoSolver.Result doInBackground() {
                return solver.solveDetailed(
                        boardSnapshot,
                        HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES,
                        (expanded, discovered) -> SwingUtilities.invokeLater(() -> {
                            if (!disposed && enabled && generation == taskGeneration) {
                                actionButton.setText(formatSearchCount(discovered));
                                actionButton.setToolTipText(text("ai.search.tooltip", expanded, discovered));
                            }
                        }));
            }

            @Override
            protected void done() {
                if (disposed || isCancelled() || generation != taskGeneration) {
                    return;
                }
                try {
                    handleResult(get());
                } catch (CancellationException exception) {
                    stop();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    restoreAfterError(text("ai.interrupted"));
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    restoreAfterError(text("ai.failed",
                            cause == null ? exception.getMessage() : cause.getMessage()));
                } finally {
                    worker = null;
                }
            }
        };
        worker.execute();
    }

    private void handleResult(HuaRongDaoSolver.Result result) {
        String metrics = text("ai.metrics", result.expandedStates(), result.discoveredStates());
        actionButton.setToolTipText(metrics);
        switch (result.status()) {
            case SOLVED -> startPlayback(result.moves());
            case ALREADY_SOLVED -> restoreAfterError(text("ai.already.solved", metrics));
            case NO_SOLUTION -> restoreAfterError(text("ai.no.solution", metrics));
            case CANCELLED -> stop();
            case STATE_LIMIT_REACHED -> restoreAfterError(text("ai.state.limit",
                    HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES, metrics));
        }
    }

    private void startPlayback(List<AIMovement> moves) {
        if (disposed) {
            return;
        }
        List<AIMovement> remaining = new ArrayList<>(moves);
        if (remaining.isEmpty()) {
            restoreAfterError(text("ai.empty.path"));
            return;
        }

        actionButton.setEnabled(true);
        actionButton.setText(text("ai.stop.playback"));
        playbackTimer = new Timer(500, event -> {
            if (remaining.isEmpty()) {
                finishNormally();
                return;
            }
            AIMovement movement = remaining.removeFirst();
            gamePanel.AIMove(movement.getRow(), movement.getCol(), movement.getDirection());
        });
        playbackTimer.start();
    }

    private void finishNormally() {
        if (playbackTimer != null) {
            playbackTimer.stop();
            playbackTimer = null;
        }
        enabled = false;
        actionButton.setText(text("control.ai"));
        actionButton.setToolTipText(null);
        inputState.accept(true);
    }

    private void restoreAfterError(String message) {
        if (disposed) {
            return;
        }
        finishNormally();
        JOptionPane.showMessageDialog(owner, message);
    }

    public void stop() {
        taskGeneration++;
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
        worker = null;
        finishNormally();
    }

    private static String formatSearchCount(int discoveredStates) {
        if (discoveredStates >= 100_000) {
            return text("ai.search.progress", discoveredStates / 1_000 + "k");
        }
        if (discoveredStates >= 1_000) {
            return text("ai.search.progress", String.format(java.util.Locale.ROOT,
                    "%.1fk", discoveredStates / 1_000.0));
        }
        return text("ai.search.progress", discoveredStates);
    }

    @Override
    public void close() {
        disposed = true;
        taskGeneration++;
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
        worker = null;
        if (playbackTimer != null) {
            playbackTimer.stop();
            playbackTimer = null;
        }
        enabled = false;
    }
}
