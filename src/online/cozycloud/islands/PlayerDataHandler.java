package online.cozycloud.islands;

import online.cozycloud.islands.local.LocalIsland;
import org.bukkit.Bukkit;
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
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {

        UUID uuid = event.getUniqueId();
        String name = event.getName();

        // Updates players' username if it has changed
        String insertCmd = "INSERT INTO player_data(uuid, name) VALUES ('" + uuid + "', '" + name + "') " +
                "ON DUPLICATE KEY UPDATE name = '" + name + "';";

        // Gets last world
        String selectCmd = "SELECT world FROM player_data WHERE uuid = '" + uuid + "';";

        try {

            Connection connection = Islands.getSqlHandler().getConnection();
            connection.prepareStatement(insertCmd).executeUpdate();
            ResultSet result = connection.prepareStatement(selectCmd).executeQuery();

            // Loads an island's world if the player logs in there
            while (result.next()) {
                String world = result.getString("world");
                LocalIsland island = world != null && world.startsWith("0") ? Islands.getLocalIslandManager().getIsland(world) : null;
                if (island != null) Bukkit.getScheduler().runTask(Islands.getInstance(), island::loadWorld);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        String world = player.getWorld().getName();

        // Updates the player's last world
        String insertCmd = "INSERT INTO player_data(uuid, name, world) VALUES ('" + player.getUniqueId() + "', '" + player.getName() + "', '" + world + "') " +
                "ON DUPLICATE KEY UPDATE world = '" + world + "';";

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), () -> {

            try {
                Islands.getSqlHandler().getConnection().prepareStatement(insertCmd).executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

    }

}
