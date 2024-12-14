package gay.skitbet.world;

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.biome.Biome;

public class WorldGenerator {

    private static final JNoise biomeNoise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().setInterpolation(Interpolation.LINEAR).build())
            .scale(0.1)  // Adjust scale to control biome transitions
            .octavate(
                    6,
                    0.5,
                    2.0,
                    FractalFunction.FBM,
                    true
            )
            .build();

    private static final JNoise terrainNoise = JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().setInterpolation(Interpolation.LINEAR).build())
            .scale(0.01)
            .octavate(
                    4,
                    0.5,
                    2.0,
                    FractalFunction.FBM,
                    true
            )
            .build();

    private static final DynamicRegistry<Biome> biomeRegistry = Biome.createDefaultRegistry();

    private static DynamicRegistry.Key<Biome> desertKey;
    private static DynamicRegistry.Key<Biome> plainsKey;
    private static DynamicRegistry.Key<Biome> forestKey;

    public static void setupWorld(Instance instance) {
        registerBiomes();

        instance.setGenerator(unit -> {
            Point start = unit.absoluteStart();
            Point size = unit.size();
            for (int x = 0; x < size.blockX(); x++) {
                for (int z = 0; z < size.blockX(); z++) {
                    int worldX = start.blockX() + x;
                    int worldZ = start.blockZ() + z;

                    double noiseHeight = terrainNoise.evaluateNoise(worldX, worldZ);
                    int height = (int) (32 + noiseHeight * 16);

                    for (int y = start.blockY(); y < height; y++) {
                        unit.modifier().setBlock(new Vec(worldX, y, worldZ), Block.STONE);
                    }

                    unit.modifier().setBlock(new Vec(worldX, height, worldZ), Block.GRASS_BLOCK);

                    float noiseBiome = (float) biomeNoise.evaluateNoise(worldX, worldZ);
                    Biome selectedBiome = selectBiome(noiseBiome);

                    setBiomeBlocks(unit, selectedBiome, worldX, worldZ, height);
                }
            }
        });
    }

    private static void registerBiomes() {
        Biome desert = Biome.builder().temperature(0.8f).downfall(0.4f).build();
        Biome plains = Biome.builder().temperature(0.7f).downfall(0.6f).build();
        Biome forest = Biome.builder().temperature(0.5f).downfall(0.9f).build();

        desertKey = biomeRegistry.register("desert", desert);
        plainsKey = biomeRegistry.register("plains", plains);
        forestKey = biomeRegistry.register("forest", forest);
    }

    private static Biome selectBiome(float noiseValue) {
        // Modify the thresholds for biome selection, increasing differentiation
        if (noiseValue < -0.4) {
            return biomeRegistry.get(desertKey); // Desert biome for lower values
        } else if (noiseValue < 0.3) {
            return biomeRegistry.get(plainsKey); // Plains biome for mid-range values
        } else {
            return biomeRegistry.get(forestKey); // Forest biome for higher values
        }
    }

    private static void setBiomeBlocks(GenerationUnit unit, Biome biome, int worldX, int worldZ, int height) {
        if (biome == biomeRegistry.get(desertKey)) {
            unit.modifier().setBlock(new Vec(worldX, height, worldZ), Block.SAND);
        } else if (biome == biomeRegistry.get(plainsKey)) {
            unit.modifier().setBlock(new Vec(worldX, height, worldZ), Block.GRASS_BLOCK);
        } else if (biome == biomeRegistry.get(forestKey)) {
            unit.modifier().setBlock(new Vec(worldX, height, worldZ), Block.GRASS_BLOCK);
        }
    }
}
