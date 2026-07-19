package me.funnyairdrop.listener;

import java.util.*;

public class AirdropListenerConfig {
    private final Map<String, Boolean> enabledListeners;
    private final Map<String, List<String>> customCommands;

    public AirdropListenerConfig() {
        this.enabledListeners = new LinkedHashMap<>();
        this.customCommands = new LinkedHashMap<>();

        enabledListeners.put("start", true);
        enabledListeners.put("end", true);
        enabledListeners.put("click_closed", true);
        enabledListeners.put("click_opened", true);
        enabledListeners.put("click_looted", true);
        enabledListeners.put("timer", true);
        enabledListeners.put("pickup_delay", false);
        enabledListeners.put("progressive_loot", false);
        enabledListeners.put("loot_disguise", false);
        enabledListeners.put("drop_loot", false);
        enabledListeners.put("break_to_activate", false);
        enabledListeners.put("boss_bar_break", false);
        enabledListeners.put("boss_bar_closed", false);
        enabledListeners.put("boss_bar_opened", false);
        enabledListeners.put("boss_bar_click_activate", false);
        enabledListeners.put("actionbar", false);
        enabledListeners.put("particle", false);
    }

    @SuppressWarnings("unchecked")
    public AirdropListenerConfig(Map<String, Object> map) {
        this.enabledListeners = new LinkedHashMap<>();
        this.customCommands = new LinkedHashMap<>();

        enabledListeners.put("start", true);
        enabledListeners.put("end", true);
        enabledListeners.put("click_closed", true);
        enabledListeners.put("click_opened", true);
        enabledListeners.put("click_looted", true);
        enabledListeners.put("timer", true);
        enabledListeners.put("pickup_delay", false);
        enabledListeners.put("progressive_loot", false);
        enabledListeners.put("loot_disguise", false);
        enabledListeners.put("drop_loot", false);
        enabledListeners.put("break_to_activate", false);
        enabledListeners.put("boss_bar_break", false);
        enabledListeners.put("boss_bar_closed", false);
        enabledListeners.put("boss_bar_opened", false);
        enabledListeners.put("boss_bar_click_activate", false);
        enabledListeners.put("actionbar", false);
        enabledListeners.put("particle", false);

        if (map == null) return;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Boolean) {
                enabledListeners.put(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Map) {
                Map<String, Object> listenerData = (Map<String, Object>) entry.getValue();
                if (listenerData.containsKey("enabled")) {
                    enabledListeners.put(entry.getKey(), (Boolean) listenerData.get("enabled"));
                }
                if (listenerData.containsKey("commands")) {
                    customCommands.put(entry.getKey(), (List<String>) listenerData.get("commands"));
                }
            }
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> entry : enabledListeners.entrySet()) {
            Map<String, Object> listenerMap = new LinkedHashMap<>();
            listenerMap.put("enabled", entry.getValue());
            if (customCommands.containsKey(entry.getKey())) {
                listenerMap.put("commands", customCommands.get(entry.getKey()));
            }
            map.put(entry.getKey(), listenerMap);
        }
        return map;
    }

    public boolean isEnabled(String listenerName) {
        return enabledListeners.getOrDefault(listenerName, false);
    }

    public void setEnabled(String listenerName, boolean enabled) {
        enabledListeners.put(listenerName, enabled);
    }

    public List<String> getCustomCommands(String listenerName) {
        return customCommands.get(listenerName);
    }

    public void setCustomCommands(String listenerName, List<String> commands) {
        customCommands.put(listenerName, commands);
    }

    public Map<String, Boolean> getEnabledListeners() {
        return enabledListeners;
    }
}