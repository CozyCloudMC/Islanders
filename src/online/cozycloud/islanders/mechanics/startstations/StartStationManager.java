package online.cozycloud.islanders.mechanics.startstations;

import online.cozycloud.islanders.Islanders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;

public class StartStationManager implements Listener {

    private HashMap<Block, StartStation> startStations = new HashMap<>();

    public StartStationManager() {
        Bukkit.getScheduler().runTaskLater(Islanders.getInstance(), this::reload, 1);
    }

    public void reload() {

        for (StartStation ss : startStations.values()) ss.deactivate();
        startStations.clear();

        ConfigurationSection startStationsSection = Islanders.getConfigHandler().getStartStations();
        if (startStationsSection == null) return;

        for (String id : startStationsSection.getKeys(false)) {

            String x = startStationsSection.getString(id + ".x"), z = startStationsSection.getString(id + ".z");
            String leverLoc = startStationsSection.getString(id + ".lever");
            if (x == null || z == null || leverLoc == null) continue;

            String[] xSplit = x.split(";"), zSplit = z.split(";"), leverSplit = leverLoc.split(";");

            ChatColor color;
            Block lever;
            int x1, x2, z1, z2;

            try {
                lever = Islanders.getWorldHandler().getMainWorld().getBlockAt(Integer.parseInt(leverSplit[0]), Integer.parseInt(leverSplit[1]), Integer.parseInt(leverSplit[2]));
                x1 = Integer.parseInt(xSplit[0]);
                x2 = Integer.parseInt(xSplit[1]);
                z1 = Integer.parseInt(zSplit[0]);
                z2 = Integer.parseInt(zSplit[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }

            try {
                color = ChatColor.valueOf(id);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                continue;
            }

            startStations.put(lever, new StartStation(color, lever, x1, x2, z1, z2));

        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();

        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK || block == null || block.getType() != Material.LEVER) return;

        // Ran later so lever's BlockData updates
        Bukkit.getScheduler().runTaskLater(Islanders.getInstance(), () -> {

            if (!startStations.containsKey(block)) return;

            StartStation startStation = startStations.get(block);
            startStation.interact();

        }, 1);

    }

}
