package cli;

import model.Difficulty;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolverMetricsReportTest {
    @Test
    void reportsAllPresetMetricsAsTabSeparatedRows() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            try (PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                SolverMetricsReport.writeReport(output);
            }
        });

        String[] lines = buffer.toString(StandardCharsets.UTF_8).strip().split("\\R");
        assertEquals("preset\tstatus\tmoves\texpanded\tdiscovered\telapsed_ms", lines[0]);
        assertEquals(Difficulty.values().length + 1, lines.length);

        Map<String, String[]> rows = Arrays.stream(lines, 1, lines.length)
                .map(line -> line.split("\\t"))
                .peek(fields -> assertEquals(6, fields.length))
                .collect(Collectors.toMap(fields -> fields[0], Function.identity()));

        for (Difficulty difficulty : Difficulty.values()) {
            String[] fields = rows.get(difficulty.name());
            assertEquals("SOLVED", fields[1]);
            assertTrue(Integer.parseInt(fields[2]) > 0);
            assertTrue(Integer.parseInt(fields[3]) > 0);
            assertTrue(Integer.parseInt(fields[4]) > 0);
            assertTrue(Long.parseLong(fields[5]) >= 0);
        }
    }
}
