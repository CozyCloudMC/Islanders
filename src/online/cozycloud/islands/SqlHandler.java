package online.cozycloud.islands;

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

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                openConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

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

        if (connection == null || connection.isClosed()) {

            ConfigHandler config = Islands.getConfigHandler();

            connection = DriverManager.getConnection("jdbc:mysql://" + config.getSqlAddress() + "/" +
                    config.getSqlDatabase(), config.getSqlUsername(), config.getSqlPassword());

        }

    }

}
