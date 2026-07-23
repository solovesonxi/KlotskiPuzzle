package util;

import javax.swing.ImageIcon;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/** Resolves bundled resources both from the classpath/JAR and a source checkout. */
public final class AppResources {
    private static ResourceBundle messageBundle =
        ResourceBundle.getBundle("resources/messages", Locale.SIMPLIFIED_CHINESE);
    
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
    
    public static void setLocale(Locale locale) {
        messageBundle = ResourceBundle.getBundle("resources/messages", locale);
    }

    public static String get(String key) {
        return messageBundle.getString(key);
    }

    // For strings with placeholders like {0}
    public static String get(String key, Object... args) {
        return MessageFormat.format(messageBundle.getString(key), args);
    }
}
