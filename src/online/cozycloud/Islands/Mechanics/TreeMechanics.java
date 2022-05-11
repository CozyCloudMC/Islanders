package online.cozycloud.Islands.Mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TreeMechanics implements Listener {

    @EventHandler
    public void onGrow(StructureGrowEvent event) {

        Location loc = event.getLocation();
        Block origin = get2x2SaplingOrigin(loc.getBlock());

        if (origin != null) {

            event.setCancelled(true);

            // CUSTOM TREE PASTING

        }

    }

    /**
     * Gets the origin of a 2x2 sapling formation if it exists.
     * @param block the sapling block to check
     * @return the origin of a 2x2 sapling formation or null if one does not exist
     */
    private Block get2x2SaplingOrigin(Block block) {

        if (!(block.getBlockData() instanceof Sapling)) return null;
        Material saplingType = block.getType();

        // Checks 4 possible 2x2 formations around the sapling.
        // The original sapling location will be prioritized if it can be used.
        for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z) {
            Block b = block.getLocation().clone().subtract(x, 0, z).getBlock();
            if (is2x2SaplingFormation(b)) return b;
        }

        return null;

    }

    /**
     * Checks if a 2x2 sapling formation exists given an origin block.
     * @param origin the sapling block at the smallest corner
     * @return true if all blocks in the 2x2 formation are the same sapling
     */
    private boolean is2x2SaplingFormation(Block origin) {

        if (!(origin.getBlockData() instanceof Sapling)) return false;
        Material saplingType = origin.getType();

        for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z) {

            if (x == 0 && z == 0) continue; //Ignore origin which has already been checked

            Block b = origin.getLocation().clone().add(x, 0, z).getBlock();
            if (b.getType() != saplingType) return false;

        }

        return true;

    }

}
