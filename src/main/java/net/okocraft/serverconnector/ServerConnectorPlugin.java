package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.listener.PlayerListener;
import net.okocraft.serverconnector.listener.ServerListener;
import net.okocraft.serverconnector.listener.SnapshotClientListener;
import net.okocraft.serverconnector.util.AudienceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public final class ServerConnectorPlugin extends Plugin {

    private final YamlConfiguration config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));
    private final TranslationDirectory translationDirectory =
            new TranslationDirectory(getDataFolder().toPath().resolve("languages"), Key.key("serverconnector", "language"));

    @Override
    public void onLoad() {
        try {
            ResourceUtils.copyFromClassLoaderIfNotExists(getClass().getClassLoader(), "config.yml", config.getPath());
            config.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.yml", e);
        }

        try {
            translationDirectory.createDirectoryIfNotExists(this::saveDefaultLanguages);
            translationDirectory.load();
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
        translationDirectory.unload();
    }

    public @NotNull YamlConfiguration getConfig() {
        return config;
    }

    private void enablePlayerListener() {
        var playerListener = new PlayerListener(this);
        getProxy().getPluginManager().registerListener(this, playerListener);
    }

    private void enableServerListener() {
        var serverListener = new ServerListener(this);
        getProxy().getPluginManager().registerListener(this, serverListener);
    }

    private void enableSlashServer() {
        getProxy().getServers().values().stream()
                .map(SlashServerCommand::new)
                .forEach(cmd -> getProxy().getPluginManager().registerCommand(this, cmd));
    }

    private void enableSnapshotListenerIfConfigured() {
        if (config.get(ConfigValues.ENABLE_SNAPSHOT_SERVER)) {
            var snapshotListener = new SnapshotClientListener(this);
            getProxy().getPluginManager().registerListener(this, snapshotListener);
        }
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var jarPath = getFile().toPath();
        var defaultFileName = "en.yml";
        var defaultFile = directory.resolve(defaultFileName);

        ResourceUtils.copyFromJarIfNotExists(jarPath, defaultFileName, defaultFile);

        var japaneseFileName = "ja_JP.yml";
        var japaneseFile = directory.resolve(japaneseFileName);

        ResourceUtils.copyFromJarIfNotExists(jarPath, japaneseFileName, japaneseFile);
    }
}
