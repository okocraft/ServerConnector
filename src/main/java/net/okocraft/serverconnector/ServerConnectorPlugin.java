package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.md_5.bungee.api.plugin.Plugin;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.listener.FirstJoinListener;
import net.okocraft.serverconnector.listener.PlayerListener;
import net.okocraft.serverconnector.listener.ServerListener;
import net.okocraft.serverconnector.listener.SnapshotClientListener;
import net.okocraft.serverconnector.util.AudienceUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class ServerConnectorPlugin extends Plugin {

    private final YamlConfiguration config = YamlConfiguration.create(getDataFolder().toPath().resolve("config.yml"));
    private final TranslationDirectory translationDirectory =
            TranslationDirectory.newBuilder()
                    .setDirectory(getDataFolder().toPath().resolve("languages"))
                    .setKey(Key.key("serverconnector", "language"))
                    .setDefaultLocale(Locale.ENGLISH)
                    .onDirectoryCreated(this::saveDefaultLanguages)
                    .build();

    private FirstJoinListener firstJoinListener;

    @Override
    public void onLoad() {
        try {
            ResourceUtils.copyFromClassLoaderIfNotExists(getClass().getClassLoader(), "config.yml", config.getPath());
            config.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.yml", e);
        }

        try {
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
        enableFirstJoinDetector();
        enableSnapshotListenerIfConfigured();
    }

    @Override
    public void onDisable() {
        if (firstJoinListener != null) {
            firstJoinListener.unsubscribe();
        }

        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().unregisterCommands(this);
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

    public void enableSlashServer() {
        getProxy().getServers().values().stream()
                .map(SlashServerCommand::new)
                .forEach(cmd -> getProxy().getPluginManager().registerCommand(this, cmd));
    }

    private void enableFirstJoinDetector() {
        if (getProxy().getPluginManager().getPlugin("LuckPerms") != null && config.get(ConfigValues.SEND_FIRST_JOIN_MESSAGE)) {
            firstJoinListener = new FirstJoinListener(this);
        }
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
