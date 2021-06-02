package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import org.jetbrains.annotations.NotNull;

public class SnapshotClientListener implements Listener {

    private final ServerConnectorPlugin plugin;

    public SnapshotClientListener(@NotNull ServerConnectorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull ServerConnectEvent event) {
        var player = event.getPlayer();
        var version = player.getPendingConnection().getVersion();

        if (ProtocolConstants.MINECRAFT_1_16_4 < version) {
            var serverName = plugin.getConfig().get(ConfigValues.SNAPSHOT_SERVER);
            var snapshotServer = plugin.getProxy().getServerInfo(serverName);

            if (snapshotServer != null) {
                event.setTarget(snapshotServer);
            }
        }
    }
}
