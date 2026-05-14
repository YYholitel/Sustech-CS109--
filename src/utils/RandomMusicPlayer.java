package utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Plays random music files from a directory in a loop.
 *
 * <p>It attempts to play the selected file with Java Sound. Files unsupported by the
 * current runtime (for example mp3 on plain JDK) are skipped automatically.
 */
public class RandomMusicPlayer implements AutoCloseable {
    private static final String[] SUPPORTED_EXTENSIONS =
            new String[]{".wav", ".aif", ".aiff", ".au", ".mp3"};

    private final Path musicDir;
    private final Random random = new Random();
    private volatile boolean running;
    private Thread workerThread;
    private Clip currentClip;

    public RandomMusicPlayer(Path musicDir) {
        this.musicDir = musicDir;
    }

    public static RandomMusicPlayer fromDefaultMusicDir() {
        return new RandomMusicPlayer(Paths.get("resource", "music"));
    }

    public synchronized void startLoop() {
        if (running) {
            return;
        }
        running = true;
        workerThread = new Thread(this::runLoop, "random-music-player");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public synchronized void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        stopCurrentClip();
    }

    @Override
    public void close() {
        stop();
    }

    private void runLoop() {
        while (running) {
            List<Path> candidates;
            try {
                candidates = listMusicFiles();
            } catch (IOException e) {
                System.err.println("Failed to read music directory: " + musicDir + ", " + e.getMessage());
                return;
            }

            if (candidates.isEmpty()) {
                System.err.println("No playable music files found in: " + musicDir);
                return;
            }

            Path selected = candidates.get(random.nextInt(candidates.size()));
            playOneTrack(selected);
        }
    }

    private List<Path> listMusicFiles() throws IOException {
        List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(musicDir)) {
            return files;
        }

        try (var stream = Files.list(musicDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(this::hasAllowedExtension)
                    .forEach(files::add);
        }
        return files;
    }

    private boolean hasAllowedExtension(Path file) {
        String lower = file.getFileName().toString().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private void playOneTrack(Path file) {
        CountDownLatch finished = new CountDownLatch(1);

        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile())) {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                    finished.countDown();
                }
            });
            clip.open(inputStream);

            synchronized (this) {
                currentClip = clip;
            }

            clip.start();

            while (running) {
                if (finished.await(250, TimeUnit.MILLISECONDS)) {
                    break;
                }
            }

            clip.stop();
            clip.close();
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file, skipped: " + file.getFileName());
        } catch (Exception e) {
            if (running) {
                System.err.println("Failed to play: " + file.getFileName() + ", " + e.getMessage());
            }
        } finally {
            synchronized (this) {
                currentClip = null;
            }
        }
    }

    private synchronized void stopCurrentClip() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }
}
