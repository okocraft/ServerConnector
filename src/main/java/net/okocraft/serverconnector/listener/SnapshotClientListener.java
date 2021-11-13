package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.config.ConfigValues;
import org.jetbrains.annotations.NotNull;

public class SnapshotClientListener implements Listener {

    private final ServerConnectorPlugin plugin;
    private final int snapshotProtocolVersion;

    public SnapshotClientListener(@NotNull ServerConnectorPlugin plugin) {
        this.plugin = plugin;
        snapshotProtocolVersion = plugin.getConfig().get(ConfigValues.SNAPSHOT_PROTOCOL_VERSION);
    }

    @EventHandler
    public void onJoin(@NotNull ServerConnectEvent event) {
        if (snapshotProtocolVersion == event.getPlayer().getPendingConnection().getVersion()) {
            var serverName = plugin.getConfig().get(ConfigValues.SNAPSHOT_SERVER);
            var snapshotServer = plugin.getProxy().getServerInfo(serverName);

            if (snapshotServer != null) {
                event.setTarget(snapshotServer);
            }
        }
    }
}
