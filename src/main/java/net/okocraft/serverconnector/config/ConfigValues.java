package net.okocraft.serverconnector.config;

import com.github.siroshun09.configapi.common.configurable.Configurable;
import com.github.siroshun09.configapi.common.configurable.StringValue;

public  final class ConfigValues {

    public static final StringValue SERVER_TO_SEND = Configurable.create("server-to-send-when-kicked", "hub");
}
