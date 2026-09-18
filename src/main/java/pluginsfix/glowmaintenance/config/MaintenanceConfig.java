package pluginsfix.glowmaintenance.config;

import org.bukkit.configuration.file.FileConfiguration;

public record MaintenanceConfig(
    boolean enabled,
    boolean autoWhitelist,
    boolean kickOnlinePlayers,
    String bypassPermission,
    boolean motdEnabled,
    String motdLine1,
    String motdLine2
) {
    public static MaintenanceConfig fromConfiguration(FileConfiguration config) {
        boolean enabled = config.getBoolean("maintenance.enabled");
        boolean autoWhitelist = config.getBoolean("maintenance.auto-whitelist");
        boolean kickOnlinePlayers = config.getBoolean("maintenance.kick-online-players");
        String bypassPermission = config.getString("maintenance.bypass-permission");
        if (bypassPermission == null || bypassPermission.isBlank()) {
            bypassPermission = "glowmaintenance.bypass";
        }
        boolean motdEnabled = config.getBoolean("maintenance.motd.enabled");
        String motdLine1 = config.getString("maintenance.motd.line-1");
        if (motdLine1 == null) {
            motdLine1 = "";
        }
        String motdLine2 = config.getString("maintenance.motd.line-2");
        if (motdLine2 == null) {
            motdLine2 = "";
        }
        return new MaintenanceConfig(
            enabled,
            autoWhitelist,
            kickOnlinePlayers,
            bypassPermission,
            motdEnabled,
            motdLine1,
            motdLine2
        );
    }

    public MaintenanceConfig withEnabled(boolean newEnabled) {
        return new MaintenanceConfig(
            newEnabled,
            autoWhitelist,
            kickOnlinePlayers,
            bypassPermission,
            motdEnabled,
            motdLine1,
            motdLine2
        );
    }
}