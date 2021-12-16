package net.okocraft.serverconnector.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;

import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

public class SlashServerCommand extends Command implements TabExecutor {

    private final ServerInfo server;

    public SlashServerCommand(ServerInfo serverInfo) {
        super(serverInfo.getName(), "serverconnector.slashserver." + serverInfo.getName());
        server = serverInfo;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        var audience = AudienceUtil.sender(sender);

        if (!(sender instanceof ProxiedPlayer)) {
            audience.sendMessage(Messages.SLASH_SERVER_ONLY_PLAYER);
            return;
        }

        if (!sender.hasPermission(getPermission())) {
            audience.sendMessage(Messages.SLASH_SERVER_NO_PERMISSION.apply(getPermission()));
            return;
        }

        ProxiedPlayer player;
        boolean otherPlayer;

        if (0 < args.length && !sender.getName().equalsIgnoreCase(args[0])) {
            var permission = getPermission() + ".other";
            if (!sender.hasPermission(permission)) {
                audience.sendMessage(Messages.SLASH_SERVER_NO_PERMISSION.apply(permission));
                return;
            }

            player = ProxyServer.getInstance().getPlayer(args[0]);
            otherPlayer = true;

            if (player == null) {
                audience.sendMessage(Messages.SLASH_SERVER_PLAYER_NOT_FOUND.apply(args[0]));
                return;
            }
        } else {
            player = (ProxiedPlayer) sender;
            otherPlayer = false;
        }

        var currentServerName = player.getServer().getInfo().getName();

        if (currentServerName.equalsIgnoreCase(server.getName())) {
            audience.sendMessage(Messages.SLASH_SERVER_ALREADY_CONNECTED);
            return;
        }

        if (!server.canAccess(player)) {
            audience.sendMessage(Messages.SLASH_SERVER_COULD_NOT_CONNECT.apply(server.getName()));
            return;
        }

        if (otherPlayer) {
            AudienceUtil.player(player).sendMessage(Messages.SLASH_SERVER_CONNECTING.apply(server.getName()));
            audience.sendMessage(Messages.SLASH_SERVER_CONNECTING_OTHER.apply(player, server.getName()));
        } else {
            audience.sendMessage(Messages.SLASH_SERVER_CONNECTING.apply(server.getName()));
        }

        player.connect(server);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(getPermission() + ".other")) {
            var filter = args[0].toLowerCase(Locale.ENGLISH);
            return ProxyServer.getInstance()
                    .getPlayers()
                    .stream()
                    .map(CommandSender::getName)
                    .map(name -> name.toLowerCase(Locale.ENGLISH))
                    .filter(name -> !name.equalsIgnoreCase(sender.getName()))
                    .filter(name -> name.startsWith(filter))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
