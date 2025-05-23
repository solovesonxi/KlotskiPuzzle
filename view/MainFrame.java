package view;

import model.MapModel;
import view.game.ControlPanel;
import view.game.GamePanel;
import view.login.CustomDifficultyDialog;
import view.login.LoginPanel;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 游戏的主窗口，负责显示登录界面和游戏控制面板
public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final LoginPanel loginPanel;
    private final JButton lastBtn, nextBtn, soundBtn; // 共享的播放控件
    private final List<String> audioFilePaths = new ArrayList<>();
    private Clip bgmClip;
    private volatile boolean isPausedByUser = false;
    private int currentTrackIndex = 1; // 当前播放的音轨索引

    public MainFrame(int width, int height) {
        super("三国华容道");
        this.setSize(width, height);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        loadAudioFiles();
        playBGM(audioFilePaths.get(currentTrackIndex));

        // 初始化登录面板
        loginPanel = new LoginPanel(this, this.getWidth(), this.getHeight());
        container.add(loginPanel, "login");
        this.add(container);

        // 音乐播放按钮
        soundBtn = ViewUtil.createMusicButton("resources/image/play.png", new Point(width/2-25 , height-100));
        soundBtn.addActionListener(_ -> toggleBGM());
        lastBtn = ViewUtil.createMusicButton("resources/image/last.png", new Point(width/2 - 87, height-100));
        lastBtn.addActionListener(_ -> playTrack(false));
        ViewUtil.addButtonMouseListener(lastBtn, "resources/image/last.png");
        nextBtn = ViewUtil.createMusicButton("resources/image/next.png", new Point(width/2 +35, height-100));
        nextBtn.addActionListener(_ -> playTrack(true));
        ViewUtil.addButtonMouseListener(nextBtn, "resources/image/next.png");

        showLogin();
        this.setVisible(true);
    }

    public void showLogin() {
        loginPanel.contentPanel.add(lastBtn);
        loginPanel.contentPanel.add(nextBtn);
        loginPanel.contentPanel.add(soundBtn);
        cardLayout.show(container, "login");
    }

    public void showControl(String user) {
        CustomDifficultyDialog dialog = new CustomDifficultyDialog(this);
        dialog.setVisible(true);
        MapModel mapModel= new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 0, 0, 3}, {1, 0, 0, 1}});
        if ("运筹帷幄".equals(dialog.getSelectedDifficulty())) {
            mapModel= new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 1, 0, 3}, {1, 0, 0, 1}});
        } else if ("决胜千里".equals(dialog.getSelectedDifficulty())) {
            mapModel= new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 1, 1, 3}, {1, 0, 0, 1}});
        }
        ControlPanel controlPanel = new ControlPanel(this, this.getWidth(), this.getHeight(), lastBtn, nextBtn, soundBtn, mapModel, user, dialog.getSelectedDifficulty());
        container.add(controlPanel, "control");
        cardLayout.show(container, "control");
    }


    private void loadAudioFiles() {
        File soundDirectory = new File("resources/audio/music");
        if (soundDirectory.isDirectory()) {
            File[] files = soundDirectory.listFiles((_, name) -> name.toLowerCase().endsWith(".wav")); // 只能读取wav文件
            if (files != null) {
                for (File file : files) {
                    audioFilePaths.add(file.getAbsolutePath());
                }
            }
        }
    }

    public void playBGM(String nextAudioFilePath) {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
        }
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(nextAudioFilePath));
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioInputStream);
                FloatControl volumeControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(-10.0f); // 设置音量控制
                bgmClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        if (!isPausedByUser) {  // 只有非用户暂停时才自动播放
                            playTrack(true);
                        }
                        isPausedByUser = false;
                    }
                });
                bgmClip.start();
            } catch (Exception e) {
                System.out.println("播放错误: " + e.getMessage());
            }
        }).start();
    }

    public void toggleBGM() {
        System.out.println("切换BGM播放状态");
        if (bgmClip != null) {
            isPausedByUser = bgmClip.isRunning();
            if (isPausedByUser) {
                bgmClip.stop();
                soundBtn.setIcon(new ImageIcon("resources/image/stop.png"));
            } else {
                bgmClip.start();
                soundBtn.setIcon(new ImageIcon("resources/image/play.png"));
            }
        }
    }

    // 播放上一首或下一首音轨
    public void playTrack(boolean isNext) {
        System.out.println("Play track: " + (isNext ? "next" : "last"));
        if (audioFilePaths.isEmpty()) return;
        isPausedByUser = true;
        soundBtn.setIcon(new ImageIcon("resources/image/play.png"));
        currentTrackIndex = (currentTrackIndex + (isNext ? 1 : (audioFilePaths.size() - 1))) % audioFilePaths.size();
        playBGM(audioFilePaths.get(currentTrackIndex));
    }
}