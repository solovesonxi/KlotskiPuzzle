package view;

import model.Difficulty;
import model.MapModel;
import util.AppResources;
import util.BackgroundMusicPlayer;
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
    private final BackgroundMusicPlayer musicPlayer;

    public MainFrame(int width, int height) {
        super("KlotskiPuzzle · 华容道");
        this.setSize(width, height);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null); // 窗口居中显示
        addWindowListener(this);
        // 初始化登录面板
        loginPanel = new LoginPanel(this, this.getWidth(), this.getHeight());
        container.add(loginPanel, "login");
        this.add(container); // 添加容器到窗口

        // 音乐播放按钮
        soundBtn = ViewUtil.createMusicButton("resources/original/image/icons/play.png", new Point(width - 135, 10));
        soundBtn.addActionListener(event -> toggleBGM()); // 切换背景音乐
        lastBtn = ViewUtil.createMusicButton("resources/original/image/icons/previous.png", new Point(width - 197, 10));
        lastBtn.addActionListener(event -> playTrack(false)); // 播放上一首音乐
        ViewUtil.addButtonMouseListener(lastBtn, "resources/original/image/icons/previous.png");
        nextBtn = ViewUtil.createMusicButton("resources/original/image/icons/next.png", new Point(width - 75, 10));
        nextBtn.addActionListener(event -> playTrack(true)); // 播放下一首音乐
        ViewUtil.addButtonMouseListener(nextBtn, "resources/original/image/icons/next.png");

        musicPlayer = new BackgroundMusicPlayer(
                loadAudioFiles(),
                playing -> SwingUtilities.invokeLater(() -> soundBtn.setIcon(AppResources.icon(
                        playing
                                ? "resources/original/image/icons/pause.png"
                                : "resources/original/image/icons/play.png"))),
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
                JOptionPane.showMessageDialog(this, AppResources.get("mainframe.closing.animating"));
                return;
            }
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    AppResources.get("mainframe.closing.confirm.save"),
                    AppResources.get("mainframe.closing.confirm.exit"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
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
        loginPanel.contentPanel.add(lastBtn);
        loginPanel.contentPanel.add(nextBtn);
        loginPanel.contentPanel.add(soundBtn);
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
        controlPanel = new ControlPanel(this, this.getWidth(), this.getHeight(), lastBtn, nextBtn, soundBtn, mapModel, user, difficulty);
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
                System.out.println(AppResources.get("dialog.loadaudio") + path);
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

    private void exitApplication() {
        musicPlayer.close();
        dispose();
        System.exit(0);
    }
}
