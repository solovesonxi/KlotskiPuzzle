package view.game;

import model.Direction;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GamePanelDragTest {
    @Test
    void ignoresClickSizedMovement() {
        assertNull(GamePanel.resolveDragDirection(
                new Point(50, 50), new Point(70, 65), GamePanel.DRAG_THRESHOLD));
    }

    @Test
    void usesTheDominantHorizontalAxis() {
        assertEquals(Direction.RIGHT, GamePanel.resolveDragDirection(
                new Point(50, 50), new Point(105, 65), GamePanel.DRAG_THRESHOLD));
        assertEquals(Direction.LEFT, GamePanel.resolveDragDirection(
                new Point(80, 50), new Point(20, 35), GamePanel.DRAG_THRESHOLD));
    }

    @Test
    void usesTheDominantVerticalAxis() {
        assertEquals(Direction.DOWN, GamePanel.resolveDragDirection(
                new Point(50, 50), new Point(60, 105), GamePanel.DRAG_THRESHOLD));
        assertEquals(Direction.UP, GamePanel.resolveDragDirection(
                new Point(50, 80), new Point(40, 20), GamePanel.DRAG_THRESHOLD));
    }
}
