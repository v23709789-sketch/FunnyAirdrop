package me.funnyairdrop.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldGuardHook {
    private final FunnyAirdrop plugin;
    private final Map<String, StateFlag> flagsMap;
    private final Map<String, String> activeRegions;

    public WorldGuardHook(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.flagsMap = new HashMap<>();
        this.activeRegions = new ConcurrentHashMap<>();
        initFlags();
    }

    private void initFlags() {
        flagsMap.put("use", Flags.USE);
        flagsMap.put("pvp", Flags.PVP);
        flagsMap.put("chest-access", Flags.CHEST_ACCESS);
        flagsMap.put("creeper-explosion", Flags.CREEPER_EXPLOSION);
        flagsMap.put("tnt", Flags.TNT);
        flagsMap.put("fire-spread", Flags.FIRE_SPREAD);
        flagsMap.put("lava-fire", Flags.LAVA_FIRE);
        flagsMap.put("other-explosion", Flags.OTHER_EXPLOSION);
        flagsMap.put("entity-damage", Flags.DAMAGE_ANIMALS);
        flagsMap.put("block-break", Flags.BLOCK_BREAK);
    }

    public void createRegion(Airdrop airdrop) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            return;
        }

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        World world = loc.getWorld();
        if (world == null) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return;

        int radius = airdrop.getWorldGuardRadius();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        BlockVector3 min = BlockVector3.at(x - radius, Math.max(0, y - radius), z - radius);
        BlockVector3 max = BlockVector3.at(x + radius, Math.min(255, y + radius), z + radius);

        String regionName = "airdrop_" + airdrop.getName() + "_" + UUID.randomUUID().toString().substring(0, 8);
        ProtectedRegion region = new ProtectedCuboidRegion(regionName, min, max);

        for (String flagName : plugin.getConfigManager().getAllowFlags()) {
            StateFlag flag = flagsMap.get(flagName);
            if (flag != null) {
                region.setFlag(flag, StateFlag.State.ALLOW);
            }
        }

        for (String flagName : plugin.getConfigManager().getDenyFlags()) {
            StateFlag flag = flagsMap.get(flagName);
            if (flag != null) {
                region.setFlag(flag, StateFlag.State.DENY);
            }
        }

        region.setPriority(100);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            regions.addRegion(region);
            activeRegions.put(airdrop.getName(), regionName);
        });
    }

    public void removeRegion(Airdrop airdrop) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            return;
        }

        String regionName = activeRegions.remove(airdrop.getName());
        if (regionName == null) return;

        Location loc = airdrop.getCurrentLocation();
        if (loc == null) return;

        World world = loc.getWorld();
        if (world == null) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            regions.removeRegion(regionName);
        });
    }

}