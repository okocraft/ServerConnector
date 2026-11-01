package net.okocraft.serverconnector.util;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FirstJoinPlayerHolder {

    private final Set<UUID> firstJoinPlayers = ConcurrentHashMap.newKeySet();

    public void add(UUID uuid) {
        this.firstJoinPlayers.add(uuid);
    }

    public boolean remove(UUID uuid) {
        return this.firstJoinPlayers.remove(uuid);
    }

    public void clear() {
        this.firstJoinPlayers.clear();
    }
}
