package view.lab;

import lab.SolutionReplay;
import model.Direction;
import model.PuzzleMove;
import model.PuzzleState;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.function.Consumer;

import static util.Messages.text;

/** Interactive step, slider, and autoplay controls for a validated solution path. */
final class SolutionReplayPanel extends JPanel {
    private static final int AUTOPLAY_DELAY_MILLIS = 550;

    private final JLabel titleLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();
    private final JLabel moveLabel = new JLabel();
    private final JButton previousButton = GameTheme.createButton("");
    private final JButton playButton = GameTheme.createButton("");
    private final JButton nextButton = GameTheme.createButton("");
    private final JSlider slider = new JSlider(0, 0, 0);
    private final Timer autoplay = new Timer(AUTOPLAY_DELAY_MILLIS, event -> advance());
    private final Consumer<PuzzleState> stateSelected;
    private SolutionReplay replay;

    SolutionReplayPanel(Consumer<PuzzleState> stateSelected) {
        this.stateSelected = Objects.requireNonNull(stateSelected, "stateSelected");
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        titleLabel.setFont(GameTheme.strongFont(18));
        titleLabel.setForeground(GameTheme.GOLD);
        add(titleLabel, BorderLayout.NORTH);

        JPanel detail = new JPanel(new GridLayout(2, 1, 0, 8));
        detail.setOpaque(false);
        stepLabel.setFont(GameTheme.strongFont(16));
        stepLabel.setForeground(GameTheme.TEXT);
        moveLabel.setFont(GameTheme.bodyFont(14));
        moveLabel.setForeground(GameTheme.TEXT_MUTED);
        detail.add(stepLabel);
        detail.add(moveLabel);
        add(detail, BorderLayout.CENTER);

        JPanel controls = new JPanel(new BorderLayout(8, 8));
        controls.setOpaque(false);
        slider.setOpaque(false);
        slider.setForeground(GameTheme.GOLD);
        slider.setPaintTicks(false);
        slider.addChangeListener(event -> showStep(slider.getValue()));
        controls.add(slider, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        previousButton.setName("solutionReplay.previous");
        playButton.setName("solutionReplay.play");
        nextButton.setName("solutionReplay.next");
        previousButton.addActionListener(event -> showStep(slider.getValue() - 1));
        playButton.addActionListener(event -> toggleAutoplay());
        nextButton.addActionListener(event -> showStep(slider.getValue() + 1));
        buttons.add(previousButton);
        buttons.add(playButton);
        buttons.add(nextButton);
        controls.add(buttons, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
        clear();
        applyLanguage();
    }

    void setReplay(SolutionReplay replay) {
        stop();
        this.replay = Objects.requireNonNull(replay, "replay");
        slider.setMaximum(replay.lastStep());
        slider.setValue(0);
        showStep(0);
    }

    void clear() {
        stop();
        replay = null;
        slider.setMaximum(0);
        slider.setValue(0);
        stepLabel.setText(text("lab.replay.empty"));
        moveLabel.setText(text("lab.replay.hint"));
        updateButtons();
    }

    void stop() {
        autoplay.stop();
        updatePlayText();
    }

    void applyLanguage() {
        titleLabel.setText(text("lab.replay.title"));
        previousButton.setText(text("lab.replay.previous"));
        nextButton.setText(text("lab.replay.next"));
        updatePlayText();
        if (replay == null) {
            stepLabel.setText(text("lab.replay.empty"));
            moveLabel.setText(text("lab.replay.hint"));
        } else {
            showStep(slider.getValue());
        }
    }

    int currentStep() {
        return slider.getValue();
    }

    private void showStep(int requestedStep) {
        if (replay == null) {
            return;
        }
        int step = Math.max(0, Math.min(requestedStep, replay.lastStep()));
        if (slider.getValue() != step) {
            slider.setValue(step);
            return;
        }
        stateSelected.accept(replay.stateAt(step));
        stepLabel.setText(text("lab.replay.step", step, replay.lastStep()));
        moveLabel.setText(step == 0 ? text("lab.replay.initial")
                : moveText(replay.moveInto(step)));
        if (step == replay.lastStep()) {
            autoplay.stop();
        }
        updateButtons();
        updatePlayText();
    }

    private void toggleAutoplay() {
        if (replay == null || replay.lastStep() == 0) {
            return;
        }
        if (autoplay.isRunning()) {
            autoplay.stop();
        } else {
            if (slider.getValue() == replay.lastStep()) {
                showStep(0);
            }
            autoplay.start();
        }
        updatePlayText();
    }

    private void advance() {
        if (replay == null || slider.getValue() >= replay.lastStep()) {
            stop();
            return;
        }
        showStep(slider.getValue() + 1);
    }

    private void updateButtons() {
        boolean available = replay != null;
        previousButton.setEnabled(available && slider.getValue() > 0);
        nextButton.setEnabled(available && slider.getValue() < replay.lastStep());
        playButton.setEnabled(available && replay.lastStep() > 0);
        slider.setEnabled(available);
    }

    private void updatePlayText() {
        playButton.setText(autoplay.isRunning()
                ? text("lab.replay.pause") : text("lab.replay.play"));
    }

    private String moveText(PuzzleMove move) {
        return text("lab.replay.move", move.row() + 1, move.column() + 1,
                directionText(move.direction()), move.distance());
    }

    private String directionText(Direction direction) {
        return switch (direction) {
            case LEFT -> text("lab.direction.left");
            case UP -> text("lab.direction.up");
            case RIGHT -> text("lab.direction.right");
            case DOWN -> text("lab.direction.down");
        };
    }
}
