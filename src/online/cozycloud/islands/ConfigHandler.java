package online.cozycloud.islands;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ConfigHandler {

    private final File FILE;

    private String sqlAddress;
    private String sqlDatabase;
    private String sqlUsername;
    private String sqlPassword;
    private String worldName;
    private String localTemplateName;

    protected ConfigHandler() {
        FILE = new File(Islands.getInstance().getDataFolder(), "config.yml");
        reload();
    }

    public File getFile() {return FILE;}

    //The following getters refer to config values and return defaults if no value exists.
    public String getSqlAddress() {return sqlAddress != null ? sqlAddress : "127.0.0.1:3306";}
    public String getSqlDatabase() {return sqlDatabase != null ? sqlDatabase : "islands";}
    public String getSqlUsername() {return sqlUsername != null ? sqlUsername : "root";}
    public String getSqlPassword() {return sqlPassword != null ? sqlPassword : "";}
    public String getWorldName() {return worldName != null ? worldName : "islands";}
    public String getLocalTemplateName() {return localTemplateName != null ? localTemplateName : "local_template";}

    public void reload() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(FILE);
        if (!FILE.exists()) initialize(config);

        Set<String> rootSection = config.getKeys(false);

        if (rootSection.contains("sql")) {

            Set<String> sqlSection = config.getConfigurationSection("sql").getKeys(false);

            if (sqlSection.contains("address")) sqlAddress = config.getString("sql.address");
            if (sqlSection.contains("database")) sqlDatabase = config.getString("sql.database");
            if (sqlSection.contains("username")) sqlUsername = config.getString("sql.username");
            if (sqlSection.contains("password")) sqlPassword = config.getString("sql.password");

        }

        if (rootSection.contains("worlds")) {

            Set<String> worldsSection = config.getConfigurationSection("worlds").getKeys(false);

            if (worldsSection.contains("world_name")) worldName = config.getString("worlds.world_name");
            if (worldsSection.contains("local_template_name")) localTemplateName = config.getString("worlds.local_template_name");

        }

    }

    private void initialize(YamlConfiguration config) {

        config.set("sql.address", getSqlAddress());
        config.set("sql.database", getSqlDatabase());
        config.set("sql.username", getSqlUsername());
        config.set("sql.password", getSqlPassword());
        config.set("worlds.world_name", getWorldName());
        config.set("worlds.local_template_name", getLocalTemplateName());

        try {
            config.save(FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
