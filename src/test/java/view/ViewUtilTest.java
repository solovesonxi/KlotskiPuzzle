package view;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewUtilTest {
    @Test
    void musicButtonExposesAccessibleNameAndTooltip() {
        JButton button = ViewUtil.createMusicButton(
                "resources/original/image/icons/previous.png", new Point(0, 0),
                "上一首背景音乐", "播放上一首背景音乐");

        assertEquals("上一首背景音乐", button.getAccessibleContext().getAccessibleName());
        assertEquals("播放上一首背景音乐",
                button.getAccessibleContext().getAccessibleDescription());
        assertEquals("播放上一首背景音乐", button.getToolTipText());
        assertTrue(button.isFocusable());
    }

    @Test
    void accessibilityTextCanFollowMusicStateChanges() {
        JButton button = new JButton();

        ViewUtil.configureButtonAccessibility(button, "暂停背景音乐", "暂停背景音乐");

        assertEquals("暂停背景音乐", button.getAccessibleContext().getAccessibleName());
        assertEquals("暂停背景音乐", button.getAccessibleContext().getAccessibleDescription());
        assertEquals("暂停背景音乐", button.getToolTipText());
    }
}
