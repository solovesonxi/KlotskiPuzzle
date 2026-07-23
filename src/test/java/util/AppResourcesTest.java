package util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppResourcesTest {
    @Test
    void loadsBundledResourceFromTheTestClasspath() {
        assertNotNull(AppResources.url("resources/original/image/icons/play.png"));
    }

    @Test
    void reportsMissingResourceClearly() {
        assertThrows(IllegalArgumentException.class,
                () -> AppResources.url("resources/does-not-exist.bin"));
    }

    @Test
    void optimizedLoginBackgroundRemainsAnimated() throws Exception {
        try (InputStream input = AppResources.url("resources/original/image/login-background.gif").openStream();
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            assertTrue(readers.hasNext());
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                assertTrue(reader.getNumImages(true) > 1, "登录背景必须保留动画帧");
            } finally {
                reader.dispose();
            }
        }
    }

    @Test
    void opensBundledMusicTracks() throws Exception {
        String[] tracks = {
                "resources/original/audio/music/dawn-path.wav",
                "resources/original/audio/music/woodland-steps.wav",
                "resources/original/audio/music/quiet-strategy.wav",
                "resources/original/audio/music/open-gate.wav",
                "resources/original/audio/sound-effect/move.wav",
                "resources/original/audio/sound-effect/select.wav",
                "resources/original/audio/sound-effect/invalid.wav",
                "resources/original/audio/sound-effect/undo.wav",
                "resources/original/audio/sound-effect/victory.wav",
                "resources/original/audio/sound-effect/defeat.wav"
        };
        for (String track : tracks) {
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(AppResources.url(track))) {
                assertNotNull(stream.getFormat());
            }
        }
    }
}
