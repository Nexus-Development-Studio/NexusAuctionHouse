package cc.synkdev.nah.gui;

import cc.synkdev.kyori.adventure.text.Component;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.events.ItemListEvent;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.manager.WebhookManager;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexusCore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.triumph.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ConfirmSellGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(Player p, long price) {
        Gui gui = Gui.gui()
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.YELLOW+ Lang.translate("confirmSell", core)))
                .rows(4)
                .disableAllInteractions()
                .create();
        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());
        gui.setItem(2, 5, item(p));
        gui.setItem(3, 3, confirm(price));
        gui.setItem(3, 7, cancel());
        return gui;
    }

    GuiItem item(Player p) {
        return ItemBuilder.from(p.getInventory().getItemInMainHand()).asGuiItem();
    }
    GuiItem confirm(long price) {
        long tax = Math.round(price*((double) core.getSellTaxPercent() /100));
        List<Component> lore = new ArrayList<>();
        if (core.getSellTaxPercent() > 0) {
            lore = new ArrayList<>(new ArrayList<>(Arrays.asList(Component.empty(), Component.text(Util.color("&r&e&l" + Lang.translate("taxes", core, core.getSellTaxPercent() + "", tax + ""))))));
        }
        return ItemBuilder.from(Material.GREEN_WOOL)
                .name(Component.text(Util.color("&r&c&l"+Lang.translate("confirm", core))))
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            ItemStack itemStack = pl.getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("emptyHand", core));
                return;
            }
            if (Util.serializeItemstack(itemStack).length()>20000) {
                pl.sendMessage(core.prefix()+Lang.translate("tooBig", core));
                return;
            }
            if (core.banned.contains(itemStack.getType())) {
                pl.sendMessage(core.prefix() + Lang.translate("sellBanned", core));
                return;
            }
            if (!core.getEcon().has(pl, tax)) {
                pl.sendMessage(core.prefix() + ChatColor.RED + Lang.translate("notEnoughTaxes", core));
                return;
            }
            core.getEcon().withdrawPlayer(pl, tax);
            long expire = (System.currentTimeMillis() / 1000) + core.getExpireTime();

            ItemListEvent listEvent = new ItemListEvent(pl, itemStack, price, new Date(expire * 1000L));
            Bukkit.getPluginManager().callEvent(listEvent);

            if (listEvent.isCancelled()) return;

            BINAuction bA = new BINAuction(core.getId(), listEvent.getPlayer().getUniqueId(), listEvent.getItem(), listEvent.getPrice(), expire);
            core.setId(core.getId() + 1);
            pl.getInventory().setItemInMainHand(null);
            core.runningBINs.add(bA);
            DataFileManager.sort();
            pl.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successSell", core, price + ""));
            pl.closeInventory();
            WebhookManager.sendWebhook("new-listing", bA, listEvent.getPlayer().getName(), listEvent.getPrice() + "");

            pl.closeInventory();
        });
    }
    GuiItem cancel() {
        return ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Util.color("&r&c&l"+Lang.translate("cancel", core))))
                .flags(ItemFlag.HIDE_ATTRIBUTES).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            pl.closeInventory();
        });
    }
}
