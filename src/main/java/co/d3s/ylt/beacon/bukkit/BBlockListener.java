package co.d3s.ylt.beacon.bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import co.d3s.ylt.beacon.beacon.BBeacon;
import co.d3s.ylt.beacon.beacon.BChunk;
import co.d3s.ylt.beacon.beacon.BWorld;

public class BBlockListener implements Listener {
	// BLOCK_BREAK
	// BLOCK_DAMAGE
	// BLOCK_PLACE
	// BLOCK_IGNITE
	// SIGN_CHANGE

	// BLOCK_BURN (todo after flags)

	// BLOCK_PISTON_EXTEND
	// BLOCK_PISTON_CONTRACT

	BeaconPlugin beaconplug;

	public BBlockListener(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
		if (event.isCancelled())
			return; // no permission :D

		if (block.getType() != Material.STONE_PLATE) {
			Block up = block.getRelative(BlockFace.UP);
			if (up.getType() != Material.STONE_PLATE) {
				return;
			} else {
				block = up;
			}
		}
		try {
			// Block bblock = block.getRelative(BlockFace.DOWN);

			int bx = block.getX();
			int by = block.getY();
			int bz = block.getZ();
			BWorld bworld = BeaconPlugin.manager.world(loc.getWorld());
			BChunk chunk = bworld.chunk(bx >> BeaconPlugin.manager.chunkshift,
					bz >> BeaconPlugin.manager.chunkshift);
			BBeacon beacon = null;
			outside: for (BBeacon abeacon : chunk.pbeacons) {
				if (abeacon.x == bx && abeacon.y == by && abeacon.z == bz) {
					beacon = abeacon;
					break outside;
				}
			}
			if (beacon != null) {
				bworld.del(beacon);
				player.sendMessage("Beacon deleted.");
			}
			// beacon.del(); //do later
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled())
			return;
		if (event.getCause() != IgniteCause.FLINT_AND_STEEL)
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
		if (event.isCancelled())
			return; // no permission :D

		block = block.getRelative(BlockFace.DOWN);

		if (block.getType() != Material.STONE_PLATE)
			return;

		BeaconType btype = BeaconPlugin.types.get(block.getRelative(
				BlockFace.DOWN, 1).getType());
		if (btype == null)
			return;
		// player placed a beacon! :D
		event.setCancelled(true);

		// The one rule with beacons is that protected
		// range must not go over an existing beacon.
		// this is not easy to check.
		try {
			BWorld bworld = BeaconPlugin.manager.world(loc.getWorld());

			int bx = block.getX();
			int by = block.getY();
			int bz = block.getZ();
			BChunk echunk = bworld.chunk(bx >> BeaconPlugin.manager.chunkshift,
					bz >> BeaconPlugin.manager.chunkshift);
			BBeacon ebeacon = null;
			outside: for (BBeacon abeacon : echunk.pbeacons) {
				if (abeacon.x == bx && abeacon.y == by && abeacon.z == bz) {
					ebeacon = abeacon;
					break outside;
				}
			}

			if (ebeacon != null) {
				player.sendMessage(ChatColor.YELLOW
						+ "Error: A beacon already exists at this spot.");
				return;
			}

			BBeacon beacon = new BBeacon(bworld, block.getX(), block.getY(),
					block.getZ(), btype.protection, btype.border);
			beacon.addPlayer(player.getName().toLowerCase());

			boolean perm = true;
			int bradius = beacon.pr;
			after: for (int ix = -bradius; ix <= bradius; ix += Math.pow(2,
					BeaconPlugin.manager.chunkshift)) {
				for (int iz = -bradius; iz <= bradius; iz += Math.pow(2,
						BeaconPlugin.manager.chunkshift)) {
					int x = beacon.x + ix;
					int z = beacon.z + iz;

					int cx = x >> BeaconPlugin.manager.chunkshift;
					int cz = z >> BeaconPlugin.manager.chunkshift;

					// if (beacon.inside(x, z, true)) { mode = 2; }

					BChunk chunk = bworld.chunk(cx, cz);

					// System.out.print(cx+", "+cz+" pcount: "+chunk.pbeacons.size()+", bcount: "+chunk.bbeacons.size());
					for (BBeacon abeacon : chunk.pbeacons) {
						if (beacon.inside(abeacon.x, abeacon.z, 0)) {
							if (!abeacon.players.contains(player.getName()
									.toLowerCase())) {
								perm = false;
								break after;
							}
						}
					}
				}
			}

			if (perm == true) {
				// okay, we've survived our checks :D, now to add it to world.
				// beacon.add(); //todo
				bworld.add(beacon); // :D
				player.sendMessage(ChatColor.YELLOW
						+ "You have successfuly built a beacon (id:"
						+ beacon.id + ", radius:" + beacon.pr + " meters).");
			} else {
				player.sendMessage(ChatColor.YELLOW
						+ "You have no permission to build a beacon here (radius overlaps a beacon).");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
	}
}