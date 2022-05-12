package online.cozycloud.islands.mechanics.trees;

import online.cozycloud.islands.Islands;

import java.io.File;

public enum CustomTree {

    TEST("Test");

    private String name;
    CustomTree(String name) {this.name = name;}

    public File getFile() {

        String fileName = null;

        switch (name) {

            case "Test":
                fileName = "ae.schem";
                break;

        }

        return new File(Islands.getInstance().getDataFolder() + "/schematics/trees", fileName);

    }

}
