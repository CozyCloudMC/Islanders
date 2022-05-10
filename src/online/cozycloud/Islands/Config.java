package online.cozycloud.Islands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {

    private File file;

    private String worldName;

    protected Config() {
        file = new File(Islands.getInstance().getDataFolder(), "config.yml");
        reload();
    }

    //The following getters refer to config values and return defaults if no value exists.

    public String getWorldName() {return worldName != null ? worldName : "islands";}

    public void reload() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) initialize(config);

        worldName = config.getKeys(false).contains("world_name") ? config.getString("world_name") : getWorldName();

    }

    private void initialize(YamlConfiguration config) {

        config.set("world_name", getWorldName());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
