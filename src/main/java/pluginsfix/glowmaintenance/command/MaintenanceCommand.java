package pluginsfix.glowmaintenance.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowmaintenance.service.MaintenanceService;
import pluginsfix.glowmaintenance.text.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MaintenanceCommand implements CommandExecutor, TabCompleter {
    private final MaintenanceService service;
    private final Messages messages;

    public MaintenanceCommand(MaintenanceService service, Messages messages) {
        this.service = Objects.requireNonNull(service);
        this.messages = Objects.requireNonNull(messages);
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (!sender.hasPermission("glowmaintenance.admin")) {
            messages.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            messages.send(sender, "usage");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "on", "enable" -> handleEnable(sender);
            case "off", "disable" -> handleDisable(sender);
            case "toggle" -> handleToggle(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            default -> messages.send(sender, "usage");
        }

        return true;
    }

    private void handleEnable(CommandSender sender) {
        if (service.isEnabled()) {
            messages.send(sender, "already-enabled");
            return;
        }
        service.setMaintenance(true);
        messages.send(sender, "enabled-success");
    }

    private void handleDisable(CommandSender sender) {
        if (!service.isEnabled()) {
            messages.send(sender, "already-disabled");
            return;
        }
        service.setMaintenance(false);
        messages.send(sender, "disabled-success");
    }

    private void handleToggle(CommandSender sender) {
        boolean newState = !service.isEnabled();
        service.setMaintenance(newState);
        if (newState) {
            messages.send(sender, "enabled-success");
        } else {
            messages.send(sender, "disabled-success");
        }
    }

    private void handleStatus(CommandSender sender) {
        if (service.isEnabled()) {
            messages.send(sender, "status-enabled");
        } else {
            messages.send(sender, "status-disabled");
        }
    }

    private void handleReload(CommandSender sender) {
        messages.reload();
        service.reloadConfig();
        messages.send(sender, "reloaded");
    }

    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (args.length == 1 && sender.hasPermission("glowmaintenance.admin")) {
            List<String> completions = new ArrayList<>();
            List<String> subcommands = List.of("on", "off", "toggle", "status", "reload");
            String prefix = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return Collections.emptyList();
    }
}