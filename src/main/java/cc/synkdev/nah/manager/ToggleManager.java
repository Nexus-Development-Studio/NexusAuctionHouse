package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;

import java.io.*;
import java.nio.file.Files;

public class ToggleManager {
    private ToggleManager() {
        /* This utility class should not be instantiated */
    }

    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static final File file = new File(new File(core.getDataFolder(), "data"), "toggle.yml");
    public static void set(Boolean bool) {
        core.setToggle(bool);
        try {
            Files.delete(file.toPath());
            if (!file.createNewFile()) {
                throw new IOException("Failed to create toggle.yml");
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(bool.toString());
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void read() {
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String ln;
                while ((ln = reader.readLine()) != null) {
                    core.setToggle(Boolean.valueOf(ln));
                }
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
