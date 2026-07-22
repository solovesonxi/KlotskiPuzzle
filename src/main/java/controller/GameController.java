package controller;

import data.AppData;
import data.LeaderboardRepository;
import model.BoardRules;
import model.Direction;
import model.Difficulty;
import model.MapModel;
import util.AppResources;
import view.game.BoxComponent;
import view.game.GamePanel;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/*它是将 GamePanel（view） 和 MapMatrix（model） 组合在一个游戏中的桥梁。您可以在此类中设计有关游戏逻辑的多种方法。*/
public class GameController {
    private final GamePanel view;
    public final MapModel model;
    private final ArrayList<String> history = new ArrayList<>();
    private boolean animating;
    private boolean disposed;
    private Timer movementTimer;
    public final String user;
    private final Difficulty difficulty;
    private final int[][] initialMatrix;

    public GameController(GamePanel view, MapModel model, String user, Difficulty difficulty) {
        this.view = view;
        this.model = model;
        this.user = user;
        this.difficulty = difficulty;
        this.initialMatrix = deepCopy(model.getMatrix());
        view.setController(this);
        history.add(getHistory(null));
    }

    // 深拷贝一个新的二维数组
    public static int[][] deepCopy(int[][] original) {
        if (original == null) {
            return null;
        }
        return BoardRules.copy(original);
    }

    // 重置游戏状态
    public void restartGame() {
        if (!ensureIdle()) {
            return;
        }
        System.out.println("重新游戏");
        model.updateMatrix(deepCopy(initialMatrix));
        history.clear();
        history.add(getHistory(null));
        view.initialGame(0, 180);
    }

    // 获取当前历史记录的字符串表示
    private String getHistory(int[][] matrix) {
        if (matrix == null) {
            matrix = model.getMatrix();
        }
        StringBuilder sb = new StringBuilder();
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                sb.append(anInt).append(",");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    // 从历史记录字符串中获取二维数组
    private int[][] getMatrixFromHistory(String history) {
        if (history == null || history.isEmpty()) {
            return null;
        }
        String[] rows = history.split(";");
        int[][] matrix = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            matrix[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                try {
                    matrix[i][j] = Integer.parseInt(cols[j].trim());
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
        }
        try {
            BoardRules.validateGameBoard(matrix);
            return matrix;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    // 判断移动操作是否合法并执行
    public boolean doMove(int row, int col, Direction direction) {
        if (disposed || animating) {
            return false;
        }
        int nextRow = row + direction.getRow();
        int nextCol = col + direction.getCol();
        int[][] moved = BoardRules.applyMove(model.getMatrix(), row, col, direction);
        if (moved != null) {
            animating = true;
            model.updateMatrix(moved);
            continueMove(nextRow, nextCol);
            return true;
        }
        return false;
    }

    // 连续移动动画
    private void continueMove(int nextRow, int nextCol) {
        BoxComponent box = view.getSelectedBox();
        int startRow = box.getRow();
        int startCol = box.getCol();
        int deltaX = (nextCol - startCol) * view.getGRID_SIZE();
        int deltaY = (nextRow - startRow) * view.getGRID_SIZE();
        int totalFrames = 10;
        int[] frame = {0};
        playSoundEffect("resources/audio/sound_effect/move.wav");
        movementTimer = new Timer(1, e -> { // 使用Timer实现动画效果
            if (frame[0] < totalFrames) {
                double progress = (double) frame[0] / totalFrames;
                progress = 1 - (1 - progress) * (1 - progress);
                box.setLocation((int) (startCol * view.getGRID_SIZE() + deltaX * progress + 2), (int) (startRow * view.getGRID_SIZE() + deltaY * progress + 2));
                box.repaint();
                frame[0]++;
            } else {
                ((Timer) e.getSource()).stop();
                box.setRow(nextRow);
                box.setCol(nextCol);
                box.setLocation(nextCol * view.getGRID_SIZE() + 2, nextRow * view.getGRID_SIZE() + 2);
                box.repaint();
                history.add(getHistory(null));
                animating = false;
                movementTimer = null;
                endGame(true);
            }
        });
        movementTimer.start();
    }

    // 播放音效
    public void playSoundEffect(String filePath) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(AppResources.url(filePath))) {
            Clip soundEffectClip = AudioSystem.getClip();
            soundEffectClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    soundEffectClip.close();
                }
            });
            soundEffectClip.open(audioInputStream);
            soundEffectClip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, "播放音效时发生错误: " + e.getMessage());
        }
    }

    // 检查游戏是否结束
    public void endGame(boolean check) {
        if (!check && animating) {
            if (movementTimer != null) {
                movementTimer.stop();
                movementTimer = null;
            }
            animating = false;
            view.initialGame(view.steps, -1);
        }
        if (!check || BoardRules.isSolved(model.getMatrix())) {
            view.countdownTimer.stop();
            if (check) {
                playSoundEffect("resources/audio/sound_effect/victory.wav");
                if (user != null && difficulty.isRanked()) saveLeaderBoard();
            } else { // 倒计时结束不需要检查，直接游戏失败
                playSoundEffect("resources/audio/sound_effect/defeat.wav");
            }
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(view, "是否重新开始游戏？", check ? ("恭喜你过关了！共用了" + view.steps + "步！") : ("倒计时为0，游戏失败！"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                restartGame();
            } else {
                System.exit(0);
            }
        }
    }

    // 保存战绩条目
    private void saveLeaderBoard() {
        try {
            int remainingTime = Integer.parseInt(view.countdownLabel.getText().split("：")[1].split("息")[0]);
            new LeaderboardRepository().add(
                    new LeaderboardRepository.ScoreEntry(user, view.steps, remainingTime));
        } catch (IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(view, "保存战绩时发生错误: " + e.getMessage());
        }
    }

    public void undo() {
        if (!ensureIdle()) {
            return;
        }
        if (history.size() > 1) {
            history.removeLast();
            model.updateMatrix(getMatrixFromHistory(history.getLast()));
            view.initialGame(view.steps - 1, -1);
        } else {
            JOptionPane.showMessageDialog(view, "没有更多的撤销操作");
        }
    }

    public void loadGame() {
        if (!ensureIdle()) {
            return;
        }
        try {
            if (user == null || user.isEmpty()) {
                JOptionPane.showMessageDialog(view, "请先登录");
                return;
            }
            Path path = AppData.historyFile(user);
            if (!Files.exists(path)) {
                JOptionPane.showMessageDialog(view, user+"还没有保存过历史游戏数据");
                return;
            }
            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty() || lines.getFirst().isBlank()) {
                JOptionPane.showMessageDialog(view, user+"还没有保存过历史游戏数据");
                return;
            }
            int steps = 0;
            int countdown = 0;
            ArrayList<String> newHistory = new ArrayList<>();
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                String line = lines.get(lineIndex);
                String[] parts = line.split(" ");
                if (parts.length != 2) {
                    quarantineCorruptSave(path, "历史游戏数据格式错误，原文件已保留为损坏备份。");
                    return;
                }
                if (lineIndex == 0) {
                        steps = Integer.parseInt(parts[0]);
                        countdown = Integer.parseInt(parts[1]);
                } else {
                        int[][] matrix = getMatrixFromHistory(parts[0]);
                        int[][] newMatrix = decode(parts[1]);
                        if (matrix != null && Arrays.deepEquals(matrix, newMatrix)) {
                            newHistory.add(parts[0]);
                        } else if (newMatrix != null) {
                            newHistory.add(getHistory(newMatrix));
                            JOptionPane.showMessageDialog(view, "第" + newHistory.size() + "行数据错误，成功启用数据恢复。");
                        } else {
                            quarantineCorruptSave(path, "第" + (newHistory.size() + 1) + "行无法恢复，原文件已保留为损坏备份。");
                            return;
                        }
                }
            }
            if (!newHistory.isEmpty() && newHistory.size() == steps + 1) {
                model.updateMatrix(getMatrixFromHistory(newHistory.getLast()));
                history.clear();
                history.addAll(newHistory);
                view.initialGame(steps, countdown);
            } else {
                quarantineCorruptSave(path, "历史游戏数据缺失，原文件已保留为损坏备份。");
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "读取文件时发生错误: " + e.getMessage());
        }
    }

    private void quarantineCorruptSave(Path path, String message) throws IOException {
        Path corrupt = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis());
        Files.move(path, corrupt, StandardCopyOption.REPLACE_EXISTING);
        JOptionPane.showMessageDialog(view, message + "\n" + corrupt);
    }

    // 编码二维数组为字符串
    public static String encode(int[][] matrix) throws IOException {
        BoardRules.validateGameBoard(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(matrix.length);
        dos.writeInt(matrix[0].length);
        for (int[] row : matrix) {
            for (int num : row) {
                dos.writeInt(num);
            }
        } // 二进制+Base64
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // 解码恢复二维数组
    public static int[][] decode(String token) {
        try {
            byte[] data = Base64.getDecoder().decode(token);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int rows = dis.readInt();
            int cols = dis.readInt();
            if (rows != 5 || cols != 4) {
                return null;
            }
            int[][] matrix = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = dis.readInt();
                }
            }
            BoardRules.validateGameBoard(matrix);
            return matrix;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    // 保存游戏进度
    public void saveGame() {
        if (!ensureIdle()) {
            return;
        }
        try {
            Path file = AppData.historyFile(user);
            // 步数 + 倒计时 + 多行历史记录
            StringBuilder sb = new StringBuilder(view.steps + " " + view.countdownLabel.getText().split("：")[1].split("息")[0] + "\n");
            for (String hist : history) {
                sb.append(hist).append(" ").append(encode(getMatrixFromHistory(hist))).append("\n");
            }
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, sb.toString());
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicMoveUnsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "保存游戏时发生错误: " + e.getMessage());
        }
    }

    public boolean isAnimating() {
        return animating;
    }

    public void dispose() {
        disposed = true;
        if (movementTimer != null) {
            movementTimer.stop();
            movementTimer = null;
        }
        animating = false;
    }

    private boolean ensureIdle() {
        if (disposed) {
            return false;
        }
        if (animating) {
            JOptionPane.showMessageDialog(view, "请等待当前移动动画结束");
            return false;
        }
        return true;
    }
}
