package online.cozycloud.islands;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldHandler {

    protected WorldHandler() {
        loadMainWorlds();
    }

    public void loadMainWorlds() {

        // Local template
        getLocalWorldCreator(Islands.getConfigHandler().getLocalTemplateName()).createWorld();

    }

    public static WorldCreator getLocalWorldCreator(String name) {

        // Sea level should be Y 62
        String generatorSettings = "{\"layers\": [{\"block\": \"bedrock\", \"height\": 1}, {\"block\": \"sand\", \"height\": 1}, {\"block\": \"water\", \"height\": 125}], \"biome\":\"deep_ocean\"}";

        return new WorldCreator(name)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .type(WorldType.FLAT)
                .generatorSettings(generatorSettings);

    }

}