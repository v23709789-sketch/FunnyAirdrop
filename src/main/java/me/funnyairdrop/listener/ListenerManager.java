package me.funnyairdrop.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropSpawnTask;
import me.funnyairdrop.util.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ListenerManager {
    private final FunnyAirdrop plugin;
    private final Map<String, EventListener> eventListeners;
    private final File listenerFile;
    private final Map<String, BossBar> activeBossBars;

    public ListenerManager(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.eventListeners = new LinkedHashMap<>();
        this.listenerFile = new File(plugin.getDataFolder(), "listener.yml");
        this.activeBossBars = new HashMap<>();
        loadListeners();
    }

    private void loadListeners() {
        if (!listenerFile.exists()) {
            plugin.saveResource("listener.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(listenerFile);
        ConfigurationSection listenersSection = config.getConfigurationSection("listeners");

        if (listenersSection == null) return;

        for (String key : listenersSection.getKeys(false)) {
            String description = listenersSection.getString(key + ".description", "");
            String eventName = listenersSection.getString(key + ".event", "");
            List<String> commands = listenersSection.getStringList(key + ".commands");

            Map<String, Object> settings = new HashMap<>();
            ConfigurationSection settingsSection = listenersSection.getConfigurationSection(key + ".settings");
            if (settingsSection != null) {
                for (String settingKey : settingsSection.getKeys(true)) {
                    settings.put(settingKey, settingsSection.get(settingKey));
                }
            }

            try {
                AirdropEvent event = AirdropEvent.valueOf(eventName.toUpperCase());
                EventListener listener = new EventListener(key, description, event, commands, settings);
                eventListeners.put(key, listener);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown event type: " + eventName + " for listener " + key);
            }
        }
    }

    public int getBossBarRadius(String listenerName) {
        EventListener listener = eventListeners.get(listenerName);
        if (listener == null) return -1;

        Object radiusObj = listener.getSettings().get("radius");
        if (radiusObj instanceof Number) {
            return ((Number) radiusObj).intValue();
        }
        return -1;
    }

    public double getParticleRadius(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.PARTICLE);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return 1.5;

        Object radiusObj = listener.getSettings().get("radius");
        if (radiusObj instanceof Number) {
            return ((Number) radiusObj).doubleValue();
        }
        return 1.5;
    }

    public double getParticleHeight(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.PARTICLE);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return 1.0;

        Object heightObj = listener.getSettings().get("height");
        if (heightObj instanceof Number) {
            return ((Number) heightObj).doubleValue();
        }
        return 1.0;
    }

    public void executeEvent(AirdropEvent eventType, Airdrop airdrop, Player player) {
        AirdropListenerConfig config = airdrop.getListenerConfig();
        if (config == null) return;

        for (EventListener listener : eventListeners.values()) {
            if (listener.getEvent() == eventType) {
                if (!config.isEnabled(listener.getName())) continue;

                List<String> commands = config.getCustomCommands(listener.getName());
                if (commands == null || commands.isEmpty()) {
                    commands = listener.getCommands();
                }

                if (commands != null && !commands.isEmpty()) {
                    executeCommands(commands, airdrop, player);
                }
            }
        }
    }

    public void removeBossBar(String airdropName) {
        String bossBarName = "airdrop_" + airdropName;
        BossBar bossBar = activeBossBars.remove(bossBarName);
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void removeAllBossBars() {
        for (BossBar bossBar : activeBossBars.values()) {
            bossBar.removeAll();
        }
        activeBossBars.clear();
    }

    public EventListener getListenerByEvent(AirdropEvent eventType) {
        for (EventListener listener : eventListeners.values()) {
            if (listener.getEvent() == eventType) {
                return listener;
            }
        }
        return null;
    }

    public boolean isListenerEnabled(Airdrop airdrop, AirdropEvent eventType) {
        AirdropListenerConfig config = airdrop.getListenerConfig();
        if (config == null) return false;

        for (EventListener listener : eventListeners.values()) {
            if (listener.getEvent() == eventType) {
                return config.isEnabled(listener.getName());
            }
        }
        return false;
    }

    public int getPickupDelayTicks(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.PICKUP_DELAY);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return 0;

        Object delayObj = listener.getSettings().get("delay_ticks");
        if (delayObj instanceof Number) {
            return ((Number) delayObj).intValue();
        }
        return 0;
    }

    public List<Material> getDisguiseItems(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.LOOT_DISGUISE);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return new ArrayList<>();

        List<Material> materials = new ArrayList<>();
        Object disguiseObj = listener.getSettings().get("disguise_items");
        if (disguiseObj instanceof List) {
            for (Object item : (List<?>) disguiseObj) {
                try {
                    materials.add(Material.valueOf(item.toString().toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return materials;
    }

    private void executeCommands(List<String> commands, Airdrop airdrop, Player player) {
        Location loc = airdrop.getCurrentLocation();
        int timeLeft = getTimeLeft(airdrop);
        int timeToOpen = getTimeToOpen(airdrop);
        String spawnTimeStr = airdrop.getSpawnTime() > 0 ?
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(airdrop.getSpawnTime())) : "N/A";
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int minOnline = airdrop.getMinOnlinePlayers();

        for (String command : commands) {
            String processedCommand = command;

            processedCommand = processedCommand
                    .replace("{displayname}", airdrop.getDisplayName())
                    .replace("{name}", airdrop.getName())
                    .replace("{world}", airdrop.getWorldName())
                    .replace("{radius}", String.valueOf(airdrop.getRadius()))
                    .replace("{open_time}", String.valueOf(airdrop.getOpenTime()))
                    .replace("{close_time}", String.valueOf(airdrop.getCloseTime()))
                    .replace("{inventory_size}", String.valueOf(airdrop.getInventorySize()))
                    .replace("{hologram_height}", String.format("%.1f", airdrop.getHologramHeight()))
                    .replace("{world_guard_radius}", String.valueOf(airdrop.getWorldGuardRadius()))
                    .replace("{auto_respawn_time}", String.valueOf(airdrop.getAutoRespawnTime()))
                    .replace("{min_online}", String.valueOf(minOnline))
                    .replace("{online_players}", String.valueOf(onlinePlayers))
                    .replace("{spawn_time}", spawnTimeStr)
                    .replace("{time}", formatTime(timeLeft))
                    .replace("{time-to-open}", formatTime(timeToOpen))
                    .replace("{status}", airdrop.isOpened() ? "Открыт" : "Закрыт")
                    .replace("{active}", String.valueOf(airdrop.isActive()))
                    .replace("{auto_respawn}", String.valueOf(airdrop.isAutoRespawn()))
                    .replace("{click_to_activate}", String.valueOf(airdrop.isClickToActivate()))
                    .replace("{static_mode}", String.valueOf(airdrop.isStaticMode()));

            if (loc != null) {
                processedCommand = processedCommand
                        .replace("{x}", String.valueOf(loc.getBlockX()))
                        .replace("{y}", String.valueOf(loc.getBlockY()))
                        .replace("{z}", String.valueOf(loc.getBlockZ()));
            }

            if (player != null) {
                processedCommand = processedCommand
                        .replace("{player}", player.getName())
                        .replace("{player_display}", LegacyComponentSerializer.legacySection().serialize(player.displayName()));
            }

            if (processedCommand.startsWith("[CONSOLE]")) {
                String consoleCommand = processedCommand.replace("[CONSOLE] ", "");
                consoleCommand = consoleCommand.replace("{id}", airdrop.getName());
                consoleCommand = consoleCommand.replace("{name}", airdrop.getName());
                consoleCommand = processMatchPlaceholders(consoleCommand, loc);

                consoleCommand = consoleCommand
                        .replace("{x}", loc != null ? String.valueOf(loc.getBlockX()) : "0")
                        .replace("{y}", loc != null ? String.valueOf(loc.getBlockY()) : "0")
                        .replace("{z}", loc != null ? String.valueOf(loc.getBlockZ()) : "0");

                final String finalCommand = consoleCommand;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } else if (processedCommand.startsWith("[BOSS_BAR_ALL]")) {
                String bossBarData = processedCommand.replace("[BOSS_BAR_ALL] ", "");
                handleBossBar(bossBarData, airdrop, true, player);
            } else if (processedCommand.startsWith("[BOSS_BAR]")) {
                String bossBarData = processedCommand.replace("[BOSS_BAR] ", "");
                handleBossBar(bossBarData, airdrop, false, player);
            } else if (processedCommand.startsWith("[MESSAGE_ALL]")) {
                String message = processedCommand.replace("[MESSAGE_ALL] ", "");
                Component component = LegacyComponentSerializer.legacySection().deserialize(ColorUtils.colorize(message));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(component);
                }
            } else if (processedCommand.startsWith("[MESSAGE]")) {
                String message = processedCommand.replace("[MESSAGE] ", "");
                if (player != null) {
                    player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(ColorUtils.colorize(message)));
                }
            } else if (processedCommand.startsWith("[TITLE_ALL]")) {
                String title = processedCommand.replace("[TITLE_ALL] ", "");
                String[] parts = title.split("\\\\n");
                String mainTitle = parts[0];
                String subtitle = parts.length > 1 ? parts[1] : "";
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ColorUtils.colorize(mainTitle), ColorUtils.colorize(subtitle), 10, 70, 20);
                }
            } else if (processedCommand.startsWith("[TITLE]")) {
                String title = processedCommand.replace("[TITLE] ", "");
                String[] parts = title.split("\\\\n");
                String mainTitle = parts[0];
                String subtitle = parts.length > 1 ? parts[1] : "";
                if (player != null) {
                    player.sendTitle(ColorUtils.colorize(mainTitle), ColorUtils.colorize(subtitle), 10, 70, 20);
                }
            } else if (processedCommand.startsWith("[SOUND_ALL]")) {
                String soundName = processedCommand.replace("[SOUND_ALL] ", "");
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound: " + soundName);
                }
            } else if (processedCommand.startsWith("[SOUND]")) {
                String soundName = processedCommand.replace("[SOUND] ", "");
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    if (player != null) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound: " + soundName);
                }
            } else if (processedCommand.startsWith("[ACTIONBAR_ALL]")) {
                String message = processedCommand.replace("[ACTIONBAR_ALL] ", "");
                Component component = LegacyComponentSerializer.legacySection().deserialize(ColorUtils.colorize(message));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(component);
                }
            } else if (processedCommand.startsWith("[ACTIONBAR]")) {
                String message = processedCommand.replace("[ACTIONBAR] ", "");
                if (player != null) {
                    player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(ColorUtils.colorize(message)));
                }
            } else if (processedCommand.startsWith("[PARTICLE_SHAPE_ALL]")) {
                String shapeData = processedCommand.replace("[PARTICLE_SHAPE_ALL] ", "");
                handleParticleShape(shapeData, airdrop, null);
            } else if (processedCommand.startsWith("[PARTICLE_SHAPE]")) {
                String shapeData = processedCommand.replace("[PARTICLE_SHAPE] ", "");
                handleParticleShape(shapeData, airdrop, player);
            } else if (processedCommand.startsWith("[PARTICLE_ALL]")) {
                String particleData = processedCommand.replace("[PARTICLE_ALL] ", "");
                handleParticle(particleData, airdrop, null);
            } else if (processedCommand.startsWith("[PARTICLE]")) {
                String particleData = processedCommand.replace("[PARTICLE] ", "");
                handleParticle(particleData, airdrop, player);
            }
        }
    }

    private String processMatchPlaceholders(String command, Location loc) {
        if (loc == null) return command;

        Pattern pattern = Pattern.compile("match\\(([xyz])([+-]\\d+)?\\)");
        Matcher matcher = pattern.matcher(command);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String axis = matcher.group(1);
            String offsetStr = matcher.group(2);
            int offset = 0;
            if (offsetStr != null && !offsetStr.isEmpty()) {
                offset = Integer.parseInt(offsetStr);
            }

            int value;
            switch (axis) {
                case "x" -> value = loc.getBlockX() + offset;
                case "y" -> value = loc.getBlockY() + offset;
                case "z" -> value = loc.getBlockZ() + offset;
                default -> value = 0;
            }

            matcher.appendReplacement(sb, String.valueOf(value));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private void handleParticle(String particleData, Airdrop airdrop, Player specificPlayer) {
        String[] parts = particleData.split("\\|");
        if (parts.length < 8) return;

        String particleName = parts[0].trim().toUpperCase();
        double xOffset = Double.parseDouble(parts[1].trim());
        double yOffset = Double.parseDouble(parts[2].trim());
        double zOffset = Double.parseDouble(parts[3].trim());
        int count = Integer.parseInt(parts[4].trim());
        double offsetX = Double.parseDouble(parts[5].trim());
        double offsetY = Double.parseDouble(parts[6].trim());
        double offsetZ = Double.parseDouble(parts[7].trim());
        double speed = parts.length > 8 ? Double.parseDouble(parts[8].trim()) : 0.01;

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        Location particleLoc = loc.clone().add(xOffset, yOffset + getParticleHeight(airdrop), zOffset);

        try {
            Particle particle = Particle.valueOf(particleName);
            double radius = getParticleRadius(airdrop);

            if (specificPlayer != null) {
                specificPlayer.spawnParticle(particle, particleLoc, count, offsetX, offsetY, offsetZ, speed);
            } else {
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distance(loc) <= radius + 15) {
                        p.spawnParticle(particle, particleLoc, count, offsetX, offsetY, offsetZ, speed);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + particleName);
        }
    }

    private void handleParticleShape(String shapeData, Airdrop airdrop, Player specificPlayer) {
        String[] parts = shapeData.split("\\|");
        if (parts.length < 7) return;

        String shapeType = parts[0].trim().toLowerCase();
        String particleName = parts[1].trim().toUpperCase();
        double radius = Double.parseDouble(parts[2].trim());
        double height = Double.parseDouble(parts[3].trim());
        int points = Integer.parseInt(parts[4].trim());
        double yOffset = Double.parseDouble(parts[5].trim());
        int count = Integer.parseInt(parts[6].trim());
        double speed = parts.length > 7 ? Double.parseDouble(parts[7].trim()) : 0.01;

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        try {
            Particle particle = Particle.valueOf(particleName);
            double shapeRadius = getParticleRadius(airdrop);
            double particleHeight = getParticleHeight(airdrop);

            switch (shapeType) {
                case "circle" -> {
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = loc.clone().add(x, height + particleHeight, z);
                        spawnParticleForPlayers(particle, particleLoc, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
                case "ring" -> {
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location particleLoc = loc.clone().add(x, height + particleHeight + yOffset, z);
                        spawnParticleForPlayers(particle, particleLoc, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
                case "sphere" -> {
                    for (int i = 0; i < points; i++) {
                        double phi = Math.acos(1 - 2.0 * (i + 0.5) / points);
                        double theta = Math.PI * (1 + Math.sqrt(5)) * i;
                        double x = Math.cos(theta) * Math.sin(phi) * radius;
                        double y = Math.cos(phi) * radius;
                        double z = Math.sin(theta) * Math.sin(phi) * radius;
                        Location particleLoc = loc.clone().add(x, y + height + particleHeight, z);
                        spawnParticleForPlayers(particle, particleLoc, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
                case "spiral" -> {
                    double spiralHeight = parts.length > 8 ? Double.parseDouble(parts[8].trim()) : 3.0;
                    int rotations = parts.length > 9 ? Integer.parseInt(parts[9].trim()) : 3;
                    int totalPoints = points * rotations;

                    for (int i = 0; i < totalPoints; i++) {
                        double progress = (double) i / points;
                        double angle = 2 * Math.PI * progress;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = height + particleHeight + (progress / rotations) * spiralHeight;
                        Location particleLoc = loc.clone().add(x, y, z);
                        spawnParticleForPlayers(particle, particleLoc, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
                case "helix" -> {
                    int helixPoints = points * 3;
                    for (int i = 0; i < helixPoints; i++) {
                        double progress = (double) i / points;
                        double angle = 2 * Math.PI * progress;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = height + particleHeight + progress;

                        Location particleLoc1 = loc.clone().add(x, y, z);
                        Location particleLoc2 = loc.clone().add(-x, y, -z);

                        spawnParticleForPlayers(particle, particleLoc1, count, loc, shapeRadius, speed, specificPlayer);
                        spawnParticleForPlayers(particle, particleLoc2, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
                case "star" -> {
                    int outerPoints = points;
                    for (int i = 0; i < outerPoints * 2; i++) {
                        double angle = Math.PI * i / outerPoints;
                        double r = (i % 2 == 0) ? radius : radius * 0.4;
                        double x = Math.cos(angle) * r;
                        double z = Math.sin(angle) * r;
                        Location particleLoc = loc.clone().add(x, height + particleHeight, z);
                        spawnParticleForPlayers(particle, particleLoc, count, loc, shapeRadius, speed, specificPlayer);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle: " + particleName);
        }
    }

    private void spawnParticleForPlayers(Particle particle, Location loc, int count,
                                         Location center, double radius, double speed, Player specificPlayer) {
        if (specificPlayer != null) {
            specificPlayer.spawnParticle(particle, loc, count, 0, 0, 0, speed);
        } else {
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distance(center) <= radius + 15) {
                    p.spawnParticle(particle, loc, count, 0, 0, 0, speed);
                }
            }
        }
    }

    private void handleBossBar(String bossBarData, Airdrop airdrop, boolean allPlayers, Player specificPlayer) {
        String[] parts = bossBarData.split("\\|");

        String text = ColorUtils.colorize(parts[0].trim());
        double progress = 1.0;
        BarColor color = BarColor.RED;
        BarStyle style = BarStyle.SOLID;

        if (parts.length >= 2) {
            String progressStr = parts[1].trim();
            try {
                progress = Double.parseDouble(progressStr);
            } catch (NumberFormatException e) {
                progress = 1.0;
            }
        }
        if (parts.length >= 3) {
            try {
                color = BarColor.valueOf(parts[2].trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        if (parts.length >= 4) {
            try {
                style = BarStyle.valueOf(parts[3].trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int timeLeft = getTimeLeft(airdrop);
        int timeToOpen = getTimeToOpen(airdrop);
        Location loc = airdrop.getCurrentLocation();

        if (loc != null) {
            text = text.replace("{x}", String.valueOf(loc.getBlockX()))
                    .replace("{y}", String.valueOf(loc.getBlockY()))
                    .replace("{z}", String.valueOf(loc.getBlockZ()));
        }
        text = text.replace("{time}", formatTime(timeLeft))
                .replace("{time-to-open}", formatTime(timeToOpen))
                .replace("{displayname}", airdrop.getDisplayName())
                .replace("{name}", airdrop.getName());

        String bossBarName = "airdrop_" + airdrop.getName();
        BossBar bossBar = activeBossBars.get(bossBarName);
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(text, color, style);
            activeBossBars.put(bossBarName, bossBar);
        } else {
            bossBar.setTitle(text);
            bossBar.setColor(color);
            bossBar.setStyle(style);
        }
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

        bossBar.removeAll();

        int radius = -1;
        if (allPlayers) {
            radius = getBossBarRadius("boss_bar_closed");
            if (radius == -1) radius = getBossBarRadius("boss_bar_opened");
            if (radius == -1) radius = getBossBarRadius("boss_bar_click_activate");
        }

        if (allPlayers) {
            if (radius <= 0 || loc == null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(p);
                }
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getLocation().getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) <= radius) {
                        bossBar.addPlayer(p);
                    }
                }
            }
        } else if (specificPlayer != null) {
            bossBar.addPlayer(specificPlayer);
        }
    }

    private int getTimeLeft(Airdrop airdrop) {
        if (airdrop == null) return 0;
        for (AirdropSpawnTask task : plugin.getAirdropManager().getActiveTasks().values()) {
            if (task.getAirdrop().getName().equals(airdrop.getName())) {
                if (!airdrop.isOpened()) {
                    return Math.max(0, airdrop.getOpenTime() - task.getTimeElapsed());
                } else {
                    return Math.max(0, airdrop.getCloseTime() - (task.getTimeElapsed() - airdrop.getOpenTime()));
                }
            }
        }
        return 0;
    }

    private int getTimeToOpen(Airdrop airdrop) {
        if (airdrop == null || airdrop.isOpened()) return 0;
        for (AirdropSpawnTask task : plugin.getAirdropManager().getActiveTasks().values()) {
            if (task.getAirdrop().getName().equals(airdrop.getName())) {
                return Math.max(0, airdrop.getOpenTime() - task.getTimeElapsed());
            }
        }
        return 0;
    }

    public int getProgressiveLootInterval(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.PROGRESSIVE_LOOT);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return 120;

        Object intervalObj = listener.getSettings().get("interval_ticks");
        if (intervalObj instanceof Number) {
            return ((Number) intervalObj).intValue();
        }
        return 120;
    }

    public int getProgressiveLootItemsPerInterval(Airdrop airdrop) {
        EventListener listener = getListenerByEvent(AirdropEvent.PROGRESSIVE_LOOT);
        if (listener == null || !airdrop.getListenerConfig().isEnabled(listener.getName())) return 10;

        Object itemsObj = listener.getSettings().get("items_per_interval");
        if (itemsObj instanceof Number) {
            return ((Number) itemsObj).intValue();
        }
        return 10;
    }

    private String formatTime(int seconds) {
        if (seconds <= 0) return "0 сек.";
        if (seconds < 60) return seconds + " сек.";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0) return minutes + " мин.";
        return minutes + " мин. " + secs + " сек.";
    }

    public Map<String, EventListener> getEventListeners() {
        return eventListeners;
    }

    public void reload() {
        removeAllBossBars();
        eventListeners.clear();
        loadListeners();
    }
}