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

import java.util.Arrays;

public class MaterialSelectorGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    private static final Material[] MATERIALS = {
            Material.CHEST, Material.ENDER_CHEST, Material.BARREL,
            Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX,
            Material.BEACON, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK,
            Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.NETHERITE_BLOCK,
            Material.ENCHANTING_TABLE, Material.ANVIL, Material.CRAFTING_TABLE,
            Material.FURNACE, Material.TNT, Material.OBSIDIAN,
            Material.CRYING_OBSIDIAN, Material.RESPAWN_ANCHOR, Material.LODESTONE,
            Material.HOPPER, Material.DISPENSER, Material.DROPPER,
            Material.NOTE_BLOCK, Material.JUKEBOX, Material.CAULDRON,
            Material.BREWING_STAND, Material.SMITHING_TABLE, Material.CARTOGRAPHY_TABLE,
            Material.LOOM, Material.STONECUTTER, Material.GRINDSTONE
    };

    public MaterialSelectorGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Выбор материала: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(49, createItem(Material.ARROW, "&#ffaa55Назад"));
        for (int i = 0; i < Math.min(MATERIALS.length, 45); i++) {
            Material material = MATERIALS[i];
            String name = "&#ffffff" + formatMaterialName(material.name());
            if (material == airdrop.getMaterial()) {
                name = "&#55ff55" + formatMaterialName(material.name()) + " (Выбрано)";
            }

            inventory.setItem(i, createItem(material, name,
                    "&#aaaaaaНажмите, чтобы выбрать этот материал"));
        }
    }

    private String formatMaterialName(String name) {
        String[] words = name.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (formatted.length() > 0) formatted.append(" ");
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }
        return formatted.toString();
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