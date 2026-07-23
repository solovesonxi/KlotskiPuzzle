package view.lab;

import org.junit.jupiter.api.Test;
import util.Messages;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LabPanelTest {
    @Test
    void rendersAtTheMinimumV2TargetSizeInBothLanguages() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LabPanel panel = new LabPanel(() -> {
            }, new JPanel());
            panel.setSize(1280, 720);

            assertDoesNotThrow(() -> render(panel));
            Messages.useLocale(Locale.SIMPLIFIED_CHINESE);
            panel.applyLanguage();
            assertDoesNotThrow(() -> render(panel));
            Messages.useLocale(Locale.ENGLISH);
        });
    }

    @Test
    void usesAContinuousBorderlessResizableWorkspace() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LabPanel panel = new LabPanel(() -> {
            }, new JPanel());
            JSplitPane splitPane = (JSplitPane) findComponent(panel, JSplitPane.class);

            assertNotNull(splitPane);
            assertTrue(splitPane.isContinuousLayout());
            assertNull(splitPane.getBorder());
            assertEquals(LabSplitPane.DIVIDER_SIZE, splitPane.getDividerSize());
        });
    }

    @Test
    void exposesSolutionReplayControls() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LabPanel panel = new LabPanel(() -> {
            }, new JPanel());

            assertNotNull(findNamedComponent(panel, "solutionReplay.next"));
            assertNotNull(findNamedComponent(panel, "solutionReplay.previous"));
        });
    }

    @Test
    void alignsExplanationTabsWithExperimentActions() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LabPanel panel = new LabPanel(() -> {
            }, new JPanel());
            panel.setSize(1280, 720);
            layoutTree(panel);

            String[] actions = {"lab.run", "lab.cancel", "experimentRecord.export"};
            String[] tabs = {"lab.tab.overview", "lab.tab.inspector", "lab.tab.replay"};
            for (int index = 0; index < actions.length; index++) {
                Rectangle actionBounds = absoluteBounds(panel, actions[index]);
                Rectangle tabBounds = absoluteBounds(panel, tabs[index]);
                assertEquals(actionBounds.x, tabBounds.x,
                        tabs[index] + " should share the action column start");
                assertEquals(actionBounds.width, tabBounds.width,
                        tabs[index] + " should share the action column width");
            }
        });
    }

    private static void render(LabPanel panel) {
        layoutTree(panel);
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
            image.flush();
        }
    }

    private static void layoutTree(Container owner) {
        owner.doLayout();
        for (Component component : owner.getComponents()) {
            if (component instanceof Container child) {
                layoutTree(child);
            }
        }
    }

    private static Rectangle absoluteBounds(LabPanel panel, String name) {
        Component component = findNamedComponent(panel, name);
        assertNotNull(component, () -> "Missing component " + name);
        return SwingUtilities.convertRectangle(component.getParent(), component.getBounds(), panel);
    }

    private static Component findComponent(Container owner, Class<?> type) {
        for (Component component : owner.getComponents()) {
            if (type.isInstance(component)) {
                return component;
            }
            if (component instanceof Container child) {
                Component found = findComponent(child, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
