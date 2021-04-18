package net.okocraft.serverconnector.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import org.jetbrains.annotations.NotNull;

public final class AudienceUtil {

    private static BungeeAudiences AUDIENCES = null;

    private AudienceUtil() {
        throw new UnsupportedOperationException();
    }

    public static void init(@NotNull ServerConnectorPlugin plugin) {
        AUDIENCES = BungeeAudiences.create(plugin);
    }

    public static @NotNull Audience sender(@NotNull CommandSender sender) {
        if (AUDIENCES != null) {
            return AUDIENCES.sender(sender);
        } else {
            throw new IllegalStateException();
        }
    }

    public static @NotNull Audience player(@NotNull ProxiedPlayer player) {
        if (AUDIENCES != null) {
            return AUDIENCES.player(player);
        } else {
            throw new IllegalStateException();
        }
    }

    public static @NotNull Audience all() {
        if (AUDIENCES != null) {
            return AUDIENCES.all();
        } else {
            throw new IllegalStateException();
        }
    }
}
