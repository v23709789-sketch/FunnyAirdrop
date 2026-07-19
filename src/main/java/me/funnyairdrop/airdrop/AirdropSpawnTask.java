package me.funnyairdrop.airdrop;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.gui.listener.AirdropInventoryListener;
import me.funnyairdrop.listener.AirdropEvent;
import me.funnyairdrop.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AirdropSpawnTask extends BukkitRunnable {
    private final FunnyAirdrop plugin;
    private final Airdrop airdrop;
    private int timeElapsed;
    private boolean startMessageSent;
    private boolean waitingForActivation;
    private long activatedAt;
    private boolean opened;
    private int openedTimeElapsed;
    private int progressiveTick;
    private int ticks;

    public AirdropSpawnTask(FunnyAirdrop plugin, Airdrop airdrop) {
        this.plugin = plugin;
        this.airdrop = airdrop;
        this.timeElapsed = 0;
        this.startMessageSent = false;
        this.waitingForActivation = airdrop.isClickToActivate();
        this.activatedAt = 0;
        this.opened = false;
        this.openedTimeElapsed = 0;
        this.progressiveTick = 0;
        this.ticks = 0;
    }

    @Override
    public void run() {
        ticks++;

        if (waitingForActivation) {
            if (!startMessageSent) {
                plugin.getListenerManager().executeEvent(AirdropEvent.START, airdrop, null);
                startMessageSent = true;
            }

            if (ticks % 20 == 0) {
                if (plugin.getHologramHook() != null) {
                    plugin.getHologramHook().updateClickToActivateHologram(airdrop);
                }
                plugin.getListenerManager().executeEvent(AirdropEvent.BOSS_BAR_CLICK_ACTIVATE, airdrop, null);
            }

            if (ticks % 4 == 0) {
                plugin.getListenerManager().executeEvent(AirdropEvent.PARTICLE, airdrop, null);
            }
            return;
        }

        if (ticks % 4 == 0) {
            plugin.getListenerManager().executeEvent(AirdropEvent.PARTICLE, airdrop, null);
        }

        if (ticks % 20 == 0) {
            if (activatedAt > 0) {
                timeElapsed = (int) ((System.currentTimeMillis() - activatedAt) / 1000);
            } else {
                timeElapsed++;
            }

            if (!startMessageSent) {
                plugin.getListenerManager().executeEvent(AirdropEvent.START, airdrop, null);
                startMessageSent = true;

                if (plugin.getHologramHook() != null) {
                    plugin.getHologramHook().createHologram(airdrop);
                }
            }

            if (!opened) {
                plugin.getListenerManager().executeEvent(AirdropEvent.BOSS_BAR_CLOSED, airdrop, null);
            } else {
                plugin.getListenerManager().executeEvent(AirdropEvent.BOSS_BAR_OPENED, airdrop, null);
            }

            plugin.getListenerManager().executeEvent(AirdropEvent.ACTIONBAR, airdrop, null);

            if (plugin.getHologramHook() != null) {
                plugin.getHologramHook().updateHologram(airdrop);
            }

            if (!opened && timeElapsed >= airdrop.getOpenTime()) {
                opened = true;
                airdrop.setOpened(true);
                progressiveTick = 0;

                if (plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.DROP_LOOT)) {
                    AirdropInventoryListener inventoryListener = plugin.getGuiManager().getInventoryListener();
                    if (inventoryListener != null) {
                        inventoryListener.startDroppingLootIfNeeded(airdrop);
                    }
                } else if (plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.PROGRESSIVE_LOOT)) {
                    airdrop.getInventoryManager().startProgressiveLoot();
                }

                if (plugin.getHologramHook() != null) {
                    plugin.getHologramHook().updateHologram(airdrop);
                }
            }

            if (opened) {
                openedTimeElapsed++;
            }

            if (opened && openedTimeElapsed >= airdrop.getCloseTime()) {
                despawnAirdrop();
                return;
            }

            if (opened) {
                int timeLeft = airdrop.getCloseTime() - openedTimeElapsed;
                if (timeLeft == 60 || timeLeft == 30 || timeLeft == 10 || timeLeft == 5 ||
                        timeLeft == 4 || timeLeft == 3 || timeLeft == 2 || timeLeft == 1) {
                    plugin.getListenerManager().executeEvent(AirdropEvent.TIMER, airdrop, null);
                }
            }
        }

        if (opened && plugin.getListenerManager().isListenerEnabled(airdrop, AirdropEvent.PROGRESSIVE_LOOT)) {
            progressiveTick++;
            int interval = plugin.getListenerManager().getProgressiveLootInterval(airdrop);
            int itemsPerInterval = plugin.getListenerManager().getProgressiveLootItemsPerInterval(airdrop);

            if (progressiveTick % interval == 0) {
                for (int i = 0; i < itemsPerInterval; i++) {
                    if (airdrop.getInventoryManager().hasMoreProgressiveItems()) {
                        airdrop.getInventoryManager().showNextProgressiveItem();
                    }
                }
            }
        }
    }

    public void activate() {
        this.waitingForActivation = false;
        this.activatedAt = System.currentTimeMillis();
        this.timeElapsed = 0;
        this.ticks = 0;

        plugin.getListenerManager().removeBossBar(airdrop.getName());

        if (plugin.getHologramHook() != null) {
            plugin.getHologramHook().removeHologram(airdrop.getName());
            plugin.getHologramHook().createHologram(airdrop);
        }
    }

    public boolean isWaitingForActivation() {
        return waitingForActivation;
    }

    private void despawnAirdrop() {
        if (airdrop.getCurrentLocation() != null) {
            airdrop.getCurrentLocation().getBlock().setType(Material.AIR);
        }

        if (plugin.getWorldEditHook() != null) {
            plugin.getWorldEditHook().removeSchematic(airdrop);
        }

        if (plugin.getHologramHook() != null) {
            plugin.getHologramHook().removeHologram(airdrop.getName());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(airdrop.getAirdropInventory())) {
                player.closeInventory();
            }
        }

        plugin.getListenerManager().executeEvent(AirdropEvent.END, airdrop, null);

        plugin.getListenerManager().removeBossBar(airdrop.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (VersionUtil.isVersionAtLeast(1, 20)) {
                p.sendActionBar("");
            }
        }

        AirdropSpawnTask task = plugin.getAirdropManager().getActiveTasks().remove(airdrop.getName());
        if (task != null) {
            task.cancel();
        }

        if (plugin.getWorldGuardHook() != null) {
            plugin.getWorldGuardHook().removeRegion(airdrop);
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

        plugin.getAirdropManager().saveAirdrop(airdrop);

        if (airdrop.isAutoRespawn()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    plugin.getAirdropManager().startAirdrop(airdrop.getName());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to auto-respawn airdrop: " + airdrop.getName());
                }
            }, airdrop.getAutoRespawnTime() * 20L);
        }

        this.cancel();
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public Airdrop getAirdrop() {
        return airdrop;
    }
}