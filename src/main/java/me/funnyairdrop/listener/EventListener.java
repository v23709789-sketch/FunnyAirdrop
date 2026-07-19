package me.funnyairdrop.listener;

import java.util.List;
import java.util.Map;

public class EventListener {
    private final String name;
    private final String description;
    private final AirdropEvent event;
    private final List<String> commands;
    private final Map<String, Object> settings;

    public EventListener(String name, String description, AirdropEvent event, List<String> commands, Map<String, Object> settings) {
        this.name = name;
        this.description = description;
        this.event = event;
        this.commands = commands;
        this.settings = settings;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public AirdropEvent getEvent() { return event; }
    public List<String> getCommands() { return commands; }
    public Map<String, Object> getSettings() { return settings; }
}