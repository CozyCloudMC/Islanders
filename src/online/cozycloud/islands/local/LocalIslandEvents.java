package online.cozycloud.islands.local;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import online.cozycloud.islands.Islands;
import online.cozycloud.islands.mechanics.worlds.WorldHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LocalIslandEvents implements Listener {

    private HashMap<UUID, Long> lastNetherPortalCheck = new HashMap<>();
    private HashMap<Player, BukkitTask> teleporting = new HashMap<>();

    public LocalIslandEvents() {
        runBoundsCheck();
        runAbortTPCheck();
    }

    /**
     * Checks if players are out of their island bounds and damages them if they are.
     */
    private void runBoundsCheck() {

        Bukkit.getScheduler().runTaskTimer(Islands.getInstance(), () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {

                Block block = p.getLocation().getBlock();
                if (p.getGameMode() != GameMode.SURVIVAL || block.getBiome() != LocalIslandManager.getOutterBiome(p.getWorld().getEnvironment())) continue;

                String msg;
                if (block.getType() == Material.WATER) msg = "You are swimming into the deep, turn back!";
                else if (block.getY() < block.getWorld().getSeaLevel()) msg = "You are running out of air, turn back!";
                else msg = "You are too far from your island, turn back!";

                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
                p.damage(1);

            }

        }, 1, 7);

    }

    /**
     * Checks if a player is no longer in a nether portal and removes them from the teleporting list.
     */
    private void runAbortTPCheck() {

        Bukkit.getScheduler().runTaskTimer(Islands.getInstance(), () -> {

            for (Player p : new ArrayList<>(teleporting.keySet())) {

                if (p == null || !p.isOnline() || !isInNetherPortal(p)) {
                    teleporting.get(p).cancel();
                    teleporting.remove(p);
                }

            }

        }, 1, 1);

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

            // Only replaces the death message if they were killed by the plugin and not other causes
            if (e.getDeathMessage() == null || e.getDeathMessage().endsWith("died")) {

                String msgSuffix;
                if (block.getType() == Material.WATER) msgSuffix = " tried to swim away";
                else if (block.getY() < block.getWorld().getSeaLevel()) msgSuffix = " suffocated underground";
                else msgSuffix = " traveled too far away from their island";

                e.setDeathMessage(e.getEntity().getName() + msgSuffix);

            }

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
    public void onPlayerPlace(BlockPlaceEvent e) {

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

    @EventHandler // Disables default nether portal creation because it is created in the wrong world
    public void onPortalCreate(PortalCreateEvent e) {
        if (e.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) e.setCancelled(true);
    }

    @EventHandler // Disables default nether portal player teleportation
    public void onPlayerPortalTP(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) event.setCancelled(true);
    }

    @EventHandler // Called constantly when entity is in a portal
    public void onPortalEnter(EntityPortalEnterEvent e) {

        if (e.getEntity() instanceof Player player && e.getLocation().getBlock().getType() == Material.NETHER_PORTAL) {
            lastNetherPortalCheck.put(player.getUniqueId(), System.currentTimeMillis());
            initiateNetherTeleport(player);
        }

    }

    /**
     * Prepares to teleport a player standing in a nether portal.
     * @param player the player to teleport
     */
    private void initiateNetherTeleport(Player player) {

        UUID uuid = player.getUniqueId();
        if (teleporting.containsKey(player)) return;

        World world = player.getWorld();

        // Creates a local island's nether if it does not exist
        if (world.getEnvironment() == World.Environment.NORMAL) {
            LocalIsland island = Islands.getLocalIslandManager().getIsland(world);
            if (island != null && !island.hasWorld(World.Environment.NETHER)) LocalIslandManager.getLocalIslandSetupManager().addWorld(island.getID(), World.Environment.NETHER);
        }

        teleporting.put(player, Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> {

            if (player.isOnline() && isInNetherPortal(player)) {
                netherTeleport(player);
                teleporting.remove(player);
            }

        }, 80));

    }

    /**
     * Teleports a player through a nether portal to the corresponding world.
     * @param player the player to teleport
     */
    private void netherTeleport(Player player) {

        World world = player.getWorld();
        World.Environment toEnvironment = world.getEnvironment() == World.Environment.NORMAL ? World.Environment.NETHER : World.Environment.NORMAL;
        LocalIsland island = Islands.getLocalIslandManager().getIsland(world);

        // Does not attempt to load world in case the world creation is currently taking place
        World toWorld = island != null ? island.getWorld(toEnvironment, false) : WorldHandler.getRelatedDimension(world, toEnvironment);
        if (toWorld != null) player.teleport(toWorld.getSpawnLocation());

    }

    /**
     * Checks if a player is in a nether portal.
     * @param player the player to check
     * @return true if in nether portal
     */
    private boolean isInNetherPortal(Player player) {

        UUID uuid = player.getUniqueId();
        if (!lastNetherPortalCheck.containsKey(uuid)) return false;

        // Last check must have happened at least 100 milliseconds ago
        return System.currentTimeMillis() - lastNetherPortalCheck.get(uuid) <= 100;

    }

}
