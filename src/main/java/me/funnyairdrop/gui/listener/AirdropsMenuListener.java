package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.AirdropSettingsGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class AirdropsMenuListener implements Listener {
    private final FunnyAirdrop plugin;

    public AirdropsMenuListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Меню аирдропов")) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        event.setCancelled(true);

        int slot = event.getSlot();
        ClickType clickType = event.getClick();

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot < 45 && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            var airdrops = new ArrayList<>(plugin.getAirdropManager().getAllAirdrops());
            if (slot >= airdrops.size()) return;

            Airdrop airdrop = airdrops.get(slot);

            switch (clickType) {
                case LEFT -> {
                    player.closeInventory();
                    new AirdropSettingsGUI(plugin, airdrop).open(player);
                }
                case RIGHT -> {
                    if (airdrop.isActive()) {
                        plugin.getAirdropManager().stopAirdrop(airdrop.getName());
                        player.sendMessage(ColorUtils.colorize("&#ff5555Аирдроп остановлен!"));
                    } else {
                        try {
                            plugin.getAirdropManager().startAirdrop(airdrop.getName());
                            player.sendMessage(ColorUtils.colorize("&#55ff55Аирдроп запущен!"));
                        } catch (Exception e) {
                            player.sendMessage(ColorUtils.colorize("&#ff5555Ошибка: " + e.getMessage()));
                        }
                    }
                    player.closeInventory();
                }
                case SHIFT_RIGHT -> {
                    try {
                        plugin.getAirdropManager().deleteAirdrop(airdrop.getName());
                        player.sendMessage(ColorUtils.colorize("&#ff5555Аирдроп удалён!"));
                    } catch (Exception e) {
                        player.sendMessage(ColorUtils.colorize("&#ff5555Ошибка: " + e.getMessage()));
                    }
                    player.closeInventory();
                }
            }
        }
    }
}