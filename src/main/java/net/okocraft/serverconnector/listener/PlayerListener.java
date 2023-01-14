package net.okocraft.serverconnector.listener;

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final Set<UUID> joinedPlayer = new HashSet<>();
    private final ServerConnectorPlugin plugin;

    public PlayerListener(ServerConnectorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        var player = e.getPlayer();
        var server = e.getTarget();
        var serverName = server.getName();
        var permission = server.getPermission();

        // If the player do not have the permission, switch to trying to connect to the fallback server.
        //
        // This will only be executed if the player are still in the process of connecting to the proxy
        // and are not connected to any server.
        if (player.getServer() == null && !player.hasPermission(permission)) {
            var fallbackServerName = plugin.getConfig().get(ConfigValues.SERVER_TO_SEND);
            var fallbackServer = ProxyServer.getInstance().getServerInfo(fallbackServerName);

            if (!serverName.equals(fallbackServerName) && fallbackServer != null) {
                e.setTarget(fallbackServer);
                serverName = fallbackServerName;
                permission = fallbackServer.getPermission();
            }
        }

        // Check the permissions of the server.
        if (!player.hasPermission(permission)) {
            var audience = AudienceUtil.player(player);
            e.setCancelled(true);

            if (player.getServer() != null) {
                audience.sendMessage(Messages.NO_PERMISSION_TO_CONNECT_TO_SERVER.apply(serverName, permission));
            } else {
                var locale = Objects.requireNonNullElse(player.getLocale(), Locale.ENGLISH);
                var translated = GlobalTranslator.render(Messages.NO_PERMISSION_TO_CONNECT_TO_PROXY, locale);
                player.disconnect(BungeeComponentSerializer.get().serialize(translated));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onKick(ServerKickEvent e) {
        var player = e.getPlayer();
        var proxy = plugin.getProxy();

        var from = e.getKickedFrom();

        if (from == null) {
            from = player.getServer().getInfo();
        }

        var serverName = plugin.getConfig().get(ConfigValues.SERVER_TO_SEND);
        var sendTo = proxy.getServerInfo(serverName);

        if (sendTo == null) {
            joinedPlayer.remove(player.getUniqueId());
            plugin.getLogger().warning("Unknown server: " + serverName);
            return;
        }

        if (sendTo.getName().equals(from.getName())) {
            joinedPlayer.remove(player.getUniqueId());
            return;
        }

        e.setCancelled(true);
        e.setCancelServer(sendTo);

        var kickedServerName = from.getName();
        var reason = BungeeComponentSerializer.get().deserialize(e.getKickReasonComponent());
        var message = Messages.KICKED_FROM_SERVER.apply(kickedServerName, reason);

        AudienceUtil.player(player).sendMessage(message);
    }

    @EventHandler(priority = 127)
    public void onSwitch(ServerSwitchEvent e) {
        var player = e.getPlayer();
        var playerName = player.getName();

        if (e.getFrom() == null) {
            var uuid = player.getUniqueId();
            plugin.getProxy().getScheduler().schedule(
                    plugin,
                    () -> {
                        if (plugin.getProxy().getPlayer(uuid) != null) {
                            joinedPlayer.add(uuid);

                            AudienceUtil.all().sendMessage(Messages.JOIN_PROXY.apply(playerName));

                            if (FirstJoinPlayerHolder.remove(uuid)) {
                                AudienceUtil.all().sendMessage(Messages.FIRST_JOIN_MESSAGE.apply(playerName));
                            }
                        }
                    },
                    2,
                    TimeUnit.SECONDS
            );
        } else {
            var serverName = player.getServer().getInfo().getName();
            var message = Messages.SWITCH_SERVER.apply(playerName, serverName);
            AudienceUtil.all().sendMessage(message);
        }
    }

    @EventHandler(priority = 127)
    public void onDisconnect(PlayerDisconnectEvent e) {
        var player = e.getPlayer();

        if (joinedPlayer.contains(player.getUniqueId())) {
            joinedPlayer.remove(e.getPlayer().getUniqueId());

            var message = Messages.LEFT_PROXY.apply(player.getName());
            AudienceUtil.all().sendMessage(message);
        }
    }
}
