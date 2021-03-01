package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {

    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        var serverName = e.getTarget().getName();
        var permission = "server." + serverName + ".connect";

        if (!e.getPlayer().hasPermission(permission)) {
            var message = "You don't have the permission to connect the server: " + permission;
            e.getPlayer().sendMessage(new TextComponent(message));
            e.setCancelled(true);
        }
    }
}
