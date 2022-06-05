package online.cozycloud.islands.mechanics.worlds;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.local.LocalIsland;
import online.cozycloud.islands.local.LocalIslandManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PortalHandler implements Listener {

    private HashMap<UUID, Long> lastNetherPortalCheck = new HashMap<>();
    private HashMap<Player, BukkitTask> teleporting = new HashMap<>();

    public PortalHandler() {
        runAbortTPCheck();
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

        if (e.getEntity() instanceof Player player) {

            switch (e.getLocation().getBlock().getType()) {

                case NETHER_PORTAL -> {
                    lastNetherPortalCheck.put(player.getUniqueId(), System.currentTimeMillis());
                    initiateNetherTeleport(player);
                }

                case END_PORTAL -> endPortalTeleport(player);
                
            }

        }

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

    /**
     * Prepares to teleport a player standing in a nether portal.
     * @param player the player to teleport
     */
    private void initiateNetherTeleport(Player player) {

        if (teleporting.containsKey(player)) return;
        World world = player.getWorld();

        // Creates a local island's nether if it does not exist
        if (world.getEnvironment() != World.Environment.NETHER) {
            LocalIsland island = Islands.getLocalIslandManager().getIsland(world);
            if (island != null && !island.hasWorld(World.Environment.NETHER)) LocalIslandManager.getLocalIslandSetupManager().addWorld(island.getID(), World.Environment.NETHER);
        }

        teleporting.put(player, Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> {

            if (player.isOnline() && isInNetherPortal(player)) {
                netherPortalTeleport(player);
                teleporting.remove(player);
            }

        }, 80));

    }

    /**
     * Teleports a player through a nether portal to the corresponding world.
     * @param player the player to teleport
     */
    private void netherPortalTeleport(Player player) {

        World world = player.getWorld();
        World.Environment toEnvironment = world.getEnvironment() != World.Environment.NETHER ? World.Environment.NETHER : World.Environment.NORMAL;
        LocalIsland island = Islands.getLocalIslandManager().getIsland(world);

        // Does not attempt to load world in case the world creation is currently taking place
        World toWorld = island != null ? island.getWorld(toEnvironment, false) : WorldHandler.getRelatedDimension(world, toEnvironment);
        if (toWorld != null) player.teleport(toWorld.getSpawnLocation());

    }

    /**
     * Teleports a player through an end portal to the corresponding world.
     * @param player the player to teleport
     */
    private void endPortalTeleport(Player player) {

        World world = player.getWorld();
        World.Environment toEnvironment = world.getEnvironment() != World.Environment.THE_END ? World.Environment.THE_END : World.Environment.NORMAL;
        LocalIsland island = Islands.getLocalIslandManager().getIsland(world);

        // Creates a local island's end if it does not exist
        if (world.getEnvironment() != World.Environment.THE_END && island != null && !island.hasWorld(World.Environment.THE_END))
            LocalIslandManager.getLocalIslandSetupManager().addWorld(island.getID(), World.Environment.THE_END);

        // Does not attempt to load world in case the world creation is currently taking place
        World toWorld = island != null ? island.getWorld(toEnvironment, false) : WorldHandler.getRelatedDimension(world, toEnvironment);
        if (toWorld != null) player.teleport(toWorld.getSpawnLocation());

    }

}
