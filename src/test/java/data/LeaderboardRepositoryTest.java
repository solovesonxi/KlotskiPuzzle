package data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void skipsMalformedEntries() throws Exception {
        Path file = temporaryDirectory.resolve("leaderboard.txt");
        Files.write(file, List.of(
                "alice 12 100",
                "broken entry",
                "bob not-a-number 90",
                "carol 10 999"
        ));

        List<LeaderboardRepository.ScoreEntry> scores = new LeaderboardRepository(file).load();

        assertEquals(1, scores.size());
        assertEquals("alice", scores.getFirst().user());
    }

    @Test
    void keepsFasterScoreFirstWhenStepCountsMatch() throws Exception {
        LeaderboardRepository repository = new LeaderboardRepository(
                temporaryDirectory.resolve("leaderboard.txt"));

        repository.add(new LeaderboardRepository.ScoreEntry("slower", 20, 60));
        repository.add(new LeaderboardRepository.ScoreEntry("faster", 20, 120));

        List<LeaderboardRepository.ScoreEntry> scores = repository.load();
        assertEquals("faster", scores.getFirst().user());
        assertEquals("slower", scores.getLast().user());
    }
}
