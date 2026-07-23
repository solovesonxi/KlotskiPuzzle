package view.start;

import org.junit.jupiter.api.Test;

import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.concurrent.atomic.AtomicInteger;

class StartPanelTest {
    @Test
    void startScreenDoesNotAskForPasswords() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            StartPanel panel = new StartPanel(() -> { }, () -> { }, 1280, 720);

            assertFalse(containsComponent(panel, JPasswordField.class));
        });
    }

    @Test
    void exposesPlayAndLabAsTheTwoPrimaryActions() throws Exception {
        AtomicInteger playActions = new AtomicInteger();
        AtomicInteger labActions = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            StartPanel panel = new StartPanel(
                    playActions::incrementAndGet, labActions::incrementAndGet, 1280, 720);
            JButton play = (JButton) findNamedComponent(panel, "start.play");
            JButton lab = (JButton) findNamedComponent(panel, "start.lab");

            assertNotNull(play);
            assertNotNull(lab);
            play.doClick();
            lab.doClick();
        });

        assertEquals(1, playActions.get());
        assertEquals(1, labActions.get());
    }

    private static boolean containsComponent(Container owner, Class<?> type) {
        for (Component component : owner.getComponents()) {
            if (type.isInstance(component)) {
                return true;
            }
            if (component instanceof Container child && containsComponent(child, type)) {
                return true;
            }
        }
        return false;
    }

    private static Component findNamedComponent(Container owner, String name) {
        for (Component component : owner.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container child) {
                Component found = findNamedComponent(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
