package online.cozycloud.islands;

import online.cozycloud.islands.commands.IslandsAdminCommand;
import online.cozycloud.islands.commands.IslandsCommand;
import online.cozycloud.islands.mechanics.trees.TreeMechanics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    private static Islands instance;
    public static Islands getInstance() {return instance;}

    private static Config config;
    public static Config getConfigHandler() {return config;}

    @Override
    public void onEnable() {

        instance = this;
        config = new Config();

        getCommand("islands").setExecutor(new IslandsCommand());
        getCommand("islandsadmin").setExecutor(new IslandsAdminCommand());

        Bukkit.getPluginManager().registerEvents(new TreeMechanics(), this);

    }

}