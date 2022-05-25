package online.cozycloud.islands.local;

import com.sun.tools.javac.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import online.cozycloud.islands.Islands;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bukkit.Bukkit.createWorld;
import static org.bukkit.Bukkit.getLogger;

public class LocalIslandEvents implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {

        // Ran 1 tick later so that the player is not still in the world
        Bukkit.getScheduler().runTaskLater(Islands.getInstance(), () -> Islands.getLocalIslandManager().unloadInactiveIslandWorlds(), 1);

    }
    public LocalIslandEvents(){

        Bukkit.getPluginManager().registerEvents(this,Islands.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Islands.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
//                    Location loc = p.getLocation();
//                    World world = p.getWorld();
//                    Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockZ());
//                    p.spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText(p.getLocation().getBlock().getBiome().toString()));
                    if(p.getLocation().getBlock().getBiome().toString().equalsIgnoreCase(LocalIslandManager.getOutterBiome(p.getWorld().getEnvironment()).toString())){
                        if(p.getGameMode() == GameMode.SURVIVAL){
                            if(p.getLocation().getBlock().getType().toString().equalsIgnoreCase("Water")){
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("You are swimming into the deep turn back!"));
                            }else{
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("You are running out of air turn back!"));
                            }
                            if(p.getHealth()<=1&&p.getHealth()!=0){
                                p.setHealth(0);
                            }else{
                                p.setHealth(p.getHealth()-1);
                            }
                        }
                    }
                }
            }

        }, 1, 7);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(e.getEntity().getLocation().getBlock().getBiome().toString().equalsIgnoreCase(LocalIslandManager.getOutterBiome(e.getEntity().getWorld().getEnvironment()).toString())){
            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setKeepLevel(true);
            e.setDroppedExp(0);
            if(e.getEntity().getLocation().getBlock().getType().toString().equalsIgnoreCase("Water")) {
                e.setDeathMessage(e.getEntity().getName() + " tried to swim away");
            }else{
                e.setDeathMessage(e.getEntity().getName() + " suffocated in a tunnel");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        if(!e.isCancelled()) {
            if (e.getBlock().getBiome() == LocalIslandManager.getOutterBiome(e.getPlayer().getWorld().getEnvironment()) && e.getPlayer().getGameMode().toString().equalsIgnoreCase("Survival")) {
                e.setCancelled(true);
                e.getPlayer().spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText("You cant destroy blocks here. You're too far from your Island"));
            }
        }
    }
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent e){
        if(!e.isCancelled()){
            if(e.getBlock().getBiome()==LocalIslandManager.getOutterBiome(e.getPlayer().getWorld().getEnvironment())&&e.getPlayer().getGameMode().toString().equalsIgnoreCase("Survival")){
                e.getPlayer().spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText("You cant place blocks here. You're too far from your Island"));
                e.setCancelled(true);
            }
        }
    }

}
