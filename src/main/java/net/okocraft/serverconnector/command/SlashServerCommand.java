package net.okocraft.serverconnector.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;

public class SlashServerCommand extends Command {

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

        var player = (ProxiedPlayer) sender;
        var currentServerName = player.getServer().getInfo().getName();

        if (currentServerName.equalsIgnoreCase(server.getName())) {
            audience.sendMessage(Messages.SLASH_SERVER_ALREADY_CONNECTED);
            return;
        }

        if (!server.canAccess(player)) {
            audience.sendMessage(Messages.SLASH_SERVER_COULD_NOT_CONNECT.apply(server.getName()));
            return;
        }

        audience.sendMessage(Messages.SLASH_SERVER_CONNECTING.apply(server.getName()));
        player.connect(server);
    }
}
