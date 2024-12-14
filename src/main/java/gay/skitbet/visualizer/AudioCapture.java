package gay.skitbet.visualizer;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AudioCapture {
    private AudioDispatcher dispatcher;

    public void start(BiConsumer<float[], float[]> frequencyCallback) throws LineUnavailableException {
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

        Mixer.Info vbCable = null;
        for (Mixer.Info info : mixerInfo) {
            if (info.getName().contains("CABLE Output")) { // Adjust this if needed
                vbCable = info;
                break;
            }
        }

        if (vbCable == null) {
            System.out.println("VB-Audio Cable not found!");
            return;
        }

        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(1024, 0);

        if (dispatcher == null) {
            System.out.println("Could not create audio dispatcher!");
            return;
        }

        dispatcher.addAudioProcessor(new be.tarsos.dsp.AudioProcessor() {
            private final FFT fft = new FFT(1024);

            @Override
            public boolean process(be.tarsos.dsp.AudioEvent audioEvent) {
                float[] spectrum = audioEvent.getFloatBuffer();
                float[] amplitudes = new float[fft.size() / 2];

                // Filter out values that are too small
                float threshold = 0.001f;
                for (int i = 0; i < amplitudes.length; i++) {
                    if (amplitudes[i] < threshold) {
                        amplitudes[i] = 0;  // Consider it silence
                    }
                }

                frequencyCallback.accept(spectrum, amplitudes);
                return true;
            }

            @Override
            public void processingFinished() {
                System.out.println("Audio processing finished.");
            }
        });

//        dispatcher.addAudioProcessor(new AudioPlayer(format));

        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    public void stop() {
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }
}
