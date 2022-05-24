package online.cozycloud.islands.mechanics.worlds;

import online.cozycloud.islands.local.LocalIslandManager;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TemplateBiomeProvider extends BiomeProvider {

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
        return LocalIslandManager.getOutterBiome(worldInfo.getEnvironment());
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(LocalIslandManager.getOutterBiome(worldInfo.getEnvironment()));
    }

}
