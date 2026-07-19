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

import java.util.Arrays;
import java.util.Map;

public class LootSettingsGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    public LootSettingsGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Настройка лута: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(45, createItem(Material.ARROW, "&#ffaa55Назад к страницам"));
        inventory.setItem(49, createItem(Material.EMERALD, "&#55ff55Сохранить лут"));
        inventory.setItem(51, createItem(Material.CLOCK, "&#ffaa55Настроить шансы",
                "&#aaaaaaНажмите для настройки шансов"));
        inventory.setItem(53, createItem(Material.BARRIER, "&#ff5555Выход и сохранить"));

        Map<Integer, AirdropLootItem> lootItems = airdrop.getLootItems();
        if (lootItems != null) {
            for (Map.Entry<Integer, AirdropLootItem> entry : lootItems.entrySet()) {
                int slot = entry.getKey();
                if (slot < 45) {
                    inventory.setItem(slot, entry.getValue().getItemStack().clone());
                }
            }
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