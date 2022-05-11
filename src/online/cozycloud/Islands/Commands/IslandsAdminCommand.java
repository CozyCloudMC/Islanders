package online.cozycloud.Islands.Commands;

import online.cozycloud.Islands.Islands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class IslandsAdminCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("islandsadmin")) {

            if (sender.hasPermission("islands.admin")) {

                String usage = ChatColor.RED + "Usage: /isa reload";

                if (args.length >= 1) {

                    if (args[0].equalsIgnoreCase("reload")) {
                        Islands.getConfigHandler().reload();
                        sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else sender.sendMessage(usage);

                } else sender.sendMessage(usage);

            } else sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");

        }

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        List<String> result = new ArrayList<String>();

        if (cmd.getName().equalsIgnoreCase("islandsadmin")) {

            switch (args.length) {

                case 1:
                    result.add("reload");
                    break;

            }

        }

        return result;

    }

}
