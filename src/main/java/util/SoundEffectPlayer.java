package util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.FloatControl;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

import static util.Messages.text;

/** Plays short effects away from the Swing event-dispatch thread. */
public final class SoundEffectPlayer implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Set<Clip> activeClips = ConcurrentHashMap.newKeySet();
    private final Consumer<String> errorListener;
    private volatile boolean closed;

    public SoundEffectPlayer(Consumer<String> errorListener) {
        this.errorListener = errorListener;
    }

    public void play(String resourcePath) {
        if (closed) {
            return;
        }
        try {
            executor.execute(() -> openAndPlay(resourcePath));
        } catch (RejectedExecutionException ignored) {
            // The owning window is shutting down.
        }
    }

    private void openAndPlay(String resourcePath) {
        Clip clip = null;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(AppResources.url(resourcePath))) {
            clip = AudioSystem.getClip();
            Clip openedClip = clip;
            openedClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP
                        && openedClip.getFramePosition() >= openedClip.getFrameLength()) {
                    activeClips.remove(openedClip);
                    openedClip.close();
                }
            });
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Math.max(volume.getMinimum(), Math.min(-5.0f, volume.getMaximum())));
            }
            if (closed) {
                clip.close();
                return;
            }
            activeClips.add(clip);
            clip.start();
        } catch (Exception exception) {
            if (clip != null) {
                activeClips.remove(clip);
                clip.close();
            }
            if (!closed && errorListener != null) {
                errorListener.accept(text("sound.error", exception.getMessage()));
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdownNow();
        for (Clip clip : activeClips) {
            clip.stop();
            clip.close();
        }
        activeClips.clear();
    }
}
