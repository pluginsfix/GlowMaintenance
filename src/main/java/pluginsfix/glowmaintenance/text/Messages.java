package pluginsfix.glowmaintenance.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Messages {
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(?:#&|&#)([0-9a-fA-F]{6})");
    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\[([a-zA-Z0-9_-]+)\\]\\s?(.*)$");
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();

    private final Plugin plugin;
    private final Path filePath;
    private final Logger logger;
    private final Map<String, List<String>> entries = new HashMap<>();

    public Messages(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.filePath = plugin.getDataFolder().toPath().resolve("messages.yml");
        this.logger = plugin.getLogger();
        reload();
    }

    public void reload() {
        if (!Files.exists(filePath)) {
            plugin.saveResource("messages.yml", false);
        }
        entries.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(filePath.toFile());
        for (String key : config.getKeys(true)) {
            if (config.isList(key)) {
                entries.put(key, config.getStringList(key));
            } else if (config.isString(key)) {
                entries.put(key, List.of(config.getString(key, "")));
            }
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Collections.emptyMap());
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        List<String> lines = entries.get(key);
        if (lines == null || lines.isEmpty()) {
            logger.warning("Missing message key in messages.yml: " + key);
            return;
        }

        for (String line : lines) {
            executeLine(sender, line, placeholders);
        }
    }

    public Component component(String key) {
        return component(key, Collections.emptyMap());
    }

    public Component component(String key, Map<String, String> placeholders) {
        List<String> lines = entries.get(key);
        if (lines == null || lines.isEmpty()) {
            logger.warning("Missing message key in messages.yml: " + key);
            return Component.empty();
        }

        List<Component> parts = new ArrayList<>(lines.size());
        for (String line : lines) {
            String clean = extractContent(line);
            parts.add(format(clean, placeholders));
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }

        Component combined = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            combined = combined.append(Component.newline()).append(parts.get(i));
        }
        return combined;
    }

    public Component format(String raw) {
        return format(raw, Collections.emptyMap());
    }

    public Component format(String raw, Map<String, String> placeholders) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }

        String text = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            text = text.replace("%" + entry.getKey() + "%", entry.getValue());
            text = text.replace("<" + entry.getKey() + ">", entry.getValue());
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder b = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                b.append('&').append(c);
            }
            matcher.appendReplacement(sb, b.toString());
        }
        matcher.appendTail(sb);

        return SERIALIZER.deserialize(sb.toString());
    }

    private void executeLine(CommandSender sender, String line, Map<String, String> placeholders) {
        Matcher matcher = ACTION_PATTERN.matcher(line);
        String action = "message";
        String payload = line;

        if (matcher.matches()) {
            action = matcher.group(1).toLowerCase();
            payload = matcher.group(2);
        }

        switch (action) {
            case "message" -> {
                if (sender != null) {
                    sender.sendMessage(format(payload, placeholders));
                }
            }
            case "broadcast" -> {
                Component comp = format(payload, placeholders);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(comp);
                }
                Bukkit.getConsoleSender().sendMessage(comp);
            }
            case "sound" -> {
                if (sender instanceof Player player) {
                    playSound(player, payload);
                }
            }
            case "actionbar" -> {
                if (sender instanceof Player player) {
                    player.sendActionBar(format(payload, placeholders));
                }
            }
            case "title" -> {
                if (sender instanceof Player player) {
                    showTitle(player, payload, placeholders);
                }
            }
            default -> {
                if (sender != null) {
                    sender.sendMessage(format(line, placeholders));
                }
            }
        }
    }

    private String extractContent(String line) {
        Matcher matcher = ACTION_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return line;
    }

    private void playSound(Player player, String payload) {
        String[] parts = payload.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(parts[0].toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void showTitle(Player player, String payload, Map<String, String> placeholders) {
        String[] parts = payload.split(";", 2);
        Component mainTitle = format(parts[0], placeholders);
        Component subTitle = parts.length > 1 ? format(parts[1], placeholders) : Component.empty();
        player.showTitle(Title.title(mainTitle, subTitle));
    }
}