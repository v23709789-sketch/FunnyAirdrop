package me.funnyairdrop.util;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("(&#|#)([A-Fa-f0-9]{6})");
    private static boolean hexSupported = true;

    static {
        try {
            ChatColor.of("#FFFFFF");
        } catch (Exception e) {
            hexSupported = false;
        }
    }

    public static String colorize(String message) {
        if (message == null) return "";

        if (hexSupported) {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                String hexCode = matcher.group(2);
                String replacement = ChatColor.of("#" + hexCode).toString();
                matcher.appendReplacement(buffer, replacement);
            }
            matcher.appendTail(buffer);
            message = buffer.toString();
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}