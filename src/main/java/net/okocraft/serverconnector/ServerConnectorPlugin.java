package net.okocraft.serverconnector;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.translation.Translator;
import net.okocraft.serverconnector.command.SlashServerCommand;
import net.okocraft.serverconnector.config.ServerConnectorConfig;
import net.okocraft.serverconnector.lang.Messages;
import net.okocraft.serverconnector.listener.FirstJoinListener;
import net.okocraft.serverconnector.listener.PlayerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class ServerConnectorPlugin {

    private final ProxyServer proxy;
    private final ServerConnectorConfig config = new ServerConnectorConfig();
    private final Map<Locale, Messages> localizedMessagesMap = new HashMap<>();
    private final Path dataDirectory;
    private final List<SlashServerCommand> registeredSlashServerCommands = new ArrayList<>();

    private FirstJoinListener firstJoinListener;

    @Inject
    public ServerConnectorPlugin(@NotNull ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent ignored) {
        try {
            Files.createDirectories(this.dataDirectory);
            this.loadConfig();
            this.loadMessages();
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("Failed to load config.yml or languages", e);
        }

        enablePlayerListener();
        enableSlashServer();
        enableFirstJoinDetector();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent ignored) {
        if (this.firstJoinListener != null) {
            this.firstJoinListener.unsubscribe();
        }

        getProxy().getEventManager().unregisterListeners(this);

        this.registeredSlashServerCommands.forEach(SlashServerCommand::unregister);
        this.registeredSlashServerCommands.clear();
        this.localizedMessagesMap.clear();
    }

    @Subscribe
    public void onReload(ProxyReloadEvent ignored) {
        this.registeredSlashServerCommands.forEach(SlashServerCommand::unregister);
        enableSlashServer();
    }

    public @NotNull ProxyServer getProxy() {
        return proxy;
    }

    public @NotNull ServerConnectorConfig getConfig() {
        return config;
    }

    public @NotNull Messages getLocalizedMessages(@Nullable CommandSource sender) {
        if (sender instanceof Player) {
            return this.getLocalizedMessages(((Player) sender).getEffectiveLocale());
        } else {
            return this.getLocalizedMessages((Locale) null);
        }
    }

    public @NotNull Messages getLocalizedMessages(@Nullable Locale locale) {
        var messages = this.localizedMessagesMap.get(locale);

        if (messages == null && locale != null) {
            messages = this.localizedMessagesMap.get(new Locale(locale.getLanguage()));
        }

        return messages != null ? messages : this.localizedMessagesMap.get(null);
    }

    private void loadConfig() throws IOException {
        var filepath = this.dataDirectory.resolve("config.yml");

        if (!Files.isRegularFile(filepath)) {
            this.saveResource("config.yml", filepath);
        }

        var node = YamlConfigurationLoader.builder().path(filepath).build().load();

        var messageSendingSetting = node.node("message-sending-setting");
        this.config.sendJoinMessage = messageSendingSetting.node("join").getBoolean();
        this.config.sendFirstJoinMessage = messageSendingSetting.node("first-join").getBoolean();
        this.config.sendLeaveMessage = messageSendingSetting.node("leave").getBoolean();
        this.config.sendSwitchMessage = messageSendingSetting.node("switch").getBoolean();

        for (var entry : node.node("server-permission").childrenMap().entrySet()) {
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

        this.config.fallbackServer = node.node("server-to-send-when-kicked").getString();
    }

    private void loadMessages() throws IOException {
        Path directory = this.dataDirectory.resolve("languages");
        Files.createDirectories(directory);
        this.localizedMessagesMap.put(null, new Messages(BasicConfigurationNode.root()));
        this.saveResource("en.yml", directory.resolve("en.yml"));
        this.saveResource("ja_JP.yml", directory.resolve("ja_JP.yml"));
        try (Stream<Path> list = Files.list(directory)) {
            list.forEach(this::loadMessageFile);
        }
    }

    private void loadMessageFile(Path filepath) {
        String filename = filepath.getFileName().toString();
        if (!filename.endsWith(".yml")) {
            return;
        }

        Locale locale = Translator.parseLocale(filename.substring(0, filename.length() - 4));

        if (locale == null) {
            return;
        }

        ConfigurationNode source;

        try {
           source = YamlConfigurationLoader.builder().path(filepath).build().load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var messageMap = new Messages(source);
        this.localizedMessagesMap.put(locale, messageMap);
        this.localizedMessagesMap.put(new Locale(locale.getLanguage()), messageMap);
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
}
