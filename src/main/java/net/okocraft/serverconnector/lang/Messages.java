package net.okocraft.serverconnector.lang;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

public final class Messages {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Component proxyNoPermission;
    private final String proxyJoin;
    private final String proxyFirstJoin;
    private final String proxyLeave;
    private final String serverNoPermission;
    private final String serverKick;
    private final String serverSwitch;
    private final Component slashServerNoPermission;
    private final Component slashServerAlreadyConnected;
    private final String slashServerConnecting;
    private final String slashServerConnectingOther;
    private final String slashServerPlayerNotFound;

    public Messages(ConfigurationNode source) {
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

    /**
     * Creates {@link Messages} that only contains the built-in default messages.
     *
     * @return the default {@link Messages}
     */
    public static Messages defaults() {
        return new Messages(BasicConfigurationNode.root());
    }

    public Component proxyNoPermission() {
        return this.proxyNoPermission;
    }

    public Component proxyJoin(Player player) {
        return format(this.proxyJoin, player(player));
    }

    public Component firstJoin(Player player) {
        return format(this.proxyFirstJoin, player(player));
    }

    public Component proxyLeave(Player player) {
        return format(this.proxyLeave, player(player));
    }

    public Component serverNoPermission(String serverName) {
        return format(this.serverNoPermission, server(serverName));
    }

    public Component serverKick(String serverName, Component reason) {
        return format(this.serverKick, server(serverName), Placeholder.component("reason", reason));
    }

    public Component serverSwitch(Player player, String serverName) {
        return format(this.serverSwitch, player(player), server(serverName));
    }

    public Component slashServerNoPermission() {
        return this.slashServerNoPermission;
    }

    public Component slashServerAlreadyConnected() {
        return this.slashServerAlreadyConnected;
    }

    public Component slashServerConnecting(String serverName) {
        return format(this.slashServerConnecting, server(serverName));
    }

    public Component slashServerConnectingOther(Player player, String serverName) {
        return format(this.slashServerConnectingOther, player(player), server(serverName));
    }

    public Component slashServerPlayerNotFound(String arg) {
        return format(this.slashServerPlayerNotFound, Placeholder.unparsed("arg", arg));
    }

    private static Component preFormatted(ConfigurationNode source, String def, Object... keys) {
        return MINI_MESSAGE.deserialize(getMessage(source, def, keys));
    }

    private static Component format(String input, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(input, resolvers);
    }

    private static String getMessage(ConfigurationNode source, String def, Object... keys) {
        return source.node(keys).getString(def);
    }

    private static TagResolver player(Player player) {
        return Placeholder.component("player", Component.text().content(player.getUsername()).hoverEvent(player));
    }

    private static TagResolver server(String serverName) {
        return Placeholder.unparsed("server", serverName);
    }
}
