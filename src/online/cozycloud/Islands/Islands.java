package online.cozycloud.Islands;

import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    private static Islands instance;
    public static Islands getInstance() {return instance;}

    @Override
    public void onEnable() {

        instance = this;

    }

}