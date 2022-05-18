package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.WorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalIsland {

    private final String NAME;
    private final File WORLD_FILE;

    private ArrayList<UUID> members;

    protected LocalIsland(String name) {
        this(name, null);
    }

    protected LocalIsland(String name, @Nullable List<UUID> members) {

        NAME = name;
        WORLD_FILE = new File(Islands.getWorldHandler().getWorldFolder(), NAME);
        if (members != null) this.members = new ArrayList<>(members);

        loadData();

    }

    private void loadData() {

        // Asynchronously load data from SQL

    }

    /**
     * Loads the local island's world, but does not create one if it doesn't exist.
     * @return the loaded world or null if world does not exist
     */
    public World loadWorld() {
        if (!WORLD_FILE.exists()) return null;
        return WorldHandler.getLocalWorldCreator(NAME).createWorld();
    }

    public void unloadWorld() {
        Bukkit.unloadWorld(NAME, true);
    }

    public String getName() {return NAME;}

    public ArrayList<UUID> getMembers() {return new ArrayList<>(members);}

    /**
     * Get the world used by this island.
     * @return the island's world or null if it is not loaded
     */
    @Nullable
    public World getWorld() {return Bukkit.getWorld(NAME);}

}
