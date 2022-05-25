package online.cozycloud.islands;

import online.cozycloud.islands.local.LocalIsland;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataHandler implements Listener {

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {

        UUID uuid = e.getUniqueId();
        String name = e.getName();

        // Already async
        try {
            handleLogin(uuid, name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Updates the player's username and loads an island's world if the player logs in there.
     * This includes islands not owned by the player.
     * @param uuid the UUID of the player
     * @param name the name of the player
     * @throws SQLException thrown if a connection could not be made to the database
     */
    private void handleLogin(UUID uuid, String name) throws SQLException {

        // Updates players' username if it has changed
        String insertCmd = "INSERT INTO player_data(uuid, name) VALUES ('" + uuid + "', '" + name + "') " +
                "ON DUPLICATE KEY UPDATE name = '" + name + "';";

        // Gets last world
        String selectCmd = "SELECT world FROM player_data WHERE uuid = '" + uuid + "';";

        Connection connection = Islands.getSqlHandler().getConnection();
        connection.prepareStatement(insertCmd).executeUpdate();
        ResultSet result = connection.prepareStatement(selectCmd).executeQuery();

        // Loads an island's worlds if the player logs in there
        while (result.next()) {

            String world = result.getString("world");

            if (world != null && world.startsWith("0")) {

                String islandID = world.split("_")[0];
                LocalIsland island = Islands.getLocalIslandManager().getIsland(islandID);

                if (island != null) Bukkit.getScheduler().runTask(Islands.getInstance(), island::loadWorlds); // Sync

            }

        }

    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {

        Player player = e.getPlayer();
        World world = player.getWorld();

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                updateLastWorld(player, world);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

    }

    /**
     * Saves the name of the last world a player has entered to the database.
     * This is used to tell where a player is about to spawn in on next join so that the world can be loaded if it is not already.
     * @param player the player to update info
     * @param world the new world
     * @throws SQLException thrown if a connection could not be made to the database
     */
    private void updateLastWorld(Player player, World world) throws SQLException {

        String worldName = world != null ? world.getName() : null;
        String insertCmd = "INSERT INTO player_data(uuid, name, world) VALUES ('" + player.getUniqueId() + "', '" + player.getName() + "', '" + worldName + "') " +
                "ON DUPLICATE KEY UPDATE world = '" + worldName + "';";

        Islands.getSqlHandler().getConnection().prepareStatement(insertCmd).executeUpdate();

    }

}
