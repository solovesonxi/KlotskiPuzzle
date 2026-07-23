package view;

import model.Difficulty;
import model.MapModel;
import util.AppResources;
import util.BackgroundMusicPlayer;
import view.game.ControlPanel;
import view.game.CustomDifficultyDialog;
import view.lab.LabPanel;
import view.start.StartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static util.Messages.text;

// 游戏的主窗口，负责显示开始页、游戏模式和算法实验室
public class MainFrame extends JFrame implements WindowListener {
    private static final List<String> MUSIC_RESOURCES = List.of(
            "resources/original/audio/music/dawn-path.wav",
            "resources/original/audio/music/woodland-steps.wav",
            "resources/original/audio/music/quiet-strategy.wav",
            "resources/original/audio/music/open-gate.wav"
    );
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final StartPanel startPanel;
    private final LabPanel labPanel;
    private ControlPanel controlPanel; // 控制面板
    private final JButton lastBtn, nextBtn, soundBtn; // 共享的播放控件
    private final JPanel musicControls = new ToolbarPanel();
    private final LanguageToggleButton languageButton;
    private final BackgroundMusicPlayer musicPlayer;
    private volatile boolean musicPlaying;

    public MainFrame(int width, int height) {
        super(text("app.title"));
        this.setSize(width, height);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null); // 窗口居中显示
        addWindowListener(this);
        startPanel = new StartPanel(() -> showControl(null), this::showLab,
                this.getWidth(), this.getHeight());
        languageButton = new LanguageToggleButton(this::languageDidChange);
        container.add(startPanel, "start");
        this.add(container); // 添加容器到窗口

        // 音乐播放按钮
        soundBtn = ViewUtil.createMusicButton(
                "resources/original/image/icons/play.png", new Point(width - 135, 10),
                text("music.play"), text("music.play"));
        soundBtn.addActionListener(event -> toggleBGM()); // 切换背景音乐
        lastBtn = ViewUtil.createMusicButton(
                "resources/original/image/icons/previous.png", new Point(width - 197, 10),
                text("music.previous"), text("music.previous.tooltip"));
        lastBtn.addActionListener(event -> playTrack(false)); // 播放上一首音乐
        ViewUtil.addButtonMouseListener(lastBtn, "resources/original/image/icons/previous.png");
        nextBtn = ViewUtil.createMusicButton(
                "resources/original/image/icons/next.png", new Point(width - 75, 10),
                text("music.next"), text("music.next.tooltip"));
        nextBtn.addActionListener(event -> playTrack(true)); // 播放下一首音乐
        ViewUtil.addButtonMouseListener(nextBtn, "resources/original/image/icons/next.png");
        configureMusicControls();

        labPanel = new LabPanel(this::showStart, musicControls);
        container.add(labPanel, "lab");

        musicPlayer = new BackgroundMusicPlayer(
                loadAudioFiles(),
                playing -> SwingUtilities.invokeLater(() -> updateSoundButton(playing)),
                message -> System.err.println(message));
        musicPlayer.start();
        showStart();
        this.setVisible(true); // 显示窗口
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    // 窗口关闭事件处理
    @Override
    public void windowClosing(WindowEvent e) {
        if (controlPanel != null && controlPanel.isVisible() && controlPanel.controller != null && controlPanel.controller.user != null) {
            if (controlPanel.controller.isAnimating()) {
                JOptionPane.showMessageDialog(this, text("common.animation.wait"));
                return;
            }
            int choice = JOptionPane.showConfirmDialog(this, text("game.exit.save.prompt"),
                    text("game.exit.title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (JOptionPane.YES_OPTION == choice) {
                controlPanel.controller.saveGame();
                exitApplication();
            } else if (JOptionPane.NO_OPTION == choice) {
                exitApplication();
            }
        } else {
            exitApplication();
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
        musicPlayer.close();
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    public void showStart() {
        if (controlPanel != null) {
            controlPanel.disposePanel();
        }
        startPanel.attachToolbar(musicControls);
        cardLayout.show(container, "start");
    }

    // 显示控制面板
    public void showControl(String user) {
        if (controlPanel != null) {
            controlPanel.disposePanel();
            container.remove(controlPanel);
        }
        CustomDifficultyDialog dialog = new CustomDifficultyDialog(this);
        dialog.setVisible(true); // 打开难度选择对话框
        Difficulty difficulty = dialog.getSelectedDifficulty();
        MapModel mapModel = new MapModel(difficulty.initialBoard());
        controlPanel = new ControlPanel(this, this.getWidth(), this.getHeight(), musicControls,
                mapModel, user, difficulty);
        container.add(controlPanel, "control"); // 添加控制面板
        cardLayout.show(container, "control"); // 显示控制面板
        container.revalidate();
        container.repaint();
    }

    /** Opens the experiment workspace without creating a player or timed game session. */
    public void showLab() {
        if (controlPanel != null) {
            controlPanel.disposePanel();
        }
        labPanel.attachToolbar(musicControls);
        cardLayout.show(container, "lab");
        labPanel.onShown();
    }

    // 加载音频文件
    private List<URL> loadAudioFiles() {
        List<URL> audioResources = new ArrayList<>();
        for (String path : MUSIC_RESOURCES) {
            try {
                audioResources.add(AppResources.url(path));
            } catch (IllegalArgumentException exception) {
                System.out.println(text("music.missing", path));
            }
        }
        return audioResources;
    }

    // 切换背景音乐播放状态
    public void toggleBGM() {
        musicPlayer.toggle();
    }

    // 播放上一首或下一首音轨
    public void playTrack(boolean isNext) {
        musicPlayer.skip(isNext);
    }

    private void updateSoundButton(boolean playing) {
        musicPlaying = playing;
        String actionText = playing ? text("music.pause") : text("music.play");
        soundBtn.setIcon(AppResources.icon(playing
                ? "resources/original/image/icons/pause.png"
                : "resources/original/image/icons/play.png"));
        ViewUtil.configureButtonAccessibility(soundBtn, actionText, actionText);
    }

    private void configureMusicControls() {
        musicControls.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 4));
        musicControls.setOpaque(false);
        musicControls.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        for (JButton button : List.of(lastBtn, soundBtn, nextBtn)) {
            button.setPreferredSize(new Dimension(42, 42));
        }
        musicControls.add(lastBtn);
        musicControls.add(soundBtn);
        musicControls.add(nextBtn);
        languageButton.setPreferredSize(new Dimension(76, 36));
        musicControls.add(languageButton);
        applyLanguage();
    }

    private void languageDidChange() {
        applyLanguage();
        startPanel.applyLanguage();
        labPanel.applyLanguage();
        if (controlPanel != null && controlPanel.isVisible()) {
            controlPanel.applyLanguage();
        }
    }

    private void applyLanguage() {
        setTitle(text("app.title"));
        languageButton.refreshLanguage();
        ViewUtil.configureButtonAccessibility(lastBtn, text("music.previous"), text("music.previous.tooltip"));
        ViewUtil.configureButtonAccessibility(nextBtn, text("music.next"), text("music.next.tooltip"));
        updateSoundButton(musicPlaying);
        musicControls.revalidate();
        musicControls.repaint();
    }

    private void exitApplication() {
        musicPlayer.close();
        dispose();
        System.exit(0);
    }

    /** A compact in-game toolbar that belongs to the visual system instead of the OS menu bar. */
    private static final class ToolbarPanel extends JPanel {
        private ToolbarPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(GameTheme.INK.getRed(), GameTheme.INK.getGreen(),
                    GameTheme.INK.getBlue(), 210));
            graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            graphics2D.setColor(GameTheme.GOLD_SOFT);
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }
}
