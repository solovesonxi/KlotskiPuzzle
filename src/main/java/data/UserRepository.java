package data;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/** Stores local player credentials as salted PBKDF2 hashes. */
public final class UserRepository {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private final Path file;
    private final SecureRandom random = new SecureRandom();

    public UserRepository() throws IOException {
        this(AppData.usersFile());
    }

    UserRepository(Path file) {
        this.file = file;
    }

    public synchronized boolean exists(String username) throws IOException {
        return load().containsKey(username);
    }

    public synchronized boolean verify(String username, char[] password) throws IOException {
        String encoded = load().getProperty(username);
        if (encoded == null) {
            return false;
        }
        String[] parts = encoded.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            return false;
        }
    }

    public synchronized boolean register(String username, char[] password) throws IOException {
        Properties users = load();
        if (users.containsKey(username)) {
            return false;
        }
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        try {
            byte[] hash = derive(password, salt, ITERATIONS);
            users.setProperty(username, "pbkdf2-sha256$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash));
            save(users);
            return true;
        } catch (GeneralSecurityException exception) {
            throw new IOException("Cannot hash password", exception);
        }
    }

    private Properties load() throws IOException {
        Properties users = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                users.load(reader);
            }
        }
        return users;
    }

    private void save(Properties users) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            users.store(writer, "KlotskiPuzzle local players - passwords are salted PBKDF2 hashes");
        }
        try {
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException atomicMoveUnsupported) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
