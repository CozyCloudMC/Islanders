package online.cozycloud.islanders.local;

import online.cozycloud.islanders.Islanders;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocalIslandEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islanders.getInstance(), () -> Islanders.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islanders.getInstance(), () -> Islanders.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

}
