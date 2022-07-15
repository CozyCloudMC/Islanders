package online.cozycloud.islanders.mechanics.worlds;

import online.cozycloud.islanders.Utils;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TemplateBiomeProvider extends BiomeProvider {

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
        return Utils.getOutterBiome(worldInfo.getEnvironment());
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(Utils.getOutterBiome(worldInfo.getEnvironment()));
    }

}
