package view.game;

import data.LeaderboardRepository.ScoreEntry;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardDialogTest {
    @Test
    void emptyLeaderboardStillOwnsAnOpaqueBackground() {
        JPanel content = LeaderboardDialog.createContent(List.of());

        assertTrue(content.isOpaque());
        assertTrue(content.getComponentCount() >= 2);
    }

    @Test
    void populatedLeaderboardDoesNotExposeTheBoardThroughItsViewport() {
        JPanel content = LeaderboardDialog.createContent(
                List.of(new ScoreEntry("player", 42, 100)));
        JScrollPane scrollPane = findScrollPane(content);

        assertNotNull(scrollPane);
        assertTrue(scrollPane.isOpaque());
        assertTrue(scrollPane.getViewport().isOpaque());
    }

    private JScrollPane findScrollPane(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JScrollPane scrollPane) {
                return scrollPane;
            }
        }
        return null;
    }
}
