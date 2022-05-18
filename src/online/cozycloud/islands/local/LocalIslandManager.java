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
import java.sql.SQLException;
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
     * @param players the players starting the island
     */
    public void createIsland(List<Player> players) {

        for (Player p : players) p.sendMessage(ChatColor.GRAY + "Creating your island...");

        // SQL operations and file duplication are run asynchronously to avoid lagging the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            String name = createWorldName();
            ArrayList<UUID> members = new ArrayList<>();
            for (Player p : players) members.add(p.getUniqueId());

            try {
                createData(name, members);
            } catch (SQLException e) {
                e.printStackTrace();
                cancelIslandCreation(name, players);
                return;
            }

            try {
                duplicateTemplate(name);
            } catch (IOException e) {
                e.printStackTrace();
                cancelIslandCreation(name, players);
                return;
            }

            // World loading must be synchronous
            // Must occur *after* the folder exists, or it will make its own empty world
            Bukkit.getScheduler().runTask(Islands.getInstance(), () -> {

                World world = setUpWorld(name);

                if (world == null) {
                    cancelIslandCreation(name, players);
                    return;
                }

                LocalIsland island = new LocalIsland(name, members);
                localIslands.put(name, island);

                for (Player p : players) {
                    p.teleport(world.getSpawnLocation());
                    p.sendMessage(ChatColor.GREEN + "Welcome to your island!");
                }

            });

        });

    }

    /**
     * Deletes database data, deletes the world folder of the specified island, and sends the specified players an error message.
     * This is to be used when part of the island creation fails; this prevents incomplete island setups.
     * @param name the island name
     * @param players the players attempting to start an island
     */
    private void cancelIslandCreation(String name, List<Player> players) {

        for (Player p : players) p.sendMessage(ChatColor.RED + "An error occurred. Contact an admin for help!");

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            // Removes SQL data if it exists
            try {
                String deleteCmd = "DELETE FROM local_islands WHERE name = '" + name + "';";
                Islands.getSqlHandler().getConnection().prepareStatement(deleteCmd).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Deletes world folder if it exists
            File file = new File(Islands.getWorldHandler().getWorldFolder(), name);
            if (file.exists() && !file.delete()) Islands.getInstance().getLogger().warning("Failed to delete world folder: " + name);

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
     * @param name the island name
     * @throws IOException thrown if the file could not be duplicated
     */
    private void duplicateTemplate(String name) throws IOException {

        File worldFolder = Islands.getWorldHandler().getWorldFolder();
        String templateName = Islands.getConfigHandler().getLocalTemplateName();

        FileFilter filter = FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("uid.dat"));
        FileUtils.copyDirectory(new File(worldFolder, templateName), new File(worldFolder, name), filter,false);

    }

    /**
     * Inserts local island data into the database.
     * @param name the island name
     * @param members the UUIDs of the island's members
     * @throws SQLException thrown if a connection could not be made to the database
     */
    private void createData(String name, List<UUID> members) throws SQLException {
        String insertCmd = "INSERT INTO local_islands(name, members) VALUES ('" + name + "', '" + membersToString(members) + "');";
        Islands.getSqlHandler().getConnection().prepareStatement(insertCmd).executeUpdate();
    }

    /**
     * Loads the specified local world and establishes basic properties like game rules.
     * @param name the island name
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

    /**
     * Convert a list of UUIDs to a string that can be saved to the database.
     * @param members a list of the island members' UUIDs
     * @return a string of UUIDs separated by commas
     */
    public static String membersToString(List<UUID> members) {

        String result = "";
        for (UUID uuid : members) result += uuid.toString() + ",";
        if (result.endsWith(",")) result = result.substring(0, result.length()-1);

        return result;

    }

    /**
     * Convert a string to a list of UUIDs.
     * @param members a comma separated string of UUIDs
     * @return a list of UUIDs
     */
    public static ArrayList<UUID> membersToList(String members) {

        ArrayList<UUID> result = new ArrayList<>();
        for (String member : members.split(",")) result.add(UUID.fromString(member));

        return result;

    }

}
