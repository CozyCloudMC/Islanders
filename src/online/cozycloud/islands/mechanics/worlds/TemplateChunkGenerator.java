package online.cozycloud.islands.mechanics.worlds;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TemplateChunkGenerator extends ChunkGenerator {

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.ChunkData chunkData) {

        switch (worldInfo.getEnvironment()) {

            default -> {
                chunkData.setRegion(0, worldInfo.getMinHeight() + 1, 0, 16, worldInfo.getMinHeight() + 2, 16, Material.SAND);
                chunkData.setRegion(0, worldInfo.getMinHeight() + 2, 0, 16, 63, 16, Material.WATER);
            }

            case NETHER -> {
                chunkData.setRegion(0, worldInfo.getMinHeight() + 1, 0, 16, worldInfo.getMinHeight() + 2, 16, Material.NETHERRACK);
                chunkData.setRegion(0, worldInfo.getMinHeight() + 2, 0, 16, 63, 16, Material.LAVA);
            }

            case THE_END -> {}

        }

    }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkGenerator.ChunkData chunkData) {
        if (worldInfo.getEnvironment() != World.Environment.THE_END)
            chunkData.setRegion(0, worldInfo.getMinHeight(), 0, 16, worldInfo.getMinHeight()+1, 16, Material.BEDROCK);
    }

    @Override
    public @Nullable Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0, 63, 0);
    }
}
