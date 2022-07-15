package online.cozycloud.islanders;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {

    private final File FILE;

    private String sqlAddress;
    private String sqlDatabase;
    private String sqlUsername;
    private String sqlPassword;
    private String localTemplateName;
    private ConfigurationSection loadWorlds;
    private ConfigurationSection startStations;

    protected ConfigHandler() {
        FILE = new File(Islanders.getInstance().getDataFolder(), "config.yml");
        reload();
    }

    public File getFile() {return FILE;}

    // The following getters refer to config values and return defaults if no value exists.
    public String getSqlAddress() {return sqlAddress != null ? sqlAddress : "127.0.0.1:3306";}
    public String getSqlDatabase() {return sqlDatabase != null ? sqlDatabase : "islanders";}
    public String getSqlUsername() {return sqlUsername != null ? sqlUsername : "root";}
    public String getSqlPassword() {return sqlPassword != null ? sqlPassword : "";}
    public String getLocalTemplateName() {return localTemplateName != null ? localTemplateName : "template";}
    public @Nullable ConfigurationSection getLoadWorlds() {return loadWorlds;}
    public @Nullable ConfigurationSection getStartStations() {return startStations;}

    /**
     * Sets variables to values specified by the config or null if they do not exist.
     * This must be run for any changes in the config to take effect.
     */
    public void reload() {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(FILE);
        if (!FILE.exists()) initialize(config);

        // sql
        sqlAddress = config.getString("sql.address");
        sqlDatabase = config.getString("sql.database");
        sqlUsername = config.getString("sql.username");
        sqlPassword = config.getString("sql.password");

        // worlds
        localTemplateName = config.getString("worlds.local_template_name");
        loadWorlds = config.getConfigurationSection("worlds.load");

        // components
        startStations = config.getConfigurationSection("components.start_stations");

    }

    /**
     * Creates the config with default values.
     * This will create the plugin directory if it does not exist.
     * @param config the config to save to
     */
    private void initialize(YamlConfiguration config) {

        config.set("sql.address", getSqlAddress());
        config.set("sql.database", getSqlDatabase());
        config.set("sql.username", getSqlUsername());
        config.set("sql.password", getSqlPassword());
        config.set("worlds.local_template_name", getLocalTemplateName());
        config.createSection("worlds.load");
        config.createSection("components.start_stations");

        try {
            config.save(FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
