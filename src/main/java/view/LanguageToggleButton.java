package view;

import util.Messages;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Locale;

import static util.Messages.text;

/** Owns the complete locale-toggle event path so it can be tested without opening a frame. */
final class LanguageToggleButton extends JButton {
    private final Runnable localeChanged;

    LanguageToggleButton(Runnable localeChanged) {
        this.localeChanged = localeChanged;
        setFont(GameTheme.strongFont(13));
        setForeground(GameTheme.TEXT);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        addActionListener(event -> toggleLocale());
        refreshLanguage();
    }

    void refreshLanguage() {
        boolean english = Locale.ENGLISH.equals(Messages.locale());
        setText(english ? "中文" : "EN");
        ViewUtil.configureButtonAccessibility(this,
                text("language.switch"), text("language.switch.tooltip"));
    }

    private void toggleLocale() {
        Locale next = Locale.ENGLISH.equals(Messages.locale())
                ? Locale.SIMPLIFIED_CHINESE
                : Locale.ENGLISH;
        Messages.useLocale(next);
        refreshLanguage();
        localeChanged.run();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(getModel().isPressed()
                ? GameTheme.LACQUER.darker()
                : getModel().isRollover() ? GameTheme.LACQUER_HOVER : GameTheme.SURFACE_RAISED);
        graphics2D.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
        graphics2D.setColor(GameTheme.GOLD_SOFT);
        graphics2D.setStroke(new BasicStroke(1.2f));
        graphics2D.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }
}
