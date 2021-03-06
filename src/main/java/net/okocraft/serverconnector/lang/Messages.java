package net.okocraft.serverconnector.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public final class Messages {

    private static final TranslatableComponent PREFIX = translatable("serverconnector.prefix", GRAY);

    public static final BiFunction<String, String, Component> NO_PERMISSION_TO_CONNECT_TO_SERVER =
            (serverName, permission) ->
                    PREFIX.toBuilder()
                            .append(translatable()
                                    .key("serverconnector.server.no-permission")
                                    .args(text(serverName, AQUA), text(permission, AQUA))
                                    .color(RED)
                                    .build()
                            )
                            .build();

    public static final BiFunction<String, Component, Component> KICKED_FROM_SERVER =
            (serverName, reason) ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.server.kick")
                                            .args(text(serverName, AQUA), reason)
                                            .color(RED)
                                            .build()
                            )
                            .build();

    public static final BiFunction<String, String, Component> SWITCH_SERVER =
            (playerName, serverName) ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.server.switch")
                                            .args(text(playerName, AQUA), text(serverName, AQUA))
                                            .color(GRAY)
                                            .build()
                            )
                            .build();

    public static final Function<String, Component> JOIN_PROXY =
            playerName ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.proxy.join")
                                            .args(text(playerName, AQUA))
                                            .color(GRAY)
                                            .build()
                            )
                            .build();

    public static final Function<String, Component> LEFT_PROXY =
            playerName ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.proxy.leave")
                                            .args(text(playerName, AQUA))
                                            .color(GRAY)
                                            .build()
                            )
                            .build();

    public static final Component NO_PERMISSION_TO_CONNECT_TO_PROXY =
            PREFIX.toBuilder().append(
                    translatable()
                            .key("serverconnector.proxy.no-permission")
                            .color(WHITE)
                            .build()
            ).build();

    public static final Component SLASH_SERVER_ONLY_PLAYER =
            PREFIX.toBuilder().append(translatable("serverconnector.only-player", RED)).build();

    public static final Function<String, Component> SLASH_SERVER_NO_PERMISSION =
            permission ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.command.slash-server.no-permission")
                                            .args(text(permission, AQUA))
                                            .color(RED)
                            )
                            .build();

    public static final Component SLASH_SERVER_ALREADY_CONNECTED =
            PREFIX.toBuilder().append(translatable("serverconnector.command.slash-server.already-connected", RED)).build();

    public static final Function<String, Component> SLASH_SERVER_COULD_NOT_CONNECT =
            serverName ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.command.slash-server.could-not-connect")
                                            .args(text(serverName, AQUA))
                                            .color(RED)
                            )
                            .build();

    public static final Function<String, Component> SLASH_SERVER_CONNECTING =
            serverName ->
                    PREFIX.toBuilder()
                            .append(
                                    translatable()
                                            .key("serverconnector.command.slash-server.connecting")
                                            .args(text(serverName, AQUA))
                                            .color(GRAY)
                                            .build()
                            )
                            .build();

    private Messages() {
        throw new UnsupportedOperationException();
    }
}
