package online.cozycloud.islands.commands;

import online.cozycloud.islands.Islands;
import online.cozycloud.islands.local.LocalIsland;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class IslandsCommand implements TabExecutor {

    private HashMap<UUID, Long> abandonConfirmation = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("islands") && sender instanceof Player player) {

            String usage = ChatColor.RED + "Usage: /" + label + " <go|abandon>";
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

                    if (island != null) {

                        long lastConfirmation = abandonConfirmation.containsKey(uuid) ? abandonConfirmation.get(uuid) : 0;

                        // 15 seconds to confirm
                        if (System.currentTimeMillis() - lastConfirmation < 15000) {
                            sender.sendMessage(ChatColor.DARK_RED + "Abandoning your island...");
                            island.abandon(uuid);
                            abandonConfirmation.remove(uuid);
                        }

                        else {
                            abandonConfirmation.put(uuid, System.currentTimeMillis());
                            if (island.getMembers().size() <= 1) sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "WARNING:" + ChatColor.DARK_RED + " You are the only member of this island; the island will be deleted.");
                            sender.sendMessage(ChatColor.RED + "Type " + ChatColor.WHITE + "/" + label + " abandon" + ChatColor.RED + " again to confirm.");
                        }

                    }

                    else sender.sendMessage(ChatColor.RED + "You do not have an island yet!");

                } else sender.sendMessage(usage);

            } else sender.sendMessage(usage);

        }

        return true;

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {

        List<String> result = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("islands")) {

            switch (args.length) {
                case 1 -> result.addAll(List.of("go", "abandon"));
            }

        }

        return result;

    }

}
