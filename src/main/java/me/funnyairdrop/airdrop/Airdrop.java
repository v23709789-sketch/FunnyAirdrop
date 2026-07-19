package me.funnyairdrop.airdrop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import me.funnyairdrop.listener.AirdropHoloConfig;
import me.funnyairdrop.listener.AirdropListenerConfig;

import java.util.*;

@SerializableAs("Airdrop")
public class Airdrop implements ConfigurationSerializable {
    private final String name;
    private Material material;
    private String displayName;
    private String worldName;
    private int radius;
    public List<Map<Integer, AirdropLootItem>> lootPages;
    private int currentLootPage;
    private boolean active;
    private Location currentLocation;
    private int inventorySize;
    private int openTime;
    private int closeTime;
    private boolean isOpened;
    private boolean isLooted;
    private Inventory airdropInventory;
    private double hologramHeight;
    private int worldGuardRadius;
    private String schematic;
    public int schematicOffsetX;
    public int schematicOffsetY;
    public int schematicOffsetZ;
    public boolean ignoreAirBlocks;
    private int autoRespawnTime;
    private boolean autoRespawn;
    private boolean clickToActivate;
    public List<String> clickToActivateHoloLines;
    public AirdropListenerConfig listenerConfig;
    public AirdropHoloConfig holoConfig;
    private Location schematicLocation;
    private boolean staticMode;
    private Location staticLocation;
    private long lastDespawnTime;
    private final AirdropInventoryManager inventoryManager;
    private int minOnlinePlayers;
    private long spawnTime;
    private int spawnHeightOffset;

    public Airdrop(String name) {
        this.name = name;
        this.material = Material.CHEST;
        this.displayName = "&#ffaa55Аирдроп";
        this.worldName = "world";
        this.radius = 2000;
        this.lootPages = new ArrayList<>();
        this.lootPages.add(new LinkedHashMap<>());
        this.currentLootPage = 0;
        this.active = false;
        this.inventorySize = 27;
        this.openTime = 300;
        this.closeTime = 900;
        this.isOpened = false;
        this.isLooted = false;
        this.hologramHeight = 1.5;
        this.worldGuardRadius = 10;
        this.schematic = null;
        this.schematicOffsetX = 0;
        this.schematicOffsetY = 0;
        this.schematicOffsetZ = 0;
        this.ignoreAirBlocks = true;
        this.autoRespawnTime = 300;
        this.autoRespawn = false;
        this.clickToActivate = true;
        this.clickToActivateHoloLines = new ArrayList<>(Arrays.asList(
                "{displayname}",
                "&7Нажми для активации"
        ));
        this.listenerConfig = new AirdropListenerConfig();
        this.holoConfig = new AirdropHoloConfig();
        this.staticMode = false;
        this.staticLocation = null;
        this.lastDespawnTime = 0;
        this.inventoryManager = new AirdropInventoryManager(this);
        this.minOnlinePlayers = 0;
        this.spawnTime = 0;
        this.spawnHeightOffset = 0;
    }

    @SuppressWarnings("unchecked")
    public Airdrop(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.material = Material.valueOf((String) map.getOrDefault("material", "CHEST"));
        this.displayName = (String) map.getOrDefault("display_name", "&#ffaa55Аирдроп");
        this.worldName = (String) map.getOrDefault("world", "world");
        this.radius = ((Number) map.getOrDefault("radius", 2000)).intValue();
        this.active = (boolean) map.getOrDefault("active", false);
        this.inventorySize = ((Number) map.getOrDefault("inventory_size", 27)).intValue();
        this.openTime = ((Number) map.getOrDefault("open_time", 300)).intValue();
        this.closeTime = ((Number) map.getOrDefault("close_time", 900)).intValue();
        this.hologramHeight = ((Number) map.getOrDefault("hologram_height", 1.5)).doubleValue();
        this.worldGuardRadius = ((Number) map.getOrDefault("world_guard_radius", 10)).intValue();
        this.schematic = (String) map.getOrDefault("schematic", null);
        this.schematicOffsetX = ((Number) map.getOrDefault("schematic_offset_x", 0)).intValue();
        this.schematicOffsetY = ((Number) map.getOrDefault("schematic_offset_y", 0)).intValue();
        this.schematicOffsetZ = ((Number) map.getOrDefault("schematic_offset_z", 0)).intValue();
        this.ignoreAirBlocks = (boolean) map.getOrDefault("ignore_air_blocks", true);
        this.autoRespawnTime = ((Number) map.getOrDefault("auto_respawn_time", 300)).intValue();
        this.autoRespawn = (boolean) map.getOrDefault("auto_respawn", false);
        this.clickToActivate = (boolean) map.getOrDefault("click_to_activate", true);
        this.currentLootPage = ((Number) map.getOrDefault("current_loot_page", 0)).intValue();
        this.minOnlinePlayers = ((Number) map.getOrDefault("min_online_players", 0)).intValue();
        this.spawnTime = ((Number) map.getOrDefault("spawn_time", 0)).longValue();
        this.spawnHeightOffset = ((Number) map.getOrDefault("spawn_height_offset", 0)).intValue();

        this.clickToActivateHoloLines = new ArrayList<>();
        Object clickHoloObj = map.get("click_to_activate_holo");
        if (clickHoloObj instanceof List) {
            for (Object line : (List<?>) clickHoloObj) {
                clickToActivateHoloLines.add(line.toString());
            }
        }
        if (clickToActivateHoloLines.isEmpty()) {
            clickToActivateHoloLines.add("{displayname}");
            clickToActivateHoloLines.add("&7Нажми для активации");
        }

        this.isOpened = false;
        this.isLooted = false;

        this.lootPages = new ArrayList<>();
        Object pagesObj = map.get("loot_pages");
        if (pagesObj instanceof List) {
            for (Object pageObj : (List<?>) pagesObj) {
                Map<Integer, AirdropLootItem> page = new LinkedHashMap<>();
                if (pageObj instanceof Map) {
                    Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) pageObj;
                    for (Map.Entry<String, Map<String, Object>> entry : pageMap.entrySet()) {
                        int slot = Integer.parseInt(entry.getKey());
                        ItemStack itemStack = (ItemStack) entry.getValue().get("itemstack");
                        double chance = ((Number) entry.getValue().get("chance")).doubleValue();
                        page.put(slot, new AirdropLootItem(itemStack, chance));
                    }
                }
                lootPages.add(page);
            }
        }
        if (lootPages.isEmpty()) {
            lootPages.add(new LinkedHashMap<>());
        }

        Object listenerObj = map.get("listeners");
        if (listenerObj instanceof Map) {
            this.listenerConfig = new AirdropListenerConfig((Map<String, Object>) listenerObj);
        } else {
            this.listenerConfig = new AirdropListenerConfig();
        }

        Object holoObj = map.get("holo_config");
        if (holoObj instanceof Map) {
            this.holoConfig = new AirdropHoloConfig((Map<String, Object>) holoObj);
        } else {
            this.holoConfig = new AirdropHoloConfig();
        }

        this.staticMode = (boolean) map.getOrDefault("static_mode", false);
        Object staticLocObj = map.get("static_location");
        if (staticLocObj instanceof Location) {
            this.staticLocation = (Location) staticLocObj;
        } else {
            this.staticLocation = null;
        }

        this.lastDespawnTime = ((Number) map.getOrDefault("last_despawn_time", 0L)).longValue();
        this.inventoryManager = new AirdropInventoryManager(this);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("material", material.name());
        map.put("display_name", displayName);
        map.put("world", worldName);
        map.put("radius", radius);
        map.put("active", false);
        map.put("inventory_size", inventorySize);
        map.put("open_time", openTime);
        map.put("close_time", closeTime);
        map.put("hologram_height", hologramHeight);
        map.put("world_guard_radius", worldGuardRadius);
        map.put("schematic", schematic);
        map.put("schematic_offset_x", schematicOffsetX);
        map.put("schematic_offset_y", schematicOffsetY);
        map.put("schematic_offset_z", schematicOffsetZ);
        map.put("ignore_air_blocks", ignoreAirBlocks);
        map.put("auto_respawn_time", autoRespawnTime);
        map.put("auto_respawn", autoRespawn);
        map.put("click_to_activate", clickToActivate);
        map.put("click_to_activate_holo", clickToActivateHoloLines);
        map.put("current_loot_page", currentLootPage);
        map.put("min_online_players", minOnlinePlayers);
        map.put("spawn_time", spawnTime);
        map.put("spawn_height_offset", spawnHeightOffset);

        List<Map<String, Map<String, Object>>> pagesList = new ArrayList<>();
        for (Map<Integer, AirdropLootItem> page : lootPages) {
            Map<String, Map<String, Object>> pageMap = new LinkedHashMap<>();
            for (Map.Entry<Integer, AirdropLootItem> entry : page.entrySet()) {
                Map<String, Object> itemData = new LinkedHashMap<>();
                itemData.put("itemstack", entry.getValue().getItemStack());
                itemData.put("chance", entry.getValue().getChance());
                pageMap.put(String.valueOf(entry.getKey()), itemData);
            }
            pagesList.add(pageMap);
        }
        map.put("loot_pages", pagesList);

        map.put("listeners", listenerConfig.serialize());
        map.put("holo_config", holoConfig.serialize());
        map.put("static_mode", staticMode);
        map.put("static_location", staticLocation);
        map.put("last_despawn_time", lastDespawnTime);

        return map;
    }

    public AirdropInventoryManager getInventoryManager() { return inventoryManager; }
    public AirdropListenerConfig getListenerConfig() { return listenerConfig; }
    public AirdropHoloConfig getHoloConfig() { return holoConfig; }
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    public Map<Integer, AirdropLootItem> getLootItems() {
        if (currentLootPage >= lootPages.size()) {
            currentLootPage = 0;
        }
        return lootPages.get(currentLootPage);
    }

    public void setLootItems(Map<Integer, AirdropLootItem> lootItems) {
        if (currentLootPage >= lootPages.size()) {
            lootPages.add(lootItems);
        } else {
            lootPages.set(currentLootPage, lootItems);
        }
    }

    public List<Map<Integer, AirdropLootItem>> getLootPages() {
        return lootPages;
    }

    public void setCurrentLootPage(int page) {
        this.currentLootPage = page;
    }

    public int getLootPageCount() {
        return lootPages.size();
    }

    public void addLootPage() {
        lootPages.add(new LinkedHashMap<>());
        currentLootPage = lootPages.size() - 1;
    }

    public void removeLootPage(int index) {
        if (lootPages.size() > 1 && index >= 0 && index < lootPages.size()) {
            lootPages.remove(index);
            if (currentLootPage >= lootPages.size()) {
                currentLootPage = lootPages.size() - 1;
            }
            if (currentLootPage < 0) {
                currentLootPage = 0;
            }
        }
    }

    public Map<Integer, AirdropLootItem> getRandomLoot() {
        Map<Integer, AirdropLootItem> combinedLoot = new LinkedHashMap<>();
        Random random = new Random();

        for (Map<Integer, AirdropLootItem> page : lootPages) {
            for (Map.Entry<Integer, AirdropLootItem> entry : page.entrySet()) {
                double chance = entry.getValue().getChance();
                double roll = random.nextDouble() * 100.0;

                if (roll < chance) {
                    combinedLoot.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return combinedLoot;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location location) { this.currentLocation = location; }
    public int getInventorySize() { return inventorySize; }
    public void setInventorySize(int inventorySize) { this.inventorySize = inventorySize; }
    public int getOpenTime() { return openTime; }
    public void setOpenTime(int openTime) { this.openTime = openTime; }
    public int getCloseTime() { return closeTime; }
    public void setCloseTime(int closeTime) { this.closeTime = closeTime; }
    public boolean isOpened() { return isOpened; }
    public void setOpened(boolean opened) { isOpened = opened; }
    public boolean isLooted() { return isLooted; }
    public void setLooted(boolean looted) { isLooted = looted; }
    public Inventory getAirdropInventory() { return airdropInventory; }
    public void setAirdropInventory(Inventory inventory) { this.airdropInventory = inventory; }
    public double getHologramHeight() { return hologramHeight; }
    public void setHologramHeight(double height) { this.hologramHeight = height; }
    public int getWorldGuardRadius() { return worldGuardRadius; }
    public void setWorldGuardRadius(int worldGuardRadius) { this.worldGuardRadius = worldGuardRadius; }
    public String getSchematic() { return schematic; }
    public void setSchematic(String schematic) { this.schematic = schematic; }
    public int getSchematicOffsetX() { return schematicOffsetX; }
    public int getSchematicOffsetY() { return schematicOffsetY; }
    public int getSchematicOffsetZ() { return schematicOffsetZ; }
    public boolean isIgnoreAirBlocks() { return ignoreAirBlocks; }
    public int getAutoRespawnTime() { return autoRespawnTime; }
    public void setAutoRespawnTime(int autoRespawnTime) { this.autoRespawnTime = autoRespawnTime; }
    public boolean isAutoRespawn() { return autoRespawn; }
    public void setAutoRespawn(boolean autoRespawn) { this.autoRespawn = autoRespawn; }
    public boolean isClickToActivate() { return clickToActivate; }
    public void setClickToActivate(boolean clickToActivate) { this.clickToActivate = clickToActivate; }
    public List<String> getClickToActivateHoloLines() { return clickToActivateHoloLines; }
    public boolean isStaticMode() { return staticMode; }
    public void setStaticMode(boolean staticMode) { this.staticMode = staticMode; }
    public Location getStaticLocation() { return staticLocation; }
    public void setStaticLocation(Location staticLocation) { this.staticLocation = staticLocation; }
    public long getLastDespawnTime() { return lastDespawnTime; }
    public void setLastDespawnTime(long lastDespawnTime) { this.lastDespawnTime = lastDespawnTime; }
    public Location getSchematicLocation() { return schematicLocation; }
    public void setSchematicLocation(Location schematicLocation) { this.schematicLocation = schematicLocation; }
    public int getMinOnlinePlayers() { return minOnlinePlayers; }
    public void setMinOnlinePlayers(int minOnlinePlayers) { this.minOnlinePlayers = minOnlinePlayers; }
    public long getSpawnTime() { return spawnTime; }
    public void setSpawnTime(long spawnTime) { this.spawnTime = spawnTime; }
    public int getSpawnHeightOffset() { return spawnHeightOffset; }
    public void setSpawnHeightOffset(int spawnHeightOffset) { this.spawnHeightOffset = spawnHeightOffset; }
}