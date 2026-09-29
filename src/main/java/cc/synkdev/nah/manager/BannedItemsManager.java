package cc.synkdev.nah.manager;

import cc.synkdev.nah.NexusAuctionHouse;
import org.bukkit.Material;

import java.io.*;
import java.nio.file.Files;

public class BannedItemsManager {
    private BannedItemsManager() {
        /* This utility class should not be instantiated */
    }

    private static final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    private static final File file = new File(new File(core.getDataFolder(), "data"), "banned.yml");
    public static void add(Material m) {
        if (!core.banned.contains(m)) core.banned.add(m);
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) throw new IOException("Failed to create data folder");

            if (!file.exists() && !file.createNewFile()) throw new IOException("Failed to create banned.yml file");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(m.name());
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void remove(Material m) {
        core.banned.remove(m);
        try {
            Files.delete(file.toPath());
            if (!file.createNewFile()) throw new IOException("Failed to create banned.yml file");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Material material : core.banned) {
                writer.write(material.name());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void read() {
        core.banned.clear();
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String ln;
                while ((ln = reader.readLine()) != null) {
                    core.banned.add(Material.valueOf(ln));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
