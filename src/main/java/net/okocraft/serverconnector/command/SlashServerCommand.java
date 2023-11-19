package net.okocraft.serverconnector.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.lang.Messages;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SlashServerCommand implements SimpleCommand {

    private static final String GLOBAL_OTHER_PLAYER_PERMISSION = "serverconnector.slashserver.other-players";

    private final ServerConnectorPlugin plugin;
    private final RegisteredServer server;
    private final ServerInfo serverInfo;
    private final String permission;
    private final String otherPermission;
    private final CommandMeta meta;

    public SlashServerCommand(ServerConnectorPlugin plugin, RegisteredServer server) {
        this.plugin = plugin;
        this.server = server;
        this.serverInfo = server.getServerInfo();
        this.permission = "serverconnector.slashserver." + this.serverInfo.getName();
        this.otherPermission = this.permission + ".other";
        this.meta = plugin.getProxy().getCommandManager().metaBuilder(this.serverInfo.getName()).plugin(plugin).build();
    }

    public void register() {
        this.plugin.getProxy().getCommandManager().register(this.meta, this);
    }

    public void unregister() {
        this.plugin.getProxy().getCommandManager().unregister(this.meta);
    }

    @Override
    public void execute(Invocation invocation) {
        var sender = invocation.source();

        if (!sender.hasPermission(this.permission)) {
            sender.sendMessage(Messages.SLASH_SERVER_NO_PERMISSION.apply(this.permission));
            return;
        }

        Player target;
        var args = invocation.arguments();

        if (0 < args.length) {
            if (!sender.hasPermission(GLOBAL_OTHER_PLAYER_PERMISSION) && !sender.hasPermission(otherPermission)) {
                sender.sendMessage(Messages.SLASH_SERVER_NO_PERMISSION.apply(otherPermission));
                return;
            }

            target = this.plugin.getProxy().getPlayer(args[0]).orElse(null);

            if (target == null) {
                sender.sendMessage(Messages.SLASH_SERVER_PLAYER_NOT_FOUND.apply(args[0]));
                return;
            }
        } else {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Messages.SLASH_SERVER_ONLY_PLAYER);
                return;
            }
        }

        var currentServer = target.getCurrentServer().map(ServerConnection::getServerInfo);

        if (serverInfo.equals(currentServer.orElse(null))) {
            sender.sendMessage(Messages.SLASH_SERVER_ALREADY_CONNECTED);
            return;
        }

        target.createConnectionRequest(server).fireAndForget();

        if (sender != target) {
            target.sendMessage(Messages.SLASH_SERVER_CONNECTING.apply(serverInfo.getName()));
            sender.sendMessage(Messages.SLASH_SERVER_CONNECTING_OTHER.apply(target, serverInfo.getName()));
        } else {
            sender.sendMessage(Messages.SLASH_SERVER_CONNECTING.apply(serverInfo.getName()));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        var sender = invocation.source();
        var args = invocation.arguments();

        if (args.length <= 1 && (sender.hasPermission(GLOBAL_OTHER_PLAYER_PERMISSION) || sender.hasPermission(otherPermission))) {
            return plugin.getProxy().getAllPlayers().stream()
                    .filter(Predicate.not(sender::equals))
                    .filter(Predicate.not(player -> serverInfo.equals(player.getCurrentServer().map(ServerConnection::getServerInfo).orElse(null))))
                    .map(Player::getUsername)
                    .filter(name -> args.length == 0 || name.regionMatches(true, 0, args[0], 0, args[0].length()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
