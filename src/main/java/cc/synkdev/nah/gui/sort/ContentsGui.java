package cc.synkdev.nah.gui.sort;

import cc.synkdev.nah.NexusAuctionHouse;
import cc.synkdev.nah.manager.Util;
import cc.synkdev.nah.objects.ItemSort;
import cc.synkdev.nexuscore.bukkit.Lang;
import cc.synkdev.triumph.builder.item.ItemBuilder;
import cc.synkdev.triumph.guis.Gui;
import cc.synkdev.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import cc.synkdev.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import cc.synkdev.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContentsGui {
    private final NexusAuctionHouse core = NexusAuctionHouse.getInstance();
    public Gui gui(ItemSort sort, int page, String search) {
        Gui gui = Gui.gui()
                .rows(6)
                .disableAllInteractions()
                .title(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("sortContentsTitle", core)))
                .create();
        List<Material> materials = new ArrayList<>(Util.getFilteredMaterials(search));
        int max = (materials.size()+44)/45;
        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(LegacyComponentSerializer.legacyAmpersand().deserialize(" ")).asGuiItem());

        if (page > 1) {
            gui.setItem(6, 4, ItemBuilder.from(Material.ARROW)
                    .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("prevPage", core)))
                    .asGuiItem(event -> gui(sort, page-1, search).open(event.getWhoClicked())));
        }

        if (page < max) {
            gui.setItem(6, 6, ItemBuilder.from(Material.ARROW)
                    .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("nextPage", core)))
                    .asGuiItem(event -> gui(sort, page+1, search).open(event.getWhoClicked())));
        }

        gui.setItem(6, 5, ItemBuilder.from(Material.BARRIER)
                .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("back", core)))
                .asGuiItem(event -> new EditSortGui().gui(sort).open(event.getWhoClicked())));


        List<Component> lore = searchItemLore(search, core);
        gui.setItem(6, 2, ItemBuilder.from(Material.OAK_SIGN)
                .name(LegacyComponentSerializer.legacyAmpersand().deserialize(Util.color("&r&e"+Lang.translate("search", core))))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .lore(lore)
                .asGuiItem(event -> {
                    Player p = (Player) event.getWhoClicked();
                    if (event.isRightClick() && search != null) {
                        gui(sort, 1, null).open(p);
                        return;
                    }


                    AnvilGUI.Builder anvil = new AnvilGUI.Builder();
                    anvil.plugin(core);
                    anvil.itemLeft(new ItemStack(Material.PAPER));
                    anvil.itemOutput(ItemBuilder.from(Material.PAPER)
                            .name(Component.text(ChatColor.GOLD+Lang.translate("search", core))).build());

                    anvil.text(Lang.translate("enterSearch", core));
                    anvil.onClick((integer, stateSnapshot) -> {
                        if (integer != 2) {
                            return List.of();
                        }
                        p.closeInventory();
                        gui(sort, 1, stateSnapshot.getText()).open(p);
                        return List.of();
                    });

                    anvil.onClose(stateSnapshot -> gui(sort, 1, stateSnapshot.getText()).open(p));
                    anvil.open(p);
                }));

        int minSlot = 45*(page-1);
        int maxSlot = 45*page;

        for (int i = minSlot; i < maxSlot; i++) {
            if (materials.size() <= i) break;
            Material m = materials.get(i);
            boolean has = sort.getContents().contains(m);
            try {
                gui.setItem(i - minSlot, ItemBuilder.from(m)
                        .name(LegacyComponentSerializer.legacyAmpersand().deserialize(ChatColor.GOLD+m.name()))
                        .glow(has)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(LegacyComponentSerializer.legacyAmpersand().deserialize(""),
                                has ? LegacyComponentSerializer.legacyAmpersand().deserialize("  "+Lang.translate("selected", core)) : LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("clickSelect", core)),
                                has ? LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("clickUnselect", core)) : null)
                        .asGuiItem(event -> {
                            if (has) {
                                sort.getContents().remove(m);
                            } else {
                                sort.getContents().add(m);
                            }
                            gui(sort, page, search).open(event.getWhoClicked());
                        }));
            } catch (IllegalArgumentException _) {
                // Ignore invalid materials
            }
        }
        return gui;
    }

    public static List<Component> searchItemLore(String search, NexusAuctionHouse core) {
        List<Component> lore = new ArrayList<>();
        if (search != null) {
            lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(""));
            lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("  "+ Lang.translate("currSearch", core, search)));
            lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("  "+Lang.translate("searchReset", core)));
        }
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(""));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.translate("clickSearch", core)));
        return lore;
    }
}
