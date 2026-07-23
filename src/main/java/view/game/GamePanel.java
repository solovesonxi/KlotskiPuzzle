package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;
import view.GameTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static util.Messages.text;

// 游戏面板，显示棋盘和方块
public class GamePanel extends ListenerPanel {
    static final int DRAG_THRESHOLD = 28;
    private final List<BoxComponent> boxes; // 存储方块组件
    private final MapModel model; // 游戏地图模型
    private GameController controller;

    private final int GRID_SIZE = 100; // 网格大小
    private BoxComponent selectedBox; // 被选中的方块
    private Point dragStart;
    private BoxComponent dragBox;
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
        this.setOpaque(false);
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
        List<PieceVisual> verticalPieces = new ArrayList<>(Arrays.asList(
                new PieceVisual("resources/original/image/pieces/vertical-general-1.png", "piece.zhang.fei"),
                new PieceVisual("resources/original/image/pieces/vertical-general-2.png", "piece.zhao.yun"),
                new PieceVisual("resources/original/image/pieces/vertical-general-3.png", "piece.ma.chao"),
                new PieceVisual("resources/original/image/pieces/vertical-general-4.png", "piece.huang.zhong")));
        List<PieceVisual> soldiers = new ArrayList<>(Arrays.asList(
                new PieceVisual("resources/original/image/pieces/soldier-1.png", "piece.soldier.1"),
                new PieceVisual("resources/original/image/pieces/soldier-2.png", "piece.soldier.2"),
                new PieceVisual("resources/original/image/pieces/soldier-3.png", "piece.soldier.3"),
                new PieceVisual("resources/original/image/pieces/soldier-4.png", "piece.soldier.4")));
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                BoxComponent box = null;
                if (map[i][j] == 1) {
                    PieceVisual visual = soldiers.removeFirst();
                    box = new BoxComponent(visual.resourcePath(), visual.labelKey(), i, j);
                    box.setSize(GRID_SIZE, GRID_SIZE);
                    map[i][j] = 0;
                } else if (map[i][j] == 2) {
                    box = new BoxComponent("resources/original/image/pieces/horizontal-general.png",
                            "piece.guan.yu", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE);
                    map[i][j] = 0;
                    map[i][j + 1] = 0; // 占用两个格子
                } else if (map[i][j] == 3) {
                    if (!verticalPieces.isEmpty()) {
                        PieceVisual visual = verticalPieces.removeFirst();
                        box = new BoxComponent(visual.resourcePath(), visual.labelKey(), i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE * 2);
                        map[i][j] = 0;
                        map[i + 1][j] = 0; // 占用两个格子
                    }
                } else if (map[i][j] == 4) {
                    box = new BoxComponent("resources/original/image/pieces/commander.png",
                            "piece.cao.cao", i, j);
                    box.setSize(GRID_SIZE * 2, GRID_SIZE * 2); // 占用四个格子
                    map[i][j] = 0;
                    map[i + 1][j] = 0;
                    map[i][j + 1] = 0;
                    map[i + 1][j + 1] = 0;
                }

                if (box != null) {
                    box.setLocation(j * GRID_SIZE + 2, i * GRID_SIZE + 2); // 设置位置
                    installDragGesture(box);
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
        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(20, 18, 20, 238));
        graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        graphics.setColor(new Color(214, 177, 105, 34));
        for (int column = 1; column < model.getWidth(); column++) {
            int x = column * GRID_SIZE + 2;
            graphics.drawLine(x, 10, x, getHeight() - 12);
        }
        for (int row = 1; row < model.getHeight(); row++) {
            int y = row * GRID_SIZE + 2;
            graphics.drawLine(10, y, getWidth() - 12, y);
        }
        graphics.setColor(GameTheme.GOLD_SOFT);
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
        int exitLeft = GRID_SIZE + 8;
        int exitRight = GRID_SIZE * 3 - 6;
        graphics.setColor(GameTheme.GOLD);
        graphics.setStroke(new BasicStroke(4f));
        graphics.drawLine(exitLeft, getHeight() - 3, exitLeft, getHeight() - 15);
        graphics.drawLine(exitRight, getHeight() - 3, exitRight, getHeight() - 15);
        graphics.dispose();
    }

    @Override
    public void doMouseClick(Point point) {
        Component component = this.getComponentAt(point);
        if (component instanceof BoxComponent clickedComponent) {
            selectBox(clickedComponent);
        }
    }

    @Override
    public void doMoveRight() {
        attemptMove(Direction.RIGHT);
    }

    @Override
    public void doMoveLeft() {
        attemptMove(Direction.LEFT);
    }

    @Override
    public void doMoveUp() {
        attemptMove(Direction.UP);
    }

    @Override
    public void doMoveDown() {
        attemptMove(Direction.DOWN);
    }

    // 更新步数
    public void afterMove() {
        this.steps++;
        if (this.stepLabel != null) {
            this.stepLabel.setText(text("status.steps", this.steps));
        }
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

    public void applyLanguage() {
        if (stepLabel != null) {
            stepLabel.setText(text("status.steps", steps));
        }
        if (countdownLabel != null) {
            countdownLabel.setText(text("status.countdown", remainingSeconds));
        }
        boxes.forEach(BoxComponent::repaint);
    }

    static Direction resolveDragDirection(Point start, Point end, int threshold) {
        int deltaX = end.x - start.x;
        int deltaY = end.y - start.y;
        if (Math.max(Math.abs(deltaX), Math.abs(deltaY)) < threshold) {
            return null;
        }
        if (Math.abs(deltaX) >= Math.abs(deltaY)) {
            return deltaX >= 0 ? Direction.RIGHT : Direction.LEFT;
        }
        return deltaY >= 0 ? Direction.DOWN : Direction.UP;
    }

    private void installDragGesture(BoxComponent box) {
        MouseAdapter gesture = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!isInputEnabled()) {
                    return;
                }
                requestFocusInWindow();
                boolean changed = selectedBox != box;
                selectBox(box);
                if (changed && controller != null) {
                    controller.playSelectionFeedback();
                }
                dragBox = box;
                dragStart = SwingUtilities.convertPoint(box, event.getPoint(), GamePanel.this);
                box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!isInputEnabled() || dragBox != box || dragStart == null) {
                    return;
                }
                Point end = SwingUtilities.convertPoint(box, event.getPoint(), GamePanel.this);
                Direction direction = resolveDragDirection(dragStart, end, DRAG_THRESHOLD);
                dragStart = null;
                dragBox = null;
                box.setCursor(Cursor.getDefaultCursor());
                if (direction != null) {
                    attemptMove(direction);
                }
            }
        };
        box.addMouseListener(gesture);
        box.addMouseMotionListener(gesture);
    }

    private void selectBox(BoxComponent box) {
        if (selectedBox != null && selectedBox != box) {
            selectedBox.setSelected(false);
        }
        selectedBox = box;
        selectedBox.setSelected(true);
    }

    private void attemptMove(Direction direction) {
        if (!isInputEnabled() || selectedBox == null || controller == null) {
            return;
        }
        if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), direction)) {
            afterMove();
        }
    }

    private record PieceVisual(String resourcePath, String labelKey) {
    }
}
