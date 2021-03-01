package net.okocraft.serverconnector.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SlashServerCommand extends Command {

    private final ServerInfo server;

    public SlashServerCommand(ServerInfo serverInfo) {
        super(serverInfo.getName(), "serverconnector.slashserver." + serverInfo.getName());
        server = serverInfo;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Only player."));
            return;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(new TextComponent("No permission: " + getPermission()));
            return;
        }

        var player = (ProxiedPlayer) sender;

        if (player.getServer().getInfo().getName().equalsIgnoreCase(server.getName())) {
            sender.sendMessage(new TextComponent("Already connected."));
            return;
        }

        if (!server.canAccess(player)) {
            sender.sendMessage(new TextComponent("Could not connect to " + server.getName()));
            return;
        }

        sender.sendMessage(new TextComponent("Connecting to " + server.getName()));
        player.connect(server);
    }
}
