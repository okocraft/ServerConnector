package net.okocraft.serverconnector.lang;

import com.github.siroshun09.configapi.common.util.FileUtils;
import com.github.siroshun09.configapi.common.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.mcmessage.loader.MessageLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class LanguageLoader {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String DIRECTORY_NAME = "languages";

    private static TranslationRegistry REGISTRY;

    private LanguageLoader() {
        throw new UnsupportedOperationException();
    }

    public static void load(@NotNull ServerConnectorPlugin plugin) throws IOException {
        var directory = plugin.getDataFolder().toPath().resolve(DIRECTORY_NAME);

        FileUtils.createDirectoriesIfNotExists(directory);

        REGISTRY = TranslationRegistry.create(Key.key("serverconnector", "language"));

        saveDefaultIfNotExists(plugin, directory);

        loadDefault(plugin, directory);
        loadCustom(directory, plugin.getLogger());

        REGISTRY.defaultLocale(DEFAULT_LOCALE);
        GlobalTranslator.get().addSource(REGISTRY);
    }

    public static void reload(@NotNull ServerConnectorPlugin plugin) throws IOException {
        GlobalTranslator.get().removeSource(REGISTRY);
        load(plugin);
    }

    private static void saveDefaultIfNotExists(@NotNull ServerConnectorPlugin plugin, @NotNull Path directory) throws IOException {
        var defaultFileName = DEFAULT_LOCALE.toString() + ".yml";
        var defaultFile = directory.resolve(defaultFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(
                plugin.getClass().getClassLoader(),
                DIRECTORY_NAME + '/' + defaultFileName,
                defaultFile
        );

        var japaneseFileName = Locale.JAPAN.toString() + ".yml";
        var japaneseFile = directory.resolve(japaneseFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(
                plugin.getClass().getClassLoader(),
                DIRECTORY_NAME + '/' + japaneseFileName,
                japaneseFile
        );
    }

    private static void loadDefault(@NotNull ServerConnectorPlugin plugin, @NotNull Path directory) throws IOException {
        var defaultFileName = DEFAULT_LOCALE.toString() + ".yml";
        var defaultFile = directory.resolve(defaultFileName);

        if (!Files.exists(defaultFile)) {
            throw new IllegalStateException();
        }

        loadFile(defaultFile, plugin.getLogger());
    }

    private static void loadCustom(@NotNull Path directory, @NotNull Logger logger) throws IOException {
        try (var files = Files.list(directory)) {
            var languages = files.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().endsWith(DEFAULT_LOCALE.toString() + ".yml"))
                    .collect(Collectors.toUnmodifiableSet());

            for (var language : languages) {
                loadFile(language, logger);
            }
        }
    }

    private static void loadFile(@NotNull Path yamlPath, @NotNull Logger logger) throws IOException {
        var yaml = YamlConfiguration.create(yamlPath);
        var loader = MessageLoader.fromFileConfiguration(yaml);

        loader.load();
        loader.registerToRegistry(REGISTRY);

        logger.info("Loaded " + yamlPath.getFileName().toString());
    }
}