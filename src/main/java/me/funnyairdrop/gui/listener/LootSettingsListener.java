package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropLootItem;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.ChanceSettingsGUI;
import me.funnyairdrop.gui.menu.LootPagesGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class LootSettingsListener implements Listener {
    private final FunnyAirdrop plugin;

    public LootSettingsListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Настройка лута")) return;

        Player player = (Player) event.getWhoClicked();
        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null) return;

        int slot = event.getSlot();

        if (slot < 45) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);

        switch (slot) {
            case 45 -> {
                saveLoot(event.getInventory(), airdrop);
                player.closeInventory();
                new LootPagesGUI(plugin, airdrop).open(player);
            }
            case 49 -> {
                saveLoot(event.getInventory(), airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Лут сохранён!"));
            }
            case 51 -> {
                saveLoot(event.getInventory(), airdrop);
                player.closeInventory();
                new ChanceSettingsGUI(plugin, airdrop).open(player);
            }
            case 53 -> {
                saveLoot(event.getInventory(), airdrop);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Настройка лута")) return;

        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);
        if (airdrop != null) {
            saveLoot(event.getInventory(), airdrop);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Настройка лута")) return;

        for (int slot : event.getRawSlots()) {
            if (slot >= 45) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void saveLoot(Inventory inventory, Airdrop airdrop) {
        Map<Integer, AirdropLootItem> lootItems = new LinkedHashMap<>();

        for (int slot = 0; slot < 45; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                double chance = 10.0;
                Map<Integer, AirdropLootItem> currentLoot = airdrop.getLootItems();
                if (currentLoot != null) {
                    AirdropLootItem existing = currentLoot.get(slot);
                    if (existing != null) {
                        chance = existing.getChance();
                    }
                }
                lootItems.put(slot, new AirdropLootItem(item.clone(), chance));
            }
        }

        airdrop.setLootItems(lootItems);
        plugin.getAirdropManager().saveAirdrop(airdrop);
    }
}