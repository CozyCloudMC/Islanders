package online.cozycloud.islands.mechanics.trees;

import online.cozycloud.islands.Islands;
import org.bukkit.Material;

import java.io.File;

public enum CustomTree {

    //TO BE EXPANDED
    TEST("Test"), TEST2("Test2");

    private String name;
    CustomTree(String name) {this.name = name;}

    public File getFile() {

        String fileName = null;

        switch (name) {

            case "Test":
                fileName = "ae.schem";
                break;

            case "Test2":
                fileName = "sus.schem";
                break;

        }

        return new File(Islands.getInstance().getDataFolder() + "/schematics/trees", fileName);

    }

    public Material getSapling() {

        switch (name) {

            case "Test":
            case "Test2":
                return Material.OAK_SAPLING;

        }

        return null;

    }

}
