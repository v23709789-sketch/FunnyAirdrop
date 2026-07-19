package me.funnyairdrop.gui.menu;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class WorldSelectorGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    public WorldSelectorGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Выбор мира: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(49, createItem(Material.ARROW, "&#ffaa55Назад"));
        List<World> worlds = Bukkit.getWorlds();
        for (int i = 0; i < Math.min(worlds.size(), 45); i++) {
            World world = worlds.get(i);
            Material icon = getWorldIcon(world);
            String name = "&#ffffff" + world.getName();

            if (world.getName().equals(airdrop.getWorldName())) {
                name = "&#55ff55" + world.getName() + " (Выбрано)";
            }

            inventory.setItem(i, createItem(icon, name,
                    "&#aaaaaaТип мира: " + world.getEnvironment().name(),
                    "&#aaaaaaНажмите, чтобы выбрать этот мир"));
        }
    }

    private Material getWorldIcon(World world) {
        return switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            default -> Material.GRASS_BLOCK;
        };
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