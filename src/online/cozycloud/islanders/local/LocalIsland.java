package online.cozycloud.islanders.local;

import online.cozycloud.islanders.Islanders;
import online.cozycloud.islanders.mechanics.worlds.WorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class LocalIsland {

    private final String ID;

    private ArrayList<UUID> members;

    protected LocalIsland(String id, @Nullable List<UUID> members) {

        ID = id;
        if (members != null) this.members = new ArrayList<>(members);

        loadData();

    }

    /**
     * Loads island data from the database.
     */
    private void loadData() {

        Bukkit.getScheduler().runTaskAsynchronously(Islanders.getInstance(), () -> {

            if (members == null) try {
                String selectCmd = "SELECT members FROM local_islands WHERE id = '" + ID + "';";
                ResultSet result = Islanders.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();
                while (result.next()) members = new ArrayList<>(LocalIslandManager.membersToList(result.getString("members")));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (UUID uuid : members) Islanders.getLocalIslandManager().assignIslandToMember(uuid, ID);

        });

    }

    /**
     * Teleport a player to the island's spawn.
     * @param player the player to teleport
     */
    public void spawn(Player player) {

        World world = getWorld(World.Environment.NORMAL, true);

        if (world != null) player.teleport(world.getSpawnLocation());
        else player.sendMessage(ChatColor.RED + "An error occurred. Contact an admin for help!");

    }

    /**
     * Removes a member from the island and deletes the island if there are no remaining members.
     * @param member the member to remove
     */
    public void abandon(UUID member) {

        if (!members.contains(member)) return;
        members.remove(member);

        if (members.isEmpty()) LocalIslandManager.getLocalIslandSetupManager().deleteIsland(ID, null);

        else {

            Player player = Bukkit.getPlayer(member);
            if (player != null) player.teleport(Islanders.getWorldHandler().getMainWorld().getSpawnLocation());
            Islanders.getLocalIslandManager().unassignIslandToMember(member, ID);

            Bukkit.getScheduler().runTaskAsynchronously(Islanders.getInstance(), () -> {

                try {
                    String updateCmd = "UPDATE local_islands SET members = '" + LocalIslandManager.membersToString(members) + "' WHERE id = '" + ID + "';";
                    Islanders.getSqlHandler().getConnection().prepareStatement(updateCmd).executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });

        }

    }

    /**
     * Loads the local island's worlds, but does not create them if they don't exist.
     * Island is considered active when this method is called.
     */
    public void loadWorlds() {

        File worldFolder = Islanders.getWorldHandler().getWorldFolder();

        for (World.Environment env : WorldHandler.getValidEnvironments()) {

            String worldName = ID + WorldHandler.getWorldSuffix(env);
            File file = new File(worldFolder, worldName);
            if (!file.exists()) continue;

            WorldHandler.getLocalWorldCreator(worldName, env).createWorld();

        }

        updateActivity();

    }

    public void unloadWorlds() {

        for (World.Environment env : WorldHandler.getValidEnvironments()) {
            String worldName = ID + WorldHandler.getWorldSuffix(env);
            Islanders.getWorldHandler().safelyUnloadWorld(worldName, true);
        }

    }

    /**
     * Sets the island's activity to the current time in the database.
     */
    public void updateActivity() {

        Bukkit.getScheduler().runTaskAsynchronously(Islanders.getInstance(), () -> {

            try {
                String updateCmd = "UPDATE local_islands SET last_active = '" + System.currentTimeMillis() + "' WHERE id = '" + ID + "';";
                Islanders.getSqlHandler().getConnection().prepareStatement(updateCmd).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

    }

    public String getID() {return ID;}
    public ArrayList<UUID> getMembers() {return new ArrayList<>(members);}

    public ArrayList<Player> getOnlineMembers() {

        ArrayList<Player> result = new ArrayList<>();

        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) result.add(p);
        }

        return result;
    }

    /**
     * Get a world used by this island. If not loaded, all the island's worlds will be loaded.
     * @param environment the environment for the world to get
     * @param load whether to load the island's worlds if they are not loaded
     * @return one of the island's worlds or null if it does not exist
     */
    @Nullable
    public World getWorld(World.Environment environment, boolean load) {

        String worldName = ID + WorldHandler.getWorldSuffix(environment);
        World world = Bukkit.getWorld(worldName);

        if (world == null && load) {
            loadWorlds();
            world = Bukkit.getWorld(worldName);
        }

        return world;

    }

    /**
     * Get all worlds corresponding to this island.
     * @param load whether to load the island's worlds if they are not loaded
     * @return all the island's worlds
     */
    public ArrayList<World> getWorlds(boolean load) {

        ArrayList<World> result = new ArrayList<>();

        for (World.Environment env : WorldHandler.getValidEnvironments()) {
            World world = getWorld(env, load);
            if (world != null) result.add(world);
        }

        return result;

    }

    /**
     * Checks if a particular world related to this island has been created.
     * @param environment the environment of the world to check
     * @return true if the world has been created
     */
    public boolean hasWorld(World.Environment environment) {
        File file = new File(Islanders.getWorldHandler().getWorldFolder(), ID + WorldHandler.getWorldSuffix(environment));
        return file.exists();
    }

    /**
     * Check if the island's worlds have no players and no island members are online
     * @return true if the worlds are empty and no island members are online
     */
    public boolean hasNoRelevantPlayers() {

        boolean emptyWorlds = true, noOnlineMembers = getOnlineMembers().isEmpty();
        for (World w : getWorlds(false)) if (!w.getPlayers().isEmpty()) emptyWorlds = false;

        return emptyWorlds && noOnlineMembers;

    }

}
