package gay.skitbet.command;

import gay.skitbet.visualizer.AudioCapture;
import gay.skitbet.visualizer.AudioVisualizer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class VisualizeCommand extends Command {
    private final AudioCapture audioCapture;
    private final AudioVisualizer audioVisualizer;

    public VisualizeCommand(AudioCapture audioCapture, AudioVisualizer audioVisualizer) {
        super("visualize");

        this.audioCapture = audioCapture;
        this.audioVisualizer = audioVisualizer;

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("Loading dem bars!");
                startVisualization(player);
            }
        });
    }

    private void startVisualization(Player player) {
        try {
            audioVisualizer.createVisualizers();
            audioCapture.start(audioVisualizer::update);
            player.sendMessage("Started dem bars!");
        } catch (LineUnavailableException e) {
            player.sendMessage("Error starting audio visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
