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

// 游戏面板，显示棋盘和方块
public class GamePanel extends ListenerPanel {
    private final List<BoxComponent> boxes;
    private final MapModel model;
    private GameController controller;

    private final int GRID_SIZE = 100;
    private BoxComponent selectedBox;
    public JLabel stepLabel;
    public JLabel countdownLabel;
    public int steps;
    public Timer countdownTimer;


    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.setLayout(null);
        this.setSize(model.getWidth() * GRID_SIZE + 4, model.getHeight() * GRID_SIZE + 4);
        this.model = model;
        this.selectedBox = null;
        initialGame(0, 180);
    }

    // 初始化棋盘图像
    public void initialGame(int step, int countdown) {
        this.steps = step;
        if (this.stepLabel != null) this.stepLabel.setText(String.format("行军步数：%d", steps));
        if (countdown > 0) {
            if (this.countdownLabel != null) this.countdownLabel.setText(String.format("剩余时限：%d息", countdown));
            final int[] finalCountDown = {countdown};
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            countdownTimer = new Timer(1000, _ -> {
                if (finalCountDown[0] > 0) {
                    finalCountDown[0]--;
                    if (countdownLabel != null)
                        countdownLabel.setText(String.format("剩余时限：%d息", finalCountDown[0]));
                    if (finalCountDown[0] <= 0) {
                        countdownTimer.stop();
                        controller.endGame(false);
                    }
                }
            });
            countdownTimer.start();
        }
        boxes.clear();
        this.removeAll(); // 清空面板
        int[][] map = new int[model.getHeight()][model.getWidth()];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = model.getId(i, j);
            }
        }
        // 绘制四种方块
        List<String> fiveTiggerImages = new ArrayList<>(Arrays.asList("马超.png", "张飞.png", "赵云.png", "黄忠.png"));
        List<String> soldierImages = new ArrayList<>(Arrays.asList("小兵1.png", "小兵2.png", "小兵3.png", "小兵4.png"));
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                BoxComponent box = null;
                if (map[i][j] == 1) {
                    box = new BoxComponent(soldierImages.removeFirst(), i, j);
                    box.setSize(GRID_SIZE, GRID_SIZE);
                    map[i][j] = 0;
                } else if (map[i][j] == 2) {
                    box = new BoxComponent("关羽.png", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE);
                    map[i][j] = 0;
                    map[i][j + 1] = 0;
                } else if (map[i][j] == 3) {
                    if (!fiveTiggerImages.isEmpty()) {
                        box = new BoxComponent(fiveTiggerImages.removeFirst(), i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE * 2);
                        map[i][j] = 0;
                        map[i + 1][j] = 0;
                    }
                } else if (map[i][j] == 4) {
                    box = new BoxComponent("曹操.png", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                    map[i][j] = 0;
                    map[i + 1][j] = 0;
                    map[i][j + 1] = 0;
                    map[i + 1][j + 1] = 0;
                }

                if (box != null) {
                    box.setLocation(j * GRID_SIZE + 2, i * GRID_SIZE + 2);
                    boxes.add(box);
                    this.add(box);
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
        this.setBorder(border);
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
        System.out.println("向右");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveLeft() {
        System.out.println("向左");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveUp() {
        System.out.println("向上");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveDown() {
        System.out.println("向下");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                afterMove();
            }
        }
    }

    public void afterMove() {
        this.steps++;
        this.stepLabel.setText(String.format("行军步数：%d", this.steps));
    }

    public void setLabel(JLabel stepLabel, JLabel countdownLabel) {
        this.stepLabel = stepLabel;
        this.countdownLabel = countdownLabel;
    }

    public void AIMove(int row, int col, Direction direction) {
        for (BoxComponent box : boxes) {
            if (box.getRow() == row && box.getCol() == col) {
                selectedBox = box;
                System.out.println("AI Move box: (" + row + ", " + col + ") " + direction);
                if (controller.doMove(box.getRow(), box.getCol(), direction)) afterMove();
                break;
            }
        }
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    public BoxComponent getSelectedBox() {
        return selectedBox;
    }

    public int getGRID_SIZE() {
        return GRID_SIZE;
    }
}
