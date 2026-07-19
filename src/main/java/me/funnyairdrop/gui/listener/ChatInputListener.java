package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.menu.AirdropSettingsGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputListener implements Listener {
    private final FunnyAirdrop plugin;
    private static final Map<UUID, Airdrop> renamingPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> inventorySizePlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> openTimePlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> closeTimePlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> hologramHeightPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> worldGuardRadiusPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> schematicPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Airdrop> autoRespawnPlayers = new ConcurrentHashMap<>();

    public ChatInputListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();

        if (renamingPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleRename(player, message);
        } else if (inventorySizePlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleInventorySize(player, message);
        } else if (openTimePlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleTimeInput(player, message, openTimePlayers, "openTime", "Время до открытия", "сек.");
        } else if (closeTimePlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleTimeInput(player, message, closeTimePlayers, "closeTime", "Общее время ивента", "сек.");
        } else if (hologramHeightPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleHologramHeight(player, message);
        } else if (worldGuardRadiusPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleWorldGuardRadius(player, message);
        } else if (schematicPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleSchematic(player, message);
        } else if (autoRespawnPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            handleAutoRespawn(player, message);
        }
    }

    private void handleRename(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = renamingPlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Переименование отменено");
            return;
        }

        Airdrop airdrop = renamingPlayers.remove(player.getUniqueId());
        final String name = message;
        Bukkit.getScheduler().runTask(plugin, () -> {
            airdrop.setDisplayName(name);
            plugin.getAirdropManager().saveAirdrop(airdrop);
            player.sendMessage(ColorUtils.colorize("&#55ff55Название изменено на: " + name));
            new AirdropSettingsGUI(plugin, airdrop).open(player);
        });
    }

    private void handleInventorySize(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = inventorySizePlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Изменение размера отменено");
            return;
        }

        try {
            int size = Integer.parseInt(message);
            if (size != 9 && size != 18 && size != 27 && size != 36 && size != 45 && size != 54) {
                throw new NumberFormatException();
            }

            Airdrop airdrop = inventorySizePlayers.remove(player.getUniqueId());
            final int finalSize = size;
            Bukkit.getScheduler().runTask(plugin, () -> {
                airdrop.setInventorySize(finalSize);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Размер инвентаря изменён на: " + finalSize));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Неверный размер! Используйте: 9, 18, 27, 36, 45, 54"));
        }
    }

    private void handleTimeInput(Player player, String message, Map<UUID, Airdrop> map,
                                 String field, String fieldName, String unit) {
        if (isCancel(message)) {
            Airdrop airdrop = map.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Изменение отменено");
            return;
        }

        try {
            int value = Integer.parseInt(message);
            if (value <= 0) throw new NumberFormatException();

            Airdrop airdrop = map.remove(player.getUniqueId());
            final int finalValue = value;
            Bukkit.getScheduler().runTask(plugin, () -> {
                switch (field) {
                    case "openTime" -> airdrop.setOpenTime(finalValue);
                    case "closeTime" -> airdrop.setCloseTime(finalValue);
                }
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55" + fieldName + " изменено на: " + finalValue + " " + unit));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Введите целое положительное число!"));
        }
    }

    private void handleHologramHeight(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = hologramHeightPlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Изменение высоты голограммы отменено");
            return;
        }

        try {
            double height = Double.parseDouble(message.replace(",", "."));
            if (height < 0.5 || height > 5.0) {
                player.sendMessage(ColorUtils.colorize("&#ff5555Высота должна быть от 0.5 до 5.0 блоков!"));
                return;
            }

            Airdrop airdrop = hologramHeightPlayers.remove(player.getUniqueId());
            final double finalHeight = height;
            Bukkit.getScheduler().runTask(plugin, () -> {
                airdrop.setHologramHeight(finalHeight);
                plugin.getAirdropManager().saveAirdrop(airdrop);

                if (airdrop.isActive() && plugin.getHologramHook() != null) {
                    plugin.getHologramHook().updateHologram(airdrop);
                }

                player.sendMessage(ColorUtils.colorize("&#55ff55Высота голограммы изменена на: " + String.format("%.1f", finalHeight)));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Введите число (например 1.5)!"));
        }
    }

    private void handleWorldGuardRadius(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = worldGuardRadiusPlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Изменение радиуса защиты отменено");
            return;
        }

        try {
            int radius = Integer.parseInt(message);
            if (radius < 1 || radius > 100) {
                player.sendMessage(ColorUtils.colorize("&#ff5555Радиус должен быть от 1 до 100 блоков!"));
                return;
            }

            Airdrop airdrop = worldGuardRadiusPlayers.remove(player.getUniqueId());
            final int finalRadius = radius;
            Bukkit.getScheduler().runTask(plugin, () -> {
                airdrop.setWorldGuardRadius(finalRadius);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Радиус защиты изменён на: " + finalRadius + " блоков"));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Введите целое число от 1 до 100!"));
        }
    }

    private void handleSchematic(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = schematicPlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Выбор схематики отменен");
            return;
        }

        Airdrop airdrop = schematicPlayers.remove(player.getUniqueId());
        final String schematicName = message;
        Bukkit.getScheduler().runTask(plugin, () -> {
            airdrop.setSchematic(schematicName);
            plugin.getAirdropManager().saveAirdrop(airdrop);
            player.sendMessage(ColorUtils.colorize("&#55ff55Схематика установлена: " + schematicName));
            new AirdropSettingsGUI(plugin, airdrop).open(player);
        });
    }

    private void handleAutoRespawn(Player player, String message) {
        if (isCancel(message)) {
            Airdrop airdrop = autoRespawnPlayers.remove(player.getUniqueId());
            openSettingsBack(player, airdrop, "Изменение интервала отменено");
            return;
        }

        try {
            int interval = Integer.parseInt(message);
            if (interval < 10) {
                player.sendMessage(ColorUtils.colorize("&#ff5555Интервал должен быть не менее 10 секунд!"));
                return;
            }

            Airdrop airdrop = autoRespawnPlayers.remove(player.getUniqueId());
            final int finalInterval = interval;
            Bukkit.getScheduler().runTask(plugin, () -> {
                airdrop.setAutoRespawnTime(finalInterval);
                plugin.getAirdropManager().saveAirdrop(airdrop);
                player.sendMessage(ColorUtils.colorize("&#55ff55Интервал авто-спавна изменён на: " + finalInterval + " сек."));
                new AirdropSettingsGUI(plugin, airdrop).open(player);
            });
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&#ff5555Введите целое число!"));
        }
    }

    private boolean isCancel(String message) {
        return message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel");
    }

    private void openSettingsBack(Player player, Airdrop airdrop, String cancelMessage) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ColorUtils.colorize("&#ffaa55" + cancelMessage));
            new AirdropSettingsGUI(plugin, airdrop).open(player);
        });
    }

    public void cleanup() {
        renamingPlayers.clear();
        inventorySizePlayers.clear();
        openTimePlayers.clear();
        closeTimePlayers.clear();
        hologramHeightPlayers.clear();
        worldGuardRadiusPlayers.clear();
        schematicPlayers.clear();
        autoRespawnPlayers.clear();
    }

    public static Map<UUID, Airdrop> getRenamingPlayers() { return renamingPlayers; }
    public static Map<UUID, Airdrop> getInventorySizePlayers() { return inventorySizePlayers; }
    public static Map<UUID, Airdrop> getOpenTimePlayers() { return openTimePlayers; }
    public static Map<UUID, Airdrop> getCloseTimePlayers() { return closeTimePlayers; }
    public static Map<UUID, Airdrop> getHologramHeightPlayers() { return hologramHeightPlayers; }
    public static Map<UUID, Airdrop> getWorldGuardRadiusPlayers() { return worldGuardRadiusPlayers; }
    public static Map<UUID, Airdrop> getSchematicPlayers() { return schematicPlayers; }
    public static Map<UUID, Airdrop> getAutoRespawnPlayers() { return autoRespawnPlayers; }
}