package cli;

import lab.ExperimentRecord;
import lab.ExperimentRecordJson;
import lab.SearchExperiment;
import lab.SearchExperimentRunner;
import lab.SearchStrategy;
import model.MovementRule;
import model.PuzzleDefinition;
import model.PuzzlePreset;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Prints one comparable search report for every Lab strategy under a shared experiment contract. */
public final class SearchStrategyReport {
    private static final List<SearchStrategy> REPORT_STRATEGIES = List.of(
            SearchStrategy.BFS,
            SearchStrategy.GREEDY_BEST_FIRST,
            SearchStrategy.A_STAR,
            SearchStrategy.WEIGHTED_A_STAR);

    private SearchStrategyReport() {
    }

    public static void main(String[] args) throws IOException {
        if (Arrays.asList(args).contains("--help")) {
            printUsage(System.out);
            return;
        }
        Config config = Config.parse(args);
        List<ReportRow> rows = execute(config);
        writeTsv(System.out, rows);
        writeTsvFile(config, rows);
        writeJsonRecords(config, rows);
    }

    static List<ReportRow> execute(Config config) {
        Objects.requireNonNull(config, "config");
        PuzzleDefinition puzzle = config.preset().definition(config.movementRule());
        SearchExperimentRunner runner = new SearchExperimentRunner();
        List<ReportRow> rows = new ArrayList<>(REPORT_STRATEGIES.size());
        for (SearchStrategy strategy : REPORT_STRATEGIES) {
            double weight = strategy == SearchStrategy.WEIGHTED_A_STAR
                    ? config.weightedAStarWeight() : 1.0;
            SearchExperiment experiment = new SearchExperiment(puzzle, strategy, weight,
                    config.maxDiscoveredStates());
            SearchExperimentRunner.Result result = runner.run(experiment);
            rows.add(new ReportRow(config.preset(), experiment, result));
        }
        return List.copyOf(rows);
    }

    static void printUsage(PrintStream output) {
        Objects.requireNonNull(output, "output");
        output.println("Usage: SearchStrategyReport [preset] [movement-rule] [options]");
        output.println("Presets: tutorial, intermediate, heng-dao-li-ma");
        output.println("Movement rules: cell-step, piece-move");
        output.println("Options:");
        output.println("  --max-discovered-states=<positive integer>");
        output.println("  --weighted-a-star-weight=<number >= 1.0>");
        output.println("  --tsv=<output file>");
        output.println("  --json-dir=<experiment record directory>");
    }

    static void writeTsv(PrintStream output, List<ReportRow> rows) {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(rows, "rows");
        output.println("preset\tmovement_rule\tstrategy\theuristic_weight\tmax_discovered_states"
                + "\tstatus\tmoves\texpanded\tdiscovered\tmaximum_frontier\telapsed_ms");
        for (ReportRow row : rows) {
            SearchExperiment experiment = row.experiment();
            SearchExperimentRunner.Result result = row.result();
            output.printf(Locale.ROOT, "%s\t%s\t%s\t%.1f\t%d\t%s\t%d\t%d\t%d\t%d\t%d%n",
                    row.preset().id(), experiment.puzzle().movementRule().name(),
                    experiment.strategy().name(), experiment.heuristicWeight(),
                    experiment.maxDiscoveredStates(), result.status().name(),
                    result.solution().size(), result.metrics().expandedStates(),
                    result.metrics().discoveredStates(), result.metrics().maximumFrontier(),
                    result.elapsedNanos() / 1_000_000L);
        }
    }

    static void writeJsonRecords(Config config, List<ReportRow> rows) throws IOException {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(rows, "rows");
        if (config.jsonDirectory() == null) {
            return;
        }
        for (ReportRow row : rows) {
            String fileName = row.preset().id() + '-'
                    + hyphenated(row.experiment().puzzle().movementRule().name()) + '-'
                    + hyphenated(row.experiment().strategy().name()) + ".json";
            ExperimentRecord record = ExperimentRecord.capture(row.experiment(), row.result());
            ExperimentRecordJson.write(record, config.jsonDirectory().resolve(fileName));
        }
    }

    static void writeTsvFile(Config config, List<ReportRow> rows) throws IOException {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(rows, "rows");
        if (config.tsvPath() == null) {
            return;
        }
        Path absolute = config.tsvPath().toAbsolutePath();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent());
        }
        try (PrintStream output = new PrintStream(
                Files.newOutputStream(absolute), true, java.nio.charset.StandardCharsets.UTF_8)) {
            writeTsv(output, rows);
        }
    }

    private static String hyphenated(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', '-');
    }

    record ReportRow(PuzzlePreset preset, SearchExperiment experiment,
                     SearchExperimentRunner.Result result) {
        ReportRow {
            Objects.requireNonNull(preset, "preset");
            Objects.requireNonNull(experiment, "experiment");
            Objects.requireNonNull(result, "result");
        }
    }

    record Config(PuzzlePreset preset, MovementRule movementRule, int maxDiscoveredStates,
                  double weightedAStarWeight, Path tsvPath, Path jsonDirectory) {
        Config {
            Objects.requireNonNull(preset, "preset");
            Objects.requireNonNull(movementRule, "movementRule");
            if (maxDiscoveredStates < 1) {
                throw new IllegalArgumentException("maxDiscoveredStates must be positive");
            }
            if (!Double.isFinite(weightedAStarWeight) || weightedAStarWeight < 1.0) {
                throw new IllegalArgumentException(
                        "weightedAStarWeight must be finite and at least 1.0");
            }
        }

        static Config parse(String[] args) {
            Objects.requireNonNull(args, "args");
            PuzzlePreset preset = PuzzlePreset.TUTORIAL;
            MovementRule movementRule = MovementRule.CELL_STEP;
            int maxDiscoveredStates = SearchExperiment.DEFAULT_MAX_DISCOVERED_STATES;
            double weightedAStarWeight = SearchExperiment.DEFAULT_WEIGHTED_A_STAR_WEIGHT;
            Path tsvPath = null;
            Path jsonDirectory = null;
            int positionalIndex = 0;

            for (String argument : args) {
                if (argument.startsWith("--preset=")) {
                    preset = parsePreset(value(argument));
                } else if (argument.startsWith("--movement-rule=")) {
                    movementRule = parseMovementRule(value(argument));
                } else if (argument.startsWith("--max-discovered-states=")) {
                    maxDiscoveredStates = Integer.parseInt(value(argument));
                } else if (argument.startsWith("--weighted-a-star-weight=")) {
                    weightedAStarWeight = Double.parseDouble(value(argument));
                } else if (argument.startsWith("--tsv=")) {
                    tsvPath = Path.of(value(argument));
                } else if (argument.startsWith("--json-dir=")) {
                    jsonDirectory = Path.of(value(argument));
                } else if (argument.startsWith("--")) {
                    throw new IllegalArgumentException("Unknown option: " + argument);
                } else if (positionalIndex == 0) {
                    preset = parsePreset(argument);
                    positionalIndex++;
                } else if (positionalIndex == 1) {
                    movementRule = parseMovementRule(argument);
                    positionalIndex++;
                } else {
                    throw new IllegalArgumentException("Unexpected argument: " + argument);
                }
            }
            return new Config(preset, movementRule, maxDiscoveredStates,
                    weightedAStarWeight, tsvPath, jsonDirectory);
        }

        private static String value(String argument) {
            int separator = argument.indexOf('=');
            if (separator < 0 || separator == argument.length() - 1) {
                throw new IllegalArgumentException("Option requires a value: " + argument);
            }
            return argument.substring(separator + 1);
        }

        private static PuzzlePreset parsePreset(String value) {
            String normalized = value.toLowerCase(Locale.ROOT).replace('_', '-');
            for (PuzzlePreset preset : PuzzlePreset.values()) {
                if (preset.id().equals(normalized)
                        || preset.name().toLowerCase(Locale.ROOT).replace('_', '-').equals(normalized)) {
                    return preset;
                }
            }
            throw new IllegalArgumentException("Unknown preset: " + value);
        }

        private static MovementRule parseMovementRule(String value) {
            return switch (value.toLowerCase(Locale.ROOT).replace('_', '-')) {
                case "cell-step" -> MovementRule.CELL_STEP;
                case "piece-move" -> MovementRule.PIECE_MOVE;
                default -> throw new IllegalArgumentException("Unknown movement rule: " + value);
            };
        }
    }
}
