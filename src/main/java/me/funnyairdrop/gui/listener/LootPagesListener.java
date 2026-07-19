package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.AirdropSettingsGUI;
import me.funnyairdrop.gui.menu.LootSettingsGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LootPagesListener implements Listener {
    private final FunnyAirdrop plugin;

    public LootPagesListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Страницы лута")) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null) return;

        int slot = event.getSlot();
        ClickType clickType = event.getClick();

        if (slot == 53) {
            player.closeInventory();
            new AirdropSettingsGUI(plugin, airdrop).open(player);
            return;
        }

        if (slot == 49) {
            airdrop.addLootPage();
            plugin.getAirdropManager().saveAirdrop(airdrop);
            player.sendMessage(ColorUtils.colorize("&#55ff55Создана новая страница лута! Всего страниц: " + airdrop.getLootPageCount()));
            new me.funnyairdrop.gui.menu.LootPagesGUI(plugin, airdrop).open(player);
            return;
        }

        if (slot < 45 && slot < airdrop.getLootPageCount()) {
            if (clickType == ClickType.LEFT) {
                airdrop.setCurrentLootPage(slot);
                player.closeInventory();
                new LootSettingsGUI(plugin, airdrop).open(player);
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                if (airdrop.getLootPageCount() > 1) {
                    airdrop.removeLootPage(slot);
                    plugin.getAirdropManager().saveAirdrop(airdrop);
                    player.sendMessage(ColorUtils.colorize("&#ff5555Страница " + (slot + 1) + " удалена!"));
                    new me.funnyairdrop.gui.menu.LootPagesGUI(plugin, airdrop).open(player);
                } else {
                    player.sendMessage(ColorUtils.colorize("&#ff5555Нельзя удалить последнюю страницу!"));
                }
            }
        }
    }
}