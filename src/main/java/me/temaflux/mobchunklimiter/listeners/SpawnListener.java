package me.temaflux.mobchunklimiter.listeners;

import java.util.Arrays;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import me.temaflux.mobchunklimiter.MobChunkLimiterPlugin;

import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class SpawnListener
implements Listener {
	private final MobChunkLimiterPlugin plugin;
	
	// Constructor
	
	public SpawnListener(MobChunkLimiterPlugin plugin) {
		this.plugin = plugin;
	}
	
	// Events
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent e) {
		if (!allowedEvent(e) || !allowedSpawn(e)) return; // Ignore unallowed event or spawn
		
		final EntityType type = e.getEntityType();
		int maxSize = maxSizeType(e.getEntityType());
		
		if (maxSize <= 0) return; // Ignore
		
		final Chunk chunk = e.getLocation().getChunk();
		
		if (sizeEntities(chunk, type) + 1 > maxSize)
			e.setCancelled(true); // Ignore "current > maximum" entities
		
		if (sizeEntities(chunk, null) + 1 > maxSizeType(null))
			e.setCancelled(true); // Ignore "current > global" entities
	}
	
	// Actions
	
	private static boolean allowedEvent(EntitySpawnEvent event) {
		return !(event instanceof ItemSpawnEvent || event instanceof ProjectileLaunchEvent);
	}
	
	private boolean allowedSpawn(EntitySpawnEvent event) {
		return allowedReason(event) && allowedType(event.getEntityType());
	}
	
	private boolean allowedReason(EntitySpawnEvent event) {
		if (event instanceof SpawnerSpawnEvent) return setting("spawn.spawner");
		final SpawnReason reason = event instanceof CreatureSpawnEvent ? ((CreatureSpawnEvent) event).getSpawnReason() : SpawnReason.DEFAULT;
		switch (reason) {
			case EGG:
			case DISPENSE_EGG:
			case SPAWNER_EGG: return setting("spawn.egg");
			case SPAWNER: return setting("spawn.spawner");
			case COMMAND: return setting("spawn.command");
			default: return setting("spawn.another");
		}
	}
	
	private int sizeEntities(Chunk chunk, EntityType type) {
		Stream<Entity> stream = Arrays.asList(chunk.getEntities()).stream();
		
		if (type != null) stream = stream.filter(e -> e.getType().equals(type));
		
		return (int) stream.filter(this::allowedType).count();
	}
	
	private boolean allowedType(Entity entity) {
		return allowedType(entity.getType());
	}
	
	private int maxSizeType(EntityType type) {
		return config().getInt(
			"settings.spawn.maximum." + (type == null ? "global" : type.name().toLowerCase()),
			config().getInt("settings.spawn.maximum.default", -1)
		);
	}
	
	/*
	 * Default unallowed Player
	 */
	private boolean allowedType(EntityType type) {
		return type != EntityType.PLAYER && !config().getStringList("settings.spawn.blacklist").contains(type.name());
	}
	
	// Utils
	
	private boolean setting(String id) {
		return config().getBoolean("settings." + id);
	}
	
	private Configuration config() {
		return plugin.getConfig();
	}
}
