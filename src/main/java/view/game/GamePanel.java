package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static util.Messages.text;

// 游戏面板，显示棋盘和方块
public class GamePanel extends ListenerPanel {
    private final List<BoxComponent> boxes; // 存储方块组件
    private final MapModel model; // 游戏地图模型
    private GameController controller;

    private final int GRID_SIZE = 100; // 网格大小
    private BoxComponent selectedBox; // 被选中的方块
    public JLabel stepLabel; // 步数标签
    public JLabel countdownLabel; //倒计时标签
    public int steps; // 当前步数
    private int remainingSeconds;
    public Timer countdownTimer; // 倒计时计时器

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setLayout(null);
        this.setSize(model.getWidth() * GRID_SIZE + 4, model.getHeight() * GRID_SIZE + 4);
        this.model = model;
        this.selectedBox = null;
        initialGame(0, 180); // 初始化游戏
    }

    // 初始化棋盘图像
    public void initialGame(int step, int countdown) {
        this.steps = step; // 更新步数
        if (this.stepLabel != null) this.stepLabel.setText(text("status.steps", steps));
        if (countdown > 0) {
            remainingSeconds = countdown;
            if (this.countdownLabel != null) this.countdownLabel.setText(text("status.countdown", remainingSeconds));
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            countdownTimer = new Timer(1000, event -> { // 每秒减少倒计时时间
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    if (countdownLabel != null)
                        countdownLabel.setText(text("status.countdown", remainingSeconds));
                    if (remainingSeconds <= 0) {
                        countdownTimer.stop();
                        controller.endGame(false); // 倒计时结束，游戏失败
                    }
                }
            });
            countdownTimer.start(); // 启动计时器
        }
        boxes.clear();
        selectedBox = null;
        this.removeAll(); // 清空面板
        int[][] map = new int[model.getHeight()][model.getWidth()];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = model.getId(i, j); // 从模型获取地图信息
            }
        }
        // 绘制四种方块
        List<String> verticalPieceImages = new ArrayList<>(Arrays.asList(
                "resources/original/image/pieces/vertical-general-1.png",
                "resources/original/image/pieces/vertical-general-2.png",
                "resources/original/image/pieces/vertical-general-3.png",
                "resources/original/image/pieces/vertical-general-4.png"));
        List<String> soldierImages = new ArrayList<>(Arrays.asList(
                "resources/original/image/pieces/soldier-1.png",
                "resources/original/image/pieces/soldier-2.png",
                "resources/original/image/pieces/soldier-3.png",
                "resources/original/image/pieces/soldier-4.png"));
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                BoxComponent box = null;
                if (map[i][j] == 1) {
                    box = new BoxComponent(soldierImages.removeFirst(), i, j); // 创建士兵方块
                    box.setSize(GRID_SIZE, GRID_SIZE);
                    map[i][j] = 0;
                } else if (map[i][j] == 2) {
                    box = new BoxComponent("resources/original/image/pieces/horizontal-general.png", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE);
                    map[i][j] = 0;
                    map[i][j + 1] = 0; // 占用两个格子
                } else if (map[i][j] == 3) {
                    if (!verticalPieceImages.isEmpty()) {
                        box = new BoxComponent(verticalPieceImages.removeFirst(), i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE * 2);
                        map[i][j] = 0;
                        map[i + 1][j] = 0; // 占用两个格子
                    }
                } else if (map[i][j] == 4) {
                    box = new BoxComponent("resources/original/image/pieces/commander.png", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE * 2); // 占用四个格子
                    map[i][j] = 0;
                    map[i + 1][j] = 0;
                    map[i][j + 1] = 0;
                    map[i + 1][j + 1] = 0;
                }

                if (box != null) {
                    box.setLocation(j * GRID_SIZE + 2, i * GRID_SIZE + 2); // 设置位置
                    boxes.add(box);
                    this.add(box); // 添加到面板
                }
            }
        }
        this.repaint(); // 刷新面板
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);
        this.setBorder(border); // 设置边框
    }

    @Override
    public void doMouseClick(Point point) {
        Component component = this.getComponentAt(point);
        if (component instanceof BoxComponent clickedComponent) {
            if (selectedBox == null) { // 选中块
                selectedBox = clickedComponent;
                selectedBox.setSelected(true);
            } else if (selectedBox != clickedComponent) { // 选中其他块
                selectedBox.setSelected(false);
                clickedComponent.setSelected(true);
                selectedBox = clickedComponent;
            } else { // 取消选中
                clickedComponent.setSelected(false);
                selectedBox = null;
            }
        }
    }

    @Override
    public void doMoveRight() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                afterMove(); // 更新步数
            }
        }
    }

    @Override
    public void doMoveLeft() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                afterMove(); // 更新步数
            }
        }
    }

    @Override
    public void doMoveUp() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                afterMove(); // 更新步数
            }
        }
    }

    @Override
    public void doMoveDown() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                afterMove(); // 更新步数
            }
        }
    }

    // 更新步数
    public void afterMove() {
        this.steps++;
        this.stepLabel.setText(text("status.steps", this.steps));
    }

    // 设置标签（行军步数和倒计时）
    public void setLabel(JLabel stepLabel, JLabel countdownLabel) {
        this.stepLabel = stepLabel;
        this.countdownLabel = countdownLabel;
        this.stepLabel.setText(text("status.steps", steps));
        this.countdownLabel.setText(text("status.countdown", remainingSeconds));
    }

    // AI移动
    public void AIMove(int row, int col, Direction direction) {
        for (BoxComponent box : boxes) {
            if (box.getRow() == row && box.getCol() == col) {
                selectedBox = box;
                if (controller.doMove(box.getRow(), box.getCol(), direction)) afterMove(); // 更新步数
                break;
            }
        }
    }

    public void setController(GameController controller) {
        this.controller = controller; // 设置控制器
    }

    public BoxComponent getSelectedBox() {
        return selectedBox; // 获取被选中的方块
    }

    public int getGRID_SIZE() {
        return GRID_SIZE; // 获取网格大小
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
