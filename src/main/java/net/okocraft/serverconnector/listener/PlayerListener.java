package net.okocraft.serverconnector.listener;

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final Set<UUID> joinedPlayer = new HashSet<>();
    private final ServerConnectorPlugin plugin;

    public PlayerListener(ServerConnectorPlugin plugin) {
        this.plugin = plugin;
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
            throw new IllegalStateException("Unknown server: " + serverName);
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
    public void onDisconnect(PlayerDisconnectEvent e) {
        var player = e.getPlayer();

        if (joinedPlayer.contains(player.getUniqueId())) {
            joinedPlayer.remove(e.getPlayer().getUniqueId());

            var message = Messages.LEFT_PROXY.apply(player.getName());
            AudienceUtil.all().sendMessage(message);
        }
    }

    @EventHandler(priority = 127)
    public void onSwitch(ServerSwitchEvent e) {
        var player = e.getPlayer();
        var playerName = player.getName();

        if (e.getFrom() == null) {
            joinedPlayer.add(player.getUniqueId());
            var message = Messages.JOIN_PROXY.apply(playerName);
            plugin.getProxy().getScheduler().schedule(
                    plugin,
                    () -> AudienceUtil.all().sendMessage(message),
                    2,
                    TimeUnit.SECONDS
            );
        } else {
            var serverName = player.getServer().getInfo().getName();
            var message = Messages.SWITCH_SERVER.apply(playerName, serverName);
            AudienceUtil.all().sendMessage(message);
        }
    }
}
