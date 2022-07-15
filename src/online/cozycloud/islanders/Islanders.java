package online.cozycloud.islanders;

import online.cozycloud.islanders.commands.IslandersAdminCommand;
import online.cozycloud.islanders.commands.IslandersCommand;
import online.cozycloud.islanders.local.LocalIslandManager;
import online.cozycloud.islanders.mechanics.npcs.NpcHandler;
import online.cozycloud.islanders.mechanics.startstations.StartStationManager;
import online.cozycloud.islanders.mechanics.trees.TreeMechanics;
import online.cozycloud.islanders.mechanics.worlds.WorldHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Islanders extends JavaPlugin {

    private static Islanders instance;
    private static ConfigHandler configHandler;
    private static SqlHandler sqlHandler;
    private static WorldHandler worldHandler;
    private static NpcHandler npcHandler;
    private static LocalIslandManager localIslandManager;
    private static StartStationManager startStationManager;

    @Override
    public void onEnable() {

        instance = this;
        configHandler = new ConfigHandler();
        sqlHandler = new SqlHandler();
        worldHandler = new WorldHandler();
        npcHandler = new NpcHandler();
        localIslandManager = new LocalIslandManager();
        startStationManager = new StartStationManager();

        getCommand("islanders").setExecutor(new IslandersCommand());
        getCommand("islandersadmin").setExecutor(new IslandersAdminCommand());

        Bukkit.getPluginManager().registerEvents(new TreeMechanics(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDataHandler(), this);
        Bukkit.getPluginManager().registerEvents(startStationManager, this);

    }

    public static Islanders getInstance() {return instance;}
    public static ConfigHandler getConfigHandler() {return configHandler;}
    public static SqlHandler getSqlHandler() {return sqlHandler;}
    public static WorldHandler getWorldHandler() {return worldHandler;}
    public static NpcHandler getNpcHandler() {return npcHandler;}
    public static LocalIslandManager getLocalIslandManager() {return localIslandManager;}
    public static StartStationManager getStartStationManager() {return startStationManager;}

}