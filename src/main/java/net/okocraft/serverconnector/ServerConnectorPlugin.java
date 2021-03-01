package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.bungee.BungeeYamlFactory;
import com.github.siroshun09.configapi.common.yaml.Yaml;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.listener.PlayerListener;
import net.okocraft.serverconnector.listener.ServerListener;

public final class ServerConnectorPlugin extends Plugin {

    private Yaml config;

    @Override
    public void onLoad() {
        config = BungeeYamlFactory.loadUnsafe(this, "config.yml");
    }

    @Override
    public void onEnable() {
        enablePlayerListener();
        enableServerListener();
        enableSlashServer();
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListeners(this);
    }

    public Yaml getConfig() {
        return config;
    }

    private void enablePlayerListener() {
        var playerListener = new PlayerListener(this);
        getProxy().getPluginManager().registerListener(this, playerListener);
    }

    private void enableServerListener() {
        var serverListener = new ServerListener();
        getProxy().getPluginManager().registerListener(this, serverListener);
    }

    private void enableSlashServer() {
        getProxy().getServers().values().stream()
                .map(SlashServerCommand::new)
                .forEach(cmd -> getProxy().getPluginManager().registerCommand(this, cmd));
    }
}
