package me.funnyairdrop.hook;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmptyHologram implements IHologram {
    @Override
    public void createOrUpdateHologram(@NotNull List<String> lines, @NotNull Location location, @NotNull String name) {
    }

    @Override
    public void remove(@NotNull String name) {
    }
}