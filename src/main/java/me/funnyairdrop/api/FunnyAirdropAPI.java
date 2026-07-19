package me.funnyairdrop.api;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class FunnyAirdropAPI {

    private final FunnyAirdrop plugin;

    public FunnyAirdropAPI(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    public boolean isPluginLoaded() {
        return plugin != null && plugin.isEnabled();
    }

    public Collection<Airdrop> getAllAirdrops() {
        return new ArrayList<>(plugin.getAirdropManager().getAllAirdrops());
    }

    public Airdrop getAirdrop(String name) {
        return plugin.getAirdropManager().getAirdrop(name);
    }

    public void startAirdrop(String name) {
        plugin.getAirdropManager().startAirdrop(name);
    }

    public void stopAirdrop(String name) {
        plugin.getAirdropManager().stopAirdrop(name);
    }

    public Location getAirdropLocation(String name) {
        Airdrop airdrop = getAirdrop(name);
        if (airdrop != null) {
            return airdrop.getCurrentLocation();
        }
        return null;
    }

    public boolean isAirdropActive(String name) {
        Airdrop airdrop = getAirdrop(name);
        if (airdrop != null) {
            return airdrop.isActive();
        }
        return false;
    }

    public List<String> getActiveAirdropNames() {
        List<String> names = new ArrayList<>();
        for (Airdrop airdrop : getAllAirdrops()) {
            if (airdrop.isActive()) {
                names.add(airdrop.getName());
            }
        }
        return names;
    }

    public List<String> getAllAirdropNames() {
        List<String> names = new ArrayList<>();
        for (Airdrop airdrop : getAllAirdrops()) {
            names.add(airdrop.getName());
        }
        return names;
    }
}