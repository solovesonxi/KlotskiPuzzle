package data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Reads and writes the local challenge leaderboard. */
public final class LeaderboardRepository {
    private static final int MAX_ENTRIES = 100;
    private static final int CHALLENGE_SECONDS = 180;

    private static final Comparator<ScoreEntry> BY_STEPS = Comparator
            .comparingInt(ScoreEntry::steps)
            .thenComparing(Comparator.comparingInt(ScoreEntry::remainingTime).reversed());

    private static final Comparator<ScoreEntry> BY_ELAPSED_TIME = Comparator
            .comparingInt(ScoreEntry::remainingTime).reversed()
            .thenComparingInt(ScoreEntry::steps);

    private final Path file;

    public LeaderboardRepository() throws IOException {
        this(AppData.leaderboardFile());
    }

    LeaderboardRepository(Path file) {
        this.file = file;
    }

    public synchronized List<ScoreEntry> load() throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        List<ScoreEntry> scores = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            ScoreEntry score = parse(line);
            if (score != null) {
                scores.add(score);
            }
        }
        return scores;
    }

    public synchronized void add(ScoreEntry newScore) throws IOException {
        List<ScoreEntry> scores = load();
        scores.add(newScore);
        scores.sort(BY_STEPS);
        if (scores.size() > MAX_ENTRIES) {
            scores = new ArrayList<>(scores.subList(0, MAX_ENTRIES));
        }
        save(scores);
    }

    public static Comparator<ScoreEntry> bySteps() {
        return BY_STEPS;
    }

    public static Comparator<ScoreEntry> byElapsedTime() {
        return BY_ELAPSED_TIME;
    }

    private ScoreEntry parse(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new ScoreEntry(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void save(List<ScoreEntry> scores) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        List<String> lines = scores.stream()
                .map(score -> score.user() + " " + score.steps() + " " + score.remainingTime())
                .toList();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveUnsupported) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public record ScoreEntry(String user, int steps, int remainingTime) {
        public ScoreEntry {
            if (user == null || !user.matches("^[a-zA-Z0-9一-龥]+$")) {
                throw new IllegalArgumentException("Invalid username");
            }
            if (steps < 0 || remainingTime < 0 || remainingTime > CHALLENGE_SECONDS) {
                throw new IllegalArgumentException("Invalid score");
            }
        }
    }
}
