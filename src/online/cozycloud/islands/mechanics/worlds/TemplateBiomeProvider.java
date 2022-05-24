package online.cozycloud.islands.mechanics.worlds;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TemplateBiomeProvider extends BiomeProvider {

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
        return getOutterBiome(worldInfo.getEnvironment());
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(getOutterBiome(worldInfo.getEnvironment()));
    }

    private Biome getOutterBiome(World.Environment environment) {

        return switch (environment) {
            default -> Biome.DEEP_OCEAN;
            case NETHER -> Biome.CRIMSON_FOREST;
            case THE_END -> Biome.END_BARRENS;
        };

    }

}
