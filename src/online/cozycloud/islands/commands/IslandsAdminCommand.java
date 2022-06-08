package online.cozycloud.islands.commands;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.local.LocalIsland;
import online.cozycloud.islands.local.LocalIslandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IslandsAdminCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("islandsadmin")) {

            if (sender.hasPermission("islands.admin")) {

                String usage = ChatColor.RED + "Usage: /" + label + " <reload|worlds|tp|test>";

                if (args.length >= 1) {

                    if (args[0].equalsIgnoreCase("reload")) {

                        Islands.getConfigHandler().reload();
                        Islands.getWorldHandler().loadMainWorlds();
                        Islands.getStartStationManager().reload();

                        sender.sendMessage(ChatColor.GREEN + "Plugin reloaded!");

                    }

                    else if (args[0].equalsIgnoreCase("worlds")) {

                        String worlds = "Worlds: ";
                        for (World w : Bukkit.getWorlds()) worlds += ChatColor.GREEN + w.getName() + ChatColor.WHITE + ", ";
                        if (worlds.endsWith(", ")) worlds = worlds.substring(0, worlds.length()-2);

                        sender.sendMessage(worlds);

                    }

                    else if (args[0].equalsIgnoreCase("tp") && sender instanceof Player player) {

                        if (args.length >= 2) {

                            World world = Bukkit.getWorld(args[1]);

                            if (world != null) {

                                player.teleport(world.getSpawnLocation());
                                sender.sendMessage("Teleported to " + ChatColor.GREEN + world.getName());

                            } else sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not loaded!");

                        } else sender.sendMessage(ChatColor.RED + "Usage: /" + label + " tp <world>");

                    }

                    else if (args[0].equalsIgnoreCase("delete") && sender instanceof Player player) {

                        if (args.length >= 2) LocalIslandManager.getLocalIslandSetupManager().deleteIsland(args[1], sender);
                        else sender.sendMessage(ChatColor.RED + "Usage: /" + label + " delete <name>");

                    } else sender.sendMessage(usage);

                } else sender.sendMessage(usage);

            } else sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");

        }

        return true;

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        List<String> result = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("islandsadmin")) {

            switch (args.length) {

                case 1 -> result.addAll(List.of("reload", "worlds", "tp", "delete"));

                case 2 -> {

                    if (args[0].equalsIgnoreCase("tp")) for (World w : Bukkit.getWorlds()) {
                        String name = w.getName();
                        if (name.toLowerCase().startsWith(args[1].toLowerCase())) result.add(name); // Hides irrelevant results
                    }

                    else if (args[0].equalsIgnoreCase("delete")) for (LocalIsland island : Islands.getLocalIslandManager().getIslands()) {
                        String name = island.getID();
                        if (name.toLowerCase().startsWith(args[1].toLowerCase())) result.add(name);
                    }

                }

            }

        }

        return result;

    }

}
