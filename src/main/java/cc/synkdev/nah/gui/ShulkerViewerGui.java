package cc.synkdev.nah.gui;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexuscore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerViewerGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        ItemStack item = bA.getItem();
        if (!item.getType().name().endsWith("SHULKER_BOX")) return null;
        Gui gui = Gui.gui()
                .rows(4)
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("inventoryViewer", core))).disableAllInteractions().create();
        try {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            assert meta != null;
            ShulkerBox box = (ShulkerBox) meta.getBlockState();
            for (ItemStack itemStack : box.getInventory().getContents()) {
                if (itemStack != null) gui.addItem(ItemBuilder.from(itemStack).asGuiItem());
            }
        } catch (ClassCastException | NullPointerException _) {
            //Not a shulker box, do nothing
        }
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());
        gui.setItem(4, 5, ItemBuilder.from(Material.BARRIER).name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("back", core))).asGuiItem(event -> new ConfirmBuyGui().gui(bA).open(event.getWhoClicked())));
        return gui;
    }
}
