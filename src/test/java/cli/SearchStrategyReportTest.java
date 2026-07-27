package cli;

import lab.SearchExperimentRunner;
import lab.SearchStrategy;
import model.MovementRule;
import model.PuzzlePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchStrategyReportTest {
    @TempDir
    Path tempDirectory;

    @Test
    void tutorialCellStepReportUsesOneSharedContractAndStableMetrics() {
        SearchStrategyReport.Config config = SearchStrategyReport.Config.parse(new String[0]);
        List<SearchStrategyReport.ReportRow> rows = SearchStrategyReport.execute(config);

        assertEquals(PuzzlePreset.TUTORIAL, config.preset());
        assertEquals(MovementRule.CELL_STEP, config.movementRule());
        assertEquals(List.of(SearchStrategy.BFS, SearchStrategy.GREEDY_BEST_FIRST,
                        SearchStrategy.A_STAR, SearchStrategy.WEIGHTED_A_STAR),
                rows.stream().map(row -> row.experiment().strategy()).toList());
        assertTrue(rows.stream().allMatch(row -> row.experiment().puzzle().movementRule()
                == MovementRule.CELL_STEP));
        assertTrue(rows.stream().allMatch(row -> row.result().status()
                == SearchExperimentRunner.Status.SOLVED));
        assertEquals(List.of(23, 28, 23, 23),
                rows.stream().map(row -> row.result().solution().size()).toList());
        assertEquals(List.of(
                        new SearchExperimentRunner.Metrics(19_837, 22_233, 2_422),
                        new SearchExperimentRunner.Metrics(1_292, 1_621, 345),
                        new SearchExperimentRunner.Metrics(12_445, 14_848, 2_453),
                        new SearchExperimentRunner.Metrics(10_716, 13_002, 2_295)),
                rows.stream().map(row -> row.result().metrics()).toList());
    }

    @Test
    void tsvAndJsonOutputsExposeTheSameFourRuns() throws Exception {
        SearchStrategyReport.Config config = SearchStrategyReport.Config.parse(new String[]{
                "tutorial", "cell-step", "--tsv=" + tempDirectory.resolve("report.tsv"),
                "--json-dir=" + tempDirectory
        });
        List<SearchStrategyReport.ReportRow> rows = SearchStrategyReport.execute(config);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        SearchStrategyReport.writeTsv(new PrintStream(bytes, true, StandardCharsets.UTF_8), rows);
        SearchStrategyReport.writeTsvFile(config, rows);
        SearchStrategyReport.writeJsonRecords(config, rows);

        String tsv = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(tsv.startsWith("preset\tmovement_rule\tstrategy\theuristic_weight"));
        assertEquals(5, tsv.lines().count());
        assertEquals(tsv, Files.readString(tempDirectory.resolve("report.tsv")));
        for (SearchStrategy strategy : SearchStrategy.values()) {
            assertTrue(tsv.contains("\t" + strategy.name() + "\t"));
            Path json = tempDirectory.resolve("tutorial-cell-step-"
                    + strategy.name().toLowerCase().replace('_', '-') + ".json");
            assertTrue(Files.isRegularFile(json));
            String content = Files.readString(json);
            assertTrue(content.contains("\"strategy\": \"" + strategy.name() + "\""));
            assertTrue(content.contains("\"movementRule\": \"CELL_STEP\""));
        }
    }

    @Test
    void rejectsUnknownOrInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> SearchStrategyReport.Config.parse(new String[]{"unknown"}));
        assertThrows(IllegalArgumentException.class,
                () -> SearchStrategyReport.Config.parse(
                        new String[]{"--weighted-a-star-weight=0.5"}));
        assertThrows(IllegalArgumentException.class,
                () -> SearchStrategyReport.Config.parse(new String[]{"--unknown=value"}));
    }

    @Test
    void helpDocumentsTheReproducibleOutputOptions() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        SearchStrategyReport.printUsage(
                new PrintStream(bytes, true, StandardCharsets.UTF_8));

        String help = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("tutorial"));
        assertTrue(help.contains("cell-step"));
        assertTrue(help.contains("--tsv="));
        assertTrue(help.contains("--json-dir="));
    }
}
