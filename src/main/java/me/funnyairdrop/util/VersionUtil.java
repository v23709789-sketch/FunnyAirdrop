package me.funnyairdrop.util;

import org.bukkit.Bukkit;

public class VersionUtil {
    public static int majorVersion;
    public static int minorVersion;

    static {
        String version = Bukkit.getBukkitVersion();
        String[] versionParts = version.split("-")[0].split("\\.");
        majorVersion = Integer.parseInt(versionParts[0]);
        minorVersion = versionParts.length > 1 ? Integer.parseInt(versionParts[1]) : 0;
    }

    public static boolean isVersionAtLeast(int major, int minor) {
        return majorVersion > major || (majorVersion == major && minorVersion >= minor);
    }

}