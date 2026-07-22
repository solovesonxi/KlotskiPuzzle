package util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

/** Serializes background-music operations and owns the active audio clip. */
public final class BackgroundMusicPlayer implements AutoCloseable {
    private final List<URL> tracks;
    private final ExecutorService executor;
    private final Consumer<Boolean> playbackListener;
    private final Consumer<String> errorListener;
    private volatile boolean closed;
    private Clip currentClip;
    private int currentTrackIndex;
    private boolean paused;
    private long generation;

    public BackgroundMusicPlayer(List<URL> tracks,
                                 Consumer<Boolean> playbackListener,
                                 Consumer<String> errorListener) {
        this.tracks = List.copyOf(tracks);
        this.playbackListener = playbackListener;
        this.errorListener = errorListener;
        this.executor = Executors.newSingleThreadExecutor(
                Thread.ofPlatform().daemon(true).name("klotski-bgm").factory());
    }

    public void start() {
        enqueue(() -> {
            if (!tracks.isEmpty()) {
                playCurrentTrack();
            }
        });
    }

    public void toggle() {
        enqueue(() -> {
            if (currentClip == null) {
                if (!tracks.isEmpty()) {
                    playCurrentTrack();
                }
                return;
            }
            if (currentClip.isRunning()) {
                paused = true;
                currentClip.stop();
                notifyPlayback(false);
            } else {
                paused = false;
                currentClip.start();
                notifyPlayback(true);
            }
        });
    }

    public void skip(boolean next) {
        enqueue(() -> {
            if (tracks.isEmpty()) {
                return;
            }
            int offset = next ? 1 : -1;
            currentTrackIndex = Math.floorMod(currentTrackIndex + offset, tracks.size());
            paused = false;
            playCurrentTrack();
        });
    }

    private void playCurrentTrack() {
        long token = ++generation;
        closeCurrentClip();
        if (closed) {
            return;
        }

        Clip openedClip = null;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(tracks.get(currentTrackIndex))) {
            openedClip = AudioSystem.getClip();
            Clip clipForListener = openedClip;
            openedClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    handleStopEvent(clipForListener, token);
                }
            });
            openedClip.open(stream);
            if (openedClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volume = (FloatControl) openedClip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Math.max(volume.getMinimum(), Math.min(-10.0f, volume.getMaximum())));
            }
            if (closed || token != generation) {
                openedClip.close();
                return;
            }
            currentClip = openedClip;
            paused = false;
            openedClip.start();
            notifyPlayback(true);
        } catch (Exception exception) {
            if (openedClip != null) {
                openedClip.close();
            }
            currentClip = null;
            notifyPlayback(false);
            notifyError("播放背景音乐失败: " + exception.getMessage());
        }
    }

    private void handleStopEvent(Clip stoppedClip, long token) {
        try {
            executor.execute(() -> {
                if (closed || paused || token != generation || stoppedClip != currentClip) {
                    return;
                }
                if (stoppedClip.getFramePosition() >= stoppedClip.getFrameLength()) {
                    currentTrackIndex = (currentTrackIndex + 1) % tracks.size();
                    playCurrentTrack();
                }
            });
        } catch (RejectedExecutionException ignored) {
            // The application is shutting down.
        }
    }

    private void closeCurrentClip() {
        Clip clipToClose = currentClip;
        currentClip = null;
        if (clipToClose != null) {
            clipToClose.stop();
            clipToClose.close();
        }
    }

    private void enqueue(Runnable operation) {
        if (closed) {
            return;
        }
        try {
            executor.execute(() -> {
                if (!closed) {
                    operation.run();
                }
            });
        } catch (RejectedExecutionException ignored) {
            // The application is shutting down.
        }
    }

    private void notifyPlayback(boolean playing) {
        if (playbackListener != null) {
            playbackListener.accept(playing);
        }
    }

    private void notifyError(String message) {
        if (errorListener != null) {
            errorListener.accept(message);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            executor.execute(() -> {
                generation++;
                closeCurrentClip();
            });
        } catch (RejectedExecutionException ignored) {
            closeCurrentClip();
        } finally {
            executor.shutdown();
        }
    }
}
