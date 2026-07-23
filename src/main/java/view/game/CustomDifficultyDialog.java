package view.game;

import model.Difficulty;
import util.AppResources;
import view.GameTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import static util.Messages.text;

/** Compact difficulty picker styled as three readable strategy cards. */
public class CustomDifficultyDialog extends JDialog {
    private Difficulty selectedDifficulty = Difficulty.BEGINNER;

    public CustomDifficultyDialog(JFrame parent) {
        super(parent, text("difficulty.dialog.title"), true);
        setupUI();
        setSize(680, 300);
        setMinimumSize(new Dimension(620, 280));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void setupUI() {
        JPanel content = new ParchmentPanel();
        content.setLayout(new BorderLayout(18, 22));
        content.setBorder(BorderFactory.createEmptyBorder(28, 34, 34, 34));

        JLabel title = new JLabel(text("difficulty.dialog.prompt"), SwingConstants.CENTER);
        title.setFont(GameTheme.displayFont(28));
        title.setForeground(GameTheme.PAPER_TEXT);
        content.add(title, BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(1, 3, 16, 0));
        choices.setOpaque(false);
        for (Difficulty difficulty : Difficulty.values()) {
            JButton button = GameTheme.createButton(difficulty.displayName());
            button.setFont(GameTheme.strongFont(18));
            button.putClientProperty("difficulty", difficulty);
            button.addActionListener(this::handleSelection);
            choices.add(button);
        }
        content.add(choices, BorderLayout.CENTER);
        setContentPane(content);
    }

    private void handleSelection(ActionEvent event) {
        selectedDifficulty = (Difficulty) ((JButton) event.getSource())
                .getClientProperty("difficulty");
        dispose();
    }

    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }

    private static final class ParchmentPanel extends JPanel {
        private final Image parchment = AppResources.icon(
                "resources/original/image/parchment.png").getImage();

        private ParchmentPanel() {
            setOpaque(true);
            setBackground(GameTheme.PAPER);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(parchment, 0, 0, getWidth(), getHeight(), this);
            graphics2D.setColor(new Color(255, 248, 224, 35));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
            graphics2D.dispose();
        }
    }
}
