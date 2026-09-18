package pluginsfix.glowmaintenance.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaintenanceConfigTest {

    @Test
    void shouldParseDefaultConfigurationCorrectly() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("maintenance.enabled", true);
        yaml.set("maintenance.auto-whitelist", true);
        yaml.set("maintenance.kick-online-players", true);
        yaml.set("maintenance.bypass-permission", "glowmaintenance.bypass");
        yaml.set("maintenance.motd.enabled", true);
        yaml.set("maintenance.motd.line-1", "&#FB8808Line 1");
        yaml.set("maintenance.motd.line-2", "&fLine 2");

        MaintenanceConfig config = MaintenanceConfig.fromConfiguration(yaml);

        assertThat(config.enabled()).isTrue();
        assertThat(config.autoWhitelist()).isTrue();
        assertThat(config.kickOnlinePlayers()).isTrue();
        assertThat(config.bypassPermission()).isEqualTo("glowmaintenance.bypass");
        assertThat(config.motdEnabled()).isTrue();
        assertThat(config.motdLine1()).isEqualTo("&#FB8808Line 1");
        assertThat(config.motdLine2()).isEqualTo("&fLine 2");
    }

    @Test
    void shouldToggleEnabledStateImmutably() {
        MaintenanceConfig initial = new MaintenanceConfig(
            false,
            true,
            true,
            "glowmaintenance.bypass",
            true,
            "1",
            "2"
        );

        MaintenanceConfig toggled = initial.withEnabled(true);

        assertThat(initial.enabled()).isFalse();
        assertThat(toggled.enabled()).isTrue();
        assertThat(toggled.bypassPermission()).isEqualTo(initial.bypassPermission());
    }
}