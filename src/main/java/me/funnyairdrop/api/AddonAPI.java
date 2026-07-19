package me.funnyairdrop.api;

import me.funnyairdrop.airdrop.Airdrop;

import java.util.Collection;

@SuppressWarnings("unused")
public final class AddonAPI {

    private AddonAPI() {}

    public static FunnyAirdropAPI getAirdropAPI() {
        return me.funnyairdrop.FunnyAirdrop.getInstance().getApi();
    }

    public static Collection<Airdrop> getAllAirdrops() {
        return getAirdropAPI().getAllAirdrops();
    }

    public static Airdrop getAirdrop(String name) {
        return getAirdropAPI().getAirdrop(name);
    }

    public static void startAirdrop(String name) {
        getAirdropAPI().startAirdrop(name);
    }

    public static void stopAirdrop(String name) {
        getAirdropAPI().stopAirdrop(name);
    }
}