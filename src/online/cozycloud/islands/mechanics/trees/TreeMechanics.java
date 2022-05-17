package online.cozycloud.islands.mechanics.trees;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import online.cozycloud.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class TreeMechanics implements Listener {

    private BlockMask treeMask;
    private HashMap<Material, ArrayList<CustomTree>> possibleTrees;

    public TreeMechanics() {
        treeMask = createTreeMask();
        possibleTrees = createPossibleTrees();
    }

    @EventHandler
    public void onGrow(StructureGrowEvent event) {

        Location loc = event.getLocation();
        Block origin = get2x2SaplingOrigin(loc.getBlock());

        if (origin != null) {

            Material sapling = loc.getBlock().getType();
            CustomTree tree = getRandomTree(sapling);

            if (tree != null) {

                //Removes saplings
                for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z) origin.getLocation().clone().add(x, 0, z).getBlock().setType(Material.AIR);
                event.setCancelled(true);

                pasteTree(tree, origin.getLocation());

            }

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

    /**
     * Gets a random custom tree type.
     * @param sapling the type of sapling being grown
     * @return a random tree type
     */
    private CustomTree getRandomTree(Material sapling) {
        ArrayList<CustomTree> trees = new ArrayList<>(possibleTrees.get(sapling));
        return trees.isEmpty() ? null : trees.get(new Random().nextInt(trees.size()));
    }

    /**
     * Asynchronously pastes a tree schematic at a location with a random rotation.
     * Tree can only replace air and terrain blocks like dirt.
     * @param type tree type to paste
     * @param loc origin
     */
    private void pasteTree(CustomTree type, Location loc) {

        Bukkit.getScheduler().runTaskAsynchronously(Islands.getInstance(), new Runnable() {

            @Override
            public void run() {

                EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(loc.getWorld()));
                session.setMask(treeMask);

                //Random rotation
                double angle = new Random().nextInt(4) * 90;
                int xOffset = angle == 270 || angle == 180 ? 1 : 0, zOffset = angle == 90 || angle == 180 ? 1 : 0;

                try {
                    FaweAPI.load(type.getFile()).paste(session, BlockVector3.at(loc.getX()+xOffset, loc.getY(), loc.getZ()+zOffset), false, new AffineTransform().rotateY(angle));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                session.flushQueue();

            }

        });

    }

    /**
     * Creates a mask that only replaces non-solid blocks or soft terrain blocks.
     * @return the mask
     */
    private BlockMask createTreeMask() {

        ArrayList<Material> canReplace = new ArrayList<>();

        // Non-solid blocks
        for (Material type : Material.values()) if (!type.isSolid()) canReplace.add(type);

        // Soft terrain blocks
        canReplace.addAll(Arrays.asList(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.ROOTED_DIRT,
                Material.PODZOL, Material.MYCELIUM, Material.SAND, Material.RED_SAND, Material.CLAY, Material.MOSS_BLOCK));

        BlockMask mask = new BlockMask();
        for (Material type : canReplace) mask.add(BukkitAdapter.asBlockType(type));
        return mask;

    }

    /**
     * Create a set of possible trees for each sapling type.
     * @return a set of possible custom trees per sapling
     */
    private HashMap<Material, ArrayList<CustomTree>> createPossibleTrees() {

        HashMap<Material, ArrayList<CustomTree>> result = new HashMap<>();
        Material[] saplings = {Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING, Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING};

        for (Material sapling : saplings) {
            ArrayList<CustomTree> trees = new ArrayList<>();
            for (CustomTree type : CustomTree.values()) if (type.getSapling() == sapling) trees.add(type);
            result.put(sapling, trees);
        }

        return result;

    }

}
