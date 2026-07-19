package me.funnyairdrop.gui;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.gui.listener.*;

public class GuiManager {
    private final FunnyAirdrop plugin;
    private final ChatInputListener chatInputListener;
    private AirdropInventoryListener inventoryListener;

    public GuiManager(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.chatInputListener = new ChatInputListener(plugin);
        registerListeners();
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new AirdropsMenuListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SettingsMenuListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MaterialSelectListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WorldSelectListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LootSettingsListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ChanceSettingsListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LootPagesListener(plugin), plugin);
        inventoryListener = new AirdropInventoryListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(inventoryListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(new AirdropListenersListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(chatInputListener, plugin);
    }

    public void cleanup() {
        chatInputListener.cleanup();
        if (inventoryListener != null) {
            inventoryListener.cleanup();
        }
    }

    public AirdropInventoryListener getInventoryListener() {
        return inventoryListener;
    }
}