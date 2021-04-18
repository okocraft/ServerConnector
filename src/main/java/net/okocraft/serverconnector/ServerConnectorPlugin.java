package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.lang.LanguageLoader;
import net.okocraft.serverconnector.listener.PlayerListener;
import net.okocraft.serverconnector.listener.ServerListener;
import net.okocraft.serverconnector.util.AudienceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class ServerConnectorPlugin extends Plugin {

    private final YamlConfiguration config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));

    @Override
    public void onLoad() {
        try {
            config.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.yml", e);
        }

        try {
            LanguageLoader.load(this);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load languages", e);
        }
    }

    @Override
    public void onEnable() {
        AudienceUtil.init(this);

        enablePlayerListener();
        enableServerListener();
        enableSlashServer();
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListeners(this);
    }

    public @NotNull YamlConfiguration getConfig() {
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
