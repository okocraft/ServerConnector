package net.okocraft.serverconnector.config;

import com.github.siroshun09.configapi.common.value.ConfigValue;

public  final class ConfigValues {

    public static final ConfigValue<String> SERVER_TO_SEND = c -> c.getString("server-to-send-when-kicked", "hub");
}
