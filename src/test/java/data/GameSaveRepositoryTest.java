package data;

import model.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSaveRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void roundTripsValidatedHistory() throws Exception {
        GameSaveRepository repository = new GameSaveRepository(temporaryDirectory);
        int[][] board = Difficulty.BEGINNER.initialBoard();
        repository.save("tester", new GameSaveRepository.SavedGame(
                1, 123, List.of(board, board), 0));

        GameSaveRepository.SavedGame loaded = repository.load("tester");

        assertEquals(1, loaded.steps());
        assertEquals(123, loaded.remainingSeconds());
        assertEquals(0, loaded.recoveredEntries());
        assertArrayEquals(board, loaded.currentBoard());
    }

    @Test
    void recoversFromTheEncodedCopyWhenReadableHistoryIsDamaged() throws Exception {
        GameSaveRepository repository = new GameSaveRepository(temporaryDirectory);
        int[][] board = Difficulty.EXPERT.initialBoard();
        repository.save("tester", new GameSaveRepository.SavedGame(
                0, 180, List.<int[][]>of(board), 0));
        Path file = temporaryDirectory.resolve("tester.txt");
        List<String> lines = Files.readAllLines(file);
        String encoded = lines.get(1).split("\\s+")[1];
        Files.write(file, List.of(lines.getFirst(), "damaged " + encoded));

        GameSaveRepository.SavedGame loaded = repository.load("tester");

        assertEquals(1, loaded.recoveredEntries());
        assertArrayEquals(board, loaded.currentBoard());
    }

    @Test
    void quarantinesAnUnrecoverableSave() throws Exception {
        GameSaveRepository repository = new GameSaveRepository(temporaryDirectory);
        Path file = temporaryDirectory.resolve("tester.txt");
        Files.write(file, List.of("0 180", "damaged not-base64"));

        GameSaveRepository.CorruptSaveException exception = assertThrows(
                GameSaveRepository.CorruptSaveException.class,
                () -> repository.load("tester"));

        assertFalse(Files.exists(file));
        assertTrue(Files.exists(exception.backupPath()));
    }

    @Test
    void rejectsInvalidUsernames() {
        GameSaveRepository repository = new GameSaveRepository(temporaryDirectory);
        int[][] board = Difficulty.BEGINNER.initialBoard();

        assertThrows(IllegalArgumentException.class, () -> repository.save(
                "../escape", new GameSaveRepository.SavedGame(
                        0, 180, List.<int[][]>of(board), 0)));
    }
}
