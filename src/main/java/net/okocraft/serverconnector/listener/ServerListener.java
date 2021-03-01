package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {

    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        var player = e.getPlayer();
        var serverName = e.getTarget().getName();
        var permission = "server." + serverName + ".connect";

        if (!player.hasPermission(permission)) {
            var message = "You don't have the permission to connect the server: " + permission;
            var toSend = new TextComponent(message);

            e.setCancelled(true);

            if (player.getServer() != null) {
                player.sendMessage(toSend);
            } else {
                player.disconnect(toSend);
            }
        }
    }
}
