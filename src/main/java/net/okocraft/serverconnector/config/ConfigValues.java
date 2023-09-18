package net.okocraft.serverconnector.config;

import com.github.siroshun09.configapi.api.value.ConfigValue;

import java.util.function.Function;

public final class ConfigValues {

    public static final ConfigValue<String> SERVER_TO_SEND = c -> c.getString("server-to-send-when-kicked", "hub");

    public static final ConfigValue<Boolean> SEND_JOIN_MESSAGE = c -> c.getBoolean("message-sending-setting.join", true);

    public static final ConfigValue<Boolean> SEND_LEAVE_MESSAGE = c -> c.getBoolean("message-sending-setting.leave", true);

    public static final ConfigValue<Boolean> SEND_SWITCH_MESSAGE = c -> c.getBoolean("message-sending-setting.switch", true);

    public static final ConfigValue<Boolean> SEND_FIRST_JOIN_MESSAGE = c -> c.getBoolean("message-sending-setting.first-join", true);

    public static final ConfigValue<Boolean> SERVER_PERMISSION_ENABLE = c -> c.getBoolean("server-permission.enable", true);

    public static final Function<String, ConfigValue<String>> SERVER_CUSTOM_PERMISSION = serverName -> c -> c.getString("server-permission." + serverName);

    public static final ConfigValue<String> PROXY_PERMISSION = SERVER_CUSTOM_PERMISSION.apply("proxy");
}
