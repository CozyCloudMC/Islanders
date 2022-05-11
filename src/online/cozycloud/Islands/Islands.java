package online.cozycloud.Islands;

import online.cozycloud.Islands.Commands.IslandsAdminCommand;
import online.cozycloud.Islands.Commands.IslandsCommand;
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

    }

}