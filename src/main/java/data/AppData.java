package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves mutable player data outside the source tree and packaged resources. */
public final class AppData {
    private static final String DIRECTORY_NAME = ".klotski-puzzle";

    private AppData() {
    }

    public static Path root() throws IOException {
        Path root = Path.of(System.getProperty("user.home"), DIRECTORY_NAME);
        return Files.createDirectories(root);
    }

    public static Path usersFile() throws IOException {
        return root().resolve("users.properties");
    }

    public static Path leaderboardFile() throws IOException {
        return root().resolve("leaderboard.txt");
    }

    public static Path historyFile(String username) throws IOException {
        if (username == null || !username.matches("^[a-zA-Z0-9一-龥]+$")) {
            throw new IllegalArgumentException("Invalid username");
        }
        return Files.createDirectories(root().resolve("history")).resolve(username + ".txt");
    }
}
