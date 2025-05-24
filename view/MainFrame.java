package view;

import model.MapModel;
import view.game.ControlPanel;
import view.login.CustomDifficultyDialog;
import view.login.LoginPanel;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 游戏的主窗口，负责显示登录界面和游戏控制面板
public class MainFrame extends JFrame implements WindowListener {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);
    private final LoginPanel loginPanel; // 登录面板
    private ControlPanel controlPanel; // 控制面板
    private final JButton lastBtn, nextBtn, soundBtn; // 共享的播放控件
    private final List<String> audioFilePaths = new ArrayList<>(); // 音频文件路径列表
    private Clip bgmClip; // 背景音乐剪辑
    private volatile boolean isPausedByUser = false;
    private int currentTrackIndex = 1;

    public MainFrame(int width, int height) {
        super("三国华容道"); // 设置窗口标题
        this.setSize(width, height);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null); // 窗口居中显示
        loadAudioFiles(); // 加载音频文件
        playBGM(audioFilePaths.get(currentTrackIndex)); // 播放背景音乐
        addWindowListener(this);
        // 初始化登录面板
        loginPanel = new LoginPanel(this, this.getWidth(), this.getHeight());
        container.add(loginPanel, "login");
        this.add(container); // 添加容器到窗口

        // 音乐播放按钮
        soundBtn = ViewUtil.createMusicButton("resources/image/play.png", new Point(width - 135, 10));
        soundBtn.addActionListener(_ -> toggleBGM()); // 切换背景音乐
        lastBtn = ViewUtil.createMusicButton("resources/image/last.png", new Point(width - 197, 10));
        lastBtn.addActionListener(_ -> playTrack(false)); // 播放上一首音乐
        ViewUtil.addButtonMouseListener(lastBtn, "resources/image/last.png");
        nextBtn = ViewUtil.createMusicButton("resources/image/next.png", new Point(width - 75, 10));
        nextBtn.addActionListener(_ -> playTrack(true)); // 播放下一首音乐
        ViewUtil.addButtonMouseListener(nextBtn, "resources/image/next.png");

        showLogin(); // 显示登录界面
        this.setVisible(true); // 显示窗口
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    // 窗口关闭事件处理
    @Override
    public void windowClosing(WindowEvent e) {
        if (controlPanel != null && controlPanel.isVisible() && controlPanel.controller != null) {
            if (controlPanel.controller.user == null) { // 如果没有登录用户，直接关闭
                dispose();
                System.exit(0);
            } else { // 如果有登录用户，询问是否保存游戏
                int choice = JOptionPane.showConfirmDialog(this, "是否保存游戏？", "退出游戏", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (JOptionPane.YES_OPTION == choice) {
                    controlPanel.controller.saveGame();
                    System.exit(0);
                } else if (JOptionPane.NO_OPTION == choice) {
                    System.exit(0);
                }
            }
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}

    // 显示登录面板
    public void showLogin() {
        loginPanel.contentPanel.add(lastBtn);
        loginPanel.contentPanel.add(nextBtn);
        loginPanel.contentPanel.add(soundBtn);
        cardLayout.show(container, "login"); // 显示登录卡片
    }

    // 显示控制面板
    public void showControl(String user) {
        CustomDifficultyDialog dialog = new CustomDifficultyDialog(this);
        dialog.setVisible(true); // 打开难度选择对话框
        MapModel mapModel = new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 0, 0, 3}, {1, 0, 0, 1}});
        // 根据选择的难度设置地图模型
        if ("刮目相待".equals(dialog.getSelectedDifficulty())) {
            mapModel = new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 1, 0, 3}, {1, 0, 0, 1}});
        } else if ("运筹帷幄".equals(dialog.getSelectedDifficulty())) {
            mapModel = new MapModel(new int[][]{{3, 4, 4, 3}, {3, 4, 4, 3}, {3, 2, 2, 3}, {3, 1, 1, 3}, {1, 0, 0, 1}});
        }
        controlPanel = new ControlPanel(this, this.getWidth(), this.getHeight(), lastBtn, nextBtn, soundBtn, mapModel, user, dialog.getSelectedDifficulty());
        container.add(controlPanel, "control"); // 添加控制面板
        cardLayout.show(container, "control"); // 显示控制面板
    }

    // 加载音频文件
    private void loadAudioFiles() {
        File soundDirectory = new File("resources/audio/music");
        if (soundDirectory.isDirectory()) {
            File[] files = soundDirectory.listFiles((_, name) -> name.toLowerCase().endsWith(".wav")); // 只能读取wav文件
            if (files != null) {
                for (File file : files) {
                    audioFilePaths.add(file.getAbsolutePath()); // 添加每个文件的绝对路径
                }
            }
        }
    }

    // 播放背景音乐
    public void playBGM(String nextAudioFilePath) {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop(); // 停止当前播放的音乐
        }
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(nextAudioFilePath));
                bgmClip = AudioSystem.getClip(); // 获取音频剪辑
                bgmClip.open(audioInputStream); // 打开音频流
                FloatControl volumeControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(-10.0f); // 设置音量控制
                bgmClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        if (!isPausedByUser) {  // 只有非用户暂停时才自动播放
                            playTrack(true); // 自动播放下一首
                        }
                        isPausedByUser = false; // 重置暂停状态
                    }
                });
                bgmClip.start(); // 开始播放音乐
            } catch (Exception e) {
                System.out.println("播放错误: " + e.getMessage()); // 捕捉播放错误
            }
        }).start();
    }

    // 切换背景音乐播放状态
    public void toggleBGM() {
        System.out.println("切换BGM播放状态");
        if (bgmClip != null) {
            isPausedByUser = bgmClip.isRunning(); // 判断当前是否在播放
            if (isPausedByUser) {
                bgmClip.stop(); // 停止音乐
                soundBtn.setIcon(new ImageIcon("resources/image/stop.png")); // 设置图标为停止
            } else {
                bgmClip.start(); // 开始播放音乐
                soundBtn.setIcon(new ImageIcon("resources/image/play.png")); // 设置图标为播放
            }
        }
    }

    // 播放上一首或下一首音轨
    public void playTrack(boolean isNext) {
        System.out.println("Play track: " + (isNext ? "next" : "last"));
        if (audioFilePaths.isEmpty()) return; // 如果没有音轨则返回
        isPausedByUser = true; // 标记为用户暂停
        soundBtn.setIcon(new ImageIcon("resources/image/play.png")); // 设置图标为播放
        currentTrackIndex = (currentTrackIndex + (isNext ? 1 : (audioFilePaths.size() - 1))) % audioFilePaths.size(); // 更新当前音轨索引
        playBGM(audioFilePaths.get(currentTrackIndex)); // 播放新音轨
    }
}
