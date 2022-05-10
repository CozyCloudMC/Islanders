package online.cozycloud.Islands;

import org.bukkit.plugin.java.JavaPlugin;

public class Islands extends JavaPlugin {

    private Islands instance;
    public Islands getInstance() {return instance;}

    @Override
    public void onEnable() {

        instance = this;

    }

}