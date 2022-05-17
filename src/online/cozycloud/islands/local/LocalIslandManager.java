package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.WorldHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LocalIslandManager {

    private HashMap<String, LocalIsland> localIslands = new HashMap<>();

    public LocalIslandManager() {
        reload();
    }

    public void reload() {

        localIslands.clear();
        File[] files = Islands.getWorldHandler().getWorldFolder().listFiles();

        if (files != null) for (File f : files) {
            String name = f.getName();
            if (name.startsWith("0")) localIslands.put(name, new LocalIsland(name));
        }

    }

    @Nullable
    public LocalIsland getIsland(String name) {
        return localIslands.getOrDefault(name, null);
    }

    public ArrayList<LocalIsland> getIslands() {
        return new ArrayList<>(localIslands.values());
    }

    /**
     * Creates a local island for the specified player(s).
     * File duplication is run asynchronously to avoid lagging the main thread.
     * World loading through Bukkit must be synchronous.
     * @param players the players starting the island
     */
    public void createIsland(List<Player> players) {

        for (Player p : players) p.sendMessage(ChatColor.GRAY + "Creating your island...");

        // Async
        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            String name = createWorldName();

            try {
                duplicateTemplate(name);
            } catch (IOException e) {
                e.printStackTrace();
                for (Player p : players) p.sendMessage(ChatColor.RED + "An error occurred. Contact an admin for help!");
                return;
            }

            // Sync
            Bukkit.getScheduler().runTask(Islands.getInstance(), () -> {

                World world = setUpWorld(name);

                if (world == null) {
                    for (Player p : players) p.sendMessage(ChatColor.RED + "An error occurred. Contact an admin for help!");
                    return;
                }

                LocalIsland island = new LocalIsland(name, players);
                localIslands.put(name, island);

                for (Player p : players) {
                    p.teleport(world.getSpawnLocation());
                    p.sendMessage(ChatColor.GREEN + "Welcome to your island!");
                }

            });

        });

    }

    /**
     * Creates a random 13 character world name starting with 0.
     * Example: 0b6a283eb3a79
     * @return random name
     */
    private String createWorldName() {
        String[] parts = UUID.randomUUID().toString().split("-");
        return "0" + parts[parts.length-1];
    }

    /**
     * Duplicates local template world folder without the uid.dat file.
     * @param name the world name
     * @throws IOException thrown if the file could not be duplicated
     */
    private void duplicateTemplate(String name) throws IOException {

        File worldFolder = Islands.getWorldHandler().getWorldFolder();
        String templateName = Islands.getConfigHandler().getLocalTemplateName();

        FileFilter filter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("uid.dat"));
        FileUtils.copyDirectory(new File(worldFolder, templateName), new File(worldFolder, name), filter,false);

    }

    /**
     * Loads the specified local world and establishes basic properties like game rules.
     * @param name the world name
     * @return the newly loaded world or null if setup failed
     */
    @Nullable
    private World setUpWorld(String name) {

        World world = WorldHandler.getLocalWorldCreator(name).createWorld();
        if (world == null) return null;

        world.setSpawnLocation(new Location(world, 0, 64, 0)); // Temporary location
        world.setTime(0);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);

        return world;

    }

}
