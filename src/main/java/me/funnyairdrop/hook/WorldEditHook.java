package me.funnyairdrop.hook;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldEditHook {
    private final FunnyAirdrop plugin;
    private final Map<String, EditSession> editSessions;

    public WorldEditHook(FunnyAirdrop plugin) {
        this.plugin = plugin;
        this.editSessions = new ConcurrentHashMap<>();
    }

    public boolean placeSchematic(Airdrop airdrop) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            return false;
        }

        String schematicName = airdrop.getSchematic();
        if (schematicName == null || schematicName.isEmpty()) {
            return false;
        }

        Location location = airdrop.getSchematicLocation();
        if (location == null) {
            plugin.getLogger().warning("No schematic location for " + airdrop.getName());
            return false;
        }

        if (editSessions.containsKey(airdrop.getName())) {
            plugin.getLogger().warning("Schematic already placed for: " + airdrop.getName());
            return false;
        }

        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        File schematicFile = new File(schematicsFolder, schematicName);
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("Schematic not found: " + schematicName);
            return false;
        }

        try {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().warning("Unknown schematic format: " + schematicName);
                return false;
            }

            ClipboardReader reader = format.getReader(new FileInputStream(schematicFile));
            Clipboard clipboard = reader.read();

            if (location.getWorld() == null) {
                plugin.getLogger().warning("World is null for schematic location");
                return false;
            }

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
            EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld);
            editSession.setFastMode(true);

            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(
                            location.getBlockX() + airdrop.getSchematicOffsetX(),
                            location.getBlockY() + airdrop.getSchematicOffsetY(),
                            location.getBlockZ() + airdrop.getSchematicOffsetZ()
                    ))
                    .ignoreAirBlocks(airdrop.isIgnoreAirBlocks())
                    .build();

            Operations.complete(operation);
            editSession.close();

            editSessions.put(airdrop.getName(), editSession);

            plugin.getLogger().info("Schematic " + schematicName + " placed successfully");
            return true;
        } catch (IOException | WorldEditException e) {
            plugin.getLogger().severe("Failed to place schematic: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void removeSchematic(Airdrop airdrop) {
        EditSession session = editSessions.remove(airdrop.getName());
        if (session != null) {
            try {
                EditSession newEditSession = WorldEdit.getInstance().newEditSession(session.getWorld());
                session.undo(newEditSession);
                newEditSession.close();
                session.close();
                plugin.getLogger().info("Schematic removed and blocks restored for " + airdrop.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to undo schematic: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().warning("No EditSession found for " + airdrop.getName());
        }
    }

    public void createSchematicsFolder() {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }
}