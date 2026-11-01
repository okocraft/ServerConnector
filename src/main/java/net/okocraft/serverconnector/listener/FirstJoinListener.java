package net.okocraft.serverconnector.listener;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserFirstLoginEvent;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;

public final class FirstJoinListener {

    private final EventSubscription<UserFirstLoginEvent> subscription;

    public FirstJoinListener(ServerConnectorPlugin plugin, FirstJoinPlayerHolder holder) {
        this.subscription = LuckPermsProvider.get()
                .getEventBus()
                .subscribe(plugin, UserFirstLoginEvent.class, event -> holder.add(event.getUniqueId()));
    }

    public void unsubscribe() {
        this.subscription.close();
    }
}
