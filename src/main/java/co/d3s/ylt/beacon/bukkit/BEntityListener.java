package co.d3s.ylt.beacon.bukkit;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import co.d3s.ylt.beacon.beacon.BWorld;

public class BEntityListener implements Listener {
	// ENDERMAN_PICKUP
	// ENDERMAN_PLACE
	// ENTITY_EXPLODE
	// ENTITY_DEATH

	BeaconPlugin beaconplug;

	public BEntityListener(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEndermanPickup(EntityChangeBlockEvent event) {
		if (event.isCancelled())
			return;
		try {
			Block block = event.getBlock();
			BWorld bworld = BeaconPlugin.manager.world(block.getWorld());
			if (bworld.inBeacon(block.getX(), block.getZ(), 1)) {
				event.setCancelled(true);
			}
		} catch (Exception e) {

		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEndermanPlace(EntityChangeBlockEvent event) {
		if (event.isCancelled())
			return;
		try {
			//Location loc = event.getLocation();
			Block block = event.getBlock();
			BWorld bworld = BeaconPlugin.manager.world(block.getWorld());
			if (bworld.inBeacon(block.getX(), block.getZ(), 0)) {
				event.setCancelled(true);
			}
		} catch (Exception e) {

		}
	}
	
	static LinkedList<Material> protect = new LinkedList<Material>(
			Arrays.asList(new Material[] { Material.WOOD, Material.GOLD_ORE,
					Material.IRON_ORE, Material.COAL_ORE, Material.LAPIS_BLOCK,
					Material.TNT, Material.BED }));
	static LinkedList<Material> dprotect = new LinkedList<Material>(
			Arrays.asList(new Material[] { Material.AIR, Material.GRASS,
					Material.DIRT, Material.COBBLESTONE, Material.SAND,
					Material.GRAVEL, Material.WOOD, Material.LEAVES,
					Material.SANDSTONE, Material.SNOW_BLOCK,
					Material.NETHERRACK, Material.SOUL_SAND, }));
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		//Entity entity = event.getEntity();
		// event.setDroppedExp(0);
		/*
		 * if (entity instanceof Enderman) { Enderman ender = (Enderman)entity;
		 * List<ItemStack> drops = event.getDrops(); MaterialData item =
		 * ender.getCarriedMaterial(); if (item != null) {
		 * drops.add(item.toItemStack(1)); } }
		 */
	}
}
