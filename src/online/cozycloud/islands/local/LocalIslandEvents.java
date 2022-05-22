package online.cozycloud.islands.local;

import online.cozycloud.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LocalIslandEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        World world = player.getWorld();

        tryUnloadWorld(world);

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        World world = player.getWorld();

        tryUnloadWorld(world);

    }

    private void tryUnloadWorld(World world) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> {

            // Unloads an island world if there are no players in it and no online members
            LocalIsland island = Islands.getLocalIslandManager().getIsland(world.getName());
            if (island != null && island.hasNoRelevantPlayers()) Islands.getWorldHandler().safelyUnloadWorld(world, true);

        }, 1);

    }

}
