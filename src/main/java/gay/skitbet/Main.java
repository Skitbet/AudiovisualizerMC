package gay.skitbet;

import gay.skitbet.command.VisualizeCommand;
import gay.skitbet.utli.Messagener;
import gay.skitbet.visualizer.AudioCapture;
import gay.skitbet.visualizer.AudioVisualizer;
import gay.skitbet.world.WorldGenerator;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws LineUnavailableException {
        MinecraftServer server = MinecraftServer.init();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        WorldGenerator.setupWorld(instanceContainer);
        instanceContainer.setChunkSupplier(LightingChunk::new);

        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 40, 0));
        });

        eventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
           final Player player = event.getPlayer();
            Audiences.all().sendMessage(Component.text(player.getUsername() + " has joined!", NamedTextColor.GREEN));
        });

        AudioCapture audioCapture = new AudioCapture();
        AudioVisualizer audioVisualizer = new AudioVisualizer(instanceContainer);

        eventHandler.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            final Player player = event.getPlayer();
            Messagener.info(player, "Welcome to Skity Server, use /help for help!");
            player.setGameMode(GameMode.CREATIVE);
            player.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 1f, 1f));
            player.setEnableRespawnScreen(false);
        });

        AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
        eventHandler.addListener(ServerTickMonitorEvent.class, event -> {
            final TickMonitor monitor = event.getTickMonitor();
            lastTick.set(monitor);
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty()) return;

            final Runtime runtime = Runtime.getRuntime();
            final TickMonitor tickMonitor = lastTick.get();
            final long ramUsage = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;

            final Component header = Component.newline()
                    .append(Component.text("Skitty Land", Messagener.BLUE_COLOR))
                    .append(Component.newline()).append(Component.text("Players: " + players.size()))
                    .append(Component.newline()).append(Component.newline())
                    .append(Component.text("RAM USAGE: " + ramUsage + " MB", NamedTextColor.GRAY).append(Component.newline())
                            .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms", NamedTextColor.GRAY))).append(Component.newline());
            final Component footer = Component.newline().append(Component.text("Project: Screw Mojang").append(Component.newline())
                            .append(Component.text("Website: skitbet.github.io", Messagener.ORANGE_COLOR)))
                    .append(Component.newline());

            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);

        }, TaskSchedule.tick(10), TaskSchedule.tick(10));

        VisualizeCommand visualizeCommand = new VisualizeCommand(audioCapture, audioVisualizer);
        MinecraftServer.getCommandManager().register(visualizeCommand);

        MojangAuth.init();
        server.start("0.0.0.0", 25565);

        System.out.println("Server ready.");

    }
}