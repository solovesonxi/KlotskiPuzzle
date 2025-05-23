package view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

// ListenerPanel 是一个抽象类，继承自 JPanel，用于处理键盘和鼠标事件，被游戏面板 GamePanel 继承
public abstract class ListenerPanel extends JPanel {
    public ListenerPanel() {
        enableEvents(AWTEvent.KEY_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        this.setFocusable(true);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> doMoveRight();
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> doMoveLeft();
                case KeyEvent.VK_UP, KeyEvent.VK_W -> doMoveUp();
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> doMoveDown();
            }
        }
    }
    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
            doMouseClick(e.getPoint());
        }
    }
    public abstract void doMouseClick(Point point);

    public abstract void doMoveRight();

    public abstract void doMoveLeft();

    public abstract void doMoveUp();

    public abstract void doMoveDown();


}
