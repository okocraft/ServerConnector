package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;

import java.util.stream.Stream;

public class PlayerListener implements Listener {

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

        if (sendTo == null || sendTo.getName().equals(from.getName())) {
            return;
        }

        e.setCancelled(true);
        e.setCancelServer(sendTo);

        var message = new TextComponent("Kicked from " + from.getName() + ": ");

        Stream.of(e.getKickReasonComponent()).forEach(message::addExtra);
        player.sendMessage(message);
    }

    @EventHandler(priority = 127)
    public void onDisconnect(ServerDisconnectEvent e) {
        var playerName = e.getPlayer().getName();
        var message = new TextComponent(playerName + " left the proxy.");
        plugin.getProxy().getPlayers().forEach(p -> p.sendMessage(message));
    }

    @EventHandler(priority = 127)
    public void onSwitch(ServerSwitchEvent e) {
        var playerName = e.getPlayer().getName();

        if (e.getFrom() == null) {
            var joinMessage = new TextComponent(playerName + " joined the proxy.");
            plugin.getProxy().getPlayers().forEach(p -> p.sendMessage(joinMessage));
        } else {
            var switchTo = e.getPlayer().getServer().getInfo().getName();
            var switchMessage = new TextComponent(playerName + " moved to " + switchTo + ".");
            plugin.getProxy().getPlayers().forEach(p -> p.sendMessage(switchMessage));
        }
    }
}
