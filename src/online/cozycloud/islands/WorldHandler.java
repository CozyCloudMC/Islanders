package online.cozycloud.islands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;

public class WorldHandler {

    private final File WORLD_FOLDER;
    private final World MAIN_WORLD;

    protected WorldHandler() {
        WORLD_FOLDER = Bukkit.getWorldContainer();
        MAIN_WORLD = Bukkit.getWorld(Islands.getConfigHandler().getWorldName());
        loadMainWorlds();
    }

    public File getWorldFolder() {return WORLD_FOLDER;}
    public World getMainWorld() {return MAIN_WORLD;}

    public void loadMainWorlds() {

        // Local island templates
        for (World.Environment env : getValidEnvironments()) {
            String worldName = Islands.getConfigHandler().getLocalTemplateName() + getWorldSuffix(env);
            getLocalWorldCreator(worldName, env).createWorld();
        }

    }

    public void safelyUnloadWorld(String worldName, boolean save) {safelyUnloadWorld(Bukkit.getWorld(worldName), save);}

    public void safelyUnloadWorld(World world, boolean save) {

        if (world == null) return;

        for (Player p : new ArrayList<>(world.getPlayers())) p.teleport(Islands.getWorldHandler().getMainWorld().getSpawnLocation());
        Bukkit.unloadWorld(world, save);

    }

    /**
     * Gets the WorldCreator with proper generation settings for a local island world based on its environment.
     * @param worldName the name of the world
     * @param environment the environment of the world
     * @return the WorldCreator based on the environment
     */
    public static WorldCreator getLocalWorldCreator(String worldName, World.Environment environment) {

        // Sea level should be Y 62
        String generatorSettings = switch (environment) {
            default -> "{\"layers\": [{\"block\": \"bedrock\", \"height\": 1}, {\"block\": \"sand\", \"height\": 1}, {\"block\": \"water\", \"height\": 125}], \"biome\":\"deep_ocean\"}";
            case NETHER -> "{\"layers\": [{\"block\": \"bedrock\", \"height\": 1}, {\"block\": \"netherrack\", \"height\": 1}, {\"block\": \"lava\", \"height\": 125}], \"biome\":\"nether_wastes\"}";
            case THE_END -> "{\"layers\": [{\"block\": \"air\", \"height\": 1}], \"biome\":\"end_barrens\"}";
        };

        Islands.getInstance().getLogger().info(worldName + ": " + generatorSettings);

        return new WorldCreator(worldName)
                .environment(environment)
                .generateStructures(false)
                .type(WorldType.FLAT)
                .generatorSettings(generatorSettings);

    }

    /**
     * Get the world name suffix for a particular environment.
     * @param environment the environment of the world
     * @return the world name suffix
     */
    public static String getWorldSuffix(World.Environment environment) {

        return switch (environment) {
            case NETHER -> "_nether";
            case THE_END -> "_the_end";
            default -> "";
        };

    }

    /**
     * Gets the environments used by local island worlds.
     * @return an array of valid environments
     */
    public static World.Environment[] getValidEnvironments() {
        return new World.Environment[] {World.Environment.NORMAL, World.Environment.NETHER, World.Environment.THE_END};
    }

}