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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public class PlayerListener {

    private final ServerConnectorPlugin plugin;

    public PlayerListener(ServerConnectorPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(@NotNull LoginEvent event) {
        if (this.shouldIgnore(event) || this.checkProxyPermissionIfEnabled(event.getPlayer())) {
            return;
        }

        var locale = Objects.requireNonNullElse(event.getPlayer().getEffectiveLocale(), Locale.ENGLISH);
        var translated = GlobalTranslator.render(Messages.NO_PERMISSION_TO_CONNECT_TO_PROXY, locale);
        event.setResult(ResultedEvent.ComponentResult.denied(translated));
    }

    @Subscribe
    public void onChooseInitialServer(@NotNull PlayerChooseInitialServerEvent event) {
        var player = event.getPlayer();
        var initialServer = event.getInitialServer().orElse(null);

        if (initialServer != null && this.checkServerPermission(player, initialServer.getServerInfo().getName(), false)) {
            return;
        }

        var fallback = this.getFallbackServer();

        if (fallback != null && this.checkServerPermission(player, fallback.getServerInfo().getName(), false)) {
            event.setInitialServer(fallback);
        }
    }

    @Subscribe
    public void onConnect(@NotNull ServerPreConnectEvent event) {
        if (this.shouldIgnore(event) || event.getPreviousServer() == null) {
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
        var fallback = this.getFallbackServer();

        if (fallback != null && !from.equals(fallback)) {
            var message = Messages.KICKED_FROM_SERVER.apply(from.getServerInfo().getName(), event.getServerKickReason().orElse(Component.empty()));
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback, message));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onServerConnect(@NotNull ServerPostConnectEvent event) {
        var player = event.getPlayer();
        var playerName = player.getUsername();

        if (event.getPreviousServer() == null) {
            if (this.plugin.getConfig().sendJoinMessage) {
                this.plugin.getProxy().sendMessage(Messages.JOIN_PROXY.apply(playerName));
            }

            // The reason for not checking the config setting here is that if it is disabled, the user will not be added to the FirstJoinPlayerHolder.
            if (FirstJoinPlayerHolder.remove(player.getUniqueId())) {
                this.plugin.getProxy().sendMessage(Messages.FIRST_JOIN_MESSAGE.apply(playerName));
            }
        } else if (this.plugin.getConfig().sendSwitchMessage) {
            var serverName = player.getCurrentServer().map(ServerConnection::getServerInfo).map(ServerInfo::getName).orElse("");

            if (!serverName.isEmpty()) {
                this.plugin.getProxy().sendMessage(Messages.SWITCH_SERVER.apply(playerName, serverName));
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        var player = event.getPlayer();

        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN && this.plugin.getConfig().sendLeaveMessage) {
            this.plugin.getProxy().sendMessage(Messages.LEFT_PROXY.apply(player.getUsername()));
        }
    }

    private boolean checkProxyPermissionIfEnabled(@NotNull Player player) {
        return this.checkPermissionIfNotNull(player, this.plugin.getConfig().proxyPermission);
    }

    private boolean checkServerPermission(@NotNull Player player, @NotNull String serverName, boolean sendMessage) {
        if (this.checkPermissionIfNotNull(player, this.getServerPermission(serverName))) {
            return true;
        } else if (sendMessage) {
            player.sendMessage(Messages.NO_PERMISSION_TO_CONNECT_TO_SERVER.apply(serverName, this.getServerPermission(serverName)));
        }

        return false;
    }

    private boolean checkPermissionIfNotNull(@NotNull Player player, @Nullable String permissionNode) {
        return permissionNode == null || player.hasPermission(permissionNode);
    }

    private @NotNull String getServerPermission(@NotNull String serverName) {
        var customPermission = this.plugin.getConfig().serverPermissionMap.get(serverName);
        return customPermission != null && !customPermission.isEmpty() ? customPermission : this.plugin.getConfig().serverPermissionMap.get("default");
    }

    private @Nullable RegisteredServer getFallbackServer() {
        var serverName = this.plugin.getConfig().fallbackServer;
        return serverName != null ? this.plugin.getProxy().getServer(serverName).orElse(null) : null;
    }

    private boolean shouldIgnore(@NotNull ResultedEvent<?> event) {
        return !event.getResult().isAllowed() || !this.plugin.getConfig().enableServerPermission;
    }
}
