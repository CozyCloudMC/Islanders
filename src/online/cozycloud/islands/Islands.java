package online.cozycloud.islands;

import online.cozycloud.islands.commands.IslandsAdminCommand;
import online.cozycloud.islands.commands.IslandsCommand;
import online.cozycloud.islands.local.LocalIslandManager;
import online.cozycloud.islands.mechanics.trees.TreeMechanics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    private static Islands instance;
    private static ConfigHandler configHandler;
    private static SqlHandler sqlHandler;
    private static WorldHandler worldHandler;
    private static LocalIslandManager localIslandManager;

    @Override
    public void onEnable() {

        instance = this;
        configHandler = new ConfigHandler();
        sqlHandler = new SqlHandler();
        worldHandler = new WorldHandler();
        localIslandManager = new LocalIslandManager();

        getCommand("islands").setExecutor(new IslandsCommand());
        getCommand("islandsadmin").setExecutor(new IslandsAdminCommand());

        Bukkit.getPluginManager().registerEvents(new TreeMechanics(), this);

    }

    public static Islands getInstance() {return instance;}
    public static ConfigHandler getConfigHandler() {return configHandler;}
    public static SqlHandler getSqlHandler() {return sqlHandler;}
    public static WorldHandler getWorldHandler() {return worldHandler;}
    public static LocalIslandManager getLocalIslandManager() {return localIslandManager;}

}