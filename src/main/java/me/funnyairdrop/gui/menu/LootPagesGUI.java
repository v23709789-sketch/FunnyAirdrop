package me.funnyairdrop.gui.menu;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootPagesGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    public LootPagesGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Страницы лута: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(49, createItem(Material.EMERALD_BLOCK, "&#55ff55Создать новую страницу",
                "&#aaaaaaНажмите чтобы добавить",
                "&#aaaaaaновую пустую страницу лута"));

        inventory.setItem(53, createItem(Material.ARROW, "&#ffaa55Назад к настройкам"));

        for (int i = 0; i < Math.min(airdrop.getLootPageCount(), 45); i++) {
            String name = "&#ffffffСтраница " + (i + 1);

            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&#aaaaaaПредметов: &#ffaa55" + airdrop.getLootPages().get(i).size()));
            lore.add("");
            lore.add(ColorUtils.colorize("&#aaaaaaЛКМ: &#55ff55Редактировать лут"));
            lore.add(ColorUtils.colorize("&#aaaaaaShift+ПКМ: &#ff5555Удалить страницу"));

            inventory.setItem(i, createItem(Material.CHEST_MINECART, name, lore.toArray(new String[0])));
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