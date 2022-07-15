package online.cozycloud.islanders;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class Utils {

    /**
     * Check if a player is in survival or adventure mode.
     * @param player the player to check
     * @return true if in survival or adventure mode.
     */
    public static boolean hasPlayingGameMode(Player player) {
        GameMode gm = player.getGameMode();
        return gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE;
    }

    /**
     * Get the outter biome of a particular environment. This is the biome that damages players.
     * @param environment the environment of the world
     * @return the outter biome
     */
    public static Biome getOutterBiome(World.Environment environment) {

        return switch (environment) {
            default -> Biome.WARM_OCEAN;
            case NETHER -> Biome.CRIMSON_FOREST;
            case THE_END -> Biome.END_BARRENS;
        };

    }

    /**
     * Get the inner biome of a particular environment. This is the biome that is safe for players.
     * @param environment the environment of the world
     * @return the inner biome
     */
    public static Biome getInnerBiome(World.Environment environment) {

        return switch (environment) {
            default -> Biome.OCEAN;
            case NETHER -> Biome.NETHER_WASTES;
            case THE_END -> Biome.END_HIGHLANDS;
        };

    }

}
