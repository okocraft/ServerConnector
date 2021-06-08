package net.okocraft.serverconnector.lang;

import com.github.siroshun09.adventureextender.loader.translation.YamlTranslationLoader;
import com.github.siroshun09.configapi.common.util.ResourceUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.TranslationRegistry;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;

public final class LanguageLoader extends YamlTranslationLoader {

    private static final String DIRECTORY_NAME = "languages";

    private final ServerConnectorPlugin plugin;

    public LanguageLoader(@NotNull ServerConnectorPlugin plugin) {
        super(
                plugin.getDataFolder().toPath().resolve(DIRECTORY_NAME),
                Locale.ENGLISH
        );
        this.plugin = plugin;
    }

    @Override
    protected void saveDefaultIfNotExists() throws IOException {
        var defaultFileName = getDefaultLocale() + ".yml";
        var defaultFile = getDirectory().resolve(defaultFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(
                plugin.getClass().getClassLoader(),
                DIRECTORY_NAME + '/' + defaultFileName,
                defaultFile
        );

        var japaneseFileName = Locale.JAPAN + ".yml";
        var japaneseFile = getDirectory().resolve(japaneseFileName);

        ResourceUtils.copyFromClassLoaderIfNotExists(
                plugin.getClass().getClassLoader(),
                DIRECTORY_NAME + '/' + japaneseFileName,
                japaneseFile
        );
    }

    @Override
    protected @NotNull TranslationRegistry createRegistry() {
        var key = Key.key("serverconnector", "language");
        return TranslationRegistry.create(key);
    }
}