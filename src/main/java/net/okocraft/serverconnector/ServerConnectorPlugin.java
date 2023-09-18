package net.okocraft.serverconnector;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.key.Key;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.config.ConfigValues;
import net.okocraft.serverconnector.listener.FirstJoinListener;
import net.okocraft.serverconnector.listener.PlayerListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ServerConnectorPlugin {

    private final ProxyServer proxy;
    private final Logger logger;
    private final YamlConfiguration config;
    private final TranslationDirectory translationDirectory;
    private final List<SlashServerCommand> registeredSlashServerCommands = new ArrayList<>();

    private FirstJoinListener firstJoinListener;

    @Inject
    public ServerConnectorPlugin(@NotNull ProxyServer proxy, @NotNull Logger logger,
                                 @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;

        this.config = YamlConfiguration.create(dataDirectory.resolve("config.yml"));
        this.translationDirectory =
                TranslationDirectory.newBuilder()
                        .setDirectory(dataDirectory.resolve("languages"))
                        .setKey(Key.key("serverconnector", "language"))
                        .setDefaultLocale(Locale.ENGLISH)
                        .onDirectoryCreated(this::saveDefaultLanguages)
                        .build();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onEnable(ProxyInitializeEvent ignored) {
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

        enablePlayerListener();
        enableSlashServer();
        enableFirstJoinDetector();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisable(ProxyShutdownEvent ignored) {
        if (firstJoinListener != null) {
            firstJoinListener.unsubscribe();
        }

        getProxy().getEventManager().unregisterListeners(this);

        registeredSlashServerCommands.forEach(SlashServerCommand::unregister);

        translationDirectory.unload();
    }

    @Subscribe
    public void onReload(ProxyReloadEvent ignored) {
        if (!registeredSlashServerCommands.isEmpty()) {
            registeredSlashServerCommands.forEach(SlashServerCommand::unregister);
            enableSlashServer();
        }
    }

    public @NotNull ProxyServer getProxy() {
        return proxy;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public @NotNull YamlConfiguration getConfig() {
        return config;
    }

    private void enablePlayerListener() {
        var playerListener = new PlayerListener(this);
        getProxy().getEventManager().register(this, playerListener);
    }

    public void enableSlashServer() {
        getProxy().getAllServers().stream()
                .map(server -> new SlashServerCommand(this, server))
                .forEach(command -> {
                    command.register();
                    registeredSlashServerCommands.add(command);
                });
    }

    private void enableFirstJoinDetector() {
        if (getProxy().getPluginManager().getPlugin("LuckPerms").isPresent() && config.get(ConfigValues.SEND_FIRST_JOIN_MESSAGE)) {
            firstJoinListener = new FirstJoinListener(this);
        }
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var defaultFileName = "en.yml";
        var defaultFile = directory.resolve(defaultFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(getClass().getClassLoader(), defaultFileName, defaultFile);

        var japaneseFileName = "ja_JP.yml";
        var japaneseFile = directory.resolve(japaneseFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(getClass().getClassLoader(), japaneseFileName, japaneseFile);
    }
}
