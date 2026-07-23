package view;

import model.Difficulty;
import model.MapModel;
import util.AppResources;
import util.BackgroundMusicPlayer;
import util.Messages;
import view.game.ControlPanel;
import view.login.CustomDifficultyDialog;
import view.login.LoginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static util.Messages.text;

// 游戏的主窗口，负责显示登录界面和游戏控制面板
public class MainFrame extends JFrame implements WindowListener {
    private static final List<String> MUSIC_RESOURCES = List.of(
            "resources/original/audio/music/dawn-path.wav",
            "resources/original/audio/music/woodland-steps.wav",
            "resources/original/audio/music/quiet-strategy.wav",
            "resources/original/audio/music/open-gate.wav"
    );
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final LoginPanel loginPanel; // 登录面板
    private ControlPanel controlPanel; // 控制面板
    private final JButton lastBtn, nextBtn, soundBtn; // 共享的播放控件
    private final JPanel musicControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
    private final JLabel musicLabel = new JLabel();
    private final JMenu languageMenu = new JMenu();
    private final JRadioButtonMenuItem englishItem = new JRadioButtonMenuItem();
    private final JRadioButtonMenuItem chineseItem = new JRadioButtonMenuItem();
    private final BackgroundMusicPlayer musicPlayer;
    private volatile boolean musicPlaying;

    public MainFrame(int width, int height) {
        super(text("app.title"));
        this.setSize(width, height);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null); // 窗口居中显示
        addWindowListener(this);
        // 初始化登录面板
        loginPanel = new LoginPanel(this, this.getWidth(), this.getHeight());
        container.add(loginPanel, "login");
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
        configureLanguageMenu();

        musicPlayer = new BackgroundMusicPlayer(
                loadAudioFiles(),
                playing -> SwingUtilities.invokeLater(() -> updateSoundButton(playing)),
                message -> System.err.println(message));
        musicPlayer.start();
        showLogin(); // 显示登录界面
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

    // 显示登录面板
    public void showLogin() {
        if (controlPanel != null) {
            controlPanel.disposePanel();
        }
        musicControls.setBounds(getWidth() - 300, 8, 280, 58);
        loginPanel.contentPanel.add(musicControls);
        cardLayout.show(container, "login"); // 显示登录卡片
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
        musicControls.setOpaque(true);
        musicControls.setBackground(new Color(42, 31, 28));
        musicControls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 170, 109), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        musicLabel.setForeground(new Color(255, 223, 186));
        musicLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        for (JButton button : List.of(lastBtn, soundBtn, nextBtn)) {
            button.setPreferredSize(new Dimension(44, 44));
        }
        musicControls.add(musicLabel);
        musicControls.add(lastBtn);
        musicControls.add(soundBtn);
        musicControls.add(nextBtn);
        applyLanguage();
    }

    private void configureLanguageMenu() {
        ButtonGroup languages = new ButtonGroup();
        languages.add(englishItem);
        languages.add(chineseItem);
        languageMenu.add(englishItem);
        languageMenu.add(chineseItem);
        englishItem.addActionListener(event -> changeLanguage(Locale.ENGLISH));
        chineseItem.addActionListener(event -> changeLanguage(Locale.SIMPLIFIED_CHINESE));
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(languageMenu);
        setJMenuBar(menuBar);
        applyLanguage();
    }

    private void changeLanguage(Locale locale) {
        if (Messages.locale().equals(locale)) {
            return;
        }
        Messages.useLocale(locale);
        applyLanguage();
        loginPanel.applyLanguage();
        if (controlPanel != null && controlPanel.isVisible()) {
            controlPanel.applyLanguage();
        }
    }

    private void applyLanguage() {
        setTitle(text("app.title"));
        languageMenu.setText(text("settings.language"));
        englishItem.setText(text("language.english"));
        chineseItem.setText(text("language.chinese"));
        englishItem.setSelected(Locale.ENGLISH.equals(Messages.locale()));
        chineseItem.setSelected(Locale.SIMPLIFIED_CHINESE.equals(Messages.locale()));
        musicLabel.setText(text("music.controls"));
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
}
