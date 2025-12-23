package net.nextfur.fwc.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FwAudioEngine {
    private static final Map<String, SoundRunner> activeSounds = new ConcurrentHashMap<>();

    public static void play(String urlString, float volume, int radius, boolean loop) {
        stop(urlString);

        SoundRunner runner = new SoundRunner(urlString, volume, loop);
        activeSounds.put(urlString, runner);
        runner.start();
    }

    public static void stop(String urlString) {
        if (activeSounds.containsKey(urlString)) {
            SoundRunner runner = activeSounds.get(urlString);
            runner.terminate();
            activeSounds.remove(urlString);
        }
    }

    public static void setPaused(String urlString, boolean paused) {
        if (activeSounds.containsKey(urlString)) {
            SoundRunner runner = activeSounds.get(urlString);
            runner.setPaused(paused);
        }
    }

    private static class SoundRunner extends Thread {
        private final String urlString;
        private final boolean loop;
        private float volume;

        private volatile boolean running = true;
        private volatile boolean paused = false;
        private SourceDataLine line;

        public SoundRunner(String url, float volume, boolean loop) {
            this.urlString = url;
            this.volume = Math.max(0.0f, Math.min(1.0f, volume)); // Clamp 0-1
            this.loop = loop;
            this.setDaemon(true);
            this.setName("FwAudioStream-" + url.hashCode());
        }

        @Override
        public void run() {
            try {
                do {
                    playStream();
                } while (loop && running);
            } catch (Exception e) {
                Minecraft.getInstance().execute(() ->
                        Minecraft.getInstance().player.displayClientMessage(Component.literal("Erro no audio: " + e.getMessage()), false)
                );
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        private void playStream() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
            URL url = new URL(urlString);

            try (AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()))) {

                AudioFormat format = in.getFormat();
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    throw new LineUnavailableException("Formato de audio nao suportado: " + format);
                }

                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);

                applyVolume(this.volume);

                line.start();

                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                while (running && (bytesRead = in.read(buffer)) != -1) {
                    synchronized (this) {
                        while (paused && running) {
                            line.stop();
                            wait();
                            if(running) line.start();
                        }
                    }

                    if (!running) break;

                    line.write(buffer, 0, bytesRead);
                }

                line.drain();
                line.stop();
            } catch (InterruptedException e) {
                running = false;
            }
        }

        public void setPaused(boolean paused) {
            synchronized (this) {
                this.paused = paused;
                notifyAll();
            }
        }

        public void terminate() {
            running = false;
            setPaused(false);
            if (line != null && line.isOpen()) {
                line.close();
            }
        }

        private void applyVolume(float vol) {
            if (line != null && line.isOpen() && line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

                float dB = (float) (Math.log10(vol > 0 ? vol : 0.0001f) * 20.0f);
                gainControl.setValue(Math.max(dB, -80.0f));
            }
        }

        private void cleanup() {
            activeSounds.remove(urlString);
            if (line != null) {
                line.close();
            }
        }
    }
}
