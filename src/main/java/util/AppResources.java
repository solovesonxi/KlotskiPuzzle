package util;

import javax.swing.ImageIcon;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves bundled resources both from the classpath/JAR and a source checkout. */
public final class AppResources {
    private AppResources() {
    }

    public static URL url(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        URL bundled = AppResources.class.getClassLoader().getResource(normalized);
        if (bundled != null) {
            return bundled;
        }

        Path localFile = Path.of(path).toAbsolutePath().normalize();
        if (Files.isRegularFile(localFile)) {
            try {
                return localFile.toUri().toURL();
            } catch (MalformedURLException exception) {
                throw new IllegalArgumentException("Invalid resource path: " + path, exception);
            }
        }
        throw new IllegalArgumentException("Resource not found: " + path);
    }

    public static ImageIcon icon(String path) {
        return new ImageIcon(url(path));
    }
}
