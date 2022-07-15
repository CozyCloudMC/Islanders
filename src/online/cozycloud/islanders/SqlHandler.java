package online.cozycloud.islanders;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles processes involving the SQL database.
 * All methods should be run asynchronously!
 */
public class SqlHandler {

    private Connection connection;

    protected SqlHandler() {

        Bukkit.getScheduler().runTaskAsynchronously(Islanders.getInstance(), () -> {

            try {
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

    }

    /**
     * Creates the tables used by the plugin if they do not exist.
     * @throws SQLException thrown if the SQL connection could not be opened
     */
    private void createTables() throws SQLException {

        String localIslandsCmd = "CREATE TABLE IF NOT EXISTS local_islands(id CHAR(13) NOT NULL PRIMARY KEY, members VARCHAR(1000), last_active BIGINT(255) unsigned);",
                playerDataCmd = "CREATE TABLE IF NOT EXISTS player_data(uuid CHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), world VARCHAR(50));";

        Connection connection = getConnection();
        connection.prepareStatement(localIslandsCmd).executeUpdate();
        connection.prepareStatement(playerDataCmd).executeUpdate();

    }

    /**
     * Establishes a connection to the SQL database if it is not already open and returns it.
     * @return the SQL connection
     * @throws SQLException thrown if the SQL connection could not be opened
     */
    public Connection getConnection() throws SQLException {
        openConnection();
        return connection;
    }

    /**
     * Establishes a connection to the SQL database if it is not already open.
     * @throws SQLException thrown if the SQL connection could not be opened
     */
    private void openConnection() throws SQLException {

        if (connection == null || !connection.isValid(3)) {

            ConfigHandler config = Islanders.getConfigHandler();

            connection = DriverManager.getConnection("jdbc:mysql://" + config.getSqlAddress() + "/" +
                    config.getSqlDatabase(), config.getSqlUsername(), config.getSqlPassword());

        }

    }

}
