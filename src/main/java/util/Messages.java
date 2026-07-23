package util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/** Runtime messages with English and Simplified Chinese bundles. */
public final class Messages {
    static final String BASE_NAME = "resources.i18n.messages";

    private static volatile Locale locale = normalize(Locale.getDefault());
    private static volatile ResourceBundle bundle = load(locale);

    private Messages() {
    }

    public static void configure(String... arguments) {
        if (arguments == null) {
            return;
        }
        for (String argument : arguments) {
            Locale requested = parseLanguageArgument(argument);
            if (requested != null) {
                useLocale(requested);
                return;
            }
        }
    }

    public static void useLocale(Locale requested) {
        locale = normalize(Objects.requireNonNull(requested, "requested"));
        bundle = load(locale);
    }

    public static Locale locale() {
        return locale;
    }

    public static String text(String key, Object... arguments) {
        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException exception) {
            throw new IllegalArgumentException("Missing message key: " + key, exception);
        }
        if (arguments == null || arguments.length == 0) {
            return pattern;
        }
        return new MessageFormat(pattern, locale).format(arguments);
    }

    static ResourceBundle load(Locale requested) {
        return ResourceBundle.getBundle(BASE_NAME, requested);
    }

    static Locale parseLanguageArgument(String argument) {
        if (argument == null || argument.isBlank()) {
            return null;
        }
        String value = argument.startsWith("--lang=")
                ? argument.substring("--lang=".length())
                : argument;
        return switch (value.toLowerCase(Locale.ROOT).replace('_', '-')) {
            case "en", "en-us", "en-gb" -> Locale.ENGLISH;
            case "zh", "zh-cn", "zh-hans" -> Locale.SIMPLIFIED_CHINESE;
            default -> null;
        };
    }

    private static Locale normalize(Locale requested) {
        return "zh".equalsIgnoreCase(requested.getLanguage())
                ? Locale.SIMPLIFIED_CHINESE
                : Locale.ENGLISH;
    }
}
