package me.funnyairdrop.hook;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DecentHologram implements IHologram {
    private static boolean dhIsEnable = false;

    @Override
    public void createOrUpdateHologram(@NotNull List<String> lines, @NotNull Location location, @NotNull String name) {
        if (!dhIsEnable) {
            if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                dhIsEnable = true;
            } else {
                return;
            }
        }

        Location holoLocation = location.clone();

        double x = holoLocation.getX();
        double z = holoLocation.getZ();

        if (Math.abs(x - Math.round(x)) < 0.01) {
            holoLocation.setX(Math.floor(x) + 0.5);
        }
        if (Math.abs(z - Math.round(z)) < 0.01) {
            holoLocation.setZ(Math.floor(z) + 0.5);
        }

        Hologram hologram = DHAPI.getHologram(name);
        if (hologram != null) {
            if (!hologram.getLocation().equals(holoLocation)) {
                hologram.setLocation(holoLocation);
            }
            DHAPI.setHologramLines(hologram, lines);
        } else {
            try {
                hologram = DHAPI.createHologram(name, holoLocation);
                DHAPI.setHologramLines(hologram, lines);
            } catch (Exception e) {
                hologram = DHAPI.createHologram(name, holoLocation);
                DHAPI.setHologramLines(hologram, lines);
            }
        }
    }

    @Override
    public void remove(@NotNull String name) {
        if (!dhIsEnable) {
            if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
                dhIsEnable = true;
            } else {
                return;
            }
        }

        Hologram hologram = DHAPI.getHologram(name);
        if (hologram != null) {
            hologram.delete();
        }
    }
}