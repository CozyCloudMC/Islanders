package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Manages existing local islands and contains other relevant methods.
 */
public class LocalIslandManager {

    private static LocalIslandSetupManager localIslandSetupManager;

    private HashMap<String, LocalIsland> localIslands = new HashMap<>();

    public LocalIslandManager() {

        localIslandSetupManager = new LocalIslandSetupManager();
        Bukkit.getPluginManager().registerEvents(new LocalIslandEvents(), Islands.getInstance());

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                reload();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

    }

    public static LocalIslandSetupManager getLocalIslandSetupManager() {return localIslandSetupManager;}

    /**
     * Reloads all local islands from the database.
     * @throws SQLException thrown if a connection could not be made to the database
     */
    public void reload() throws SQLException {

        localIslands.clear();

        String selectCmd = "SELECT id FROM local_islands;";
        ResultSet result = Islands.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();
        while (result.next()) loadIsland(result.getString("id"));

    }

    public void loadIsland(String id) {loadIsland(id, null);}
    public void loadIsland(String id, List<UUID> cachedMembers) {if (!localIslands.containsKey(id)) localIslands.put(id, new LocalIsland(id, cachedMembers));}
    public void unloadIsland(String id) {localIslands.remove(id);}

    @Nullable
    public LocalIsland getIsland(String id) {
        return localIslands.getOrDefault(id, null);
    }
    public ArrayList<LocalIsland> getIslands() {
        return new ArrayList<>(localIslands.values());
    }

    /**
     * Gets a list of all islands that have a particular member.
     * @param member the UUID of the member to query
     * @return a list of islands containing the member
     * @throws SQLException thrown if a connection could not be made to the database
     */
    public ArrayList<LocalIsland> getIslandsWithMember(UUID member) throws SQLException {

        ArrayList<LocalIsland> islands = new ArrayList<>();

        String selectCmd = "SELECT id FROM local_islands WHERE members LIKE '%" + member + "%';";
        ResultSet result = Islands.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();

        while (result.next()) {
            String id = result.getString("id");
            if (localIslands.containsKey(id)) islands.add(localIslands.get(id));
        }

        return islands;

    }

    /**
     * Convert a list of island members' UUIDs to a string.
     * This string can be used to save a list of members to the database.
     * @param members a list of UUIDs
     * @return a string of UUIDs separated by commas
     */
    public static String membersToString(List<UUID> members) {

        String result = "";
        for (UUID uuid : members) result += uuid + ",";
        if (result.endsWith(",")) result = result.substring(0, result.length()-1);

        return result;

    }

    /**
     * Convert a string of island members' UUIDs to a list.
     * @param members a string of UUIDs separated by commas
     * @return a list of UUIDs
     */
    public static ArrayList<UUID> membersToList(String members) {

        ArrayList<UUID> result = new ArrayList<>();
        for (String member : members.split(",")) result.add(UUID.fromString(member));

        return result;

    }

}
