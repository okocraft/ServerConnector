package net.okocraft.serverconnector;

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
import net.okocraft.serverconnector.config.ServerConnectorConfig;
import net.okocraft.serverconnector.listener.FirstJoinListener;
import net.okocraft.serverconnector.listener.PlayerListener;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ServerConnectorPlugin {

    private final ProxyServer proxy;
    private final ServerConnectorConfig config = new ServerConnectorConfig();
    private final TranslationDirectory translationDirectory;
    private final Path dataDirectory;
    private final List<SlashServerCommand> registeredSlashServerCommands = new ArrayList<>();

    private FirstJoinListener firstJoinListener;

    @Inject
    public ServerConnectorPlugin(@NotNull ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;

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
            this.loadConfig();
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
        if (this.firstJoinListener != null) {
            this.firstJoinListener.unsubscribe();
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

    public @NotNull ServerConnectorConfig getConfig() {
        return config;
    }

    private void loadConfig() throws IOException {
        var filepath = this.dataDirectory.resolve("config.yml");

        if (!Files.isRegularFile(filepath)) {
            this.saveResource("config.yml", filepath);
        }

        var node = YAMLConfigurationLoader.builder().setPath(filepath).build().load();

        var messageSendingSetting = node.getNode("message-sending-setting");
        this.config.sendJoinMessage = messageSendingSetting.getNode("join").getBoolean();
        this.config.sendFirstJoinMessage = messageSendingSetting.getNode("first-join").getBoolean();
        this.config.sendLeaveMessage = messageSendingSetting.getNode("leave").getBoolean();
        this.config.sendSwitchMessage = messageSendingSetting.getNode("switch").getBoolean();

        for (var entry : node.getNode("server-permission").getChildrenMap().entrySet()) {
            var key = String.valueOf(entry.getKey());
            switch (key) {
                case "enable":
                    this.config.enableServerPermission = entry.getValue().getBoolean();
                    break;
                case "proxy":
                    this.config.proxyPermission = entry.getValue().getString();
                    break;
                default:
                    this.config.serverPermissionMap.put(key, entry.getValue().getString());
            }
        }

        this.config.fallbackServer = node.getNode("server-to-send-when-kicked").getString();
    }

    private void saveResource(String resourceName, Path filepath) throws IOException {
        if (!Files.isRegularFile(filepath)) {
            try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
                Files.copy(Objects.requireNonNull(input), filepath);
            }
        }
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
        if (this.config.sendFirstJoinMessage && this.getProxy().getPluginManager().getPlugin("LuckPerms").isPresent()) {
            this.firstJoinListener = new FirstJoinListener(this);
        }
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var defaultFileName = "en.yml";
        var defaultFile = directory.resolve(defaultFileName);
        this.saveResource(defaultFileName, defaultFile);

        var japaneseFileName = "ja_JP.yml";
        var japaneseFile = directory.resolve(japaneseFileName);
        this.saveResource(japaneseFileName, japaneseFile);
    }
}
