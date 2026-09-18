package pluginsfix.glowmaintenance.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import pluginsfix.glowmaintenance.service.MaintenanceService;
import pluginsfix.glowmaintenance.text.Messages;

import java.util.Objects;

public final class MaintenanceListener implements Listener {
    private final MaintenanceService service;
    private final Messages messages;

    public MaintenanceListener(MaintenanceService service, Messages messages) {
        this.service = Objects.requireNonNull(service);
        this.messages = Objects.requireNonNull(messages);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!service.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (service.hasBypass(player)) {
            event.allow();
            return;
        }

        Component kickReason = messages.component("kick-screen");
        event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, kickReason);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent event) {
        if (!service.isEnabled()) {
            return;
        }

        if (!service.getConfig().motdEnabled()) {
            return;
        }

        Component line1 = messages.format(service.getConfig().motdLine1());
        Component line2 = messages.format(service.getConfig().motdLine2());
        Component combined = line1.append(Component.newline()).append(line2);

        event.motd(combined);
    }
}