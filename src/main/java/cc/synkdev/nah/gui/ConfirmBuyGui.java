package cc.synkdev.nah.gui;

import cc.synkdev.kyori.adventure.text.Component;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.api.NAHUtil;
import cc.synkdev.nah.manager.DataFileManager;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.manager.WebhookManager;
import cc.synkdev.nah.objects.BINAuction;
import cc.synkdev.nexuscore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.triumph.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfirmBuyGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(BINAuction bA) {
        Gui gui = Gui.gui().rows(4)
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.YELLOW+ Lang.translate("confirmBuy", core)))
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());
        gui.setItem(2, 5, item(bA));
        gui.setItem(3, 3, confirm(bA));
        gui.setItem(3, 7, cancel());

        return gui;
    }
    GuiItem item(BINAuction bA) {
        return ItemBuilder.from(bA.getItem()).asGuiItem();
    }
    GuiItem confirm(BINAuction bAa) {
        double tax = Math.round(bAa.getPrice()*((double) core.getBuyTaxPercent()/100));
        List<Component> lore = new ArrayList<>();
        if (core.getBuyTaxPercent() > 0) lore.add(Component.text(Util.color("&r&e&l"+Lang.translate("taxes", core, core.getBuyTaxPercent()+"", tax+""))));
        return ItemBuilder.from(Material.GREEN_WOOL)
                .name(Component.text(Util.color("&r&c&l"+Util.translate("confirm"))))
                .lore(lore)
                .asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            BINAuction bA = NAHUtil.getAuction(bAa.getId());
            if (!bA.getBuyable()) {
                pl.sendMessage(core.prefix()+Lang.translate("already-bought", core));
                pl.closeInventory();
                return;
            }
            if (!bA.getSeller().toString().equalsIgnoreCase(pl.getUniqueId().toString())) {
                if (core.getEcon().has(pl, bA.getPrice()+tax)) {
                    core.getEcon().withdrawPlayer(pl, bA.getPrice()+tax);
                    core.getEcon().depositPlayer(Bukkit.getOfflinePlayer(bA.getSeller()), bA.getPrice());
                    if (Util.isOnline(bA.getSeller())) Objects.requireNonNull(Bukkit.getPlayer(bA.getSeller())).sendMessage(core.prefix()+ChatColor.GOLD+ pl.getName()+" "+Lang.translate("smnBought", core, bA.getPrice()+""));

                    if (pl.getInventory().firstEmpty() == -1) {
                        List<ItemStack> list = core.retrieveMap.getOrDefault(pl.getUniqueId(), new ArrayList<>());
                        list.add(bA.getItem());
                        core.retrieveMap.put(pl.getUniqueId(), list);
                        pl.sendMessage(core.prefix()+Lang.translate("buyInvFull", core));
                    } else {
                        pl.getInventory().addItem(bA.getItem());
                    }

                    core.runningBINs.remove(bA);
                    bA.setBuyer(pl.getUniqueId());
                    core.expiredBINs.add(bA);
                    DataFileManager.sort();
                    pl.closeInventory();
                    WebhookManager.sendWebhook("listing-bought", bA, pl.getName(), Util.getName(bA.getSeller()), bA.getPrice()+"");
                    core.money = core.money+bA.getPrice();
                    pl.sendMessage(core.prefix() + ChatColor.GREEN + Lang.translate("successBuy", core, Util.getName(bA.getSeller()), bA.getPrice()+""));
                } else {
                    pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("notEnoughBuy", core));
                }
            } else {
                pl.sendMessage(core.prefix()+ChatColor.RED+Lang.translate("buyFromYou", core));
            }
            pl.closeInventory();
        });
    }
    GuiItem cancel() {
        return ItemBuilder.from(Material.BARRIER)
                .name(Component.text(Util.color("&r&c&l"+Util.translate("cancel")))).asGuiItem(event -> {
            Player pl = (Player) event.getWhoClicked();
            pl.closeInventory();
            NAHUtil.open(pl, false, null, 1);
        });
    }
}
