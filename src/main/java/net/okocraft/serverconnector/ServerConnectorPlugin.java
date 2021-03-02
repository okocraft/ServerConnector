package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.listener.PlayerListener;
import net.okocraft.serverconnector.listener.ServerListener;

import java.io.IOException;
import java.util.logging.Level;

public final class ServerConnectorPlugin extends Plugin {

    private final YamlConfiguration config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));

    @Override
    public void onLoad() {
        try {
            config.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
        }
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

    public YamlConfiguration getConfig() {
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
