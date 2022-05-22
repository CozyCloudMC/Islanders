package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.WorldHandler;
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

public class LocalIsland {

    private final String ID;
    private final File WORLD_FILE;

    private ArrayList<UUID> members;

    protected LocalIsland(String id, @Nullable List<UUID> members) {

        ID = id;
        WORLD_FILE = new File(Islands.getWorldHandler().getWorldFolder(), ID);
        if (members != null) this.members = new ArrayList<>(members);

        loadData();

    }

    /**
     * Loads island data from the database.
     */
    private void loadData() {

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            if (members == null) try {
                String selectCmd = "SELECT members FROM local_islands WHERE id = '" + ID + "';";
                ResultSet result = Islands.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();
                while (result.next()) members = new ArrayList<>(LocalIslandManager.membersToList(result.getString("members")));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (UUID uuid : members) Islands.getLocalIslandManager().assignIslandToMember(uuid, ID);

        });

    }

    /**
     * Teleport a player to the island's spawn.
     * @param player the player to teleport
     */
    public void spawn(Player player) {

        World world = getWorld();
        if (world == null) world = loadWorld();

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
            if (player != null) player.teleport(Islands.getWorldHandler().getMainWorld().getSpawnLocation());
            Islands.getLocalIslandManager().unassignIslandToMember(member, ID);

            Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

                try {
                    String updateCmd = "UPDATE local_islands SET members = '" + LocalIslandManager.membersToString(members) + "' WHERE id = '" + ID + "';";
                    Islands.getSqlHandler().getConnection().prepareStatement(updateCmd).executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            });

        }

    }

    /**
     * Loads the local island's world, but does not create one if it doesn't exist.
     * @return the loaded world or null if world does not exist
     */
    @Nullable
    public World loadWorld() {
        if (!WORLD_FILE.exists()) return null;
        return WorldHandler.getLocalWorldCreator(ID).createWorld();
    }

    public void unloadWorld() {Bukkit.unloadWorld(ID, true);}

    public String getName() {return ID;}
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
     * Get the world used by this island.
     * @return the island's world or null if it is not loaded
     */
    @Nullable
    public World getWorld() {return Bukkit.getWorld(ID);}

    /**
     * Check if the island's world has no players and no island members are online
     * @return true if the world is empty and no island members are online
     */
    public boolean hasNoRelevantPlayers() {

        World world = getWorld();
        boolean emptyWorld = world == null || world.getPlayers().isEmpty(), noOnlineMembers = getOnlineMembers().isEmpty();
        return emptyWorld && noOnlineMembers;

    }

}
