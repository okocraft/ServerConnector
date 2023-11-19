package net.okocraft.serverconnector.config;

import java.util.HashMap;
import java.util.Map;

public class ServerConnectorConfig {

    public boolean sendJoinMessage = true;
    public boolean sendFirstJoinMessage = true;
    public boolean sendLeaveMessage = true;
    public boolean sendSwitchMessage = true;

    public boolean enableServerPermission = false;
    public String proxyPermission = null;
    public final Map<String, String> serverPermissionMap = new HashMap<>();

    public String fallbackServer = null;

}
