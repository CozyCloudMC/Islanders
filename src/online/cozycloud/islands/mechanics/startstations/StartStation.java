package online.cozycloud.islands.mechanics.startstations;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import online.cozycloud.islands.Islands;
import online.cozycloud.islands.local.LocalIslandManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.UUID;

public class StartStation {

    private final ChatColor COLOR;
    private final Block LEVER;
    private final int X1;
    private final int X2;
    private final int Z1;
    private final int Z2;
    private final Hologram HOLOGRAM;

    private BukkitTask timer;
    private int time = 5;

    public StartStation(ChatColor color, Block lever, int x1, int x2, int z1, int z2) {

        COLOR = color;
        LEVER = lever;
        X1 = x1;
        X2 = x2;
        Z1 = z1;
        Z2 = z2;

        HOLOGRAM = HologramsAPI.createHologram(Islands.getInstance(), lever.getLocation().clone().add(0.5, 2, 0.5));
        HOLOGRAM.appendTextLine(COLOR + "" + ChatColor.BOLD + "Start Your Island");
        HOLOGRAM.appendTextLine("Stand in the boat with your team");
        HOLOGRAM.appendTextLine("and flip the lever to begin!");

        resetLever();

    }

    /**
     * Handles the starting and stopping of the timer when the lever is interacted with.
     */
    public void interact() {

        if (timer != null) cancelTimer();
        if (LEVER.getBlockData() instanceof Switch lever && !lever.isPowered()) return;

        time = 5;
        ArrayList<UUID> original = getPlayersInZone();

        if (original.isEmpty()) {
            resetLever();
            return;
        }

        ArrayList<Player> players = new ArrayList<>();

        for (UUID uuid : original) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) players.add(p);
        }

        timer = Bukkit.getScheduler().runTaskTimer(Islands.getInstance(), () -> {

            if (getPlayersInZone().equals(original)) {

                String msg = time > 0 ? "Creating island in " + ChatColor.YELLOW + ChatColor.BOLD + time + ChatColor.RESET + "..." :
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Creating island...";

                for (Player p : players) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
                    if (time <= 4) p.playSound(p.getLocation(), time > 0 ? Sound.BLOCK_NOTE_BLOCK_HAT : Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }

            } else {
                cancelTimer();
                return;
            }

            if (time <= 0) {
                LocalIslandManager.getLocalIslandSetupManager().createIsland(players);
                cancelTimer();
            } else --time;

        }, 0, 20);

    }

    /**
     * Cancels the timer and deletes the hologram.
     */
    public void deactivate() {
        if (HOLOGRAM != null) HOLOGRAM.delete();
        cancelTimer();
    }

    /**
     * Cancels the timer and resets the lever.
     */
    public void cancelTimer() {

        resetLever();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

    }

    /**
     * Resets the lever to the off position.
     */
    private void resetLever() {

        if (LEVER.getBlockData() instanceof Switch lever && lever.isPowered()) {

            lever.setPowered(false);
            LEVER.setBlockData(lever);

            LEVER.getWorld().playSound(LEVER.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 0);

        }

    }

    /**
     * Gets the UUIDs of the players currently in this station's zone.
     * @return the UUIDs of the players
     */
    private ArrayList<UUID> getPlayersInZone() {

        ArrayList<UUID> result = new ArrayList<>();

        for (Player p : LEVER.getWorld().getPlayers()) {

            UUID uuid = p.getUniqueId();
            if (p.getGameMode() == GameMode.SPECTATOR || !Islands.getLocalIslandManager().getIslandsWithMember(uuid).isEmpty()) continue;

            Location loc = p.getLocation();
            int x = loc.getBlockX(), z = loc.getBlockZ();

            if (x >= X1 && x <= X2 && z >= Z1 && z <= Z2) result.add(uuid);

        }

        return result;

    }

}
