package online.cozycloud.islands;

import online.cozycloud.islands.local.LocalIsland;
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

        // Local template
        getLocalWorldCreator(Islands.getConfigHandler().getLocalTemplateName()).createWorld();

    }

    public void unloadInactiveWorlds() {

        for (World w : Bukkit.getWorlds()) {
            LocalIsland island = Islands.getLocalIslandManager().getIsland(w.getName());
            if (island != null && island.hasNoRelevantPlayers()) safelyUnloadWorld(w, true);
        }

    }

    public void safelyUnloadWorld(String name, boolean save) {safelyUnloadWorld(Bukkit.getWorld(name), save);}

    public void safelyUnloadWorld(World world, boolean save) {

        if (world == null) return;

        for (Player p : new ArrayList<>(world.getPlayers())) p.teleport(Islands.getWorldHandler().getMainWorld().getSpawnLocation());
        Bukkit.unloadWorld(world, save);

    }

    public static WorldCreator getLocalWorldCreator(String name) {

        // Sea level should be Y 62
        String generatorSettings = "{\"layers\": [{\"block\": \"bedrock\", \"height\": 1}, {\"block\": \"sand\", \"height\": 1}, {\"block\": \"water\", \"height\": 125}], \"biome\":\"deep_ocean\"}";

        return new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .type(WorldType.FLAT)
                .generatorSettings(generatorSettings);

    }

}