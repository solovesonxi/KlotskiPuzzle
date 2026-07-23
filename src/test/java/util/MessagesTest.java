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
        assertNotEquals(english.getString("start.lab"), chinese.getString("start.lab"));
    }

    @Test
    void configuresSupportedCommandLineLanguages() {
        Messages.configure("--lang=zh-CN");
        assertEquals(Locale.SIMPLIFIED_CHINESE, Messages.locale());
        assertEquals("进入算法实验室", Messages.text("start.lab"));

        Messages.configure("en");
        assertEquals(Locale.ENGLISH, Messages.locale());
        assertEquals("Open Algorithm Lab", Messages.text("start.lab"));
    }

    @Test
    void formatsArgumentsAndNewlines() {
        Messages.useLocale(Locale.ENGLISH);
        assertEquals("Moves: 12", Messages.text("status.steps", 12));
        assertTrue(Messages.text("lab.export.success", "record.json").contains("\n"));
    }
}
