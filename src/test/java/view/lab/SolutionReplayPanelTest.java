package view.lab;

import lab.SearchExperiment;
import lab.SearchExperimentRunner;
import lab.SearchStrategy;
import lab.SolutionReplay;
import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzlePreset;
import model.PuzzleState;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SolutionReplayPanelTest {
    @Test
    void nextControlAdvancesTheDisplayedPuzzleState() throws Exception {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.CELL_STEP);
        SearchExperimentRunner.Result result = new SearchExperimentRunner().run(
                SearchExperiment.of(puzzle, SearchStrategy.A_STAR));
        SolutionReplay replay = SolutionReplay.of(puzzle, result.solution());
        AtomicReference<PuzzleState> displayed = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            SolutionReplayPanel panel = new SolutionReplayPanel(displayed::set);
            panel.setReplay(replay);
            PuzzleState initial = displayed.get();
            JButton next = (JButton) findNamedComponent(panel, "solutionReplay.next");

            assertNotNull(next);
            next.doClick();
            assertEquals(1, panel.currentStep());
            assertNotEquals(initial, displayed.get());
        });
    }

    private static Component findNamedComponent(Container owner, String name) {
        for (Component component : owner.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container child) {
                Component found = findNamedComponent(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
