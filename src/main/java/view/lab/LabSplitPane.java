package view.lab;

import view.GameTheme;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** User-resizable Lab workspace with continuous dragging and a minimal themed divider. */
final class LabSplitPane extends JSplitPane {
    static final int DIVIDER_SIZE = 10;

    LabSplitPane(JComponent left, JComponent right) {
        super(JSplitPane.HORIZONTAL_SPLIT, left, right);
        setUI(new ThemedSplitPaneUI());
        setContinuousLayout(true);
        setResizeWeight(0.42);
        setDividerLocation(0.42);
        setDividerSize(DIVIDER_SIZE);
        setOneTouchExpandable(false);
        setBorder(null);
        setOpaque(false);
    }

    private static final class ThemedSplitPaneUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new ThemedDivider(this);
        }
    }

    private static final class ThemedDivider extends BasicSplitPaneDivider {
        private boolean hovered;

        private ThemedDivider(BasicSplitPaneUI ui) {
            super(ui);
            setBorder(null);
            setBackground(new Color(0, 0, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int handleWidth = hovered ? 4 : 3;
            int handleHeight = Math.min(92, Math.max(48, getHeight() / 5));
            int x = (getWidth() - handleWidth) / 2;
            int y = (getHeight() - handleHeight) / 2;
            graphics2D.setColor(hovered ? GameTheme.GOLD : GameTheme.GOLD_SOFT);
            graphics2D.fillRoundRect(x, y, handleWidth, handleHeight,
                    handleWidth, handleWidth);
            if (hovered) {
                graphics2D.setColor(new Color(255, 230, 170, 90));
                graphics2D.setStroke(new BasicStroke(1f));
                graphics2D.drawRoundRect(x - 2, y - 2,
                        handleWidth + 3, handleHeight + 3, 5, 5);
            }
            graphics2D.dispose();
        }
    }
}
