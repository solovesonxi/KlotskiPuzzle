package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {
    @AfterEach
    void restoreEnglish() {
        Messages.useLocale(Locale.ENGLISH);
    }

    @Test
    void loadsEnglishAndChineseWithMatchingKeys() {
        ResourceBundle english = Messages.load(Locale.ENGLISH);
        ResourceBundle chinese = Messages.load(Locale.SIMPLIFIED_CHINESE);

        assertEquals(new HashSet<>(english.keySet()), new HashSet<>(chinese.keySet()));
        assertNotEquals(english.getString("login.sign.in"), chinese.getString("login.sign.in"));
    }

    @Test
    void configuresSupportedCommandLineLanguages() {
        Messages.configure("--lang=zh-CN");
        assertEquals(Locale.SIMPLIFIED_CHINESE, Messages.locale());
        assertEquals("登录", Messages.text("login.sign.in"));

        Messages.configure("en");
        assertEquals(Locale.ENGLISH, Messages.locale());
        assertEquals("Sign in", Messages.text("login.sign.in"));
    }

    @Test
    void formatsArgumentsAndNewlines() {
        Messages.useLocale(Locale.ENGLISH);
        assertEquals("Moves: 12", Messages.text("status.steps", 12));
        assertTrue(Messages.text("login.credentials.syntax").contains("\n"));
    }
}
