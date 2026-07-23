package view.game;

import model.Difficulty;
import model.MapModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import util.Messages;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static util.Messages.text;

class ControlPanelLanguageTest {
    private ControlPanel panel;

    @AfterEach
    void tearDown() throws Exception {
        if (panel != null) {
            SwingUtilities.invokeAndWait(panel::disposePanel);
        }
        Messages.useLocale(Locale.ENGLISH);
    }

    @Test
    void changingLanguageDoesNotStopAnActiveAiSolve() throws Exception {
        AtomicReference<JButton> aiButton = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            Messages.useLocale(Locale.ENGLISH);
            Difficulty difficulty = Difficulty.BEGINNER;
            panel = new ControlPanel(null, 1440, 900, new JPanel(),
                    new MapModel(difficulty.initialBoard()), null, difficulty);
            aiButton.set(findButton(panel, text("control.ai")));
            assertNotNull(aiButton.get());

            aiButton.get().doClick();
            Messages.useLocale(Locale.SIMPLIFIED_CHINESE);
            panel.applyLanguage();

            assertNotEquals(text("control.ai"), aiButton.get().getText(),
                    "language refresh must preserve the active AI task");
        });
    }

    @Test
    void playModeDoesNotDescribeTheSessionAsAGuest() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Messages.useLocale(Locale.ENGLISH);
            Difficulty difficulty = Difficulty.BEGINNER;
            panel = new ControlPanel(null, 1440, 900, new JPanel(),
                    new MapModel(difficulty.initialBoard()), null, difficulty);

            assertNotNull(findLabel(panel, text("control.play.mode")));
        });
    }

    private static JButton findButton(Container root, String label) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && label.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                JButton match = findButton(child, label);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    private static JLabel findLabel(Container root, String value) {
        for (Component component : root.getComponents()) {
            if (component instanceof JLabel label && value.equals(label.getText())) {
                return label;
            }
            if (component instanceof Container child) {
                JLabel match = findLabel(child, value);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
}
