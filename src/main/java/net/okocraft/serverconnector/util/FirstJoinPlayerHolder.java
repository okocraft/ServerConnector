package net.okocraft.serverconnector.util;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FirstJoinPlayerHolder {

    private static final Set<UUID> FIRST_JOIN_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void add(@NotNull UUID uuid) {
        FIRST_JOIN_PLAYERS.add(uuid);
    }

    public static boolean remove(@NotNull UUID uuid) {
        return FIRST_JOIN_PLAYERS.remove(uuid);
    }
}
