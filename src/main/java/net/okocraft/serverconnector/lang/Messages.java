package net.okocraft.serverconnector.lang;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;

public final class Messages {

    public final Component proxyNoPermission;
    private final String proxyJoin;
    private final String proxyFirstJoin;
    private final String proxyLeave;
    private final String serverNoPermission;
    private final String serverKick;
    private final String serverSwitch;
    public final Component slashServerNoPermission;
    public final Component slashServerAlreadyConnected;
    private final String slashServerConnecting;
    private final String slashServerConnectingOther;
    private final String slashServerPlayerNotFound;

    public Messages(@NotNull ConfigurationNode source) {
        this.proxyNoPermission = preFormatted(source, "<red>You don't have the permission to connect to the proxy.", "proxy", "no-permission");
        this.proxyJoin = getMessage(source, "<aqua><player><gray> joined the proxy.", "proxy", "join");
        this.proxyFirstJoin = getMessage(source, "<aqua><player><gray> is logged in for the first time. <yellow>Welcome to the server!", "proxy", "first-join");
        this.proxyLeave = getMessage(source, "<aqua><player><gray> left the proxy.", "proxy", "leave");

        this.serverNoPermission = getMessage(source, "<red>You don't have the permission to connect to the server <aqua><server><red>.", "server", "no-permission");
        this.serverKick = getMessage(source, "<red>You have been kicked from the server <aqua><server><red>: <reset><reason>", "server", "kick");
        this.serverSwitch = getMessage(source, "<aqua><player><gray> has been moved to the server <aqua><server><gray>.", "server", "switch");

        this.slashServerNoPermission = preFormatted(source, "<red>You don't have the permission to execute this command.", "slash-server", "no-permission");
        this.slashServerAlreadyConnected = preFormatted(source, "<red>You are already connected to the same server.", "slash-server", "already-connected");
        this.slashServerConnecting = getMessage(source, "<gray>Connecting to the server <aqua><server><gray>...", "slash-server", "connecting");
        this.slashServerConnectingOther = getMessage(source, "<gray>Connecting <aqua><player><gray> to the server <aqua><server><gray>...", "slash-server", "connecting-other");
        this.slashServerPlayerNotFound = getMessage(source, "<gray>The player <aqua><arg><gray> could not be found.", "slash-server", "player-not-found");
    }

    public @NotNull Component proxyJoin(@NotNull Player player) {
        return format(this.proxyJoin, player(player));
    }

    public @NotNull Component firstJoin(@NotNull Player player) {
        return format(this.proxyFirstJoin, player(player));
    }

    public @NotNull Component proxyLeave(@NotNull Player player) {
        return format(this.proxyLeave, player(player));
    }

    public @NotNull Component serverNoPermission(@NotNull String serverName) {
        return format(this.serverNoPermission, server(serverName));
    }

    public @NotNull Component serverKick(@NotNull String serverName, @NotNull Component reason) {
        return format(this.serverKick, server(serverName), Placeholder.component("reason", reason));
    }

    public @NotNull Component serverSwitch(@NotNull Player player, @NotNull String serverName) {
        return format(this.serverSwitch, player(player), server(serverName));
    }

    public @NotNull Component slashServerConnecting(@NotNull String serverName) {
        return format(this.slashServerConnecting, server(serverName));
    }

    public @NotNull Component slashServerConnectingOther(@NotNull Player player, @NotNull String serverName) {
        return format(this.slashServerConnectingOther, player(player), server(serverName));
    }

    public @NotNull Component slashServerPlayerNotFound(@NotNull String arg) {
        return format(this.slashServerPlayerNotFound, Placeholder.component("arg", Component.text(arg)));
    }

    private static @NotNull Component preFormatted(@NotNull ConfigurationNode source, @NotNull String def, @NotNull Object @NotNull ... keys) {
        return MiniMessage.miniMessage().deserialize(getMessage(source, def, keys));
    }

    private static @NotNull Component format(@NotNull String input, @NotNull TagResolver resolver) {
        return MiniMessage.miniMessage().deserialize(input, resolver);
    }

    private static @NotNull Component format(@NotNull String input, @NotNull TagResolver @NotNull ... resolvers) {
        return MiniMessage.miniMessage().deserialize(input, resolvers);
    }

    private static @NotNull String getMessage(@NotNull ConfigurationNode source, @NotNull String def, @NotNull Object @NotNull ... keys) {
        return source.getNode(keys).getString(def);
    }

    private static @NotNull TagResolver player(@NotNull Player player) {
        return Placeholder.component("player", Component.text().content(player.getUsername()).hoverEvent(player).build());
    }

    private static @NotNull TagResolver server(@NotNull String serverName) {
        return Placeholder.component("server", Component.text(serverName));
    }
}
