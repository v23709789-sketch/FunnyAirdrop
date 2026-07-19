package me.funnyairdrop.command;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AirdropTabCompleter implements TabCompleter {
    private final FunnyAirdrop plugin;
    private final List<String> SUB_COMMANDS = Arrays.asList("start", "stop", "create", "delete", "tp", "menu", "reload");

    public AirdropTabCompleter(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                                @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        if (!player.hasPermission("funnyairdrop.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("start") || subCommand.equals("stop") ||
                    subCommand.equals("delete") || subCommand.equals("tp")) {
                return plugin.getAirdropManager().getAllAirdrops().stream()
                        .map(Airdrop::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}