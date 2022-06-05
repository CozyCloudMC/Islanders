package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.mechanics.worlds.WorldHandler;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Contains methods pertaining to the creation and deletion of local islands.
 */
public class LocalIslandSetupManager {

    private HashMap<String, Long> dimensionCreationCooldowns = new HashMap<>();
    
    /**
     * Creates a local island for the specified player(s). Only creates overworld.
     * @param players the players starting the island
     */
    public void createIsland(List<Player> players) {

        for (Player p : players) p.sendMessage(ChatColor.GRAY + "Creating your island...");

        // SQL operations and file duplication are run asynchronously to avoid lagging the main thread
        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            String id = createIslandID();
            ArrayList<UUID> members = new ArrayList<>();
            for (Player p : players) members.add(p.getUniqueId());

            try {
                createData(id, members);
            } catch (SQLException e) {
                e.printStackTrace();
                cancelIslandCreation(id, players);
                return;
            }

            try {
                duplicateTemplate(id, World.Environment.NORMAL);
            } catch (IOException e) {
                e.printStackTrace();
                cancelIslandCreation(id, players);
                return;
            }

            // World loading must be synchronous
            // Must occur *after* the world folders exist, or it will make its own empty worlds
            Bukkit.getScheduler().runTask(Islands.getInstance(), () -> {

                World world = setUpWorld(id, World.Environment.NORMAL);

                if (world == null) {
                    cancelIslandCreation(id, players);
                    return;
                }

                LocalIsland island = Islands.getLocalIslandManager().loadIsland(id, members);

                for (Player p : players) {
                    island.spawn(p);
                    p.sendMessage(ChatColor.GREEN + "Welcome to your island!");
                }

            });

        });

    }

    /**
     * Deletes database data, deletes the world folders of the specified island, and sends the specified players an error message.
     * This is to be used when part of the island creation fails; this prevents incomplete island setups.
     * @param id the island ID
     * @param players the players attempting to start an island
     */
    private void cancelIslandCreation(String id, List<Player> players) {
        for (Player p : players) p.sendMessage(ChatColor.RED + "An error occurred. Contact an admin for help!");
        deleteIsland(id, null);
    }

    /**
     * Creates a random 13 character ID starting with 0.
     * Example: 0b6a283eb3a79
     * @return random ID
     */
    private String createIslandID() {
        String[] parts = UUID.randomUUID().toString().split("-");
        return "0" + parts[parts.length-1];
    }

    /**
     * Duplicates a local template world's region files without the other data files.
     * @param id the island ID
     * @param environment the environment of the world to duplicate
     * @throws IOException thrown if the folders could not be duplicated
     */
    private void duplicateTemplate(String id, World.Environment environment) throws IOException {

        File worldFolder = Islands.getWorldHandler().getWorldFolder();
        String suffix = WorldHandler.getWorldSuffix(environment);

        String dimFolder = switch (environment) {
            default -> "";
            case NETHER -> "/DIM-1";
            case THE_END -> "/DIM1";
        };

        File templateRegion = new File(worldFolder + "/" + Islands.getConfigHandler().getLocalTemplateName() + suffix + dimFolder, "region");
        File newRegion = new File(worldFolder + "/" + id + suffix + dimFolder, "region");

        FileUtils.copyDirectory(templateRegion, newRegion, false);

    }

    /**
     * Inserts local island data into the database.
     * @param id the island ID
     * @param members the UUIDs of the island's members
     * @throws SQLException thrown if a connection could not be made to the database
     */
    private void createData(String id, List<UUID> members) throws SQLException {
        String insertCmd = "INSERT INTO local_islands(id, members, last_active) VALUES ('" + id + "', '" + LocalIslandManager.membersToString(members) + "', '" + System.currentTimeMillis() + "');";
        Islands.getSqlHandler().getConnection().prepareStatement(insertCmd).executeUpdate();
    }

    /**
     * Loads the specified local world and establishes basic properties like game rules.
     * @param id the island ID
     * @param environment the environment of the world
     * @return the newly loaded world or null if setup failed
     */
    @Nullable
    private World setUpWorld(String id, World.Environment environment) {

        String worldName = id + WorldHandler.getWorldSuffix(environment);
        World world = WorldHandler.getLocalWorldCreator(worldName, environment).createWorld();
        if (world == null) return null;

        world.setSpawnLocation(new Location(world, 0, 64, 0)); // Temporary location
        world.getWorldBorder().setSize(300); // Temporary size
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
     * Deletes local island's SQL data and world folder. Be careful!
     * @param id the island ID
     * @param sender sender that initiated the island deletion or null if not applicable
     */
    public void deleteIsland(String id, @Nullable CommandSender sender) {

        if (sender != null) sender.sendMessage(ChatColor.RED + "Deleting island...");
        Islands.getLocalIslandManager().unloadIsland(id); // Must be synchronous

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                deleteTemplates(id);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                deleteData(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (sender != null) sender.sendMessage(ChatColor.DARK_RED + "Island deleted.");

        });

    }

    /**
     * Deletes all world folders related to the island provided.
     * @param id the island ID
     * @throws IOException thrown if the folders could not be deleted
     */
    private void deleteTemplates(String id) throws IOException {

        File worldFolder = Islands.getWorldHandler().getWorldFolder();

        for (World.Environment env : WorldHandler.getValidEnvironments()) {
            File file = new File(worldFolder, id + WorldHandler.getWorldSuffix(env));
            if (file.exists()) FileUtils.deleteDirectory(file);
        }

    }

    /**
     * Deletes local island data from the database.
     * @param id the island ID
     * @throws SQLException thrown if a connection could not be made to the database
     */
    private void deleteData(String id) throws SQLException {
        String deleteCmd = "DELETE FROM local_islands WHERE id = '" + id + "';";
        Islands.getSqlHandler().getConnection().prepareStatement(deleteCmd).executeUpdate();
    }

    /**
     * Creates additional dimension worlds for an existing island.
     * This is separated so that islands don't have nether and end worlds until they're needed.
     * Method has a 5 second cooldown to prevent the operation from
     * @param id the island ID
     * @param environment the environment of the world to add
     */
    public void addWorld(String id, World.Environment environment) {

        // Prevents the method from being used if the world already exists.
        LocalIsland island = Islands.getLocalIslandManager().getIsland(id);
        if (island != null && island.hasWorld(environment)) return;

        // Prevents multiple worlds being created at the same time due to spam; 5 second cooldown
        String cooldownKey = id + environment.toString();
        long cooldown = dimensionCreationCooldowns.containsKey(cooldownKey) ? dimensionCreationCooldowns.get(cooldownKey) : 0;
        if (System.currentTimeMillis() - cooldown < 5000) return;

        dimensionCreationCooldowns.put(cooldownKey, System.currentTimeMillis());

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                duplicateTemplate(id, environment);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Bukkit.getScheduler().runTask(Islands.getInstance(), () -> setUpWorld(id, environment));

        });

    }

}
