package net.okocraft.serverconnector.config;

import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;

public record ServerConnectorConfig(
        boolean sendJoinMessage,
        boolean sendFirstJoinMessage,
        boolean sendLeaveMessage,
        boolean sendSwitchMessage,
        boolean enableServerPermission,
        @Nullable String proxyPermission,
        Map<String, String> serverPermissionMap,
        @Nullable String fallbackServer
) {

    private static final String DEFAULT_PERMISSION_KEY = "default";
    private static final String SERVER_NAME_PLACEHOLDER = "%server_name%";

    public ServerConnectorConfig {
        serverPermissionMap = Map.copyOf(serverPermissionMap);
    }

    public static ServerConnectorConfig load(ConfigurationNode node) {
        var messageSendingSetting = node.node("message-sending-setting");
        var serverPermission = node.node("server-permission");

        boolean enableServerPermission = false;
        String proxyPermission = null;
        var serverPermissionMap = new HashMap<String, String>();

        for (var entry : serverPermission.childrenMap().entrySet()) {
            var key = String.valueOf(entry.getKey());
            switch (key) {
                case "enable" -> enableServerPermission = entry.getValue().getBoolean();
                case "proxy" -> proxyPermission = nonBlankOrNull(entry.getValue().getString());
                default -> {
                    var permission = nonBlankOrNull(entry.getValue().getString());
                    if (permission != null) {
                        serverPermissionMap.put(key, permission);
                    }
                }
            }
        }

        return new ServerConnectorConfig(
                messageSendingSetting.node("join").getBoolean(),
                messageSendingSetting.node("first-join").getBoolean(),
                messageSendingSetting.node("leave").getBoolean(),
                messageSendingSetting.node("switch").getBoolean(),
                enableServerPermission,
                proxyPermission,
                serverPermissionMap,
                nonBlankOrNull(node.node("server-to-send-when-kicked").getString())
        );
    }

    /**
     * Gets the permission node required to connect to the specified server.
     *
     * @param serverName the name of the server
     * @return the permission node, or {@code null} if no permission is required
     */
    public @Nullable String serverPermission(String serverName) {
        var customPermission = this.serverPermissionMap.get(serverName);

        if (customPermission != null) {
            return customPermission;
        }

        var defaultPermission = this.serverPermissionMap.get(DEFAULT_PERMISSION_KEY);
        return defaultPermission != null ? defaultPermission.replace(SERVER_NAME_PLACEHOLDER, serverName) : null;
    }

    private static @Nullable String nonBlankOrNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
