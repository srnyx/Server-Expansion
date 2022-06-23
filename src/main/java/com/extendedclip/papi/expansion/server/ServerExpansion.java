/*
 *
 * Server-Expansion
 * Copyright (C) 2018 Ryan McCarthy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.extendedclip.papi.expansion.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("unused")
public class ServerExpansion extends PlaceholderExpansion implements Cacheable, Configurable {
	private ServerUtils serverUtils = null;
	
	private final Map<String, SimpleDateFormat> dateFormats = new HashMap<>();
	private final Runtime runtime = Runtime.getRuntime();

	// config stuff
	private String serverName;
	private String bad = "&c";
	private String okay = "&e";
	private String good = "&a";
	private boolean isPaper = false;
	// -----
	
	private final Cache<String, Integer> cache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build();

	@Override
	public boolean canRegister() {
		serverName = this.getString("server_name", "A Minecraft Server");
		bad = this.getString("color.bad", "&c");
		okay = this.getString("color.okay", "&e");
		good = this.getString("color.good", "&a");

		try {
			//noinspection ConstantConditions
			isPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
		} catch (ClassNotFoundException ignored) {}

		if (isPaper) {
			Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
			if (papi != null) Bukkit.getPluginManager().registerEvents(new MsptUtils(), papi);
		}

		return true;
	}

	@Override
	public void clear() {
		dateFormats.clear();
		serverUtils = null;
		cache.invalidateAll();
	}

	@Override
	public @NotNull String getIdentifier() {
		return "server";
	}

	@Override
	public @NotNull String getAuthor() {
		return "clip";
	}

	@Override
	public @NotNull String getVersion() {
		return "2.6.2";
	}

	@Override
	public Map<String, Object> getDefaults() {
		final Map<String, Object> defaults = new HashMap<>();
		defaults.put("color.good", "&a");
		defaults.put("color.okay", "&e");
		defaults.put("color.bad", "&c");
		defaults.put("server_name", "A Minecraft Server");
		return defaults;
	}

	@Override
	public String onRequest(OfflinePlayer p, @NotNull String identifier) {
		if (serverUtils == null) serverUtils = new ServerUtils();

		switch (identifier) {
			// Players placeholders
			case "online": return String.valueOf(Bukkit.getOnlinePlayers().size());
			case "max_players": return String.valueOf(Bukkit.getMaxPlayers());
			case "unique_joins": return String.valueOf(Bukkit.getOfflinePlayers().length);
			// -----

			// Version placeholders
			case "version": return serverUtils.getVersion();
			case "build": return serverUtils.getBuild();
			case "version_build", "version_full": return serverUtils.getVersion() + '-' + serverUtils.getBuild();
			// -----

			// Ram placeholders
			case "ram_used": return String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / 1048576);
			case "ram_free": return String.valueOf(runtime.freeMemory() / 1048576);
			case "ram_total": return String.valueOf(runtime.totalMemory() / 1048576);
			case "ram_max": return String.valueOf(runtime.maxMemory() / 1048576);
			// -----

			// Identity placeholders
			case "name": return serverName == null ? "" : serverName;
			case "variant": return serverUtils.getServerVariant();
			// -----

			// Other placeholders
			case "tps": return getTps(null);
			case "mspt": return getMspt(null);
			case "uptime": {
				long seconds = TimeUnit.MILLISECONDS.toSeconds(ManagementFactory.getRuntimeMXBean().getUptime());
				return formatTime(Duration.of(seconds, ChronoUnit.SECONDS));
			}
			case "total_chunks": return String.valueOf(cache.get("chunks", k -> getChunks()));
			case "total_living_entities": return String.valueOf(cache.get("livingEntities", k -> getLivingEntities()));
			case "total_entities": return String.valueOf(cache.get("totalEntities", k -> getTotalEntities()));
			case "has_whitelist": return Bukkit.getServer().hasWhitelist() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
		}

		if (identifier.startsWith("tps_")) return getTps(identifier.replace("tps_", ""));

		if (identifier.startsWith("mspt_")) return getMspt(identifier.replace("mspt_", ""));

		if (identifier.startsWith("online_")) {
			int i = 0;
			for (Player online : Bukkit.getOnlinePlayers()) {
				if (online.getWorld().getName().equals(identifier.replace("online_", ""))) i++;
			}
			return String.valueOf(i);
		}

		if (identifier.startsWith("countdown_")) {
			String time = identifier.replace("countdown_", "");

			if (!time.contains("_")) {
				final Date then;
				try {
					then = PlaceholderAPIPlugin.getDateFormat().parse(time);
				} catch (Exception e) {
					return null;
				}

				final long between = then.getTime() - new Date().getTime();
				if (between <= 0) return "0";
				return formatTime(Duration.of((int) TimeUnit.MILLISECONDS.toSeconds(between), ChronoUnit.SECONDS));
			}

			final String[] parts = PlaceholderAPI.setBracketPlaceholders(p, time).split("_");
			if (parts.length != 2) return "invalid format and time";

			time = parts[1];
			SimpleDateFormat format;
			try {
				format = new SimpleDateFormat(parts[0]);
			} catch (Exception e) {
				return "invalid date format";
			}

			final Date then;
			try {
				then = format.parse(time);
			} catch (Exception e) {
				return "invalid date";
			}

			long t = System.currentTimeMillis();
			long between = then.getTime() - t;
			if (between <= 0) return "0";
			return formatTime(Duration.of((int) TimeUnit.MILLISECONDS.toSeconds(between), ChronoUnit.SECONDS));
		}

		if (identifier.startsWith("time_")) {
			identifier = identifier.replace("time_", "");
			if (dateFormats.containsKey(identifier)) return dateFormats.get(identifier).format(new Date());

			try {
				final SimpleDateFormat format = new SimpleDateFormat(identifier);
				dateFormats.put(identifier, format);
				return format.format(new Date());
			} catch (NullPointerException | IllegalArgumentException ex) {
				return null;
			}
		}

		return null;
	}

	public String getTps(String arg) {
		if (arg == null || arg.isEmpty()) {
			StringJoiner joiner = new StringJoiner(ChatColor.GRAY + ", ");
			for (double tps : serverUtils.getTps()) joiner.add(getColoredTps(tps));
			return joiner.toString();
		}

		switch (arg) {
			case "1", "one": return fix(serverUtils.getTps()[0]);
			case "5", "five": return fix(serverUtils.getTps()[1]);
			case "15", "fifteen": return fix(serverUtils.getTps()[2]);
			case "1_colored", "one_colored": return getColoredTps(serverUtils.getTps()[0]);
			case "5_colored", "five_colored": return getColoredTps(serverUtils.getTps()[1]);
			case "15_colored", "fifteen_colored": return getColoredTps(serverUtils.getTps()[2]);
			case "percent": {
				final StringJoiner joiner = new StringJoiner(ChatColor.GRAY + ", ");
				for (final double t : serverUtils.getTps()) joiner.add(getColoredTpsPercent(t));
				return joiner.toString();
			}
			case "1_percent", "one_percent": return getPercent(serverUtils.getTps()[0]);
			case "5_percent", "five_percent": return getPercent(serverUtils.getTps()[1]);
			case "15_percent", "fifteen_percent": return getPercent(serverUtils.getTps()[2]);
			case "1_percent_colored", "one_percent_colored": return getColoredTpsPercent(serverUtils.getTps()[0]);
			case "5_percent_colored", "five_percent_colored": return getColoredTpsPercent(serverUtils.getTps()[1]);
			case "15_percent_colored", "fifteen_percent_colored": return getColoredTpsPercent(serverUtils.getTps()[2]);
			default: return null;
		}
	}

	/**
	 * @author Sxtanna
	 */
	public static String formatTime(final Duration duration) {
		final StringBuilder builder = new StringBuilder();

		long seconds = duration.getSeconds();
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		final long weeks = days / 7;

		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 7;

		if (seconds > 0) builder.insert(0, seconds + "s");
		if (minutes > 0) {
			if (builder.length() > 0) builder.insert(0, ' ');
			builder.insert(0, minutes + "m");
		}
		if (hours > 0) {
			if (builder.length() > 0) builder.insert(0, ' ');
			builder.insert(0, hours + "h");
		}
		if (days > 0) {
			if (builder.length() > 0) builder.insert(0, ' ');
			builder.insert(0, days + "d");
		}
		if (weeks > 0) {
			if (builder.length() > 0) builder.insert(0, ' ');
			builder.insert(0, weeks + "w");
		}

		return builder.toString();
	}
	
	private String fix(double tps) {
		return String.valueOf(Math.min(Math.round(tps), 20.0));
	}
	
	private String color(double tps) {
		String color = bad;
		if (tps > 16.0) color = okay;
		if (tps > 18.0) color = good;
		return ChatColor.translateAlternateColorCodes('&', color);
	}
	
	private String getColoredTps(double tps) {
		return color(tps) + fix(tps);
	}
	
	private String getColoredTpsPercent(double tps){
		return color(tps) + getPercent(tps);
	}
	
	private String getPercent(double tps){
		return Math.min(Math.round(100 / 20.0 * tps), 100.0) + "%";
	}
	
	private Integer getChunks(){
		int loadedChunks = 0;
		for (final World world : Bukkit.getWorlds()) loadedChunks += world.getLoadedChunks().length;
		return loadedChunks;
	}
	
	private Integer getLivingEntities(){
		int livingEntities = 0;
		for (final World world : Bukkit.getWorlds()) {
			livingEntities += world.getLivingEntities().size();
		}
		
		return livingEntities;
	}
	
	private Integer getTotalEntities(){
		int allEntities = 0;
		for (final World world : Bukkit.getWorlds()) {
			allEntities += world.getEntities().size();
		}
		return allEntities;
	}

	private static double round(double value) {
		final int scale = (int) Math.pow(10, 1);
		return (double) Math.round(value * scale) / scale;
	}

	public String getMspt(String arg) {
		if (arg == null) return String.valueOf(getMspt1Second());

		return switch (arg) {
			case "1", "one" -> String.valueOf(getMspt1Minute());
			case "5", "five" -> String.valueOf(getMspt5Minutes());
			case "10", "ten" -> String.valueOf(getMspt10Minutes());
			case "colored" -> getColoredMspt(getMspt1Second());
			case "1_colored", "one_colored" -> getColoredMspt(getMspt1Minute());
			case "5_colored", "five_colored" -> getColoredTps(getMspt5Minutes());
			case "10_colored", "ten_colored" -> getColoredTps(getMspt10Minutes());
			default -> String.valueOf(getMspt1Second());
		};
	}

	/**
	 * @return  The average MSPT of the last 20 ticks (1 second)
	 */
	public double getMspt1Second() {
		return MsptUtils.getMspt(20);
	}

	/**
	 * @return  The average MSPT of the last 1,200 ticks (1 minute)
	 */
	public double getMspt1Minute() {
		return MsptUtils.getMspt(1200);
	}

	/**
	 * @return  The average MSPT of the last 6,000 ticks (5 minutes)
	 */
	public double getMspt5Minutes() {
		return MsptUtils.getMspt(6000);
	}

	/**
	 * @return  The average MSPT of the last 12,000 ticks (10 minutes)
	 */
	public double getMspt10Minutes() {
		return MsptUtils.getMspt(12000);
	}

	private String getColoredMspt(double mspt) {
		String color = bad;
		if (mspt < 50.0) color = okay;
		if (mspt < 25.0) color = good;
		return ChatColor.translateAlternateColorCodes('&', color) + mspt;
	}
}
