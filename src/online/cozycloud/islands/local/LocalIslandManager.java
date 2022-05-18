package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    @Nullable
    public LocalIsland getIsland(String name) {
        return localIslands.getOrDefault(name, null);
    }

    public ArrayList<LocalIsland> getIslands() {
        return new ArrayList<>(localIslands.values());
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
