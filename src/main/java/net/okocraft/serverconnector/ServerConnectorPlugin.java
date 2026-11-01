package net.okocraft.serverconnector;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
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
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class ServerConnectorPlugin {

    private static final String CONFIG_FILENAME = "config.yml";
    private static final String LANGUAGES_DIRECTORY = "languages";
    private static final List<String> BUNDLED_LANGUAGE_FILES = List.of("en.yml", "ja_JP.yml");
    private static final String YAML_EXTENSION = ".yml";

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final FirstJoinPlayerHolder firstJoinPlayerHolder = new FirstJoinPlayerHolder();
    private final List<SlashServerCommand> registeredSlashServerCommands = new ArrayList<>();

    private volatile ServerConnectorConfig config;
    private volatile Map<Locale, Messages> localizedMessages = Map.of();
    private volatile Messages defaultMessages = Messages.defaults();
    private @Nullable FirstJoinListener firstJoinListener;

    @Inject
    public ServerConnectorPlugin(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.config = ServerConnectorConfig.load(BasicConfigurationNode.root());
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent ignored) {
        try {
            Files.createDirectories(this.dataDirectory);
            this.config = this.loadConfig();
            this.loadMessages();
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("Failed to load config.yml or languages", e);
        }

        this.proxy.getEventManager().register(this, new PlayerListener(this, this.firstJoinPlayerHolder));
        this.registerSlashServerCommands();
        this.enableFirstJoinDetector();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent ignored) {
        if (this.firstJoinListener != null) {
            this.firstJoinListener.unsubscribe();
            this.firstJoinListener = null;
        }

        this.proxy.getEventManager().unregisterListeners(this);
        this.unregisterSlashServerCommands();
        this.firstJoinPlayerHolder.clear();
    }

    @Subscribe
    public void onReload(ProxyReloadEvent ignored) {
        this.unregisterSlashServerCommands();
        this.registerSlashServerCommands();
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    public ServerConnectorConfig getConfig() {
        return this.config;
    }

    public Messages getLocalizedMessages(@Nullable CommandSource sender) {
        return this.getLocalizedMessages(sender instanceof Player player ? player.getEffectiveLocale() : null);
    }

    public Messages getLocalizedMessages(@Nullable Locale locale) {
        if (locale == null) {
            return this.defaultMessages;
        }

        var messages = this.localizedMessages.get(locale);

        if (messages == null) {
            messages = this.localizedMessages.get(Locale.of(locale.getLanguage()));
        }

        return messages != null ? messages : this.defaultMessages;
    }

    private ServerConnectorConfig loadConfig() throws IOException {
        var filepath = this.dataDirectory.resolve(CONFIG_FILENAME);
        this.saveResourceIfNotExists(CONFIG_FILENAME, filepath);
        return ServerConnectorConfig.load(YamlConfigurationLoader.builder().path(filepath).build().load());
    }

    private void loadMessages() throws IOException {
        var directory = this.dataDirectory.resolve(LANGUAGES_DIRECTORY);
        Files.createDirectories(directory);

        for (var filename : BUNDLED_LANGUAGE_FILES) {
            this.saveResourceIfNotExists(filename, directory.resolve(filename));
        }

        var loaded = new HashMap<Locale, Messages>();

        try (var files = Files.list(directory)) {
            for (var filepath : files.filter(path -> path.getFileName().toString().endsWith(YAML_EXTENSION)).toList()) {
                var filename = filepath.getFileName().toString();
                var locale = Translator.parseLocale(filename.substring(0, filename.length() - YAML_EXTENSION.length()));

                if (locale == null) {
                    continue;
                }

                var messages = new Messages(YamlConfigurationLoader.builder().path(filepath).build().load());
                loaded.put(locale, messages);
                loaded.putIfAbsent(Locale.of(locale.getLanguage()), messages);
            }
        }

        this.localizedMessages = Map.copyOf(loaded);
        this.defaultMessages = loaded.getOrDefault(Locale.ENGLISH, Messages.defaults());
    }

    private void saveResourceIfNotExists(String resourceName, Path filepath) throws IOException {
        if (Files.isRegularFile(filepath)) {
            return;
        }

        try (var input = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(Objects.requireNonNull(input, () -> resourceName + " is not found in the jar"), filepath);
        }
    }

    private void registerSlashServerCommands() {
        for (var server : this.proxy.getAllServers()) {
            var command = new SlashServerCommand(this, server);
            command.register();
            this.registeredSlashServerCommands.add(command);
        }
    }

    private void unregisterSlashServerCommands() {
        this.registeredSlashServerCommands.forEach(SlashServerCommand::unregister);
        this.registeredSlashServerCommands.clear();
    }

    private void enableFirstJoinDetector() {
        if (this.config.sendFirstJoinMessage() && this.proxy.getPluginManager().isLoaded("luckperms")) {
            this.firstJoinListener = new FirstJoinListener(this, this.firstJoinPlayerHolder);
        }
    }
}
