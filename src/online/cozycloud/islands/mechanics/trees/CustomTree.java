package online.cozycloud.islands.mechanics.trees;

import online.cozycloud.islands.Islands;
import org.bukkit.Material;

import java.io.File;

// Schematic sources
// https://www.planetminecraft.com/project/fantasy-tree-repository/
// https://www.planetminecraft.com/project/native-trees-of-europe-template-repository-1779952/

public enum CustomTree {

    WOW25("WOW25"), WOW26("WOW26"), WOW27("WOW27"), WOW28("WOW28"), WOW29("WOW29"), WOW73("WOW73"), WOW80("WOW80"),
    WOW81("WOW81"), WOW82("WOW82"), WOW83("WOW83"), WOW84("WOW84"), WOW86("WOW86"), WOW87("WOW87"), WOW88("WOW88"),
    NA2L("NA2L"), NA3L("NA3L"), NA6L("NA6L"), NA7L("NA7L"), NA9L("NA9L"), NA12L("NA12L"),
    NA12S("NA12S"), NA14L("NA14L"), NA18L("NA18L"),
    SA4L("SA4L"), SA5L("SA5L"), SA5S("SA5S"), SA6L("SA6L"), SA9L("SA9L"), SA11L("SA11L");

    private final String NAME;
    CustomTree(String name) {NAME = name;}

    public File getFile() {
        return new File(Islands.getInstance().getDataFolder() + "/schematics/trees", NAME + ".schem");
    }

    public Material getSapling() {

        return switch (NAME) {

            case "WOW80", "WOW81", "WOW82", "WOW83", "WOW84", "WOW86", "WOW87", "WOW88", "SA6L", "NA3L", "NA12L", "NA12S", "NA14L" -> Material.OAK_SAPLING;
            case "WOW25", "WOW26", "WOW27", "WOW28", "WOW29", "NA2L", "NA6L", "NA9L", "NA18L" -> Material.SPRUCE_SAPLING;
            case "SA11L", "NA7L" -> Material.BIRCH_SAPLING;
            case "WOW73", "SA4L", "SA5L", "SA5S", "SA9L" -> Material.JUNGLE_SAPLING;
            default -> null;

        };

    }

}
