package controller;

import data.GameSaveRepository;
import data.LeaderboardRepository;
import model.BoardRules;
import model.Direction;
import model.Difficulty;
import model.MapModel;
import util.SoundEffectPlayer;
import view.game.BoxComponent;
import view.game.GamePanel;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static util.Messages.text;

/** Coordinates player input, movement animation, and the current game session. */
public final class GameController {
    private static final String MOVE_SOUND =
            "resources/original/audio/sound-effect/move.wav";
    private static final String VICTORY_SOUND =
            "resources/original/audio/sound-effect/victory.wav";
    private static final String DEFEAT_SOUND =
            "resources/original/audio/sound-effect/defeat.wav";

    private final GamePanel view;
    public final MapModel model;
    private final List<int[][]> history = new ArrayList<>();
    private final Difficulty difficulty;
    private final int[][] initialMatrix;
    private final GameSaveRepository saves;
    private final LeaderboardRepository leaderboard;
    private final SoundEffectPlayer soundEffects;
    public final String user;

    private boolean animating;
    private boolean disposed;
    private Timer movementTimer;

    public GameController(GamePanel view, MapModel model, String user, Difficulty difficulty) {
        this(view, model, user, difficulty, new GameSaveRepository(), null);
    }

    GameController(GamePanel view, MapModel model, String user, Difficulty difficulty,
                   GameSaveRepository saves, LeaderboardRepository leaderboard) {
        this.view = view;
        this.model = model;
        this.user = user;
        this.difficulty = difficulty;
        this.saves = saves;
        this.leaderboard = leaderboard;
        this.initialMatrix = BoardRules.copy(model.getMatrix());
        this.soundEffects = new SoundEffectPlayer(message -> SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(view, message)));
        view.setController(this);
        history.add(BoardRules.copy(model.getMatrix()));
    }

    public void restartGame() {
        if (!ensureIdle()) {
            return;
        }
        model.updateMatrix(BoardRules.copy(initialMatrix));
        history.clear();
        history.add(BoardRules.copy(initialMatrix));
        view.initialGame(0, 180);
    }

    public boolean doMove(int row, int column, Direction direction) {
        if (disposed || animating) {
            return false;
        }
        int nextRow = row + direction.getRow();
        int nextColumn = column + direction.getCol();
        int[][] moved = BoardRules.applyMove(model.getMatrix(), row, column, direction);
        if (moved == null) {
            return false;
        }
        animating = true;
        model.updateMatrix(moved);
        continueMove(nextRow, nextColumn);
        return true;
    }

    private void continueMove(int nextRow, int nextColumn) {
        BoxComponent box = view.getSelectedBox();
        int startRow = box.getRow();
        int startColumn = box.getCol();
        int deltaX = (nextColumn - startColumn) * view.getGRID_SIZE();
        int deltaY = (nextRow - startRow) * view.getGRID_SIZE();
        int totalFrames = 10;
        int[] frame = {0};
        soundEffects.play(MOVE_SOUND);
        movementTimer = new Timer(1, event -> {
            if (frame[0] < totalFrames) {
                double progress = (double) frame[0] / totalFrames;
                progress = 1 - (1 - progress) * (1 - progress);
                box.setLocation(
                        (int) (startColumn * view.getGRID_SIZE() + deltaX * progress + 2),
                        (int) (startRow * view.getGRID_SIZE() + deltaY * progress + 2));
                box.repaint();
                frame[0]++;
                return;
            }

            ((Timer) event.getSource()).stop();
            box.setRow(nextRow);
            box.setCol(nextColumn);
            box.setLocation(nextColumn * view.getGRID_SIZE() + 2,
                    nextRow * view.getGRID_SIZE() + 2);
            box.repaint();
            history.add(BoardRules.copy(model.getMatrix()));
            animating = false;
            movementTimer = null;
            endGame(true);
        });
        movementTimer.start();
    }

    public void endGame(boolean checkSolved) {
        if (!checkSolved && animating) {
            stopMovementAnimation();
            view.initialGame(view.steps, -1);
        }
        if (checkSolved && !BoardRules.isSolved(model.getMatrix())) {
            return;
        }

        view.countdownTimer.stop();
        if (checkSolved) {
            soundEffects.play(VICTORY_SOUND);
            if (user != null && difficulty.isRanked()) {
                saveLeaderboardEntry();
            }
        } else {
            soundEffects.play(DEFEAT_SOUND);
        }

        String title = checkSolved ? text("game.victory", view.steps) : text("game.defeat");
        int choice = JOptionPane.showConfirmDialog(view, text("game.restart.prompt"), title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void saveLeaderboardEntry() {
        try {
            LeaderboardRepository repository = leaderboard == null
                    ? new LeaderboardRepository()
                    : leaderboard;
            repository.add(new LeaderboardRepository.ScoreEntry(
                    user, view.steps, readRemainingSeconds()));
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(view, text("game.score.save.error", exception.getMessage()));
        }
    }

    public void undo() {
        if (!ensureIdle()) {
            return;
        }
        if (history.size() <= 1) {
            JOptionPane.showMessageDialog(view, text("game.undo.empty"));
            return;
        }
        history.removeLast();
        model.updateMatrix(BoardRules.copy(history.getLast()));
        view.initialGame(view.steps - 1, -1);
    }

    public void loadGame() {
        if (!ensureIdle()) {
            return;
        }
        if (user == null || user.isBlank()) {
            JOptionPane.showMessageDialog(view, text("game.login.required"));
            return;
        }
        try {
            if (!saves.exists(user)) {
                JOptionPane.showMessageDialog(view, text("game.save.missing", user));
                return;
            }
            GameSaveRepository.SavedGame saved = saves.load(user);
            model.updateMatrix(saved.currentBoard());
            history.clear();
            history.addAll(saved.history());
            view.initialGame(saved.steps(), saved.remainingSeconds());
            if (saved.recoveredEntries() > 0) {
                JOptionPane.showMessageDialog(view, text("game.save.recovered", saved.recoveredEntries()));
            }
        } catch (GameSaveRepository.CorruptSaveException exception) {
            String reason = switch (exception.reason()) {
                case EMPTY -> text("game.save.corrupt.empty");
                case INVALID -> text("game.save.corrupt.invalid", exception.detail());
            };
            JOptionPane.showMessageDialog(view, text("game.save.corrupt", reason, exception.backupPath()));
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(view, text("game.save.read.error", exception.getMessage()));
        }
    }

    public void saveGame() {
        if (!ensureIdle()) {
            return;
        }
        if (user == null || user.isBlank()) {
            JOptionPane.showMessageDialog(view, text("game.save.guest"));
            return;
        }
        try {
            saves.save(user, new GameSaveRepository.SavedGame(
                    view.steps, readRemainingSeconds(), history, 0));
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(view, text("game.save.write.error", exception.getMessage()));
        }
    }

    public boolean isAnimating() {
        return animating;
    }

    public void dispose() {
        disposed = true;
        stopMovementAnimation();
        soundEffects.close();
    }

    private int readRemainingSeconds() {
        return view.getRemainingSeconds();
    }

    private boolean ensureIdle() {
        if (disposed) {
            return false;
        }
        if (animating) {
            JOptionPane.showMessageDialog(view, text("common.animation.wait"));
            return false;
        }
        return true;
    }

    private void stopMovementAnimation() {
        if (movementTimer != null) {
            movementTimer.stop();
            movementTimer = null;
        }
        animating = false;
    }
}
