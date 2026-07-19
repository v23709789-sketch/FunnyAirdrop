package me.funnyairdrop.config;

import me.funnyairdrop.FunnyAirdrop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final FunnyAirdrop plugin;
    private FileConfiguration config;
    private List<String> forbiddenBlocks;
    public Set<Material> forbiddenMaterialsCache;

    public ConfigManager(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.forbiddenBlocks = new ArrayList<>();
        this.forbiddenMaterialsCache = new HashSet<>();
        loadDefaults();
    }

    private void loadDefaults() {
        config.addDefault("debug", false);
        config.addDefault("airdrops.auto_save_interval", 300);
        config.addDefault("airdrops.default_duration", 600);
        config.addDefault("settings.world-guard-flags.allow-flags", List.of("use", "pvp", "chest-access"));
        config.addDefault("settings.world-guard-flags.deny-flags", List.of("creeper-explosion", "tnt", "fire-spread", "lava-fire", "other-explosion"));

        List<String> defaultForbidden = new ArrayList<>();
        defaultForbidden.add("LAVA");
        defaultForbidden.add("WATER");

        config.addDefault("settings.spawn-restrictions.forbidden-blocks", defaultForbidden);
        config.addDefault("settings.spawn-restrictions.max-spawn-attempts", 50);
        config.addDefault("settings.spawn-restrictions.min-spawn-height", 5);
        config.addDefault("settings.spawn-restrictions.max-spawn-height", 255);

        config.options().copyDefaults(true);
        plugin.saveConfig();

        loadForbiddenBlocks();
    }

    private void loadForbiddenBlocks() {
        forbiddenBlocks = config.getStringList("settings.spawn-restrictions.forbidden-blocks");
        forbiddenMaterialsCache.clear();

        for (String materialName : forbiddenBlocks) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                forbiddenMaterialsCache.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in forbidden-blocks: " + materialName);
            }
        }
    }

    public List<String> getAllowFlags() {
        return config.getStringList("settings.world-guard-flags.allow-flags");
    }

    public List<String> getDenyFlags() {
        return config.getStringList("settings.world-guard-flags.deny-flags");
    }

    public boolean isForbiddenBlock(Material material) {
        return forbiddenMaterialsCache.contains(material);
    }

    public int getMaxSpawnAttempts() {
        return config.getInt("settings.spawn-restrictions.max-spawn-attempts", 50);
    }

    public int getMinSpawnHeight() {
        return config.getInt("settings.spawn-restrictions.min-spawn-height", 5);
    }

    public int getMaxSpawnHeight() {
        return config.getInt("settings.spawn-restrictions.max-spawn-height", 255);
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadDefaults();
        loadForbiddenBlocks();
    }
}