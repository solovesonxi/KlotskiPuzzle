package controller;

import model.Direction;
import model.MapModel;
import view.game.BoxComponent;
import view.game.GamePanel;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/*它是将 GamePanel（view） 和 MapMatrix（model） 组合在一个游戏中的桥梁。您可以在此类中设计有关游戏逻辑的多种方法。*/
public class GameController {
    private final GamePanel view;
    public final MapModel model;
    private final ArrayList<String> history = new ArrayList<>();
    private int count = 0;
    public final String user;
    private final String difficulty;
    private final int[][] initialMatrix;

    public GameController(GamePanel view, MapModel model, String user,String difficulty) {
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
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    // 重置游戏状态
    public void restartGame() {
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
        return matrix;
    }

    // 判断移动操作是否合法并执行
    public boolean doMove(int row, int col, Direction direction) {
        int nextRow = row + direction.getRow();
        int nextCol = col + direction.getCol();
        System.out.println("ID = " + model.getId(row, col) + " row = " + row + " col = " + col + " nextRow = " + nextRow + " nextCol = " + nextCol);
        boolean result;
        if (model.getId(row, col) == 1) {
            if (model.checkInSize(nextRow, nextCol)) {
                if (model.getId(nextRow, nextCol) == 0) {
                    model.set(row, col, 0);
                    model.set(nextRow, nextCol, 1);
                    continueMove(nextRow, nextCol);
                    return true;
                }
            }
        } else if (model.getId(row, col) == 2) {
            if (model.checkInSize(nextRow, nextCol) && model.checkInSize(nextRow, nextCol + 1)) {
                if (direction == Direction.UP || direction == Direction.DOWN) {
                    result = model.isEmpty(nextRow, nextCol) && model.isEmpty(nextRow, nextCol + 1);
                } else {
                    result = (direction == Direction.LEFT && model.isEmpty(nextRow, nextCol)) || (direction == Direction.RIGHT && model.isEmpty(nextRow, nextCol + 1));
                }
                if (result) {
                    model.set(row, col, 0);
                    model.set(row, col + 1, 0);
                    model.set(nextRow, nextCol, 2);
                    model.set(nextRow, nextCol + 1, 2);
                    continueMove(nextRow, nextCol);
                    return true;
                }
            }
        } else if (model.getId(row, col) == 3) {
            if (model.checkInSize(nextRow, nextCol) && model.checkInSize(nextRow + 1, nextCol)) {
                if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                    result = model.isEmpty(nextRow, nextCol) && model.isEmpty(nextRow + 1, nextCol);
                } else {
                    result = (direction == Direction.UP && model.isEmpty(nextRow, nextCol)) || (direction == Direction.DOWN && model.isEmpty(nextRow + 1, nextCol));
                }
                if (result) {
                    model.set(row, col, 0);
                    model.set(row + 1, col, 0);
                    model.set(nextRow, nextCol, 3);
                    model.set(nextRow + 1, nextCol, 3);
                    continueMove(nextRow, nextCol);
                    return true;
                }
            }
        } else if (model.getId(row, col) == 4) {
            if (model.checkInSize(nextRow, nextCol) && model.checkInSize(nextRow + 1, nextCol + 1)) {
                if (direction == Direction.UP) {
                    result = model.isEmpty(row - 1, col) && model.isEmpty(row - 1, col + 1);
                } else if (direction == Direction.DOWN) {
                    result = model.isEmpty(row + 2, col) && model.isEmpty(row + 2, col + 1);
                } else if (direction == Direction.LEFT) {
                    result = model.isEmpty(row, col - 1) && model.isEmpty(row + 1, col - 1);
                } else {
                    result = model.isEmpty(row, col + 2) && model.isEmpty(row + 1, col + 2);
                }
                if (result) {
                    model.set(row, col, 0);
                    model.set(row + 1, col, 0);
                    model.set(row, col + 1, 0);
                    model.set(row + 1, col + 1, 0);
                    model.set(nextRow, nextCol, 4);
                    model.set(nextRow + 1, nextCol, 4);
                    model.set(nextRow, nextCol + 1, 4);
                    model.set(nextRow + 1, nextCol + 1, 4);
                    continueMove(nextRow, nextCol);
                    return true;
                }
            }
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
        count = 0;
        playSoundEffect("resources/audio/sound_effect/move.wav");
        new Timer(1, e -> { // 使用Timer实现动画效果
            if (count < totalFrames) {
                double progress = (double) count / totalFrames;
                progress = 1 - (1 - progress) * (1 - progress);
                box.setLocation((int) (startCol * view.getGRID_SIZE() + deltaX * progress + 2), (int) (startRow * view.getGRID_SIZE() + deltaY * progress + 2));
                box.repaint();
                count++;
            } else {
                ((Timer) e.getSource()).stop();
                box.setRow(nextRow);
                box.setCol(nextCol);
                box.setLocation(nextCol * view.getGRID_SIZE() + 2, nextRow * view.getGRID_SIZE() + 2);
                box.repaint();
                history.add(getHistory(null));
                endGame(true);
            }
        }).start();
    }

    // 播放音效
    public void playSoundEffect(String filePath) {
        try {
            Clip soundEffectClip = AudioSystem.getClip();
            soundEffectClip.open(AudioSystem.getAudioInputStream(new File(filePath)));
            soundEffectClip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(view, "播放音效时发生错误: " + e.getMessage());
        }
    }

    // 检查游戏是否结束
    public void endGame(boolean check) {
        if (!check || model.getId(4, 1) == 4 && model.getId(4, 2) == 4) {
            view.countdownTimer.stop();
            if (check) {
                playSoundEffect("resources/audio/sound_effect/victory.wav");
                if (user != null&&"决胜千里".equals(difficulty)) saveLeaderBoard();
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
        List<String> board = new ArrayList<>();
        try {
            File file = new File("resources/leaderboard.txt");
            if (file.exists()) {
                board = Files.readAllLines(file.toPath());
            }
            String newEntry = user + " " + view.steps + " " + view.countdownLabel.getText().split("：")[1].split("息")[0];
            board.add(newEntry);
            board.sort((a, b) -> {
                int stepsA = Integer.parseInt(a.split(" ")[1]);
                int stepsB = Integer.parseInt(b.split(" ")[1]);
                if (stepsA == stepsB)
                    return Integer.compare(Integer.parseInt(a.split(" ")[2]), Integer.parseInt(b.split(" ")[2]));
                return Integer.compare(stepsA, stepsB);
            });
            if (board.size() > 100) {
                board = board.subList(0, 100);
            }
            Files.write(file.toPath(), board);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "保存战绩时发生错误: " + e.getMessage());
        }
    }

    public void undo() {
        if (history.size() > 1) {
            history.removeLast();
            model.updateMatrix(getMatrixFromHistory(history.getLast()));
            view.initialGame(view.steps - 1, -1);
        } else {
            JOptionPane.showMessageDialog(view, "没有更多的撤销操作");
        }
    }

    public void loadGame() {
        try {
            if (user == null || user.isEmpty()) {
                JOptionPane.showMessageDialog(view, "请先登录");
                return;
            }
            String path = "resources/history/" + user + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();
            if(line == null || line.isEmpty()) {
                JOptionPane.showMessageDialog(view, user+"还没有保存过历史游戏数据");
                return;
            }
            int steps = 0;
            int countdown = 0;
            ArrayList<String> newHistory = new ArrayList<>();
            boolean firstLine = true;
            while (line != null) {
                System.out.println(line);
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    if (firstLine) {
                        steps = Integer.parseInt(parts[0]);
                        countdown = Integer.parseInt(parts[1]);
                        firstLine = false;
                    } else {
                        int[][] matrix = getMatrixFromHistory(parts[0]);
                        int[][] newMatrix = decode(parts[1]);
                        if (matrix != null && Arrays.deepEquals(matrix, newMatrix)) {
                            newHistory.add(parts[0]);
                        } else if (newMatrix != null) {
                            newHistory.add(getHistory(newMatrix));
                            JOptionPane.showMessageDialog(view, "第" + newHistory.size() + "行数据错误，成功启用数据恢复。");
                        } else {
                            new File(path).delete();
                            JOptionPane.showMessageDialog(view, "第" + (newHistory.size() + 1) + "行数据错误且无法恢复，已删除该文件。");
                            return;
                        }
                    }
                } else {
                    break;
                }
                line = br.readLine();
            }
            System.out.println("newHistory: " + newHistory.size());
            if (!newHistory.isEmpty() && newHistory.size() == steps + 1) {
                model.updateMatrix(getMatrixFromHistory(newHistory.getLast()));
                history.clear();
                history.addAll(newHistory);
                view.initialGame(steps, countdown);
            } else if (new File(path).delete()) {
                JOptionPane.showMessageDialog(view, "历史游戏数据缺失，已删除该文件。");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "读取文件时发生错误: " + e.getMessage());
        }
    }

    // 编码二维数组为字符串
    public static String encode(int[][] matrix) throws IOException {
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
        byte[] data = Base64.getDecoder().decode(token);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        int rows;
        int[][] matrix;
        try {
            rows = dis.readInt();
            int cols = dis.readInt();
            matrix = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = dis.readInt();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return matrix;
    }

    // 保存游戏进度
    public void saveGame() {
        try {
            File file = new File("resources/history/" + user + ".txt");
            if (!file.exists()) { // 不存在则创建文件
                file.createNewFile();
            }
            // 步数 + 倒计时 + 多行历史记录
            StringBuilder sb = new StringBuilder(view.steps + " " + view.countdownLabel.getText().split("：")[1].split("息")[0] + "\n");
            for (String hist : history) {
                sb.append(hist).append(" ").append(encode(getMatrixFromHistory(hist))).append("\n");
            }
            Files.write(file.toPath(), sb.toString().getBytes());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "保存游戏时发生错误: " + e.getMessage());
        }
    }
}
