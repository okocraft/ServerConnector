package net.okocraft.serverconnector.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.okocraft.serverconnector.ServerConnectorPlugin;

import java.util.List;
import java.util.function.Predicate;

public final class SlashServerCommand implements SimpleCommand {

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
        var messages = this.plugin.getLocalizedMessages(sender);

        if (!sender.hasPermission(this.permission)) {
            sender.sendMessage(messages.slashServerNoPermission());
            return;
        }

        var args = invocation.arguments();
        Player target;

        if (0 < args.length) {
            if (!this.canConnectOtherPlayers(sender)) {
                sender.sendMessage(messages.slashServerNoPermission());
                return;
            }

            var found = this.plugin.getProxy().getPlayer(args[0]);

            if (found.isEmpty()) {
                sender.sendMessage(messages.slashServerPlayerNotFound(args[0]));
                return;
            }

            target = found.get();
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(Component.text("/" + this.serverInfo.getName() + " <player>"));
            return;
        }

        if (this.isConnectedToThisServer(target)) {
            sender.sendMessage(messages.slashServerAlreadyConnected());
            return;
        }

        target.sendMessage(this.plugin.getLocalizedMessages(target).slashServerConnecting(this.serverInfo.getName()));

        if (sender != target) {
            sender.sendMessage(messages.slashServerConnectingOther(target, this.serverInfo.getName()));
        }

        target.createConnectionRequest(this.server).fireAndForget();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        var sender = invocation.source();
        var args = invocation.arguments();

        if (1 < args.length || !this.canConnectOtherPlayers(sender)) {
            return List.of();
        }

        var prefix = args.length == 0 ? "" : args[0];

        return this.plugin.getProxy().getAllPlayers().stream()
                .filter(Predicate.not(sender::equals))
                .filter(Predicate.not(this::isConnectedToThisServer))
                .map(Player::getUsername)
                .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
                .toList();
    }

    private boolean canConnectOtherPlayers(CommandSource sender) {
        return sender.hasPermission(GLOBAL_OTHER_PLAYER_PERMISSION) || sender.hasPermission(this.otherPermission);
    }

    private boolean isConnectedToThisServer(Player player) {
        return player.getCurrentServer()
                .map(ServerConnection::getServerInfo)
                .filter(this.serverInfo::equals)
                .isPresent();
    }
}
