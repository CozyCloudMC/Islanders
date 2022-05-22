package online.cozycloud.islands.commands;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.local.LocalIsland;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandsCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("islands") && sender instanceof Player player) {

            String usage = ChatColor.RED + "Usage: /is <go|abandon>";
            UUID uuid = player.getUniqueId();

            if (args.length >= 1) {

                if (args[0].equalsIgnoreCase("go")) {

                    LocalIsland island = Islands.getLocalIslandManager().getMainIsland(uuid);

                    if (island != null) {
                        sender.sendMessage(ChatColor.WHITE + "Teleporting to your island...");
                        island.spawn(player);
                    }

                    else sender.sendMessage(ChatColor.RED + "You do not have an island yet!");

                }

                else if (args[0].equalsIgnoreCase("abandon")) {

                    LocalIsland island = Islands.getLocalIslandManager().getMainIsland(uuid);

                    // ADD CONFIRMATION LATER
                    if (island != null) {
                        sender.sendMessage(ChatColor.DARK_RED + "Abandoning your island...");
                        island.abandon(uuid);
                    }

                    else sender.sendMessage(ChatColor.RED + "You do not have an island yet!");

                } else sender.sendMessage(usage);

            } else sender.sendMessage(usage);

        }

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        List<String> result = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("islands")) {

            switch (args.length) {

                case 1:
                    result.addAll(List.of("go", "abandon"));
                    break;

            }

        }

        return result;

    }

}
