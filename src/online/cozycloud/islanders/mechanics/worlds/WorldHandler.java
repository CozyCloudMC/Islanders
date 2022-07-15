package online.cozycloud.islanders.mechanics.worlds;

import online.cozycloud.islanders.Islanders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;

public class WorldHandler {

    private final File WORLD_FOLDER;
    private World mainWorld;

    public WorldHandler() {

        WORLD_FOLDER = Bukkit.getWorldContainer();
        Bukkit.getScheduler().runTaskLater(Islanders.getInstance(), () -> mainWorld = Bukkit.getWorlds().get(0), 1);

        Bukkit.getPluginManager().registerEvents(new WorldEvents(), Islanders.getInstance());
        Bukkit.getPluginManager().registerEvents(new DragonPrevention(), Islanders.getInstance());
        Bukkit.getPluginManager().registerEvents(new PortalHandler(), Islanders.getInstance());
        loadMainWorlds();

    }

    public File getWorldFolder() {return WORLD_FOLDER;}
    public World getMainWorld() {return mainWorld;}

    /**
     * Loads the worlds specified in the "load" section of the config.
     */
    public void loadMainWorlds() {

        ConfigurationSection loadWorlds = Islanders.getConfigHandler().getLoadWorlds();

        for (String worldName : loadWorlds.getKeys(false)) {

            if (Bukkit.getWorld(worldName) != null) continue; // Skips already loaded worlds

            World.Environment env = World.Environment.valueOf(loadWorlds.getString(worldName + ".environment"));
            String type = loadWorlds.getString(worldName + ".type");
            if (type == null) continue;

            WorldCreator creator = switch (type) {
                case "LOCAL" -> getLocalWorldCreator(worldName, env);
                default -> new WorldCreator(worldName).environment(env).type(WorldType.valueOf(type)).generateStructures(false);
            };

            creator.createWorld();

        }

    }

    public void safelyUnloadWorld(String worldName, boolean save) {safelyUnloadWorld(Bukkit.getWorld(worldName), save);}

    public void safelyUnloadWorld(World world, boolean save) {

        if (world == null) return;

        for (Player p : new ArrayList<>(world.getPlayers())) p.teleport(Islanders.getWorldHandler().getMainWorld().getSpawnLocation());
        Bukkit.unloadWorld(world, save);

    }

    public static World getRelatedDimension(World world, World.Environment environment) {
        String baseName = world.getName().split("_")[0];
        return Bukkit.getWorld(baseName + getWorldSuffix(environment));
    }

    /**
     * Gets the WorldCreator for a local island world based on its environment.
     * @param worldName the name of the world
     * @param environment the environment of the world
     * @return the WorldCreator based on the environment
     */
    public static WorldCreator getLocalWorldCreator(String worldName, World.Environment environment) {

        return new WorldCreator(worldName)
                .environment(environment)
                .generateStructures(false)
                .biomeProvider(new TemplateBiomeProvider())
                .generator(new TemplateChunkGenerator());

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