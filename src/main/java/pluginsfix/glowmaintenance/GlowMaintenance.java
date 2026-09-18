package pluginsfix.glowmaintenance;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.glowmaintenance.command.MaintenanceCommand;
import pluginsfix.glowmaintenance.listener.MaintenanceListener;
import pluginsfix.glowmaintenance.service.MaintenanceService;
import pluginsfix.glowmaintenance.text.Messages;

public final class GlowMaintenance extends JavaPlugin {
    private Messages messages;
    private MaintenanceService service;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.messages = new Messages(this);
        this.service = new MaintenanceService(this, messages);

        getServer().getPluginManager().registerEvents(new MaintenanceListener(service, messages), this);

        PluginCommand command = getCommand("maintenance");
        if (command != null) {
            MaintenanceCommand executor = new MaintenanceCommand(service, messages);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    @Override
    public void onDisable() {
        this.service = null;
        this.messages = null;
    }
}