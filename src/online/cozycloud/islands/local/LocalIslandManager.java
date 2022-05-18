package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
        reload();
    }

    public static LocalIslandSetupManager getLocalIslandSetupManager() {return localIslandSetupManager;}

    public void reload() {

        localIslands.clear();
        File[] files = Islands.getWorldHandler().getWorldFolder().listFiles();

        if (files != null) for (File f : files) {
            String name = f.getName();
            if (name.startsWith("0")) loadIsland(name);
        }

    }

    public void loadIsland(String name) {
        if (!localIslands.containsKey(name)) localIslands.put(name, new LocalIsland(name));
    }

    public void unloadIsland(String name) {
        localIslands.remove(name);
    }

    public ArrayList<LocalIsland> getIslands() {
        return new ArrayList<>(localIslands.values());
    }

    @Nullable
    public LocalIsland getIsland(String name) {
        return localIslands.getOrDefault(name, null);
    }

    /**
     * Gets a list of all islands that have a particular member.
     * @param member the UUID of the member to query
     * @return a list of islands containing the member
     * @throws SQLException thrown if a connection could not be made to the database
     */
    public ArrayList<LocalIsland> getIslandsWithMember(UUID member) throws SQLException {

        ArrayList<LocalIsland> islands = new ArrayList<>();

        String selectCmd = "SELECT name FROM local_islands WHERE members LIKE '%" + member.toString() + "%';";
        ResultSet result = Islands.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();

        while (result.next()) {
            String name = result.getString("name");
            if (localIslands.containsKey(name)) islands.add(localIslands.get(name));
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
        for (UUID uuid : members) result += uuid.toString() + ",";
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
