package net.okocraft.serverconnector.listener;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserFirstLoginEvent;
import net.okocraft.serverconnector.ServerConnectorPlugin;
import net.okocraft.serverconnector.util.FirstJoinPlayerHolder;
import org.jetbrains.annotations.NotNull;

public class FirstJoinListener {

    private final EventSubscription<UserFirstLoginEvent> listener;

    public FirstJoinListener(@NotNull ServerConnectorPlugin plugin) {
        this.listener =
                LuckPermsProvider.get()
                        .getEventBus()
                        .subscribe(
                                plugin,
                                UserFirstLoginEvent.class,
                                event -> FirstJoinPlayerHolder.add(event.getUniqueId())
                        );
    }

    public void unsubscribe() {
        listener.close();
    }
}
