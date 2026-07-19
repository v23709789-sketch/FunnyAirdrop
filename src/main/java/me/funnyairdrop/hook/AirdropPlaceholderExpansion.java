package me.funnyairdrop.hook;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropSpawnTask;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AirdropPlaceholderExpansion extends PlaceholderExpansion {

    private final FunnyAirdrop plugin;

    public AirdropPlaceholderExpansion(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "funnyairdrop";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FunnyAirDrop";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.6";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String[] parts = identifier.split("_", 2);
        if (parts.length < 2) return null;

        String airdropName = parts[0];
        String placeholder = parts[1];

        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(airdropName);
        if (airdrop == null) return null;

        AirdropSpawnTask task = plugin.getAirdropManager().getActiveTasks().get(airdropName);

        return switch (placeholder) {
            case "displayname" -> airdrop.getDisplayName();
            case "name" -> airdrop.getName();
            case "world" -> airdrop.getWorldName();
            case "radius" -> String.valueOf(airdrop.getRadius());
            case "open_time" -> String.valueOf(airdrop.getOpenTime());
            case "close_time" -> String.valueOf(airdrop.getCloseTime());
            case "inventory_size" -> String.valueOf(airdrop.getInventorySize());
            case "hologram_height" -> String.format("%.1f", airdrop.getHologramHeight());
            case "world_guard_radius" -> String.valueOf(airdrop.getWorldGuardRadius());
            case "auto_respawn_time" -> String.valueOf(airdrop.getAutoRespawnTime());
            case "min_online" -> String.valueOf(airdrop.getMinOnlinePlayers());
            case "online" -> String.valueOf(Bukkit.getOnlinePlayers().size());
            case "active" -> String.valueOf(airdrop.isActive());
            case "opened" -> String.valueOf(airdrop.isOpened());
            case "looted" -> String.valueOf(airdrop.isLooted());
            case "auto_respawn" -> String.valueOf(airdrop.isAutoRespawn());
            case "click_to_activate" -> String.valueOf(airdrop.isClickToActivate());
            case "static_mode" -> String.valueOf(airdrop.isStaticMode());
            case "status" -> {
                if (airdrop.isActive() && airdrop.isOpened()) yield "Открыт";
                if (airdrop.isActive() && !airdrop.isOpened()) yield "Закрыт";
                if (!airdrop.isActive() && airdrop.isAutoRespawn()) yield "Ожидает спавна";
                yield "Не активен";
            }
            case "x" -> airdrop.getCurrentLocation() != null ? String.valueOf(airdrop.getCurrentLocation().getBlockX()) : "N/A";
            case "y" -> airdrop.getCurrentLocation() != null ? String.valueOf(airdrop.getCurrentLocation().getBlockY()) : "N/A";
            case "z" -> airdrop.getCurrentLocation() != null ? String.valueOf(airdrop.getCurrentLocation().getBlockZ()) : "N/A";
            case "time" -> task != null ? String.valueOf(getTimeLeft(airdrop, task)) : "0";
            case "time_formatted" -> task != null ? formatTime(getTimeLeft(airdrop, task)) : "0 сек.";
            case "time_to_open" -> task != null ? String.valueOf(getTimeToOpen(airdrop, task)) : "0";
            case "time_to_open_formatted" -> task != null ? formatTime(getTimeToOpen(airdrop, task)) : "0 сек.";
            case "time_to_respawn" -> String.valueOf(getTimeToRespawn(airdrop));
            case "time_to_respawn_formatted" -> formatTime(getTimeToRespawn(airdrop));
            case "spawn_time" -> airdrop.getSpawnTime() > 0 ? new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(airdrop.getSpawnTime())) : "N/A";
            default -> null;
        };
    }

    private int getTimeLeft(Airdrop airdrop, AirdropSpawnTask task) {
        if (!airdrop.isOpened()) {
            return Math.max(0, airdrop.getOpenTime() - task.getTimeElapsed());
        } else {
            return Math.max(0, airdrop.getCloseTime() - (task.getTimeElapsed() - airdrop.getOpenTime()));
        }
    }

    private int getTimeToOpen(Airdrop airdrop, AirdropSpawnTask task) {
        if (airdrop.isOpened()) return 0;
        return Math.max(0, airdrop.getOpenTime() - task.getTimeElapsed());
    }

    private int getTimeToRespawn(Airdrop airdrop) {
        if (airdrop.isActive() || !airdrop.isAutoRespawn()) return 0;

        long waitTime = airdrop.getAutoRespawnTime() * 1000L;
        long remaining = waitTime;

        if (airdrop.getLastDespawnTime() > 0) {
            long elapsed = System.currentTimeMillis() - airdrop.getLastDespawnTime();
            remaining = Math.max(0, waitTime - elapsed);
        }

        return (int) (remaining / 1000);
    }

    private String formatTime(int seconds) {
        if (seconds <= 0) return "0 сек.";
        if (seconds < 60) return seconds + " сек.";
        if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            if (secs == 0) return minutes + " мин.";
            return minutes + " мин. " + secs + " сек.";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (secs == 0 && minutes == 0) return hours + " ч.";
        if (secs == 0) return hours + " ч. " + minutes + " мин.";
        return hours + " ч. " + minutes + " мин. " + secs + " сек.";
    }
}