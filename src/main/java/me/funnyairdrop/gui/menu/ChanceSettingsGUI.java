package me.funnyairdrop.gui.menu;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropLootItem;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ChanceSettingsGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    public ChanceSettingsGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Настройка шансов: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(45, createItem(Material.ARROW, "&#ffaa55Назад к луту"));

        Map<Integer, AirdropLootItem> lootItems = airdrop.getLootItems();
        double totalChance = 0.0;
        for (AirdropLootItem item : lootItems.values()) {
            totalChance += item.getChance();
        }

        inventory.setItem(49, createItem(Material.BOOK, "&#ffaa55Информация",
                "&#aaaaaaЛКМ: +1% шанса",
                "&#aaaaaaПКМ: -1% шанса",
                "&#ffff55Shift + ЛКМ: +10%",
                "&#ffff55Shift + ПКМ: -10%",
                "",
                "&#ffaa55Сумма шансов: " + String.format("%.1f", totalChance) + "%"));

        inventory.setItem(51, createItem(Material.EMERALD, "&#55ff55Сохранить шансы"));
        inventory.setItem(53, createItem(Material.BARRIER, "&#ff5555Выход и сохранить"));

        for (Map.Entry<Integer, AirdropLootItem> entry : lootItems.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 45) continue;

            ItemStack displayItem = entry.getValue().getItemStack().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();

                lore.addAll(Arrays.asList(
                        ColorUtils.colorize("&#aaaaaaШанс выпадения: &#55ffff" +
                                String.format("%.1f", entry.getValue().getChance()) + "%"),
                        ColorUtils.colorize("&#666666Слот: " + slot),
                        "",
                        ColorUtils.colorize("&#ffff55ЛКМ: +1%"),
                        ColorUtils.colorize("&#ffff55ПКМ: -1%"),
                        ColorUtils.colorize("&#ffaa55Shift+ЛКМ: +10%"),
                        ColorUtils.colorize("&#ffaa55Shift+ПКМ: -10%")
                ));

                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            inventory.setItem(slot, displayItem);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(name));
            if (lore.length > 0) {
                meta.setLore(Arrays.stream(lore).map(ColorUtils::colorize).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}