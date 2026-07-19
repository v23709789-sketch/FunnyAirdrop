package me.funnyairdrop.airdrop;

import org.bukkit.inventory.ItemStack;

public class AirdropLootItem {
    private final ItemStack itemStack;
    private double chance;

    public AirdropLootItem(ItemStack itemStack, double chance) {
        this.itemStack = itemStack;
        this.chance = chance;
    }

    public ItemStack getItemStack() { return itemStack; }
    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }
}