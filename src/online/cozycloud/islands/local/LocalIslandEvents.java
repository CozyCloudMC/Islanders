package online.cozycloud.islands.local;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import online.cozycloud.islands.Islands;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LocalIslandEvents implements Listener {

    public LocalIslandEvents() {
        runBoundsCheck();
    }

    /**
     * Checks if players are out of their island bounds and damages them if they are.
     */
    private void runBoundsCheck() {

        Bukkit.getScheduler().runTaskTimer(Islands.getInstance(), () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {

                Block block = p.getLocation().getBlock();
                if (p.getGameMode() != GameMode.SURVIVAL || block.getBiome() != LocalIslandManager.getOutterBiome(p.getWorld().getEnvironment())) continue;

                String msg = block.getType() == Material.WATER ? "You are swimming into the deep, turn back!" : "You are running out of air, turn back!";
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
                p.damage(1);

            }

        }, 1, 7);

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        LocalIsland island = Islands.getLocalIslandManager().getMainIsland(player.getUniqueId());

        if (island != null) {
            World islandWorld = island.getWorld(World.Environment.NORMAL, true);
            Location respawnLoc = islandWorld != null ? islandWorld.getSpawnLocation() : Islands.getWorldHandler().getMainWorld().getSpawnLocation();
            event.setRespawnLocation(respawnLoc);
        }

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        Player player = e.getEntity();
        Block block = player.getLocation().getBlock();

        if (block.getBiome() == LocalIslandManager.getOutterBiome(player.getWorld().getEnvironment())) {

            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setKeepLevel(true);
            e.setDroppedExp(0);

            String msgSuffix = block.getType() == Material.WATER ? " tried to swim away" : " suffocated in a tunnel";
            e.setDeathMessage(e.getEntity().getName() + msgSuffix);

        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (block.getBiome() == LocalIslandManager.getOutterBiome(e.getPlayer().getWorld().getEnvironment()) && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot destroy blocks here. You are too far from your island!");
        }

    }
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent e){

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (block.getBiome() == LocalIslandManager.getOutterBiome(e.getPlayer().getWorld().getEnvironment()) && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks here. You are too far from your island!");
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

}
