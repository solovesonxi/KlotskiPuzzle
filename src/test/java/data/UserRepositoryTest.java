package data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void registersAndVerifiesHashedPassword() throws Exception {
        Path usersFile = temporaryDirectory.resolve("users.properties");
        UserRepository repository = new UserRepository(usersFile);

        assertTrue(repository.register("玩家1", "secret22".toCharArray()));
        assertTrue(repository.verify("玩家1", "secret22".toCharArray()));
        assertFalse(repository.verify("玩家1", "wrong".toCharArray()));
        assertFalse(repository.register("玩家1", "another".toCharArray()));

        String stored = java.nio.file.Files.readString(usersFile);
        assertFalse(stored.contains("secret22"));
        assertTrue(stored.contains("pbkdf2-sha256"));
    }
}
