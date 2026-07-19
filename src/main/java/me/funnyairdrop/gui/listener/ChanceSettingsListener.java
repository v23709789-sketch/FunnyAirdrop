package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropLootItem;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.LootSettingsGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChanceSettingsListener implements Listener {
    private final FunnyAirdrop plugin;

    public ChanceSettingsListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Настройка шансов")) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null) return;

        int slot = event.getSlot();
        ClickType clickType = event.getClick();

        if (slot < 45) {
            ItemStack item = event.getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                var lootItems = airdrop.getLootItems();
                AirdropLootItem lootItem = lootItems.get(slot);

                if (lootItem != null) {
                    double delta = switch (clickType) {
                        case LEFT -> 1.0;
                        case RIGHT -> -1.0;
                        case SHIFT_LEFT -> 10.0;
                        case SHIFT_RIGHT -> -10.0;
                        default -> 0.0;
                    };

                    double newChance = Math.max(0.0, Math.min(100.0, lootItem.getChance() + delta));
                    lootItem.setChance(newChance);

                    updateChanceDisplay(event.getInventory(), slot, newChance);

                    Inventory topInventory = event.getInventory();
                    double totalChance = 0.0;
                    for (int i = 0; i < 45; i++) {
                        AirdropLootItem li = lootItems.get(i);
                        if (li != null && topInventory.getItem(i) != null && topInventory.getItem(i).getType() != Material.AIR) {
                            totalChance += li.getChance();
                        }
                    }

                    ItemStack infoItem = topInventory.getItem(49);
                    if (infoItem != null && infoItem.getType() == Material.BOOK) {
                        ItemMeta infoMeta = infoItem.getItemMeta();
                        if (infoMeta != null) {
                            List<String> lore = new ArrayList<>(Arrays.asList(
                                    ColorUtils.colorize("&#aaaaaaЛКМ: +1% шанса"),
                                    ColorUtils.colorize("&#aaaaaaПКМ: -1% шанса"),
                                    ColorUtils.colorize("&#ffff55Shift + ЛКМ: +10%"),
                                    ColorUtils.colorize("&#ffff55Shift + ПКМ: -10%"),
                                    "",
                                    ColorUtils.colorize("&#ffaa55Сумма шансов: " + String.format("%.1f", totalChance) + "%")
                            ));
                            infoMeta.setLore(lore);
                            infoItem.setItemMeta(infoMeta);
                        }
                    }

                    player.updateInventory();
                }
            }
            return;
        }

        switch (slot) {
            case 45 -> {
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.closeInventory();
                new LootSettingsGUI(plugin, airdrop).open(player);
            }
            case 49 -> {
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Шансы сохранены!"));
            }
            case 53 -> {
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.closeInventory();
            }
        }
    }

    private void updateChanceDisplay(Inventory inventory, int slot, double chance) {
        ItemStack item = inventory.getItem(slot);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore != null && !lore.isEmpty()) {
                    lore.set(0, ColorUtils.colorize("&#aaaaaaШанс выпадения: &#55ffff" +
                            String.format("%.1f", chance) + "%"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
            }
        }
    }
}