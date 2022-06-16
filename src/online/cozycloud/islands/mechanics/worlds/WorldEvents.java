package online.cozycloud.islands.mechanics.worlds;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import online.cozycloud.islands.Islands;
import online.cozycloud.islands.Utils;
import online.cozycloud.islands.local.LocalIsland;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class WorldEvents implements Listener {

    protected WorldEvents() {runBoundsCheck();}

    /**
     * Checks if players are out of the island bounds and damages them if they are.
     */
    private void runBoundsCheck() {

        Bukkit.getScheduler().runTaskTimer(Islands.getInstance(), () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {

                Block block = p.getLocation().getBlock();
                if (!Utils.hasPlayingGameMode(p) || block.getBiome() != Utils.getOutterBiome(p.getWorld().getEnvironment())) continue;

                String msg;
                if (block.getType() == Material.WATER) msg = "You are swimming into the deep, turn back!";
                else if (block.getY() < block.getWorld().getSeaLevel()) msg = "You are running out of air, turn back!";
                else msg = "You are traveling too far, turn back!";

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
                p.damage(1);

            }

        }, 1, 7);

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {

        Player player = e.getPlayer();
        World world = player.getWorld();
        LocalIsland island = Islands.getLocalIslandManager().getIsland(world);

        World respawnWorld = island != null ? island.getWorld(World.Environment.NORMAL, true) : world;
        if (respawnWorld != null) e.setRespawnLocation(respawnWorld.getSpawnLocation());

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        Player player = e.getEntity();
        Block block = player.getLocation().getBlock();

        if (block.getBiome() == Utils.getOutterBiome(player.getWorld().getEnvironment())) {

            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setKeepLevel(true);
            e.setDroppedExp(0);

            // Only replaces the death message if they were killed by the plugin and not other causes
            if (e.getDeathMessage() == null || e.getDeathMessage().endsWith("died")) {

                String msgSuffix;
                if (block.getType() == Material.WATER) msgSuffix = " tried to swim away";
                else if (block.getY() < block.getWorld().getSeaLevel()) msgSuffix = " suffocated underground";
                else msgSuffix = " traveled too far away from their island";

                e.setDeathMessage(e.getEntity().getName() + msgSuffix);

            }

        }

        // Hides all death messages in the spawn world
        if (player.getWorld().equals(Islands.getWorldHandler().getMainWorld())) e.setDeathMessage(null);

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (Utils.hasPlayingGameMode(player) && block.getBiome() == Utils.getOutterBiome(e.getPlayer().getWorld().getEnvironment())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot destroy blocks here. You are too far away!");
        }

    }
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (Utils.hasPlayingGameMode(player) && block.getBiome() == Utils.getOutterBiome(e.getPlayer().getWorld().getEnvironment())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks here. You are too far away!");
        }

    }

}
