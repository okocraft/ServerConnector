package net.okocraft.serverconnector.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class PlayerListener {

    private final ServerConnectorPlugin plugin;

    public PlayerListener(ServerConnectorPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(@NotNull LoginEvent event) {
        if (shouldIgnore(event)) {
            return;
        }

        var player = event.getPlayer();

        if (!player.hasPermission(plugin.getConfig().get(ConfigValues.PROXY_PERMISSION))) {
            var locale = Objects.requireNonNullElse(player.getEffectiveLocale(), Locale.ENGLISH);
            var translated = GlobalTranslator.render(Messages.NO_PERMISSION_TO_CONNECT_TO_PROXY, locale);
            event.setResult(ResultedEvent.ComponentResult.denied(translated));
        }
    }

    @Subscribe
    public void onChooseInitialServer(@NotNull PlayerChooseInitialServerEvent event) {
        var player = event.getPlayer();
        var initialServer = event.getInitialServer().orElse(null);

        if (initialServer != null && checkServerPermission(player, initialServer.getServerInfo().getName())) {
            return;
        }

        var fallbackServerName = plugin.getConfig().get(ConfigValues.SERVER_TO_SEND);
        var fallback = plugin.getProxy().getServer(fallbackServerName).orElse(null);

        if (fallback == null) {
            plugin.getLogger().warn("Unknown server: " + fallbackServerName);
            return;
        }

        if (checkServerPermission(player, fallback.getServerInfo().getName())) {
            event.setInitialServer(fallback);
        }
    }

    @Subscribe
    public void onConnect(@NotNull ServerPreConnectEvent event) {
        if (shouldIgnore(event) || event.getPreviousServer() == null) {
            return;
        }

        var player = event.getPlayer();

        if (!checkServerPermission(player, event.getOriginalServer().getServerInfo().getName(), true)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onKick(@NotNull KickedFromServerEvent event) {
        if (event.kickedDuringServerConnect()) {
            return;
        }

        var from = event.getServer();
        var fallbackServerName = plugin.getConfig().get(ConfigValues.SERVER_TO_SEND);
        var fallback = plugin.getProxy().getServer(fallbackServerName).orElse(null);

        if (fallback == null) {
            plugin.getLogger().warn("Unknown server: " + fallbackServerName);
            return;
        }

        if (from.equals(fallback)) {
            return;
        }

        var message = Messages.KICKED_FROM_SERVER.apply(from.getServerInfo().getName(), event.getServerKickReason().orElse(Component.empty()));
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback, message));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerConnect(@NotNull ServerPostConnectEvent event) {
        var player = event.getPlayer();
        var playerName = player.getUsername();

        if (event.getPreviousServer() == null) {
            if (plugin.getConfig().get(ConfigValues.SEND_JOIN_MESSAGE)) {
                plugin.getProxy().sendMessage(Messages.JOIN_PROXY.apply(playerName));
            }

            // The reason for not checking the config setting here is that if it is disabled, the user will not be added to the FirstJoinPlayerHolder.
            if (FirstJoinPlayerHolder.remove(player.getUniqueId())) {
                plugin.getProxy().sendMessage(Messages.FIRST_JOIN_MESSAGE.apply(playerName));
            }
        } else if (plugin.getConfig().get(ConfigValues.SEND_SWITCH_MESSAGE)) {
            var serverName = player.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("");

            if (!serverName.isEmpty()) {
                plugin.getProxy().sendMessage(Messages.SWITCH_SERVER.apply(playerName, serverName));
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        var player = event.getPlayer();

        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN && plugin.getConfig().get(ConfigValues.SEND_LEAVE_MESSAGE)) {
            plugin.getProxy().sendMessage(Messages.LEFT_PROXY.apply(player.getUsername()));
        }
    }

    private boolean checkServerPermission(@NotNull Player player, @NotNull String serverName) {
        return checkServerPermission(player, serverName, false);
    }

    private boolean checkServerPermission(@NotNull Player player, @NotNull String serverName, boolean sendMessage) {
        var permission = getServerPermission(serverName);

        if (permission.isEmpty() || player.hasPermission(permission)) {
            return true;
        }

        if (sendMessage) {
            player.sendMessage(Messages.NO_PERMISSION_TO_CONNECT_TO_SERVER.apply(serverName, permission));
        }

        return false;
    }

    private @NotNull String getServerPermission(@NotNull String serverName) {
        var customPermission = plugin.getConfig().get(ConfigValues.SERVER_CUSTOM_PERMISSION.apply(serverName));

        if (!customPermission.isEmpty()) {
            return customPermission;
        }

        return plugin.getConfig().get(ConfigValues.SERVER_CUSTOM_PERMISSION.apply("default")).replace("%server_name%", serverName);
    }

    private boolean shouldIgnore(@NotNull ResultedEvent<?> event) {
        return !event.getResult().isAllowed() || !plugin.getConfig().get(ConfigValues.SERVER_PERMISSION_ENABLE);
    }
}
