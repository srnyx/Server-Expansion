package com.extendedclip.papi.expansion.server;

import me.clip.placeholderapi.PlaceholderAPIPlugin;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class ServerUtils {
    private String version = null;
    private String build = null;
    private String variant = null;
    
    private Object craftServer = null;
    private Field tps = null;
    
    private boolean hasTpsMethod = false;


    @SuppressWarnings("unused")
    public ServerUtils() {
        resolveTPSHandler();
    }
    
    private Map<String, String> getVariants() {
        final Map<String, String> variants = new HashMap<>();
        variants.put("org.spigotmc.SpigotConfig", "Spigot");
        variants.put("io.papermc.paper.configuration.ConfigurationLoaders", "Paper"); // New config location for Paper 1.19+
        variants.put("com.destroystokyo.paper.PaperConfig", "Paper"); // Still supported by Paper, but deprecated.
        variants.put("com.tuinity.tuinity.config.TuinityConfig", "Tuinity");
        variants.put("gg.airplane.AirplaneConfig", "Airplane");
        variants.put("net.pl3x.purpur.PurpurConfig", "Purpur");
        return variants;
    }

    public String getServerVariant() {
        if (variant != null) return variant;

        for (final Map.Entry<String, String> variants : getVariants().entrySet()) {
            try {
                Class.forName(variants.getKey());
                variant = variants.getValue();
            } catch (ClassNotFoundException ignored) {
                // ignored
            }
        }
        
        if (variant == null) variant = "Unknown";
        return variant;
    }
    
    public String getVersion() {
        if (version != null) return version;
        version = Bukkit.getBukkitVersion().split("-")[0];
        return version;
    }
    
    public String getBuild() {
        if (build != null) return build;
        
        final String[] split = Bukkit.getVersion().split("-");
        switch (getServerVariant().toLowerCase(Locale.ROOT)) {
            // TODO Find out what those variants return.
            case "spigot", "purpur" -> {
                build = split[0];
                return build;
            }

            case "paper" -> {
                if (split.length >= 3) {
                    if (split[2].contains(" ")) {
                        build = split[2].substring(0, split[2].indexOf(" "));
                        return build;
                    }

                    build = split[2];
                    return build;
                }

                build = "Unknown";
                return build;
            }

            default -> {
                build = "Unknown";
                return build;
            }
        }
    }
    
    public double[] getTps() {
        if (hasTpsMethod) return Bukkit.getTPS();
        if (craftServer == null || tps == null) return new double[]{0, 0, 0};
        
        try {
            return (double[]) tps.get(craftServer);
        } catch (IllegalAccessException ignored) {
            return new double[]{0, 0, 0};
        }
    }
    
    private void resolveTPSHandler() {
        try {
            // If this throws is the server not a fork...
            Bukkit.class.getMethod("getTPS");
            hasTpsMethod = true;
        } catch (NoSuchMethodException ignored) {
            final String mcVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            
            try {
                if (getMajorVersion() >= 17) {
                    craftServer = Class.forName("net.minecraft.server.MinecraftServer")
                        .getMethod("getServer").invoke(null);
                    return;
                }

                craftServer = Class.forName("net.minecraft.server." + mcVersion + ".MinecraftServer")
                        .getMethod("getServer").invoke(null);
                
                tps = craftServer.getClass().getField("recentTps");
            } catch (Exception ex) {
                PlaceholderAPIPlugin.getInstance().getLogger().warning("Could not resolve TPS handling!");
                ex.printStackTrace();
            }
        }
    }
    
    private int getMajorVersion() {
        final Matcher matcher = Pattern.compile("\\(MC: (\\d)\\.(\\d+)\\.?(\\d+?)?\\)").matcher(Bukkit.getVersion());
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.toMatchResult().group(2), 10);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }
}
