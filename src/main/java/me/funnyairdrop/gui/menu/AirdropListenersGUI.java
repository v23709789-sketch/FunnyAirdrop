package me.funnyairdrop.gui.menu;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.listener.AirdropListenerConfig;
import me.funnyairdrop.listener.EventListener;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AirdropListenersGUI {
    private final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;
    private final List<EventListener> listenerList;

    public AirdropListenersGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.listenerList = new ArrayList<>(plugin.getListenerManager().getEventListeners().values());
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Слушатели: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(49, createItem(Material.ARROW, "&#ffaa55Назад к настройкам"));

        AirdropListenerConfig config = airdrop.getListenerConfig();

        for (int i = 0; i < Math.min(listenerList.size(), 45); i++) {
            EventListener listener = listenerList.get(i);
            boolean enabled = config.isEnabled(listener.getName());

            Material icon = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
            String status = enabled ? "&#55ff55Включен" : "&#ff5555Выключен";

            ItemStack item = createItem(icon,
                    "&#ffaa55" + listener.getName(),
                    "&#aaaaaaОписание: " + listener.getDescription(),
                    "&#aaaaaaСобытие: &#55ff55" + listener.getEvent().name(),
                    "&#aaaaaaСтатус: " + status,
                    "",
                    "&#aaaaaaЛКМ: Включить/Выключить");

            inventory.setItem(i, item);
        }
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void toggleListener(int slot, Player player) {
        if (slot >= listenerList.size()) return;

        EventListener listener = listenerList.get(slot);
        AirdropListenerConfig config = airdrop.getListenerConfig();
        boolean currentState = config.isEnabled(listener.getName());

        config.setEnabled(listener.getName(), !currentState);
        plugin.getAirdropManager().saveAirdrop(airdrop);

        boolean newState = !currentState;
        Material icon = newState ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = newState ? "&#55ff55Включен" : "&#ff5555Выключен";

        ItemStack item = createItem(icon,
                "&#ffaa55" + listener.getName(),
                "&#aaaaaaОписание: " + listener.getDescription(),
                "&#aaaaaaСобытие: &#55ff55" + listener.getEvent().name(),
                "&#aaaaaaСтатус: " + status,
                "",
                "&#aaaaaaЛКМ: Включить/Выключить");

        player.getOpenInventory().getTopInventory().setItem(slot, item);
        player.updateInventory();

        player.sendMessage(ColorUtils.colorize(status + " &#aaaaaa- " + listener.getName()));
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