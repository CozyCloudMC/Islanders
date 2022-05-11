package online.cozycloud.Islands;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.math.BlockVector3;
import online.cozycloud.Islands.Commands.IslandsAdminCommand;
import online.cozycloud.Islands.Commands.IslandsCommand;
import online.cozycloud.Islands.Mechanics.TreeMechanics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Islands extends JavaPlugin {

    private static Islands instance;
    public static Islands getInstance() {return instance;}

    private static Config config;
    public static Config getConfigHandler() {return config;}

    @Override
    public void onEnable() {

        instance = this;
        config = new Config();

        getCommand("islands").setExecutor(new IslandsCommand());
        getCommand("islandsadmin").setExecutor(new IslandsAdminCommand());

        Bukkit.getPluginManager().registerEvents(new TreeMechanics(), this);

    }

    public static boolean pasteSchematic(File file, Location loc, boolean pasteAir) {

        try {
            FaweAPI.load(file).paste(FaweAPI.getWorld(loc.getWorld().getName()), BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), false, pasteAir, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

}