package view.lab;

import view.GameTheme;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Shared painted surface for Lab Mode modules. */
final class LabSurfacePanel extends JPanel {
    LabSurfacePanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(GameTheme.SURFACE);
        graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
        graphics2D.setColor(GameTheme.GOLD_SOFT);
        graphics2D.setStroke(new BasicStroke(1.2f));
        graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
