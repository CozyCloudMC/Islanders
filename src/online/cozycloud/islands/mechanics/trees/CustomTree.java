package online.cozycloud.islands.mechanics.trees;

import online.cozycloud.islands.Islands;
import org.bukkit.Material;

import java.io.File;

public enum CustomTree {

    // TO BE EXPANDED
    TEST("Test"), TEST2("Test2");

    private final String NAME;
    CustomTree(String name) {NAME = name;}

    public File getFile() {

        String fileName = switch (NAME) {
            case "Test" -> "ae.schem";
            case "Test2" -> "sus.schem";
            default -> throw new IllegalStateException("Unexpected value");
        };

        return new File(Islands.getInstance().getDataFolder() + "/schematics/trees", fileName);

    }

    public Material getSapling() {

        return switch (NAME) {
            case "Test", "Test2" -> Material.OAK_SAPLING;
            default -> null;
        };

    }

}
