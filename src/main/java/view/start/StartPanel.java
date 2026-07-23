package view.start;

import util.AppResources;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Objects;

import static util.Messages.text;

/** Password-free product entry point for Play Mode and Lab Mode. */
public final class StartPanel extends JPanel {
    private final Image backgroundImage;
    private final JPanel contentPanel = new JPanel(null);
    private final JPanel toolbarHost = new JPanel(new BorderLayout());
    private final JPanel startCard = new StartCardPanel();
    private final JLabel titleLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel subtitleLabel = new JLabel("", SwingConstants.CENTER);
    private final JButton playButton = GameTheme.createButton("");
    private final JButton labButton = GameTheme.createButton("");

    public StartPanel(Runnable playAction, Runnable labAction, int width, int height) {
        Objects.requireNonNull(playAction, "playAction");
        Objects.requireNonNull(labAction, "labAction");
        setLayout(null);
        setSize(width, height);
        setBackground(GameTheme.INK);
        backgroundImage = AppResources.icon(
                "resources/original/image/login-background.gif").getImage();

        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, width, height);
        add(contentPanel);

        startCard.setLayout(new GridBagLayout());
        contentPanel.add(startCard);
        toolbarHost.setOpaque(false);
        contentPanel.add(toolbarHost);

        titleLabel.setForeground(GameTheme.TEXT);
        titleLabel.setFont(GameTheme.displayFont(46));
        subtitleLabel.setForeground(GameTheme.TEXT_MUTED);
        subtitleLabel.setFont(GameTheme.bodyFont(16));
        playButton.setName("start.play");
        labButton.setName("start.lab");
        playButton.addActionListener(event -> playAction.run());
        labButton.addActionListener(event -> labAction.run());
        buildCard();
        applyLanguage();
    }

    public void attachToolbar(JPanel toolbar) {
        toolbarHost.removeAll();
        toolbarHost.add(Objects.requireNonNull(toolbar, "toolbar"), BorderLayout.CENTER);
        toolbarHost.revalidate();
        toolbarHost.repaint();
    }

    public void applyLanguage() {
        titleLabel.setText(text("start.title"));
        subtitleLabel.setText(text("start.subtitle"));
        playButton.setText(text("start.play"));
        labButton.setText(text("start.lab"));
        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        contentPanel.setBounds(0, 0, getWidth(), getHeight());
        int cardWidth = Math.min(560, Math.max(400, getWidth() - 96));
        int cardHeight = Math.min(390, Math.max(330, getHeight() - 180));
        startCard.setBounds((getWidth() - cardWidth) / 2,
                (getHeight() - cardHeight) / 2 + 18, cardWidth, cardHeight);
        toolbarHost.setBounds(Math.max(20, getWidth() - 330), 12, 310, 54);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }

    private void buildCard() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(12, 44, 8, 44);
        startCard.add(titleLabel, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 44, 30, 44);
        startCard.add(subtitleLabel, constraints);

        JPanel actions = new JPanel(new GridLayout(2, 1, 0, 14));
        actions.setOpaque(false);
        playButton.setPreferredSize(new Dimension(340, 52));
        labButton.setPreferredSize(new Dimension(340, 52));
        actions.add(playButton);
        actions.add(labButton);
        constraints.gridy++;
        constraints.insets = new Insets(12, 68, 26, 68);
        startCard.add(actions, constraints);
    }

    private static final class StartCardPanel extends JPanel {
        private StartCardPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(GameTheme.INK.getRed(), GameTheme.INK.getGreen(),
                    GameTheme.INK.getBlue(), 232));
            graphics2D.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 34, 34);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.setStroke(new BasicStroke(1.5f));
            graphics2D.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 34, 34);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }
}
