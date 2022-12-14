package luminous.safechat;

import luminous.safechat.util.Fmt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static FileConfiguration config = null;

    public static final Map<String, ChatColor> CHAT_COLOR_SHEET = Map.ofEntries(
            Map.entry("_BLACK", ChatColor.BLACK),
            Map.entry("_DARK_BLUE", ChatColor.DARK_BLUE),
            Map.entry("_DARK_GREEN", ChatColor.DARK_GREEN),
            Map.entry("_DARK_AQUA", ChatColor.DARK_AQUA),
            Map.entry("_DARK_RED", ChatColor.DARK_RED),
            Map.entry("_DARK_PURPLE", ChatColor.DARK_PURPLE),
            Map.entry("_GOLD", ChatColor.GOLD),
            Map.entry("_GRAY", ChatColor.GRAY),
            Map.entry("_DARK_GRAY", ChatColor.DARK_GRAY),
            Map.entry("_BLUE", ChatColor.BLUE),
            Map.entry("_GREEN", ChatColor.GREEN),
            Map.entry("_AQUA", ChatColor.AQUA),
            Map.entry("_RED", ChatColor.RED),
            Map.entry("_LIGHT_PURPLE", ChatColor.LIGHT_PURPLE),
            Map.entry("_YELLOW", ChatColor.YELLOW),
            Map.entry("_WHITE", ChatColor.WHITE),
            Map.entry("_MAGIC", ChatColor.MAGIC),
            Map.entry("_BOLD", ChatColor.BOLD),
            Map.entry("_STRIKETHROUGH", ChatColor.STRIKETHROUGH),
            Map.entry("_UNDERLINE", ChatColor.UNDERLINE),
            Map.entry("_ITALIC", ChatColor.ITALIC),
            Map.entry("_RESET", ChatColor.RESET)
    );

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("safechat").setExecutor(this);
        Bukkit.getPluginCommand("msg").setExecutor(new ChatListener());
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        saveDefaultConfig();
        config = getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        sender.sendMessage(ChatColor.GRAY + "Safe Chat is enabled!");
        return true;
    }
}

class ChatListener implements Listener, CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length < 2) {
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            try {
                Bukkit.getPlayer(UUID.fromString(args[0]));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        }
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not Found!");
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]);
        }

        Map<String, Object> formatArgs = new HashMap<>(Map.of(
                "sender", sender.getName(),
                "target", target.getName(),
                "message", message.toString()
        ));
        formatArgs.putAll(Main.CHAT_COLOR_SHEET);

        sender.sendMessage(Fmt.format(
                Main.config.getString("direct-message-sender-format"),
                formatArgs
        ));
        target.sendMessage(Fmt.format(
                Main.config.getString("direct-message-target-format"),
                formatArgs
        ));
        return true;
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        String sender = event.getPlayer().getName();
        String message = event.getMessage();
        event.setCancelled(true);

        Map<String, Object> formatArgs = new HashMap<>(Map.of(
                "sender", sender,
                "message", message.toString()
        ));
        formatArgs.putAll(Main.CHAT_COLOR_SHEET);

        Bukkit.broadcastMessage(Fmt.format(
                Main.config.getString("global-message-format"),
                formatArgs
        ));
    }
}
