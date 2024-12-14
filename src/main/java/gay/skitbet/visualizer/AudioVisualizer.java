package gay.skitbet.visualizer;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioVisualizer {
    private final List<Entity> bassBars = new ArrayList<>();
    private final List<Entity> midBars = new ArrayList<>();
    private final List<Entity> trebleBars = new ArrayList<>();
    private final List<Entity> freqAmpBars = new ArrayList<>();
    private final Instance instance;

    private static final int NUM_BARS = 50;
    private static final double DEFAULT_Y_POSITION = 100.0;
    private static final double MAX_HEIGHT = 40.0;  // Maximum height for bars
    private static final double SMOOTHING_FACTOR = 0.3;

    private final double[] lastHeightsBass = new double[NUM_BARS];
    private final double[] lastHeightsMid = new double[NUM_BARS];
    private final double[] lastHeightsTreble = new double[NUM_BARS];

    public AudioVisualizer(Instance instance) {
        this.instance = instance;
        Arrays.fill(lastHeightsBass, DEFAULT_Y_POSITION);
        Arrays.fill(lastHeightsMid, DEFAULT_Y_POSITION);
        Arrays.fill(lastHeightsTreble, DEFAULT_Y_POSITION);
    }

    public void createVisualizers() {
        // low frequencies
        for (int i = 0; i < NUM_BARS; i++) {
            Entity entity = createBarEntity(Block.BLUE_WOOL, i * 2, 0);
            bassBars.add(entity);
        }

        // mid frequencies
        for (int i = 0; i < NUM_BARS; i++) {
            Entity entity = createBarEntity(Block.GREEN_WOOL, i * 2, 1); // Offset Z position
            midBars.add(entity);
        }

        // high frequencies
        for (int i = 0; i < NUM_BARS; i++) {
            Entity entity = createBarEntity(Block.RED_WOOL, i * 2, 2); // Offset Z position
            trebleBars.add(entity);
        }
        
        for (int i = 0; i < NUM_BARS; i++) {
            Entity entity = createBarEntity(Block.ORANGE_WOOL, i * 2, 3); // Offset Z position
            freqAmpBars.add(entity);
        }
    }

    private Entity createBarEntity(Block block, double x, double z) {
        Entity entity = new Entity(EntityType.BLOCK_DISPLAY);
        BlockDisplayMeta meta = (BlockDisplayMeta) entity.getEntityMeta();
        meta.setHasNoGravity(true);
        meta.setBlockState(block);
        entity.setInstance(instance);
        entity.teleport(new Pos(x, DEFAULT_Y_POSITION, z));
        return entity;
    }

    public void update(float[] spectrum, float[] freqs) {
        updateFreqBar(freqs);
        int numBars = Math.min(spectrum.length, NUM_BARS);

        // low frequencies
        for (int i = 0; i < numBars; i++) {
            updateBar(bassBars.get(i), lastHeightsBass, spectrum[i], i, "bass");
        }

        // mid frequencies
        for (int i = 0; i < numBars; i++) {
            updateBar(midBars.get(i), lastHeightsMid, spectrum[i], i, "mid");
        }

        // high frequencies
        for (int i = 0; i < numBars; i++) {
            updateBar(trebleBars.get(i), lastHeightsTreble, spectrum[i], i, "treble");
        }
    }

    private void updateFreqBar(float[] freqAmp) {
        float maxAmplitude = findMaxAmplitude(freqAmp);

        float scaleFactor = maxAmplitude > 0 ? 1.0f / maxAmplitude : 1.0f;

        for (int i = 0; i < NUM_BARS; i++) {
            float amplitude = freqAmp[i] * scaleFactor;  // Normalize to [0, 1]

            double newY = DEFAULT_Y_POSITION + Math.min(MAX_HEIGHT, amplitude * MAX_HEIGHT);

            Entity entity = freqAmpBars.get(i);
            double currentY = entity.getPosition().y();

            double smoothedY = currentY + (newY - currentY) * SMOOTHING_FACTOR;

            if (Math.abs(smoothedY - currentY) > 0.01) {
                entity.teleport(new Pos(entity.getPosition().x(), smoothedY, entity.getPosition().z()));
            }
        }
    }

    private float findMaxAmplitude(float[] freqAmp) {
        float maxAmplitude = 0;
        for (float amplitude : freqAmp) {
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude;
            }
        }
        return maxAmplitude == 0 ? 1 : maxAmplitude;  // Prevent division by zero
    }

    private void updateBar(Entity bar, double[] lastHeights, float frequencyAmplitude, int index, String frequencyBand) {
        double barHeight = Math.log(frequencyAmplitude + 1) * 20;
        switch (frequencyBand) {
            case "bass" -> barHeight *= 4.0;  
            case "mid" -> barHeight *= 2.0; 
            case "treble" -> barHeight *= 1.5; 
        }

        barHeight = Math.min(MAX_HEIGHT, barHeight);

        barHeight = lastHeights[index] + SMOOTHING_FACTOR * (barHeight - lastHeights[index]);

        lastHeights[index] = barHeight;

        Pos currentPosition = bar.getPosition();
        bar.teleport(new Pos(currentPosition.x(), DEFAULT_Y_POSITION + barHeight, currentPosition.z()));
    }
}
