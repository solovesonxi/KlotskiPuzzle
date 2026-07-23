package cli;

import model.Difficulty;
import model.HuaRongDaoSolver;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

/** Prints reproducible A* metrics for every built-in difficulty preset. */
public final class SolverMetricsReport {
    private SolverMetricsReport() {
    }

    public static void main(String[] args) {
        writeReport(System.out);
    }

    static void writeReport(PrintStream output) {
        Objects.requireNonNull(output, "output");
        output.println("preset\tstatus\tmoves\texpanded\tdiscovered\telapsed_ms");
        HuaRongDaoSolver solver = new HuaRongDaoSolver();
        for (Difficulty difficulty : Difficulty.values()) {
            long startedAt = System.nanoTime();
            HuaRongDaoSolver.Result result = solver.solveDetailed(
                    difficulty.initialBoard(),
                    HuaRongDaoSolver.DEFAULT_MAX_DISCOVERED_STATES,
                    (expanded, discovered) -> {
                    });
            long elapsedMillis = Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
            output.printf(Locale.ROOT, "%s\t%s\t%d\t%d\t%d\t%d%n",
                    difficulty.name(), result.status(), result.moves().size(),
                    result.expandedStates(), result.discoveredStates(), elapsedMillis);
        }
    }
}
