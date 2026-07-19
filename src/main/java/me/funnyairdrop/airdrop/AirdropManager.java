package me.funnyairdrop.airdrop;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class AirdropManager {
    private final FunnyAirdrop plugin;
    private final Map<String, Airdrop> airdrops;
    private final Map<String, AirdropSpawnTask> activeTasks;
    private final File airdropsFolder;
    private final Set<String> scheduledRespawns;

    public AirdropManager(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.airdrops = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
        this.airdropsFolder = new File(plugin.getDataFolder(), "airdrops");
        this.scheduledRespawns = new HashSet<>();
    }

    public void loadAllAirdrops() {
        airdrops.clear();

        File[] files = airdropsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName().replace(".yml", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Airdrop airdrop = (Airdrop) config.get("airdrop");
            if (airdrop != null) {
                airdrops.put(name, airdrop);
            }
        }

        plugin.getLogger().info("Loaded " + airdrops.size() + " airdrops");
    }

    public void createAirdrop(String name) {
        if (airdrops.containsKey(name)) {
            throw new IllegalArgumentException("Airdrop already exists: " + name);
        }

        Airdrop airdrop = new Airdrop(name);
        airdrops.put(name, airdrop);
        saveAirdrop(airdrop);
    }

    public void deleteAirdrop(String name) {
        Airdrop airdrop = airdrops.remove(name);
        if (airdrop == null) {
            throw new IllegalArgumentException("Airdrop not found: " + name);
        }

        if (airdrop.isActive()) {
            stopAirdrop(name);
        }

        File file = new File(airdropsFolder, name + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public void startAirdrop(String name) {
        Airdrop airdrop = getAirdrop(name);
        if (airdrop.isActive()) {
            throw new IllegalStateException("Airdrop is already active");
        }

        if (airdrop.getMinOnlinePlayers() > 0) {
            int onlineCount = Bukkit.getOnlinePlayers().size();
            if (onlineCount < airdrop.getMinOnlinePlayers()) {
                throw new IllegalStateException("Недостаточно игроков онлайн! Нужно: " + airdrop.getMinOnlinePlayers() + ", сейчас: " + onlineCount);
            }
        }

        World world = Bukkit.getWorld(airdrop.getWorldName());
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + airdrop.getWorldName());
        }

        airdrop.setSpawnTime(System.currentTimeMillis());

        if (airdrop.isStaticMode() && airdrop.getStaticLocation() != null) {
            spawnAirdrop(world, airdrop, airdrop.getStaticLocation().clone());
        } else {
            int maxAttempts = plugin.getConfigManager().getMaxSpawnAttempts();
            findLocationAndSpawn(world, airdrop, maxAttempts);
        }
    }

    private void findLocationAndSpawn(World world, Airdrop airdrop, int attempts) {
        int x = ThreadLocalRandom.current().nextInt(-airdrop.getRadius(), airdrop.getRadius() + 1);
        int z = ThreadLocalRandom.current().nextInt(-airdrop.getRadius(), airdrop.getRadius() + 1);

        if (VersionUtil.isVersionAtLeast(1, 21)) {
            world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> processLocation(world, airdrop, x, z, attempts));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Bukkit.getScheduler().runTask(plugin, () -> processLocation(world, airdrop, x, z, attempts));
            });
        }
    }

    private void processLocation(World world, Airdrop airdrop, int x, int z, int attempts) {
        int minHeight = plugin.getConfigManager().getMinSpawnHeight();
        int maxHeight = plugin.getConfigManager().getMaxSpawnHeight();

        int y = world.getHighestBlockYAt(x, z);
        y = Math.max(minHeight, Math.min(maxHeight, y));

        Location loc = new Location(world, x + 0.5, y, z + 0.5);

        if (!isValidSpawnLocation(loc)) {
            if (attempts > 0) {
                findLocationAndSpawn(world, airdrop, attempts - 1);
            } else {
                spawnAirdrop(world, airdrop, loc);
            }
            return;
        }

        spawnAirdrop(world, airdrop, loc);
    }

    private boolean isValidSpawnLocation(Location location) {
        Block groundBlock = location.getBlock().getRelative(BlockFace.DOWN);
        Block spawnBlock = location.getBlock();
        Block aboveBlock = location.getBlock().getRelative(BlockFace.UP);

        if (plugin.getConfigManager().isForbiddenBlock(groundBlock.getType())) {
            return false;
        }

        if (plugin.getConfigManager().isForbiddenBlock(spawnBlock.getType())) {
            return false;
        }

        if (plugin.getConfigManager().isForbiddenBlock(aboveBlock.getType())) {
            return false;
        }

        return true;
    }

    private void spawnAirdrop(World world, Airdrop airdrop, Location spawnLocation) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            airdrop.setCurrentLocation(spawnLocation);
            airdrop.setActive(true);
            airdrop.setOpened(false);
            airdrop.setLooted(false);
            airdrop.getInventoryManager().createInventory();

            if (airdrop.getSchematic() != null && !airdrop.getSchematic().isEmpty()) {
                if (plugin.getWorldEditHook() != null) {
                    airdrop.setSchematicLocation(spawnLocation.clone());
                    plugin.getWorldEditHook().placeSchematic(airdrop);
                }
            }

            spawnAirdropBlock(airdrop, spawnLocation);

            if (plugin.getWorldGuardHook() != null) {
                plugin.getWorldGuardHook().createRegion(airdrop);
            }

            AirdropSpawnTask task = new AirdropSpawnTask(plugin, airdrop);
            task.runTaskTimer(plugin, 0L, 1L);
            activeTasks.put(airdrop.getName(), task);
        });
    }

    public void stopAirdrop(String name) {
        Airdrop airdrop = getAirdrop(name);

        AirdropSpawnTask task = activeTasks.remove(name);
        if (task != null) {
            task.cancel();
        }

        if (airdrop.getCurrentLocation() != null) {
            airdrop.getCurrentLocation().getBlock().setType(Material.AIR);
        }

        if (plugin.getWorldEditHook() != null) {
            plugin.getWorldEditHook().removeSchematic(airdrop);
        }

        if (plugin.getWorldGuardHook() != null) {
            plugin.getWorldGuardHook().removeRegion(airdrop);
        }

        if (plugin.getHologramHook() != null) {
            plugin.getHologramHook().removeHologram(airdrop.getName());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(airdrop.getAirdropInventory())) {
                player.closeInventory();
            }
        }

        plugin.getListenerManager().removeBossBar(airdrop.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendActionBar("");
        }

        airdrop.setActive(false);
        airdrop.setCurrentLocation(null);
        airdrop.setOpened(false);
        airdrop.setLooted(false);
        airdrop.setAirdropInventory(null);
        airdrop.setSchematicLocation(null);

        if (airdrop.isAutoRespawn()) {
            airdrop.setLastDespawnTime(System.currentTimeMillis());
        } else {
            airdrop.setLastDespawnTime(0);
        }

        saveAirdrop(airdrop);
    }

    public void checkAutoRespawns() {
        scheduledRespawns.clear();

        Map<Long, List<Airdrop>> timeGroups = new LinkedHashMap<>();

        for (Airdrop airdrop : getAllAirdrops()) {
            if (!airdrop.isActive() && airdrop.isAutoRespawn()) {
                int online = Bukkit.getOnlinePlayers().size();
                if (airdrop.getMinOnlinePlayers() > 0 && online < airdrop.getMinOnlinePlayers()) {
                    continue;
                }

                long waitTime = airdrop.getAutoRespawnTime() * 1000L;
                long remaining = waitTime;

                if (airdrop.getLastDespawnTime() > 0) {
                    long elapsed = System.currentTimeMillis() - airdrop.getLastDespawnTime();
                    remaining = Math.max(0, waitTime - elapsed);
                }

                if (remaining <= 0) {
                    remaining = 1000;
                }

                timeGroups.computeIfAbsent(remaining, k -> new ArrayList<>()).add(airdrop);
            }
        }

        for (Map.Entry<Long, List<Airdrop>> entry : timeGroups.entrySet()) {
            long remaining = entry.getKey();
            List<Airdrop> group = entry.getValue();

            for (Airdrop airdrop : group) {
                if (scheduledRespawns.contains(airdrop.getName())) continue;
                scheduledRespawns.add(airdrop.getName());
                scheduleAutoRespawn(airdrop, remaining);
            }
        }
    }

    private void scheduleAutoRespawn(Airdrop airdrop, long remainingMs) {
        long ticks = Math.max(1, remainingMs / 50);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            scheduledRespawns.remove(airdrop.getName());
            if (!airdrop.isActive()) {
                try {
                    startAirdrop(airdrop.getName());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to auto-respawn airdrop: " + airdrop.getName());
                }
            }
        }, ticks);
    }

    public void stopAllAirdrops() {
        new ArrayList<>(activeTasks.keySet()).forEach(this::stopAirdrop);
    }

    private void spawnAirdropBlock(Airdrop airdrop, Location location) {
        Location blockLocation = location.clone().add(0, airdrop.getSpawnHeightOffset(), 0);
        blockLocation.getBlock().setType(airdrop.getMaterial());
        airdrop.setCurrentLocation(blockLocation);
    }

    public void saveAirdrop(Airdrop airdrop) {
        File file = new File(airdropsFolder, airdrop.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("airdrop", airdrop);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save airdrop: " + airdrop.getName());
        }
    }

    public Airdrop getAirdrop(String name) {
        Airdrop airdrop = airdrops.get(name);
        if (airdrop == null) {
            throw new IllegalArgumentException("Airdrop not found: " + name);
        }
        return airdrop;
    }

    public Collection<Airdrop> getAllAirdrops() {
        return airdrops.values();
    }

    public Map<String, AirdropSpawnTask> getActiveTasks() {
        return activeTasks;
    }
}