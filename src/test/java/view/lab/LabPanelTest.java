package view.lab;

import org.junit.jupiter.api.Test;
import util.Messages;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    private static void render(LabPanel panel) {
        panel.doLayout();
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
            image.flush();
        }
    }
}
