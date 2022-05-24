package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocalIslandEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

}
