package me.funnyairdrop.gui.listener;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropSpawnTask;
import me.funnyairdrop.listener.AirdropEvent;
import me.funnyairdrop.listener.EventListener;
import me.funnyairdrop.util.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AirdropInventoryListener implements Listener {
    private final FunnyAirdrop plugin;
    private final Map<UUID, Long> lastPickupTime = new HashMap<>();
    private final NamespacedKey realItemKey;
    private final NamespacedKey realAmountKey;
    private final NamespacedKey disguiseIdKey;
    private final Set<String> droppingAirdrops = new HashSet<>();
    private final Map<String, BukkitRunnable> activeDropTasks = new HashMap<>();

    public AirdropInventoryListener(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.realItemKey = new NamespacedKey(plugin, "real_item");
        this.realAmountKey = new NamespacedKey(plugin, "real_amount");
        this.disguiseIdKey = new NamespacedKey(plugin, "disguise_id");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        for (Airdrop airdrop : plugin.getAirdropManager().getAllAirdrops()) {
            if (!airdrop.isActive()) continue;
            if (airdrop.getCurrentLocation() == null) continue;

            Location airdropLoc = airdrop.getCurrentLocation();
            if (airdropLoc.getBlockX() == blockLoc.getBlockX() &&
                    airdropLoc.getBlockY() == blockLoc.getBlockY() &&
                    airdropLoc.getBlockZ() == blockLoc.getBlockZ()) {

                event.setCancelled(true);

                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.sendMessage(ColorUtils.colorize("&#ffaa55Нельзя сломать аирдроп в креативе!"));
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location blockLoc = event.getClickedBlock().getLocation();

        for (Airdrop airdrop : plugin.getAirdropManager().getAllAirdrops()) {
            if (!airdrop.isActive()) continue;
            if (airdrop.getCurrentLocation() == null) continue;

            Location airdropLoc = airdrop.getCurrentLocation();
            if (airdropLoc.getBlockX() == blockLoc.getBlockX() &&
                    airdropLoc.getBlockY() == blockLoc.getBlockY() &&
                    airdropLoc.getBlockZ() == blockLoc.getBlockZ()) {

                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

                event.setCancelled(true);

                AirdropSpawnTask task = plugin.getAirdropManager().getActiveTasks().get(airdrop.getName());

                if (task != null && task.isWaitingForActivation()) {
                    task.activate();
                    return;
                }

                if (!airdrop.isOpened()) {
                    plugin.getListenerManager().executeEvent(AirdropEvent.CLICK_CLOSED, airdrop, player);
                    return;
                }

                if (plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.DROP_LOOT)) {
                    return;
                }

                if (plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.PROGRESSIVE_LOOT)) {
                    airdrop.getInventoryManager().startProgressiveLoot();
                } else {
                    airdrop.getInventoryManager().fillAllLoot();
                }

                plugin.getListenerManager().executeEvent(AirdropEvent.CLICK_OPENED, airdrop, player);
                player.openInventory(airdrop.getAirdropInventory());
                return;
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        for (Airdrop airdrop : plugin.getAirdropManager().getAllAirdrops()) {
            if (airdrop.isActive() && airdrop.getAirdropInventory() != null &&
                    event.getInventory().equals(airdrop.getAirdropInventory())) {

                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;

                int delayTicks = plugin.getListenerManager().getPickupDelayTicks(airdrop);
                if (delayTicks > 0) {
                    UUID playerId = player.getUniqueId();
                    long currentTime = System.currentTimeMillis();
                    long lastTime = lastPickupTime.getOrDefault(playerId, 0L);

                    if (currentTime - lastTime < delayTicks * 50L) {
                        event.setCancelled(true);
                        return;
                    }
                    lastPickupTime.put(playerId, currentTime);
                }

                int slot = event.getSlot();
                ItemStack realItem = airdrop.getInventoryManager().getRealItem(slot);

                if (realItem != null) {
                    event.setCancelled(true);

                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ColorUtils.colorize("&#ff5555Освободите инвентарь!"));
                        return;
                    }

                    event.getInventory().setItem(slot, null);
                    airdrop.getInventoryManager().removeRealItem(slot);

                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(realItem.clone());
                    if (!leftover.isEmpty()) {
                        for (ItemStack item : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    player.updateInventory();
                } else {
                    event.setCancelled(false);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        for (Airdrop airdrop : plugin.getAirdropManager().getAllAirdrops()) {
            if (airdrop.isActive() && airdrop.getAirdropInventory() != null &&
                    event.getInventory().equals(airdrop.getAirdropInventory())) {

                boolean hasItems = false;
                for (int i = 0; i < airdrop.getInventorySize(); i++) {
                    if (airdrop.getInventoryManager().getRealItem(i) != null) {
                        hasItems = true;
                        break;
                    }
                }

                if (!hasItems) {
                    airdrop.setLooted(true);
                    plugin.getListenerManager().executeEvent(AirdropEvent.CLICK_LOOTED, airdrop, player);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onPickupItem(PlayerAttemptPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String disguiseId = meta.getPersistentDataContainer().get(disguiseIdKey, PersistentDataType.STRING);
        if (disguiseId == null) return;

        String realItemData = meta.getPersistentDataContainer().get(realItemKey, PersistentDataType.STRING);
        if (realItemData != null) {
            ItemStack realItem = deserializeItemStack(realItemData);
            if (realItem != null) {
                Integer amount = meta.getPersistentDataContainer().get(realAmountKey, PersistentDataType.INTEGER);
                if (amount != null && amount > 1) {
                    realItem.setAmount(amount);
                }
                event.getItem().setItemStack(realItem);
            }
        }
    }

    public void startDroppingLootIfNeeded(Airdrop airdrop) {
        if (plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.DROP_LOOT)) {
            startDroppingLoot(airdrop);
        }
    }

    private void startDroppingLoot(Airdrop airdrop) {
        if (droppingAirdrops.contains(airdrop.getName())) return;
        droppingAirdrops.add(airdrop.getName());

        List<Material> disguiseItems = plugin.getListenerManager().getDisguiseItems(airdrop);
        boolean useDisguise = !disguiseItems.isEmpty();
        Random random = new Random();

        EventListener listener = plugin.getListenerManager().getListenerByEvent(AirdropEvent.DROP_LOOT);
        final int dropInterval;
        final int itemsPerDrop;
        final double dropHeight;
        final double velocityMultiplier;

        if (listener != null) {
            Object intervalObj = listener.getSettings().get("drop_interval_ticks");
            dropInterval = intervalObj instanceof Number ? ((Number) intervalObj).intValue() : 20;

            Object itemsObj = listener.getSettings().get("items_per_drop");
            itemsPerDrop = itemsObj instanceof Number ? ((Number) itemsObj).intValue() : 1;

            Object heightObj = listener.getSettings().get("drop_height");
            dropHeight = heightObj instanceof Number ? ((Number) heightObj).doubleValue() : 2.5;

            Object velocityObj = listener.getSettings().get("velocity_multiplier");
            velocityMultiplier = velocityObj instanceof Number ? ((Number) velocityObj).doubleValue() : 0.5;
        } else {
            dropInterval = 20;
            itemsPerDrop = 1;
            dropHeight = 2.5;
            velocityMultiplier = 0.5;
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!airdrop.isActive()) {
                    droppingAirdrops.remove(airdrop.getName());
                    activeDropTasks.remove(airdrop.getName());
                    this.cancel();
                    return;
                }

                Location loc = airdrop.getCurrentLocation();
                if (loc == null) {
                    droppingAirdrops.remove(airdrop.getName());
                    activeDropTasks.remove(airdrop.getName());
                    this.cancel();
                    return;
                }

                boolean hasItems = false;
                int dropped = 0;

                for (int slot = 0; slot < airdrop.getInventorySize() && dropped < itemsPerDrop; slot++) {
                    ItemStack realItem = airdrop.getInventoryManager().getRealItem(slot);
                    if (realItem != null) {
                        hasItems = true;
                        int totalAmount = realItem.getAmount();

                        if (useDisguise) {
                            Material disguiseMaterial = disguiseItems.get(random.nextInt(disguiseItems.size()));
                            ItemStack dropItem = new ItemStack(disguiseMaterial, 1);
                            ItemMeta meta = dropItem.getItemMeta();
                            if (meta != null) {
                                setDisplayName(meta, "&#aaaaaa???");
                                setLore(meta, Arrays.asList(
                                        "&#ffaa55Зашифрованный предмет",
                                        "&#aaaaaaПоднимите чтобы расшифровать"
                                ));

                                ItemStack singleItem = realItem.clone();
                                singleItem.setAmount(1);
                                meta.getPersistentDataContainer().set(realItemKey, PersistentDataType.STRING, serializeItemStack(singleItem));
                                meta.getPersistentDataContainer().set(realAmountKey, PersistentDataType.INTEGER, totalAmount);
                                meta.getPersistentDataContainer().set(disguiseIdKey, PersistentDataType.STRING, UUID.randomUUID().toString());
                                dropItem.setItemMeta(meta);
                            }

                            Location dropLoc = loc.clone().add(0, dropHeight, 0);
                            org.bukkit.entity.Item droppedItem = loc.getWorld().dropItem(dropLoc, dropItem);
                            droppedItem.setCustomName(ColorUtils.colorize("&#ffaa55Зашифрованный предмет"));
                            droppedItem.setCustomNameVisible(true);

                            double angle = random.nextDouble() * 2 * Math.PI;
                            double speed = random.nextDouble() * velocityMultiplier;
                            Vector velocity = new Vector(Math.cos(angle) * speed, 0.2, Math.sin(angle) * speed);
                            droppedItem.setVelocity(velocity);
                        } else {
                            Location dropLoc = loc.clone().add(0, dropHeight, 0);
                            org.bukkit.entity.Item droppedItem = loc.getWorld().dropItem(dropLoc, realItem.clone());

                            double angle = random.nextDouble() * 2 * Math.PI;
                            double speed = random.nextDouble() * velocityMultiplier;
                            Vector velocity = new Vector(Math.cos(angle) * speed, 0.2, Math.sin(angle) * speed);
                            droppedItem.setVelocity(velocity);
                        }

                        airdrop.getInventoryManager().removeRealItem(slot);
                        dropped++;
                    }
                }

                if (!hasItems) {
                    droppingAirdrops.remove(airdrop.getName());
                    activeDropTasks.remove(airdrop.getName());
                    this.cancel();
                }
            }
        };

        activeDropTasks.put(airdrop.getName(), task);
        task.runTaskTimer(plugin, 0L, dropInterval);
    }

    public void cleanup() {
        for (BukkitRunnable task : activeDropTasks.values()) {
            task.cancel();
        }
        activeDropTasks.clear();
        droppingAirdrops.clear();
    }

    private void setDisplayName(ItemMeta meta, String name) {
        String colored = ColorUtils.colorize(name);
        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(colored));
    }

    private void setLore(ItemMeta meta, List<String> lore) {
        List<Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(LegacyComponentSerializer.legacySection().deserialize(ColorUtils.colorize(line)));
        }
        meta.lore(components);
    }

    private String serializeItemStack(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", item.serialize());
        return Base64.getEncoder().encodeToString(config.saveToString().getBytes());
    }

    private ItemStack deserializeItemStack(String data) {
        try {
            String yamlString = new String(Base64.getDecoder().decode(data));
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlString);
            Object raw = config.get("item");
            if (raw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemMap = (Map<String, Object>) raw;
                return ItemStack.deserialize(itemMap);
            }
            return null;
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            return null;
        }
    }
}