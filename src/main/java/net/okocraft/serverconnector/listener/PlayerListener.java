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
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public final class PlayerListener {

    private final ServerConnectorPlugin plugin;
    private final FirstJoinPlayerHolder firstJoinPlayerHolder;

    public PlayerListener(ServerConnectorPlugin plugin, FirstJoinPlayerHolder firstJoinPlayerHolder) {
        this.plugin = plugin;
        this.firstJoinPlayerHolder = firstJoinPlayerHolder;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (this.shouldIgnore(event) || this.hasPermissionIfRequired(event.getPlayer(), this.plugin.getConfig().proxyPermission())) {
            return;
        }

        var messages = this.plugin.getLocalizedMessages(event.getPlayer());
        event.setResult(ResultedEvent.ComponentResult.denied(messages.proxyNoPermission()));
    }

    @Subscribe
    public void onChooseInitialServer(PlayerChooseInitialServerEvent event) {
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
    public void onConnect(ServerPreConnectEvent event) {
        if (this.shouldIgnore(event)) {
            return;
        }

        if (!this.checkServerPermission(event.getPlayer(), event.getOriginalServer().getServerInfo().getName(), true)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        if (event.kickedDuringServerConnect()) {
            return;
        }

        var from = event.getServer();
        var fallback = this.getFallbackServer();

        if (fallback != null && !from.equals(fallback)) {
            var messages = this.plugin.getLocalizedMessages(event.getPlayer());
            var kickMessage = messages.serverKick(from.getServerInfo().getName(), event.getServerKickReason().orElse(Component.empty()));
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(fallback, kickMessage));
        }
    }

    @Subscribe
    public void onServerConnect(ServerPostConnectEvent event) {
        var player = event.getPlayer();
        var config = this.plugin.getConfig();

        if (event.getPreviousServer() == null) {
            if (config.sendJoinMessage()) {
                this.broadcastMessage(messages -> messages.proxyJoin(player));
            }

            // The config setting is not checked here because players are never added to the holder when it is disabled.
            if (this.firstJoinPlayerHolder.remove(player.getUniqueId())) {
                this.broadcastMessage(messages -> messages.firstJoin(player));
            }
        } else if (config.sendSwitchMessage()) {
            player.getCurrentServer()
                    .map(ServerConnection::getServerInfo)
                    .map(ServerInfo::getName)
                    .filter(name -> !name.isEmpty())
                    .ifPresent(serverName -> this.broadcastMessage(messages -> messages.serverSwitch(player, serverName)));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        var player = event.getPlayer();
        this.firstJoinPlayerHolder.remove(player.getUniqueId());

        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN && this.plugin.getConfig().sendLeaveMessage()) {
            this.broadcastMessage(messages -> messages.proxyLeave(player));
        }
    }

    private boolean checkServerPermission(Player player, String serverName, boolean sendMessage) {
        if (this.hasPermissionIfRequired(player, this.plugin.getConfig().serverPermission(serverName))) {
            return true;
        }

        if (sendMessage) {
            player.sendMessage(this.plugin.getLocalizedMessages(player).serverNoPermission(serverName));
        }

        return false;
    }

    private boolean hasPermissionIfRequired(Player player, @Nullable String permissionNode) {
        return permissionNode == null || player.hasPermission(permissionNode);
    }

    private @Nullable RegisteredServer getFallbackServer() {
        var serverName = this.plugin.getConfig().fallbackServer();
        return serverName != null ? this.plugin.getProxy().getServer(serverName).orElse(null) : null;
    }

    private boolean shouldIgnore(ResultedEvent<?> event) {
        return !event.getResult().isAllowed() || !this.plugin.getConfig().enableServerPermission();
    }

    private void broadcastMessage(Function<Messages, Component> messageFunction) {
        for (var player : this.plugin.getProxy().getAllPlayers()) {
            player.sendMessage(messageFunction.apply(this.plugin.getLocalizedMessages(player)));
        }
    }
}
