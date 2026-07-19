package me.funnyairdrop.command;

import me.funnyairdrop.FunnyAirdrop;
import me.funnyairdrop.airdrop.Airdrop;
import me.funnyairdrop.gui.menu.AirdropsMenuGUI;
import me.funnyairdrop.util.ColorUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AirdropCommand implements CommandExecutor {
    private final FunnyAirdrop plugin;

    public AirdropCommand(FunnyAirdrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("funnyairdrop.admin")) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818У вас нет прав!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        try {
            switch (subCommand) {
                case "start" -> handleStart(sender, args);
                case "stop" -> handleStop(sender, args);
                case "create" -> handleCreate(sender, args);
                case "delete" -> handleDelete(sender, args);
                case "tp" -> handleTp(sender, args);
                case "menu" -> handleMenu(sender);
                case "reload" -> handleReload(sender);
                default -> sendHelp(sender);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818" + e.getMessage()));
        }

        return true;
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Используйте: /fairdrop start <id>"));
            return;
        }
        plugin.getAirdropManager().startAirdrop(args[1]);
        sender.sendMessage(ColorUtils.colorize("&#55ff55Аирдроп " + args[1] + " запущен!"));
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Используйте: /fairdrop stop <id>"));
            return;
        }
        plugin.getAirdropManager().stopAirdrop(args[1]);
        sender.sendMessage(ColorUtils.colorize("&#ff5555Аирдроп " + args[1] + " остановлен!"));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Используйте: /fairdrop create <name>"));
            return;
        }
        plugin.getAirdropManager().createAirdrop(args[1]);
        sender.sendMessage(ColorUtils.colorize("&#55ff55Аирдроп " + args[1] + " создан!"));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Используйте: /fairdrop delete <name>"));
            return;
        }
        plugin.getAirdropManager().deleteAirdrop(args[1]);
        sender.sendMessage(ColorUtils.colorize("&#FF5555Аирдроп " + args[1] + " удалён!"));
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Эта команда только для игроков!"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.colorize("&#FF1818Используйте: /fairdrop tp <id>"));
            return;
        }

        Airdrop airdrop = plugin.getAirdropManager().getAirdrop(args[1]);

        if (!airdrop.isActive()) {
            player.sendMessage(ColorUtils.colorize("&#FF1818Этот аирдроп не активен!"));
            return;
        }

        Location location = airdrop.getCurrentLocation();
        if (location == null) {
            player.sendMessage(ColorUtils.colorize("&#FF1818Локация аирдропа не найдена!"));
            return;
        }

        player.teleport(location);
        player.sendMessage(ColorUtils.colorize("&#55ff55Телепортирован к аирдропу " + airdrop.getDisplayName()));
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtils.colorize("&#FF1818Эта команда только для игроков!"));
            return;
        }
        new AirdropsMenuGUI(plugin).open(player);
    }

    private void handleReload(CommandSender sender) {
        plugin.getAirdropManager().stopAllAirdrops();
        plugin.getConfigManager().reload();
        plugin.getAirdropManager().loadAllAirdrops();
        plugin.getListenerManager().reload();
        sender.sendMessage(ColorUtils.colorize("&#55ff55Плагин успешно перезагружен!"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&#FF1818&lFunnyAirdrop v1.6"));
        sender.sendMessage("");
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop start <id> &#aaaaaa- Запустить аирдроп"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop stop <id> &#aaaaaa- Остановить аирдроп"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop tp <id> &#aaaaaa- Телепортироваться к аирдропу"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop create <name> &#aaaaaa- Создать новый аирдроп"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop delete <name> &#aaaaaa- Удалить аирдроп"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop menu &#aaaaaa- Открыть меню аирдропов"));
        sender.sendMessage(ColorUtils.colorize("&#FF1818/fairdrop reload &#aaaaaa- Перезагрузить плагин"));
    }
}