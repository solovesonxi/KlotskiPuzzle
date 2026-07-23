package data;

import model.BoardRules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/** Reads and atomically writes local game snapshots without depending on Swing. */
public final class GameSaveRepository {
    private final Path historyDirectory;

    public GameSaveRepository() {
        this.historyDirectory = null;
    }

    GameSaveRepository(Path historyDirectory) {
        this.historyDirectory = historyDirectory;
    }

    public record SavedGame(int steps, int remainingSeconds, List<int[][]> history,
                            int recoveredEntries) {
        public SavedGame {
            if (steps < 0 || remainingSeconds < 0 || recoveredEntries < 0) {
                throw new IllegalArgumentException("Save counters must not be negative");
            }
            if (history == null || history.isEmpty() || history.size() != steps + 1) {
                throw new IllegalArgumentException("History must contain the initial board and every move");
            }
            List<int[][]> copied = new ArrayList<>(history.size());
            for (int[][] board : history) {
                BoardRules.validateGameBoard(board);
                copied.add(BoardRules.copy(board));
            }
            history = List.copyOf(copied);
        }

        @Override
        public List<int[][]> history() {
            return history.stream().map(BoardRules::copy).toList();
        }

        public int[][] currentBoard() {
            return BoardRules.copy(history.getLast());
        }
    }

    public static final class CorruptSaveException extends IOException {
        public enum Reason {
            EMPTY,
            INVALID
        }

        private final Reason reason;
        private final String detail;
        private final Path backupPath;

        CorruptSaveException(Reason reason, String detail, Path backupPath) {
            super(reason + (detail == null ? "" : ": " + detail));
            this.reason = reason;
            this.detail = detail;
            this.backupPath = backupPath;
        }

        public Reason reason() {
            return reason;
        }

        public String detail() {
            return detail;
        }

        public Path backupPath() {
            return backupPath;
        }
    }

    public boolean exists(String username) throws IOException {
        return Files.exists(fileFor(username));
    }

    public SavedGame load(String username) throws IOException {
        Path path = fileFor(username);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || lines.getFirst().isBlank()) {
            throw quarantine(path, CorruptSaveException.Reason.EMPTY, null);
        }

        try {
            String[] header = lines.getFirst().trim().split("\\s+");
            if (header.length != 2) {
                throw new IllegalArgumentException("Invalid save header");
            }
            int steps = Integer.parseInt(header[0]);
            int remainingSeconds = Integer.parseInt(header[1]);
            if (steps < 0 || remainingSeconds < 0) {
                throw new IllegalArgumentException("Negative save counters");
            }

            List<int[][]> history = new ArrayList<>();
            int recoveredEntries = 0;
            for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
                String[] parts = lines.get(lineIndex).trim().split("\\s+");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid history entry at line " + (lineIndex + 1));
                }
                int[][] readableBoard = parseMatrix(parts[0]);
                int[][] encodedBoard = decode(parts[1]);
                if (readableBoard != null && Arrays.deepEquals(readableBoard, encodedBoard)) {
                    history.add(readableBoard);
                } else if (encodedBoard != null) {
                    history.add(encodedBoard);
                    recoveredEntries++;
                } else {
                    throw new IllegalArgumentException("Unrecoverable history entry at line " + (lineIndex + 1));
                }
            }
            return new SavedGame(steps, remainingSeconds, history, recoveredEntries);
        } catch (IllegalArgumentException exception) {
            throw quarantine(path, CorruptSaveException.Reason.INVALID, exception.getMessage());
        }
    }

    public void save(String username, SavedGame game) throws IOException {
        Path file = fileFor(username);
        StringBuilder content = new StringBuilder()
                .append(game.steps()).append(' ')
                .append(game.remainingSeconds()).append('\n');
        for (int[][] board : game.history()) {
            content.append(serializeMatrix(board)).append(' ')
                    .append(encode(board)).append('\n');
        }

        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveUnsupported) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static String serializeMatrix(int[][] matrix) {
        BoardRules.validateGameBoard(matrix);
        StringBuilder serialized = new StringBuilder();
        for (int[] row : matrix) {
            for (int cell : row) {
                serialized.append(cell).append(',');
            }
            serialized.append(';');
        }
        return serialized.toString();
    }

    static int[][] parseMatrix(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return null;
        }
        try {
            String[] rows = serialized.split(";");
            int[][] matrix = new int[rows.length][];
            for (int row = 0; row < rows.length; row++) {
                String[] columns = rows[row].split(",");
                matrix[row] = new int[columns.length];
                for (int column = 0; column < columns.length; column++) {
                    matrix[row][column] = Integer.parseInt(columns[column].trim());
                }
            }
            BoardRules.validateGameBoard(matrix);
            return matrix;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static String encode(int[][] matrix) throws IOException {
        BoardRules.validateGameBoard(matrix);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(matrix.length);
            output.writeInt(matrix[0].length);
            for (int[] row : matrix) {
                for (int cell : row) {
                    output.writeInt(cell);
                }
            }
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    static int[][] decode(String token) {
        try {
            byte[] bytes = Base64.getDecoder().decode(token);
            try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
                int rows = input.readInt();
                int columns = input.readInt();
                if (rows != BoardRules.GAME_ROWS || columns != BoardRules.GAME_COLUMNS) {
                    return null;
                }
                int[][] matrix = new int[rows][columns];
                for (int row = 0; row < rows; row++) {
                    for (int column = 0; column < columns; column++) {
                        matrix[row][column] = input.readInt();
                    }
                }
                if (input.available() != 0) {
                    return null;
                }
                BoardRules.validateGameBoard(matrix);
                return matrix;
            }
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static CorruptSaveException quarantine(Path path, CorruptSaveException.Reason reason,
                                                    String detail) throws IOException {
        Path backup = path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis());
        Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
        return new CorruptSaveException(reason, detail, backup);
    }

    private Path fileFor(String username) throws IOException {
        if (historyDirectory == null) {
            return AppData.historyFile(username);
        }
        if (username == null || !username.matches("^[a-zA-Z0-9一-龥]+$")) {
            throw new IllegalArgumentException("Invalid username");
        }
        return Files.createDirectories(historyDirectory).resolve(username + ".txt");
    }
}
