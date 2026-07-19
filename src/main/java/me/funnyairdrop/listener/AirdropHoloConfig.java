package me.funnyairdrop.listener;

import java.util.*;

public class AirdropHoloConfig {
    private List<String> closedLines;
    private List<String> openedLines;

    public AirdropHoloConfig() {
        this.closedLines = new ArrayList<>();
        this.closedLines.add("{displayname}");
        this.closedLines.add("&#aaaaaaЗакрыт");
        this.closedLines.add("&#ffaa55До открытия: {time-to-open}");

        this.openedLines = new ArrayList<>();
        this.openedLines.add("{displayname}");
        this.openedLines.add("&#55ff55Открыт!");
        this.openedLines.add("&#ffaa55Исчезнет через: {time}");
    }

    @SuppressWarnings("unchecked")
    public AirdropHoloConfig(Map<String, Object> map) {
        this.closedLines = new ArrayList<>();
        this.openedLines = new ArrayList<>();

        if (map == null) return;

        Object closedObj = map.get("closed");
        if (closedObj instanceof List) {
            this.closedLines = (List<String>) closedObj;
        }

        Object openedObj = map.get("opened");
        if (openedObj instanceof List) {
            this.openedLines = (List<String>) openedObj;
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("closed", closedLines);
        map.put("opened", openedLines);
        return map;
    }

    public List<String> getClosedLines() {
        return closedLines;
    }

    public List<String> getOpenedLines() {
        return openedLines;
    }

}