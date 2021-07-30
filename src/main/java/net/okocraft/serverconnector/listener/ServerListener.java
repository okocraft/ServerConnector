package net.okocraft.serverconnector.listener;

import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.AudienceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class ServerListener implements Listener {

    private final ServerConnectorPlugin plugin;

    public ServerListener(@NotNull ServerConnectorPlugin plugin) {
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
        if (player.getServer() == null && player.hasPermission(permission)) {
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
}
