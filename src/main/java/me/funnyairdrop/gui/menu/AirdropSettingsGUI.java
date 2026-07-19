package me.funnyairdrop.gui.menu;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropLootItem;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AirdropSettingsGUI {
    public final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private final Inventory inventory;

    public AirdropSettingsGUI(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.inventory = Bukkit.createInventory(
                new GUIHolder(),
                54,
                ColorUtils.colorize("&#333333Настройка: " + airdrop.getName())
        );
        setupGUI();
    }

    private void setupGUI() {
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(10, createItem(airdrop.getMaterial(),
                "&#ffaa55Материал",
                "&#aaaaaaТекущий: " + formatMaterialName(airdrop.getMaterial().name()),
                "",
                "&#aaaaaaНажмите для выбора"));

        inventory.setItem(11, createItem(Material.NAME_TAG,
                "&#ffaa55Название",
                "&#aaaaaaТекущее: " + airdrop.getDisplayName(),
                "",
                "&#aaaaaaНажмите для изменения в чате"));

        inventory.setItem(12, createItem(Material.GRASS_BLOCK,
                "&#ffaa55Мир",
                "&#aaaaaaТекущий: " + airdrop.getWorldName(),
                "",
                "&#aaaaaaНажмите для выбора мира"));

        inventory.setItem(13, createItem(Material.ENDER_PEARL,
                "&#ffaa55Статичный режим",
                "&#aaaaaaСтатус: " + (airdrop.isStaticMode() ? "&#55ff55Включен" : "&#ff5555Выключен"),
                "&#aaaaaaЛокация: " + (airdrop.getStaticLocation() != null ?
                        "&#ffffff" + airdrop.getStaticLocation().getWorld().getName() + " " +
                                airdrop.getStaticLocation().getBlockX() + " " +
                                airdrop.getStaticLocation().getBlockY() + " " +
                                airdrop.getStaticLocation().getBlockZ() : "&#ff5555Не установлена"),
                "",
                "&#aaaaaaЛКМ: Включить/Выключить",
                "&#aaaaaaПКМ: Установить текущую локацию"));

        inventory.setItem(14, createItem(Material.CHEST,
                "&#ffaa55Размер инвентаря",
                "&#aaaaaaТекущий: " + airdrop.getInventorySize(),
                "",
                "&#aaaaaaНажмите для изменения в чате",
                "&#aaaaaaДоступно: 9, 18, 27, 36, 45, 54"));

        inventory.setItem(15, createItem(Material.CLOCK,
                "&#ffaa55Время до открытия",
                "&#aaaaaaТекущее: " + airdrop.getOpenTime() + " сек.",
                "&#aaaaaa(" + formatTime(airdrop.getOpenTime()) + ")",
                "",
                "&#aaaaaaНажмите для изменения в чате"));

        inventory.setItem(16, createItem(Material.COMPARATOR,
                "&#ffaa55Общее время ивента",
                "&#aaaaaaТекущее: " + airdrop.getCloseTime() + " сек.",
                "&#aaaaaa(" + formatTime(airdrop.getCloseTime()) + ")",
                "",
                "&#aaaaaaНажмите для изменения в чате"));

        inventory.setItem(25, createItem(Material.PLAYER_HEAD,
                "&#ffaa55Мин. онлайн для спавна",
                "&#aaaaaaТекущий: " + airdrop.getMinOnlinePlayers(),
                "",
                "&#aaaaaa0 = спавн всегда",
                "&#aaaaaa1+ = нужно N игроков онлайн",
                "",
                "&#aaaaaaЛКМ: +1",
                "&#aaaaaaПКМ: -1",
                "&#aaaaaaShift+ЛКМ: +5",
                "&#aaaaaaShift+ПКМ: -5"));

        inventory.setItem(31, createItem(Material.OAK_BUTTON,
                "&#ffaa55Активация по клику",
                "&#aaaaaaСтатус: " + (airdrop.isClickToActivate() ? "&#55ff55Включена" : "&#ff5555Выключена"),
                "",
                "&#aaaaaaЛКМ: Включить/Выключить",
                "&#aaaaaaЕсли включено - после спавна",
                "&#aaaaaaнужно нажать ПКМ для запуска",
                "&#aaaaaaотсчёта до открытия"));

        inventory.setItem(20, createItem(Material.ARMOR_STAND,
                "&#ffaa55Высота голограммы",
                "&#aaaaaaТекущая: " + String.format("%.1f", airdrop.getHologramHeight()) + " блоков",
                "",
                "&#aaaaaaНажмите для изменения в чате",
                "&#aaaaaaРекомендуется: 1.0 - 3.0"));

        inventory.setItem(21, createItem(Material.SHIELD,
                "&#ffaa55Радиус защиты региона",
                "&#aaaaaaТекущий: " + airdrop.getWorldGuardRadius() + " блоков",
                "",
                "&#aaaaaaНажмите для изменения в чате",
                "&#aaaaaaЗащита от построек и взрывов"));

        inventory.setItem(22, createItem(Material.COMPASS,
                "&#ffaa55Радиус спавна",
                "&#aaaaaaТекущий: " + airdrop.getRadius() + " блоков",
                "",
                "&#aaaaaaЛКМ: +100",
                "&#aaaaaaПКМ: -100",
                "&#aaaaaaShift+ЛКМ: +500",
                "&#aaaaaaShift+ПКМ: -500"));

        inventory.setItem(29, createItem(Material.FEATHER,
                "&#ffaa55Высота спавна",
                "&#aaaaaaТекущая: " + airdrop.getSpawnHeightOffset() + " блоков",
                "&#aaaaaa0 = стандартная высота",
                "&#aaaaaaПоложительное число = выше",
                "&#aaaaaaОтрицательное = ниже",
                "",
                "&#aaaaaaЛКМ: +1",
                "&#aaaaaaПКМ: -1",
                "&#aaaaaaShift+ЛКМ: +5",
                "&#aaaaaaShift+ПКМ: -5"));

        inventory.setItem(23, createItem(Material.PAPER,
                "&#ffaa55Схематика",
                "&#aaaaaaТекущая: " + (airdrop.getSchematic() != null ? airdrop.getSchematic() : "&#ff5555Не выбрана"),
                "",
                "&#aaaaaaЛКМ: Выбрать схематику",
                "&#aaaaaaПКМ: Сбросить схематику"));

        inventory.setItem(24, createItem(Material.REPEATER,
                "&#ffaa55Авто-спавн аирдропа",
                "&#aaaaaaСтатус: " + (airdrop.isAutoRespawn() ? "&#55ff55Включен" : "&#ff5555Выключен"),
                "&#aaaaaaИнтервал: " + airdrop.getAutoRespawnTime() + " сек.",
                "",
                "&#aaaaaaЛКМ: Включить/Выключить",
                "&#aaaaaaПКМ: Изменить интервал"));

        inventory.setItem(48, createItem(Material.ARROW, "&#ffaa55Назад к списку"));

        int totalItems = 0;
        for (Map<Integer, AirdropLootItem> page : airdrop.getLootPages()) {
            totalItems += page.size();
        }

        inventory.setItem(49, createItem(Material.ENDER_CHEST,
                "&#55ff55Настроить лут",
                "&#aaaaaaВсего предметов: &#ffaa55" + totalItems,
                "",
                "&#aaaaaaНажмите для настройки"));

        inventory.setItem(50, createItem(Material.BELL,
                "&#ffaa55Слушатели событий",
                "&#aaaaaaНастройка сообщений и звуков",
                "",
                "&#aaaaaaНажмите для настройки"));
    }

    private String formatMaterialName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (formatted.length() > 0) formatted.append(" ");
                formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            }
        }
        return formatted.toString();
    }

    private String formatTime(int seconds) {
        if (seconds < 60) return seconds + " сек.";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0) return minutes + " мин.";
        return minutes + " мин. " + secs + " сек.";
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