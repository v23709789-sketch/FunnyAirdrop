package me.funnyairdrop;

import me.funnyairdrop.addon.AddonLoader;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.airdrop.AirdropManager;
import me.funnyairdrop.api.FunnyAirdropAPI;
import me.funnyairdrop.command.AirdropCommand;
import me.funnyairdrop.command.AirdropTabCompleter;
import me.funnyairdrop.config.ConfigManager;
import me.funnyairdrop.gui.GuiManager;
import me.funnyairdrop.hook.AirdropPlaceholderExpansion;
import me.funnyairdrop.hook.HologramHook;
import me.funnyairdrop.hook.WorldEditHook;
import me.funnyairdrop.hook.WorldGuardHook;
import me.funnyairdrop.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public final class FunnyAirdrop extends JavaPlugin {

    private static FunnyAirdrop instance;
    private ConfigManager configManager;
    private AirdropManager airdropManager;
    private WorldGuardHook worldGuardHook;
    private HologramHook hologramHook;
    private ListenerManager listenerManager;
    private GuiManager guiManager;
    private WorldEditHook worldEditHook;
    private FunnyAirdropAPI api;
    private AddonLoader addonLoader;

    @Override
    public void onEnable() {
        instance = this;

        this.api = new FunnyAirdropAPI(this);

        ConfigurationSerialization.registerClass(Airdrop.class);

        saveDefaultConfig();
        createAirdropsFolder();

        this.configManager = new ConfigManager(this);
        this.listenerManager = new ListenerManager(this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.worldGuardHook = new WorldGuardHook(this);
            getLogger().info("WorldGuard hook enabled!");
        }

        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            this.worldEditHook = new WorldEditHook(this);
            this.worldEditHook.createSchematicsFolder();
            getLogger().info("WorldEdit hook enabled!");
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AirdropPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI hook enabled!");
        }

        this.hologramHook = new HologramHook(this);
        getLogger().info("Hologram hook enabled!");

        this.airdropManager = new AirdropManager(this);
        this.guiManager = new GuiManager(this);

        getCommand("fairdrop").setExecutor(new AirdropCommand(this));
        getCommand("fairdrop").setTabCompleter(new AirdropTabCompleter(this));

        airdropManager.loadAllAirdrops();
        airdropManager.checkAutoRespawns();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            airdropManager.checkAutoRespawns();
        }, 200L, 200L);

        this.addonLoader = new AddonLoader(getDataFolder());
        addonLoader.findAndLoad();
        addonLoader.enableAll();

        getLogger().info("FunnyAirdrop v2.1 enabled!");
    }

    @Override
    public void onDisable() {
        if (addonLoader != null) {
            try {
                addonLoader.close();
            } catch (Exception e) {
                getLogger().severe("Ошибка выгрузки аддонов: " + e.getMessage());
            }
        }
        if (airdropManager != null) {
            airdropManager.stopAllAirdrops();
        }
        if (guiManager != null) {
            guiManager.cleanup();
        }
        if (hologramHook != null) {
            hologramHook.removeAllHolograms();
            hologramHook.stopUpdateTask();
        }
        if (listenerManager != null) {
            listenerManager.removeAllBossBars();
        }
        getLogger().info("FunnyAirdrop disabled!");
    }

    public WorldEditHook getWorldEditHook() {
        return worldEditHook;
    }

    private void createAirdropsFolder() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        java.io.File airdropsFolder = new java.io.File(getDataFolder(), "airdrops");
        if (!airdropsFolder.exists()) {
            airdropsFolder.mkdirs();
        }
    }

    public static FunnyAirdrop getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public HologramHook getHologramHook() {
        return hologramHook;
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public FunnyAirdropAPI getApi() {
        return api;
    }
}