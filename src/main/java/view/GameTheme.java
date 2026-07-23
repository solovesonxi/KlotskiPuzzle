package view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.text.JTextComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.RenderingHints;

/** Shared visual language for the modern Eastern-strategy presentation. */
public final class GameTheme {
    public static final Color INK = new Color(24, 23, 27);
    public static final Color SURFACE = new Color(43, 35, 34);
    public static final Color SURFACE_RAISED = new Color(57, 44, 39);
    public static final Color LACQUER = new Color(112, 53, 39);
    public static final Color LACQUER_HOVER = new Color(139, 68, 46);
    public static final Color GOLD = new Color(214, 177, 105);
    public static final Color GOLD_SOFT = new Color(214, 177, 105, 120);
    public static final Color TEXT = new Color(250, 237, 211);
    public static final Color TEXT_MUTED = new Color(202, 184, 151);
    public static final Color PAPER = new Color(239, 218, 174);
    public static final Color PAPER_TEXT = new Color(66, 43, 31);

    private static final String UI_FONT = "Microsoft YaHei UI";
    private static final String DISPLAY_FONT = "STKaiti";

    private GameTheme() {
    }

    public static Font bodyFont(int size) {
        return new Font(UI_FONT, Font.PLAIN, size);
    }

    public static Font strongFont(int size) {
        return new Font(UI_FONT, Font.BOLD, size);
    }

    public static Font displayFont(int size) {
        return new Font(DISPLAY_FONT, Font.BOLD, size);
    }

    public static JButton createButton(String text) {
        return new ThemeButton(text);
    }

    public static void styleTextField(JTextComponent field) {
        field.setFont(bodyFont(16));
        field.setForeground(PAPER_TEXT);
        field.setBackground(new Color(249, 238, 214));
        field.setCaretColor(PAPER_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_SOFT, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static void setGoldFocusBorder(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
    }

    public static <T> void styleComboBox(JComboBox<T> combo,
                                         java.util.function.Function<T, String> formatter) {
        combo.setFont(bodyFont(15));
        combo.setForeground(PAPER_TEXT);
        combo.setBackground(PAPER);
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_SOFT, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 4)));
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new ArrowButton(false);
                arrow.setPreferredSize(new Dimension(34, 34));
                return arrow;
            }
        });
        ListCellRenderer<? super T> renderer = (list, value, index, selected, focused) -> {
            javax.swing.JLabel label = new javax.swing.JLabel(
                    value == null ? "" : formatter.apply(value));
            label.setOpaque(true);
            label.setFont(bodyFont(15));
            label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            label.setBackground(selected ? LACQUER : PAPER);
            label.setForeground(selected ? TEXT : PAPER_TEXT);
            return label;
        };
        combo.setRenderer(renderer);
    }

    public static void styleSpinner(JSpinner spinner) {
        spinner.setUI(new BasicSpinnerUI() {
            @Override
            protected Component createNextButton() {
                JButton button = new ArrowButton(true);
                installNextButtonListeners(button);
                return button;
            }

            @Override
            protected Component createPreviousButton() {
                JButton button = new ArrowButton(false);
                installPreviousButtonListeners(button);
                return button;
            }
        });
        spinner.setFont(bodyFont(15));
        spinner.setBackground(PAPER);
        spinner.setBorder(BorderFactory.createLineBorder(GOLD_SOFT, 1));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JTextField field = editor.getTextField();
            field.setFont(bodyFont(15));
            field.setForeground(PAPER_TEXT);
            field.setBackground(PAPER);
            field.setCaretColor(PAPER_TEXT);
            field.setDisabledTextColor(new Color(133, 110, 77));
            field.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }
        styleSpinnerChildren(spinner);
    }

    private static void styleSpinnerChildren(java.awt.Container owner) {
        for (Component component : owner.getComponents()) {
            if (component instanceof JButton button) {
                button.setBackground(new Color(226, 202, 154));
                button.setForeground(PAPER_TEXT);
                button.setBorder(BorderFactory.createLineBorder(GOLD_SOFT, 1));
                button.setFocusPainted(false);
            } else if (component instanceof java.awt.Container child) {
                styleSpinnerChildren(child);
            }
        }
    }

    private static final class ThemeButton extends JButton {
        private ThemeButton(String text) {
            super(text);
            setFont(strongFont(16));
            setForeground(TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isPressed()
                    ? LACQUER.darker()
                    : getModel().isRollover() ? LACQUER_HOVER : LACQUER;
            if (Boolean.TRUE.equals(getClientProperty("selected"))) {
                fill = LACQUER_HOVER;
            }
            if (!isEnabled()) {
                fill = new Color(72, 64, 61);
            }
            graphics2D.setColor(fill);
            graphics2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
            graphics2D.setColor(isEnabled() ? GOLD_SOFT : new Color(140, 130, 118, 80));
            graphics2D.setStroke(new BasicStroke(
                    Boolean.TRUE.equals(getClientProperty("selected")) ? 2f : 1.2f));
            graphics2D.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ArrowButton extends JButton {
        private final boolean up;

        private ArrowButton(boolean up) {
            this.up = up;
            setBackground(new Color(226, 202, 154));
            setBorder(BorderFactory.createLineBorder(GOLD_SOFT, 1));
            setFocusPainted(false);
            setPreferredSize(new Dimension(28, 18));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(isEnabled() ? new Color(226, 202, 154)
                    : new Color(210, 193, 159));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int[] x = {centerX - 4, centerX + 4, centerX};
            int[] y = up
                    ? new int[]{centerY + 2, centerY + 2, centerY - 3}
                    : new int[]{centerY - 2, centerY - 2, centerY + 3};
            graphics2D.setColor(PAPER_TEXT);
            graphics2D.fillPolygon(x, y, 3);
            graphics2D.dispose();
        }
    }
}
