package cc.synkdev.nah.gui.sort;

import cc.synkdev.kyori.adventure.text.Component;
import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.ItemSort;
import cc.synkdev.nexuscore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import cc.synkdev.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class SortsManagementGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(int page) {
        Gui gui = Gui.gui()
                .disableAllInteractions()
                .rows(6)
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("sortsManagement", core)))
                .create();

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());

        gui.setItem(6, 4, ItemBuilder.from(Material.GREEN_WOOL).name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("createSort", core)))
                .lore(LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                        LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("clickCreateSort", core)))
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (!p.hasPermission("nah.sorts.create")) {
                        p.sendMessage(core.prefix()+Lang.translate("noPerm", core));
                        return;
                    }

                    AnvilGUI.Builder builder = new AnvilGUI.Builder();
                    builder.plugin(core);
                    builder.itemLeft(ItemBuilder.from(Material.PAPER)
                            .name(Component.text(Util.translate("enterName"))).build());
                    builder.text(Lang.translate("enterName", core));
                    builder.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }

                        stateSnapshot.getPlayer().closeInventory();
                        ItemSort sort = new ItemSort(stateSnapshot.getText());
                        core.itemSorts.put(stateSnapshot.getText(), sort);
                        new EditSortGui().gui(sort).open(stateSnapshot.getPlayer());
                        return List.of();
                    });
                    builder.open(p);
                }));

        int min = 45*(page-1);
        int max = 45*page;

        for (int i = min; i < max; i++) {
            if (core.itemSorts.size() > i) {
                ItemSort sort = core.itemSorts.entrySet().stream().toList().get(i).getValue();
                gui.setItem(i - min, ItemBuilder.from(sort.getIcon()).name(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.YELLOW+sort.getName())).asGuiItem(event -> new EditSortGui().gui(sort).open(event.getWhoClicked())));
            }
        }

        gui.setItem(6, 6, ItemBuilder.from(Material.BARRIER)
                .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("back", core)))
                .asGuiItem(event -> event.getWhoClicked().closeInventory()));

        if (page > 1) {
            gui.setItem(6, 3, ItemBuilder.from(Material.ARROW)
                    .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Util.color("&r&e&l"+Lang.translate("prevPage", core))))
                    .asGuiItem(inventoryClickEvent -> {
                        Player p = (Player) inventoryClickEvent.getWhoClicked();
                        gui(page-1).open(p);
                    }));
        }
        if (page < max) {
            gui.setItem(6, 7, ItemBuilder.from(Material.ARROW)
                    .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Util.color("&r&e&l"+Lang.translate("nextPage", core))))
                    .asGuiItem(inventoryClickEvent -> {
                        Player p = (Player) inventoryClickEvent.getWhoClicked();
                        gui(page+1).open(p);
                    }));
        }
        return gui;
    }
}
