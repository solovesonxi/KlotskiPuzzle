package view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import util.Messages;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageToggleButtonTest {
    @AfterEach
    void restoreEnglishDefault() {
        Messages.useLocale(Locale.ENGLISH);
    }

    @Test
    void clickSwitchesTheLocaleAndRefreshesTheButton() {
        Messages.useLocale(Locale.SIMPLIFIED_CHINESE);
        AtomicInteger refreshes = new AtomicInteger();
        LanguageToggleButton button = new LanguageToggleButton(refreshes::incrementAndGet);

        button.doClick();

        assertEquals(Locale.ENGLISH, Messages.locale());
        assertEquals("中文", button.getText());
        assertEquals(1, refreshes.get());

        button.doClick();

        assertEquals(Locale.SIMPLIFIED_CHINESE, Messages.locale());
        assertEquals("EN", button.getText());
        assertEquals(2, refreshes.get());
    }
}
