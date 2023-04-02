package com.extendedclip.papi.expansion.server;

import me.clip.placeholderapi.PlaceholderAPIPlugin;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ServerUtils {
    @NotNull private static final Map<String, String> variants = new HashMap<>();

    static {
        variants.put("org.spigotmc.SpigotConfig", "Spigot");
        variants.put("io.papermc.paper.configuration.ConfigurationLoaders", "Paper"); // New config location for Paper 1.19+
        variants.put("com.destroystokyo.paper.PaperConfig", "Paper"); // Still supported by Paper, but deprecated.
        variants.put("com.tuinity.tuinity.config.TuinityConfig", "Tuinity");
        variants.put("gg.airplane.AirplaneConfig", "Airplane");
        variants.put("net.pl3x.purpur.PurpurConfig", "Purpur");
    }

    private String version = null;
    private String build = null;
    private String variant = null;
    
    private Object craftServer = null;
    private Field tps = null;
    
    private boolean hasTpsMethod = false;


    @SuppressWarnings("unused")
    public ServerUtils() {
        // Resolve TPS handler
        try {
            // If this throws is the server not a fork...
            Bukkit.class.getMethod("getTPS");
            hasTpsMethod = true;
        } catch (final NoSuchMethodException ignored) {
            try {
                if (getMajorVersion() >= 17) {
                    craftServer = Class.forName("net.minecraft.server.MinecraftServer")
                            .getMethod("getServer").invoke(null);
                    return;
                }

                craftServer = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".MinecraftServer")
                        .getMethod("getServer").invoke(null);
                tps = craftServer.getClass().getField("recentTps");
            } catch (final ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
                PlaceholderAPIPlugin.getInstance().getLogger().warning("Could not resolve TPS handling!");
                e.printStackTrace();
            }
        }
    }

    private int getMajorVersion() {
        final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?\\)").matcher(Bukkit.getVersion());
        if (!matcher.find()) return -1;
        try {
            return Integer.parseInt(matcher.toMatchResult().group(2), 10);
        } catch (final NumberFormatException ignored2) {
            return -1;
        }
    }

    @NotNull
    public String getServerVariant() {
        if (variant != null) return variant;
        variants.forEach((key, value) -> {
            try {
                Class.forName(key);
                variant = value;
            } catch (final ClassNotFoundException ignored) {
                // ignored
            }
        });
        if (variant == null) variant = "Unknown";
        return variant;
    }

    @NotNull
    public String getVersion() {
        if (version != null) return version;
        version = Bukkit.getBukkitVersion().split("-")[0];
        return version;
    }

    @NotNull
    public String getBuild() {
        if (build != null) return build;
        final String[] split = Bukkit.getVersion().split("-");
        switch (getServerVariant().toLowerCase(Locale.ROOT)) {
            // TODO Find out what those variants return.
            case "spigot":
            case "purpur": {
                build = split[0];
                break;
            }

            case "paper": {
                if (split.length >= 3) {
                    if (split[2].contains(" ")) {
                        build = split[2].substring(0, split[2].indexOf(" "));
                        break;
                    }
                    build = split[2];
                    break;
                }
                build = "Unknown";
                break;
            }

            default: build = "Unknown";
        }
        return build;
    }
    
    public double[] getTps() {
        if (hasTpsMethod) return Bukkit.getTPS();
        if (craftServer == null || tps == null) return new double[]{0, 0, 0};
        try {
            return (double[]) tps.get(craftServer);
        } catch (final IllegalAccessException ignored) {
            return new double[]{0, 0, 0};
        }
    }
}
