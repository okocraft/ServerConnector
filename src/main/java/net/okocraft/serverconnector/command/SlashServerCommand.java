package net.okocraft.serverconnector.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.okocraft.serverconnector.ServerConnectorPlugin;

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
        var messages = this.plugin.getLocalizedMessages(sender);

        if (!sender.hasPermission(this.permission)) {
            sender.sendMessage(messages.slashServerNoPermission);
            return;
        }

        Player target;
        var args = invocation.arguments();

        if (0 < args.length) {
            if (!sender.hasPermission(GLOBAL_OTHER_PLAYER_PERMISSION) && !sender.hasPermission(otherPermission)) {
                sender.sendMessage(messages.slashServerNoPermission);
                return;
            }

            target = this.plugin.getProxy().getPlayer(args[0]).orElse(null);

            if (target == null) {
                sender.sendMessage(messages.slashServerPlayerNotFound(args[0]));
                return;
            }
        } else {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("/" + this.serverInfo.getName() + " <player>"));
                return;
            }
        }

        var currentServer = target.getCurrentServer().map(ServerConnection::getServerInfo);

        if (this.serverInfo.equals(currentServer.orElse(null))) {
            sender.sendMessage(messages.slashServerAlreadyConnected);
            return;
        }

        target.sendMessage(messages.slashServerConnecting(this.serverInfo.getName()));

        if (sender != target) {
            sender.sendMessage(messages.slashServerConnectingOther(target, this.serverInfo.getName()));
        }

        target.createConnectionRequest(this.server).fireAndForget();
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        var sender = invocation.source();
        var args = invocation.arguments();

        if (args.length <= 1 && (sender.hasPermission(GLOBAL_OTHER_PLAYER_PERMISSION) || sender.hasPermission(otherPermission))) {
            return this.plugin.getProxy().getAllPlayers().stream()
                    .filter(Predicate.not(sender::equals))
                    .filter(Predicate.not(player -> this.serverInfo.equals(player.getCurrentServer().map(ServerConnection::getServerInfo).orElse(null))))
                    .map(Player::getUsername)
                    .filter(name -> args.length == 0 || name.regionMatches(true, 0, args[0], 0, args[0].length()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
