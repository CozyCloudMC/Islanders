package online.cozycloud.islanders.local;

import online.cozycloud.islanders.Islanders;
import online.cozycloud.islanders.mechanics.worlds.WorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
    private HashMap<UUID, ArrayList<String>> membersIslands = new HashMap<>();

    public LocalIslandManager() {

        localIslandSetupManager = new LocalIslandSetupManager();
        Bukkit.getPluginManager().registerEvents(new LocalIslandEvents(), Islanders.getInstance());

        Bukkit.getScheduler().runTaskAsynchronously(Islanders.getInstance(), () -> {

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
        ResultSet result = Islanders.getSqlHandler().getConnection().prepareStatement(selectCmd).executeQuery();
        while (result.next()) loadIsland(result.getString("id"));

    }

    /**
     * Refer to {@link #loadIsland(String, List)}
     * @param id the island ID
     * @return the newly loaded island or an existing island if it is already loaded
     */
    public LocalIsland loadIsland(String id) {return loadIsland(id, null);}

    /**
     * Creates a reference to an island. Unlike worlds, islands should always be loaded as long as the island exists.
     * @param id the island ID
     * @param cachedMembers a list of members on hand that can immediately be added to the island
     * @return the newly loaded island or an existing island if it is already loaded
     */
    public LocalIsland loadIsland(String id, @Nullable List<UUID> cachedMembers) {

        if (localIslands.containsKey(id)) return localIslands.get(id);

        LocalIsland island = new LocalIsland(id, cachedMembers);
        localIslands.put(id, island);
        return island;

    }

    /**
     * Removes reference to this island and unloads all related worlds.
     * @param id the island ID
     */
    public void unloadIsland(String id) {

        for (World.Environment env : WorldHandler.getValidEnvironments())
            Islanders.getWorldHandler().safelyUnloadWorld(id + WorldHandler.getWorldSuffix(env), false);

        LocalIsland island = getIsland(id);
        if (island != null) for (UUID uuid : island.getMembers()) unassignIslandToMember(uuid, id);

        localIslands.remove(id);

    }

    protected void assignIslandToMember(UUID uuid, String id) {

        ArrayList<String> islands = membersIslands.getOrDefault(uuid, new ArrayList<>());
        if (!islands.contains(id)) islands.add(id);

        membersIslands.put(uuid, islands);

    }

    protected void unassignIslandToMember(UUID uuid, String id) {

        ArrayList<String> islands = membersIslands.getOrDefault(uuid, new ArrayList<>());
        islands.remove(id);

        if (!islands.isEmpty()) membersIslands.put(uuid, islands);
        else membersIslands.remove(uuid);

    }

    public void unloadInactiveIslandWorlds() {

        for (World w : Bukkit.getWorlds()) {
            LocalIsland island = Islanders.getLocalIslandManager().getIsland(w.getName());
            if (island != null && island.hasNoRelevantPlayers()) island.unloadWorlds();
        }

    }

    @Nullable
    public LocalIsland getIsland(String id) {
        return localIslands.getOrDefault(id, null);
    }

    @Nullable
    public LocalIsland getIsland(World world) {

        if (world == null) return null;

        String id = world.getName().split("_")[0];
        return getIsland(id);

    }

    /**
     * Check if a world belongs to a local island.
     * @param world the world to check
     * @return true if it belongs to a local island
     */
    public boolean isIslandWorld(World world) {
        return getIsland(world) != null;
    }

    public ArrayList<LocalIsland> getIslands() {
        return new ArrayList<>(localIslands.values());
    }

    /**
     * Gets a list of all islands that have a particular member.
     * @param member the UUID of the member
     * @return a list of islands containing the member
     */
    public ArrayList<LocalIsland> getIslandsWithMember(UUID member) {

        ArrayList<LocalIsland> result = new ArrayList<>();
        for (String id : membersIslands.getOrDefault(member, new ArrayList<>())) if (localIslands.containsKey(id)) result.add(localIslands.get(id));

        return result;

    }

    /**
     * Gets the main island of a member.
     * @param member the UUID of the member
     * @return the user's main island
     */
    @Nullable
    public LocalIsland getMainIsland(UUID member) {
        ArrayList<LocalIsland> islands = getIslandsWithMember(member);
        return islands.isEmpty() ? null : islands.get(0);
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
