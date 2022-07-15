package online.cozycloud.islanders.mechanics.worlds;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DragonPrevention implements Listener {

    /**
     * Attempts to place an end portal block at the top of an end world.
     * This is a workaround used to prevent the Ender Dragon from spawning because the game will think the dragon is already dead.
     * No event exists for the creation of the dragon and portal; this is the approach used by BentoBox/Skyblock.
     * @param world the world entered
     */
    private void tryPlacePortal(World world) {

        if (world == null || world.getEnvironment() != World.Environment.THE_END) return;

        Block block = world.getBlockAt(0, world.getMaxHeight()-1, 0);
        if (block.getType() != Material.END_PORTAL) block.setType(Material.END_PORTAL);

    }

    /**
     * Checks whether a block qualifies to be an end portal and should be protected.
     * @param block the block to check
     * @return true if protected
     */
    private boolean isProtectedBlock(Block block) {
        World world = block.getWorld();
        return block.getX() == 0 && block.getZ() == 0 && block.getY() == world.getMaxHeight()-1 && world.getEnvironment() == World.Environment.THE_END;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        tryPlacePortal(e.getPlayer().getWorld());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        tryPlacePortal(e.getPlayer().getWorld());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (isProtectedBlock(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (isProtectedBlock(e.getBlock())) e.setCancelled(true);
    }

}
