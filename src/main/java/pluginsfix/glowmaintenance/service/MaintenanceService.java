package pluginsfix.glowmaintenance.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowmaintenance.config.MaintenanceConfig;
import pluginsfix.glowmaintenance.text.Messages;

import java.util.Objects;

public final class MaintenanceService {
    private final Plugin plugin;
    private final Messages messages;
    private MaintenanceConfig config;

    public MaintenanceService(Plugin plugin, Messages messages) {
        this.plugin = Objects.requireNonNull(plugin);
        this.messages = Objects.requireNonNull(messages);
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = MaintenanceConfig.fromConfiguration(plugin.getConfig());
        if (this.config.enabled() && this.config.autoWhitelist()) {
            Bukkit.setWhitelist(true);
        }
    }

    public boolean isEnabled() {
        return config.enabled();
    }

    public MaintenanceConfig getConfig() {
        return config;
    }

    public void setMaintenance(boolean enabled) {
        this.config = this.config.withEnabled(enabled);
        plugin.getConfig().set("maintenance.enabled", enabled);
        plugin.saveConfig();

        if (config.autoWhitelist()) {
            Bukkit.setWhitelist(enabled);
        }

        if (enabled) {
            if (config.kickOnlinePlayers()) {
                kickNonBypassPlayers();
            }
            messages.send(null, "broadcast-enabled");
        } else {
            messages.send(null, "broadcast-disabled");
        }
    }

    public void kickNonBypassPlayers() {
        Component kickMessage = messages.component("kick-screen");
        String bypassPermission = config.bypassPermission();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(bypassPermission) || player.isOp()) {
                continue;
            }
            player.kick(kickMessage);
        }
    }

    public boolean hasBypass(Player player) {
        return player.isOp() || player.hasPermission(config.bypassPermission());
    }
}