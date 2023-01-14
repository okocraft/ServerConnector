package net.okocraft.serverconnector.config;

import com.github.siroshun09.configapi.api.value.ConfigValue;

public  final class ConfigValues {

    public static final ConfigValue<String> SERVER_TO_SEND = c -> c.getString("server-to-send-when-kicked", "hub");

    public static final ConfigValue<Boolean> SEND_JOIN_MESSAGE = c -> c.getBoolean("message-sending-setting.join", true);

    public static final ConfigValue<Boolean> SEND_LEAVE_MESSAGE = c -> c.getBoolean("message-sending-setting.leave", true);

    public static final ConfigValue<Boolean> SEND_SWITCH_MESSAGE = c -> c.getBoolean("message-sending-setting.switch", true);

    public static final ConfigValue<Boolean> SEND_FIRST_JOIN_MESSAGE = c -> c.getBoolean("message-sending-setting.first-join", true);

    public static final ConfigValue<Boolean> ENABLE_SNAPSHOT_SERVER = c -> c.getBoolean("snapshot.enable");

    public static final ConfigValue<String> SNAPSHOT_SERVER = c -> c.getString("snapshot.server-name", "snapshot");

    public static final ConfigValue<Integer> SNAPSHOT_PROTOCOL_VERSION = c -> c.getInteger("snapshot.protocol-version");
}
