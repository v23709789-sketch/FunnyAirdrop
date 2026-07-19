package me.funnyairdrop.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ListenerMenuListener implements Listener {
    public final FunnyAirdrop plugin;

    public ListenerMenuListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Слушатели событий")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 49) {
            player.closeInventory();
        }
    }
}