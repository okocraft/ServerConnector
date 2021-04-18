package net.okocraft.serverconnector.listener;

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;

import java.util.Locale;
import java.util.Objects;

public class ServerListener implements Listener {

    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        var player = e.getPlayer();
        var serverName = e.getTarget().getName();
        var permission = "serverconnector.connect." + serverName;

        if (!player.hasPermission(permission)) {
            var audience = AudienceUtil.player(player);
            var message = Messages.NO_PERMISSION_TO_CONNECT.apply(serverName, permission);

            e.setCancelled(true);

            if (player.getServer() != null) {
                audience.sendMessage(message);
            } else {
                var locale = Objects.requireNonNullElse(player.getLocale(), Locale.ENGLISH);
                var translated = GlobalTranslator.render(message, locale);
                player.disconnect(BungeeComponentSerializer.get().serialize(translated));
            }
        }
    }
}
