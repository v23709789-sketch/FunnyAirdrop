package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.AirdropSettingsGUI;
import me.funnyairdrop.util.ColorUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WorldSelectListener implements Listener {
    private final FunnyAirdrop plugin;

    public WorldSelectListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        if (!event.getView().getTitle().contains("Выбор мира")) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        String airdropName = event.getView().getTitle().substring(
                event.getView().getTitle().lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null || event.getSlot() >= 54) return;

        if (event.getSlot() == 49) {
            player.closeInventory();
            new AirdropSettingsGUI(plugin, airdrop).open(player);
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR &&
                clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) {
            String worldName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).trim();
            airdrop.setWorldName(worldName);
            plugin.getAirdropManager().saveAirdrop(airdrop);
            player.sendMessage(ColorUtils.colorize("&#55ff55Мир изменён на: " + worldName));
            player.closeInventory();
            new AirdropSettingsGUI(plugin, airdrop).open(player);
        }
    }
}