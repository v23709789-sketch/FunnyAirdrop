package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.AirdropListenersGUI;
import me.funnyairdrop.gui.menu.AirdropSettingsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class AirdropListenersListener implements Listener {
    private final FunnyAirdrop plugin;

    public AirdropListenersListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Слушатели:")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null) return;

        if (slot == 49) {
            player.closeInventory();
            new AirdropSettingsGUI(plugin, airdrop).open(player);
            return;
        }

        if (slot < 45) {
            AirdropListenersGUI gui = new AirdropListenersGUI(plugin, airdrop);
            gui.toggleListener(slot, player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (event.getView().getTitle().contains("Слушатели:")) {
            event.setCancelled(true);
        }
    }
}