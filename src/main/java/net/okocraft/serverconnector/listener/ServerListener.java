package net.okocraft.serverconnector.listener;

import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import org.jetbrains.annotations.NotNull;

public class ServerListener implements Listener {

    private final ServerConnectorPlugin plugin;

    public ServerListener(@NotNull ServerConnectorPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onReload(ProxyReloadEvent e) {
        plugin.getProxy().getPluginManager().unregisterCommands(plugin);
        plugin.enableSlashServer();
    }
}
