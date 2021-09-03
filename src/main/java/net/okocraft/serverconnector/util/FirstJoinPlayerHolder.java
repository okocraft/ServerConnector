package net.okocraft.serverconnector.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FirstJoinPlayerHolder {

    private static final List<UUID> FIRST_JOIN_PLAYERS = new ArrayList<>();

    public static void add(@NotNull UUID uuid) {
        FIRST_JOIN_PLAYERS.add(uuid);
    }

    public static boolean remove(@NotNull UUID uuid) {
        return FIRST_JOIN_PLAYERS.remove(uuid);
    }
}
