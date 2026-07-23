package lab;

import model.PuzzleMove;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Serializes Experiment Records without introducing a second solver or external JSON runtime. */
public final class ExperimentRecordJson {
    private ExperimentRecordJson() {
    }

    public static void write(ExperimentRecord record, Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        Path absolute = path.toAbsolutePath();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent());
        }
        Files.writeString(absolute, toJson(record), StandardCharsets.UTF_8);
    }

    public static String toJson(ExperimentRecord record) {
        Objects.requireNonNull(record, "record");
        StringBuilder json = new StringBuilder(2_048);
        json.append("{\n")
                .append("  \"formatVersion\": ").append(record.formatVersion()).append(",\n")
                .append("  \"createdAt\": ").append(quoted(record.createdAt())).append(",\n")
                .append("  \"puzzle\": {\n")
                .append("    \"formatVersion\": ").append(record.puzzleFormatVersion()).append(",\n")
                .append("    \"contentId\": ").append(quoted(record.puzzleContentId())).append(",\n")
                .append("    \"initialState\": ").append(quoted(record.initialState().compact())).append(",\n")
                .append("    \"movementRule\": ").append(quoted(record.movementRule())).append("\n")
                .append("  },\n")
                .append("  \"experiment\": {\n")
                .append("    \"strategy\": ").append(quoted(record.strategy())).append(",\n")
                .append("    \"heuristicWeight\": ").append(record.heuristicWeight()).append(",\n")
                .append("    \"maxDiscoveredStates\": ").append(record.maxDiscoveredStates()).append("\n")
                .append("  },\n")
                .append("  \"outcome\": {\n")
                .append("    \"status\": ").append(quoted(record.status())).append(",\n")
                .append("    \"expandedStates\": ").append(record.metrics().expandedStates()).append(",\n")
                .append("    \"discoveredStates\": ").append(record.metrics().discoveredStates()).append(",\n")
                .append("    \"maximumFrontier\": ").append(record.metrics().maximumFrontier()).append(",\n")
                .append("    \"elapsedNanos\": ").append(record.elapsedNanos()).append(",\n")
                .append("    \"solution\": [");
        for (int index = 0; index < record.solution().size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            PuzzleMove move = record.solution().get(index);
            json.append("\n      {\"row\": ").append(move.row())
                    .append(", \"column\": ").append(move.column())
                    .append(", \"direction\": ").append(quoted(move.direction().name()))
                    .append(", \"distance\": ").append(move.distance()).append('}');
        }
        if (!record.solution().isEmpty()) {
            json.append('\n').append("    ");
        }
        json.append("]\n")
                .append("  },\n")
                .append("  \"environment\": {\n")
                .append("    \"javaVersion\": ").append(quoted(record.environment().javaVersion())).append(",\n")
                .append("    \"osName\": ").append(quoted(record.environment().osName())).append(",\n")
                .append("    \"osVersion\": ").append(quoted(record.environment().osVersion())).append(",\n")
                .append("    \"osArchitecture\": ").append(quoted(record.environment().osArchitecture())).append("\n")
                .append("  }\n")
                .append("}\n");
        return json.toString();
    }

    private static String quoted(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }
}
