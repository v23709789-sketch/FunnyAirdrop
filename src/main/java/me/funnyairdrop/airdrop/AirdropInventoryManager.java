package me.funnyairdrop.airdrop;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AirdropInventoryManager {
    private final Airdrop airdrop;
    private final Map<Integer, ItemStack> realItems;
    private final Map<Integer, ItemStack> disguisedItems;
    private Inventory progressiveInventory;
    private List<Integer> shuffledSlots;
    private int progressiveIndex;
    private boolean useDisguise;
    private List<Material> disguiseMaterials;
    public boolean progressiveStarted;

    public AirdropInventoryManager(Airdrop airdrop) {
        this.airdrop = airdrop;
        this.realItems = new HashMap<>();
        this.disguisedItems = new HashMap<>();
        this.shuffledSlots = new ArrayList<>();
        this.progressiveIndex = 0;
        this.useDisguise = false;
        this.disguiseMaterials = new ArrayList<>();
        this.progressiveStarted = false;
    }

    public void createInventory() {
        Inventory inventory = Bukkit.createInventory(null, airdrop.getInventorySize(), ColorUtils.colorize(airdrop.getDisplayName()));
        airdrop.setAirdropInventory(inventory);
        realItems.clear();
        disguisedItems.clear();
        progressiveStarted = false;

        FunnyAirdrop plugin = FunnyAirdrop.getInstance();
        disguiseMaterials = plugin.getListenerManager().getDisguiseItems(airdrop);
        useDisguise = plugin.getListenerManager().isListenerEnabled(airdrop, me.funnyairdrop.listener.AirdropEvent.LOOT_DISGUISE) && !disguiseMaterials.isEmpty();

        Map<Integer, AirdropLootItem> combinedLoot = airdrop.getRandomLoot();

        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < airdrop.getInventorySize(); i++) {
            availableSlots.add(i);
        }
        Collections.shuffle(availableSlots);

        Random random = new Random();

        List<Map.Entry<Integer, AirdropLootItem>> lootEntries = new ArrayList<>(combinedLoot.entrySet());
        Collections.shuffle(lootEntries, random);

        int slotIndex = 0;
        for (Map.Entry<Integer, AirdropLootItem> entry : lootEntries) {
            if (slotIndex >= availableSlots.size()) break;

            int randomSlot = availableSlots.get(slotIndex);
            realItems.put(randomSlot, entry.getValue().getItemStack().clone());

            if (useDisguise) {
                Material disguiseMat = disguiseMaterials.get(random.nextInt(disguiseMaterials.size()));
                ItemStack disguised = new ItemStack(disguiseMat);
                ItemMeta meta = disguised.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtils.colorize("&#aaaaaa???"));
                    List<String> lore = new ArrayList<>();
                    lore.add(ColorUtils.colorize("&#ffaa55Зашифрованный предмет"));
                    meta.setLore(lore);
                    disguised.setItemMeta(meta);
                }
                disguisedItems.put(randomSlot, disguised);
            }

            slotIndex++;
        }
    }

    public void startProgressiveLoot() {
        progressiveInventory = airdrop.getAirdropInventory();
        shuffledSlots = new ArrayList<>(realItems.keySet());
        Collections.shuffle(shuffledSlots);
        progressiveIndex = 0;
        progressiveStarted = true;
    }

    public void fillAllLoot() {
        Inventory inventory = airdrop.getAirdropInventory();
        if (useDisguise) {
            for (Map.Entry<Integer, ItemStack> entry : disguisedItems.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().clone());
            }
        } else {
            for (Map.Entry<Integer, ItemStack> entry : realItems.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().clone());
            }
        }
    }

    public void showNextProgressiveItem() {
        if (progressiveInventory == null) return;
        if (progressiveIndex < shuffledSlots.size()) {
            int slot = shuffledSlots.get(progressiveIndex);
            if (useDisguise) {
                ItemStack disguised = disguisedItems.get(slot);
                if (disguised != null) {
                    progressiveInventory.setItem(slot, disguised.clone());
                }
            } else {
                ItemStack item = realItems.get(slot);
                if (item != null) {
                    progressiveInventory.setItem(slot, item.clone());
                }
            }
            progressiveIndex++;
        }
    }

    public boolean hasMoreProgressiveItems() {
        return progressiveIndex < shuffledSlots.size();
    }



    public ItemStack getRealItem(int slot) {
        return realItems.get(slot);
    }

    public void removeRealItem(int slot) {
        realItems.remove(slot);
        disguisedItems.remove(slot);
    }

}