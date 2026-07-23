package lab;

import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzlePreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentRecordJsonTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void exportsAReproducibleSelfDescribingRecord() throws Exception {
        PuzzleDefinition puzzle = PuzzlePreset.TUTORIAL.definition(MovementRule.PIECE_MOVE);
        SearchExperiment experiment = SearchExperiment.of(puzzle, SearchStrategy.A_STAR);
        SearchExperimentRunner.Result result = new SearchExperimentRunner().run(experiment);
        ExperimentRecord record = ExperimentRecord.capture(experiment, result,
                Instant.parse("2026-07-24T00:00:00Z"),
                new RuntimeEnvironment("25.0.3", "Windows", "11", "amd64"));
        Path output = temporaryDirectory.resolve("experiment.json");

        ExperimentRecordJson.write(record, output);
        String json = Files.readString(output);

        assertTrue(json.contains("\"formatVersion\": 1"));
        assertTrue(json.contains(puzzle.contentId()));
        assertTrue(json.contains("\"movementRule\": \"PIECE_MOVE\""));
        assertTrue(json.contains("\"strategy\": \"A_STAR\""));
        assertTrue(json.contains("\"solution\": ["));
        assertTrue(json.contains("\"javaVersion\": \"25.0.3\""));
        assertEquals(json, ExperimentRecordJson.toJson(record));
    }
}
