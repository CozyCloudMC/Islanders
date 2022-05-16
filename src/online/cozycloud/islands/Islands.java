package online.cozycloud.islands;

import online.cozycloud.islands.commands.IslandsAdminCommand;
import online.cozycloud.islands.commands.IslandsCommand;
import online.cozycloud.islands.local.LocalIslandManager;
import online.cozycloud.islands.mechanics.trees.TreeMechanics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    private static Islands instance;
    public static Islands getInstance() {return instance;}

    private static ConfigHandler configHandler;
    public static ConfigHandler getConfigHandler() {return configHandler;}

    private static LocalIslandManager localIslandManager;
    public static LocalIslandManager getLocalIslandManager() {return localIslandManager;}

    @Override
    public void onEnable() {

        instance = this;
        configHandler = new ConfigHandler();
        localIslandManager = new LocalIslandManager();

        getCommand("islands").setExecutor(new IslandsCommand());
        getCommand("islandsadmin").setExecutor(new IslandsAdminCommand());

        Bukkit.getPluginManager().registerEvents(new TreeMechanics(), this);

    }

}