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

import java.util.*;

public class AirdropsMenuGUI {
    public final FunnyAirdrop plugin;
    private final Inventory inventory;
    private final List<Airdrop> airdropList;

    public AirdropsMenuGUI(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(new GUIHolder(), 54,
                ColorUtils.colorize("&#333333Меню аирдропов"));
        this.airdropList = new ArrayList<>(plugin.getAirdropManager().getAllAirdrops());
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(49, createItem(Material.BARRIER, "&#ff5555Закрыть"));

        for (int i = 0; i < Math.min(airdropList.size(), 45); i++) {
            Airdrop airdrop = airdropList.get(i);
            ItemStack item = new ItemStack(airdrop.getMaterial());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ColorUtils.colorize(airdrop.getDisplayName()));
                List<String> lore = new ArrayList<>();
                lore.add(ColorUtils.colorize("&#aaaaaaНазвание: &#ffaa55" + airdrop.getName()));
                lore.add(ColorUtils.colorize("&#aaaaaaМир: &#ffaa55" + airdrop.getWorldName()));
                lore.add(ColorUtils.colorize("&#aaaaaaРадиус: &#ffaa55" + airdrop.getRadius()));
                lore.add(ColorUtils.colorize("&#aaaaaaСтатус: " + (airdrop.isActive() ? "&#55ff55Активен" : "&#ff5555Неактивен")));
                lore.add("");
                lore.add(ColorUtils.colorize("&#aaaaaaЛКМ: &#55ff55Настроить"));
                lore.add(ColorUtils.colorize("&#aaaaaaПКМ: " + (airdrop.isActive() ? "&#ff5555Остановить" : "&#55ff55Запустить")));
                lore.add(ColorUtils.colorize("&#aaaaaaShift+ПКМ: &#ff5555Удалить"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(i, item);
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