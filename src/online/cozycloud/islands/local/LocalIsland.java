package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.WorldHandler;
import org.bukkit.Bukkit;
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

    private final String NAME;
    private final File WORLD_FILE;

    private ArrayList<UUID> members;

    protected LocalIsland(String name, @Nullable List<UUID> members) {

        NAME = name;
        WORLD_FILE = new File(Islands.getWorldHandler().getWorldFolder(), NAME);
        if (members != null) this.members = new ArrayList<>(members);

        loadData();

    }

    /**
     * Loads island data from the database.
     */
    private void loadData() {

        if (members != null) Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                String selectCmd = "SELECT members FROM local_islands WHERE name = '" + NAME + "';";
                ResultSet result = Islands.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();
                while (result.next()) members = new ArrayList<>(LocalIslandManager.membersToList(result.getString("members")));
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

    }

    /**
     * Loads the local island's world, but does not create one if it doesn't exist.
     * @return the loaded world or null if world does not exist
     */
    @Nullable
    public World loadWorld() {
        if (!WORLD_FILE.exists()) return null;
        return WorldHandler.getLocalWorldCreator(NAME).createWorld();
    }

    public void unloadWorld() {Bukkit.unloadWorld(NAME, true);}

    public String getName() {return NAME;}
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
    public World getWorld() {return Bukkit.getWorld(NAME);}

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
