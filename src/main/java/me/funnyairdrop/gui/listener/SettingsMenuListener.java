package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropLootItem;
import me.funnyairdrop.gui.holder.GUIHolder;
import me.funnyairdrop.gui.menu.*;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.UUID;

public class SettingsMenuListener implements Listener {
    private final FunnyAirdrop plugin;
    private final Map<UUID, Airdrop> inventorySizePlayers;
    private final Map<UUID, Airdrop> openTimePlayers;
    private final Map<UUID, Airdrop> closeTimePlayers;
    private final Map<UUID, Airdrop> renamingPlayers;
    private final Map<UUID, Airdrop> hologramHeightPlayers;
    private final Map<UUID, Airdrop> worldGuardRadiusPlayers;

    public SettingsMenuListener(FunnyAirdrop plugin) {
        this.worldGuardRadiusPlayers = ChatInputListener.getWorldGuardRadiusPlayers();
        this.plugin = plugin;
        this.inventorySizePlayers = ChatInputListener.getInventorySizePlayers();
        this.openTimePlayers = ChatInputListener.getOpenTimePlayers();
        this.closeTimePlayers = ChatInputListener.getCloseTimePlayers();
        this.renamingPlayers = ChatInputListener.getRenamingPlayers();
        this.hologramHeightPlayers = ChatInputListener.getHologramHeightPlayers();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUIHolder)) return;
        String title = event.getView().getTitle();
        if (!title.contains("Настройка:") || title.contains("лута") || title.contains("шансов")) return;

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        String airdropName = title.substring(title.lastIndexOf(":") + 1).trim();
        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);

        if (airdrop == null) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Аирдроп не найден!"));
            player.closeInventory();
            return;
        }

        switch (slot) {
            case 10 -> {
                player.closeInventory();
                new MaterialSelectorGUI(plugin, airdrop).open(player);
            }
            case 11 -> {
                player.closeInventory();
                renamingPlayers.put(player.getUniqueId(), airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите новое название в чат:"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущее: " + airdrop.getDisplayName()));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 12 -> {
                player.closeInventory();
                new WorldSelectorGUI(plugin, airdrop).open(player);
            }
            case 13 -> {
                if (clickType == ClickType.LEFT) {
                    airdrop.setStaticMode(!airdrop.isStaticMode());
                    plugin.getAirdropManager().saveAirdrop(airdrop);
                    player.sendMessage(ColorUtils.colorize("&#55ff55Статичный режим: " + (airdrop.isStaticMode() ? "включен" : "выключен")));
                    new AirdropSettingsGUI(plugin, airdrop).open(player);
                } else if (clickType == ClickType.RIGHT) {
                    airdrop.setStaticLocation(player.getLocation());
                    airdrop.setStaticMode(true);
                    plugin.getAirdropManager().saveAirdrop(airdrop);
                    player.sendMessage(ColorUtils.colorize("&#55ff55Статичная локация установлена!"));
                    new AirdropSettingsGUI(plugin, airdrop).open(player);
                }
            }
            case 14 -> {
                inventorySizePlayers.put(player.getUniqueId(), airdrop);
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите размер инвентаря (9, 18, 27, 36, 45, 54):"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущий: " + airdrop.getInventorySize()));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 15 -> {
                openTimePlayers.put(player.getUniqueId(), airdrop);
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите время до открытия в секундах:"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущее: " + airdrop.getOpenTime() + " сек."));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 16 -> {
                closeTimePlayers.put(player.getUniqueId(), airdrop);
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите общее время ивента в секундах:"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущее: " + airdrop.getCloseTime() + " сек."));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 31 -> {
                airdrop.setClickToActivate(!airdrop.isClickToActivate());
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Активация по клику: " + (airdrop.isClickToActivate() ? "включена" : "выключена")));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            }
            case 20 -> {
                hologramHeightPlayers.put(player.getUniqueId(), airdrop);
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите высоту голограммы (например 1.5):"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущая: " + String.format("%.1f", airdrop.getHologramHeight())));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 21 -> {
                worldGuardRadiusPlayers.put(player.getUniqueId(), airdrop);
                player.closeInventory();
                player.sendMessage(ColorUtils.colorize("&#55ff55Введите радиус защиты региона в блоках:"));
                player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущий: " + airdrop.getWorldGuardRadius()));
                player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
            }
            case 22 -> {
                int delta = switch (clickType) {
                    case LEFT -> 100;
                    case RIGHT -> -100;
                    case SHIFT_LEFT -> 500;
                    case SHIFT_RIGHT -> -500;
                    default -> 0;
                };
                int newRadius = Math.max(100, airdrop.getRadius() + delta);
                airdrop.setRadius(newRadius);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Радиус спавна: " + newRadius));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            }
            case 29 -> {
                int delta = switch (clickType) {
                    case LEFT -> 1;
                    case RIGHT -> -1;
                    case SHIFT_LEFT -> 5;
                    case SHIFT_RIGHT -> -5;
                    default -> 0;
                };
                int newHeight = Math.max(-50, Math.min(50, airdrop.getSpawnHeightOffset() + delta));
                airdrop.setSpawnHeightOffset(newHeight);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Высота спавна: " + newHeight + " блоков"));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            }
            case 23 -> {
                if (clickType == ClickType.LEFT) {
                    player.closeInventory();
                    player.sendMessage(ColorUtils.colorize("&#55ff55Введите название схематики в чат (например: air1.schem):"));
                    player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущая: " + (airdrop.getSchematic() != null ? airdrop.getSchematic() : "отсутствует")));
                    player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
                    ChatInputListener.getSchematicPlayers().put(player.getUniqueId(), airdrop);
                } else if (clickType == ClickType.RIGHT) {
                    airdrop.setSchematic(null);
                    plugin.getAirdropManager().saveAirdrop(airdrop);
                    player.sendMessage(ColorUtils.colorize("&#ff5555Схематика сброшена!"));
                    player.closeInventory();
                    new AirdropSettingsGUI(plugin, airdrop).open(player);
                }
            }
            case 24 -> {
                if (clickType == ClickType.LEFT) {
                    airdrop.setAutoRespawn(!airdrop.isAutoRespawn());
                    plugin.getAirdropManager().saveAirdrop(airdrop);
                    if (airdrop.isAutoRespawn() && !airdrop.isActive()) {
                        airdrop.setLastDespawnTime(System.currentTimeMillis());
                        plugin.getAirdropManager().saveAirdrop(airdrop);
                    }
                    player.sendMessage(ColorUtils.colorize("&#55ff55Авто-спавн: " + (airdrop.isAutoRespawn() ? "включен" : "выключен")));
                    new AirdropSettingsGUI(plugin, airdrop).open(player);
                } else if (clickType == ClickType.RIGHT) {
                    player.closeInventory();
                    player.sendMessage(ColorUtils.colorize("&#55ff55Введите интервал авто-спавна в секундах:"));
                    player.sendMessage(ColorUtils.colorize("&#aaaaaaТекущий: " + airdrop.getAutoRespawnTime() + " сек."));
                    player.sendMessage(ColorUtils.colorize("&#ffaa55Введите &#ff5555\"отмена\" &#ffaa55для отмены"));
                    ChatInputListener.getAutoRespawnPlayers().put(player.getUniqueId(), airdrop);
                }
            }
            case 25 -> {
                int delta = switch (clickType) {
                    case LEFT -> 1;
                    case RIGHT -> -1;
                    case SHIFT_LEFT -> 5;
                    case SHIFT_RIGHT -> -5;
                    default -> 0;
                };
                int newMin = Math.max(0, airdrop.getMinOnlinePlayers() + delta);
                airdrop.setMinOnlinePlayers(newMin);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Мин. онлайн для спавна: " + newMin));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            }
            case 48 -> {
                player.closeInventory();
                new AirdropsMenuGUI(plugin).open(player);
            }
            case 49 -> {
                int totalItems = 0;
                for (Map<Integer, AirdropLootItem> page : airdrop.getLootPages()) {
                    totalItems += page.size();
                }
                player.closeInventory();
                new LootPagesGUI(plugin, airdrop).open(player);
            }
            case 50 -> {
                player.closeInventory();
                new AirdropListenersGUI(plugin, airdrop).open(player);
            }
        }
    }
}