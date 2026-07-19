package me.funnyairdrop.hook;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropSpawnTask;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HologramHook {
    private final FunnyAirdrop plugin;
    private final Map<String, IHologram> holograms;
    private int updateTaskId;

    public HologramHook(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.holograms = new ConcurrentHashMap<>();
        startUpdateTask();
    }

    public void createHologram(Airdrop airdrop) {
        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        removeHologram(airdrop.getName());

        double height = airdrop.getHologramHeight();
        Location holoLocation = loc.clone().add(0, height, 0);

        List<String> lines = processHoloLines(airdrop.getHoloConfig().getClosedLines(), airdrop);

        String hologramName = "airdrop_" + airdrop.getName();

        IHologram hologram;
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            hologram = new DecentHologram();
        } else {
            hologram = new EmptyHologram();
            plugin.getLogger().warning("DecentHolograms not found! Holograms disabled.");
        }

        hologram.createOrUpdateHologram(lines, holoLocation, hologramName);
        holograms.put(airdrop.getName(), hologram);
    }

    public void updateHologram(Airdrop airdrop) {
        IHologram hologram = holograms.get(airdrop.getName());
        if (hologram == null) return;

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        double height = airdrop.getHologramHeight();
        Location holoLocation = loc.clone().add(0, height, 0);

        List<String> lines;
        if (!airdrop.isOpened()) {
            lines = processHoloLines(airdrop.getHoloConfig().getClosedLines(), airdrop);
        } else {
            lines = processHoloLines(airdrop.getHoloConfig().getOpenedLines(), airdrop);
        }

        String hologramName = "airdrop_" + airdrop.getName();
        hologram.createOrUpdateHologram(lines, holoLocation, hologramName);
    }

    public void removeHologram(String airdropName) {
        IHologram hologram = holograms.remove(airdropName);
        if (hologram != null) {
            String hologramName = "airdrop_" + airdropName;
            hologram.remove(hologramName);
        }
    }

    public void removeAllHolograms() {
        for (Map.Entry<String, IHologram> entry : holograms.entrySet()) {
            String hologramName = "airdrop_" + entry.getKey();
            entry.getValue().remove(hologramName);
        }
        holograms.clear();
    }

    private void startUpdateTask() {
        updateTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Airdrop airdrop : plugin.getAirdropManager().getAllAirdrops()) {
                if (airdrop.isActive()) {
                    AirdropSpawnTask task = plugin.getAirdropManager().getActiveTasks().get(airdrop.getName());
                    if (task != null && !task.isWaitingForActivation()) {
                        updateHologram(airdrop);
                    }
                }
            }
        }, 20L, 20L).getTaskId();
    }

    private List<String> processHoloLines(List<String> lines, Airdrop airdrop) {
        List<String> processed = new ArrayList<>();

        int timeToOpen = 0;
        int timeLeft = 0;

        for (AirdropSpawnTask task : plugin.getAirdropManager().getActiveTasks().values()) {
            if (task.getAirdrop().getName().equals(airdrop.getName())) {
                if (!airdrop.isOpened()) {
                    timeToOpen = Math.max(0, airdrop.getOpenTime() - task.getTimeElapsed());
                    timeLeft = airdrop.getCloseTime();
                } else {
                    timeToOpen = 0;
                    timeLeft = Math.max(0, airdrop.getCloseTime() - (task.getTimeElapsed() - airdrop.getOpenTime()));
                }
                break;
            }
        }

        for (String line : lines) {
            String processedLine = line
                    .replace("{displayname}", airdrop.getDisplayName())
                    .replace("{air-name}", airdrop.getName())
                    .replace("{time-to-open}", formatTime(timeToOpen))
                    .replace("{time}", formatTime(timeLeft));

            processed.add(ColorUtils.colorize(processedLine));
        }

        return processed;
    }

    public void updateClickToActivateHologram(Airdrop airdrop) {
        IHologram hologram = holograms.get(airdrop.getName());
        if (hologram == null) {
            Location loc = airdrop.getCurrentLocation();
            if (loc == null) return;
            double height = airdrop.getHologramHeight();
            Location holoLocation = loc.clone().add(0, height, 0);

            List<String> lines = new ArrayList<>();
            for (String line : airdrop.getClickToActivateHoloLines()) {
                lines.add(ColorUtils.colorize(line.replace("{air-name}", airdrop.getName())
                        .replace("{displayname}", airdrop.getDisplayName())));
            }

            String hologramName = "airdrop_" + airdrop.getName();
            IHologram newHologram;
            if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                newHologram = new DecentHologram();
            } else {
                newHologram = new EmptyHologram();
            }
            newHologram.createOrUpdateHologram(lines, holoLocation, hologramName);
            holograms.put(airdrop.getName(), newHologram);
            return;
        }

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;
        double height = airdrop.getHologramHeight();
        Location holoLocation = loc.clone().add(0, height, 0);

        List<String> lines = new ArrayList<>();
        for (String line : airdrop.getClickToActivateHoloLines()) {
            lines.add(ColorUtils.colorize(line.replace("{air-name}", airdrop.getName())
                    .replace("{displayname}", airdrop.getDisplayName())));
        }

        String hologramName = "airdrop_" + airdrop.getName();
        hologram.createOrUpdateHologram(lines, holoLocation, hologramName);
    }

    private String formatTime(int seconds) {
        if (seconds <= 0) return "0 сек.";
        if (seconds < 60) return seconds + " сек.";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0) return minutes + " мин.";
        return minutes + " мин. " + secs + " сек.";
    }

    public void stopUpdateTask() {
        if (updateTaskId > 0) {
            Bukkit.getScheduler().cancelTask(updateTaskId);
        }
    }
}