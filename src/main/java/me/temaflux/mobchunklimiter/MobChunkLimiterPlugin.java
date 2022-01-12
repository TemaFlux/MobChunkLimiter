package me.temaflux.mobchunklimiter;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import me.temaflux.mobchunklimiter.listeners.SpawnListener;

public class MobChunkLimiterPlugin
extends JavaPlugin {
	private static MobChunkLimiterPlugin plugin;
	
	@Override
	public void onLoad() {
		plugin = this;
		saveDefaultConfig();
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new SpawnListener(this), this);
		getCommand("mobchunklimiter").setExecutor(this);
	}
	
	@Override
	public void onDisable() {
		plugin = null;
	}
	
	//
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("mobchunklimiter.command")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.permission", "messages.permission")));
			return false;
		}
		reloadConfig();
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.command", "messages.command")));
		return true;
	}
	
	//
	
	public static MobChunkLimiterPlugin getPlugin() {
		return plugin;
	}
}
