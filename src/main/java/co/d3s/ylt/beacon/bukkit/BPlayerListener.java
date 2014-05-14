package co.d3s.ylt.beacon.bukkit;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import co.d3s.ylt.beacon.beacon.BBeacon;
import co.d3s.ylt.beacon.beacon.BChunk;
import co.d3s.ylt.beacon.beacon.BWorld;

public class BPlayerListener implements Listener {
	// **Block protection***
	// - PLAYER_INTERACT
	// - PLAYER_INTERACT_ENTITY (help prevent leaving sitting wolves)
	// - PLAYER_BUCKET_EMPTY
	// - PLAYER_BUCKET_FILL

	// **Pickup protection***
	// - PLAYER_DROP_ITEM
	// - PLAYER_PICKUP_ITEM

	// **Entry blocking (todo)**
	// - PLAYER_MOVE
	// - PLAYER_TELEPORT
	BeaconPlugin beaconplug;

	public BPlayerListener(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		Location loc = block.getLocation();
		Material type = block.getType();
		if (type.equals(Material.CAKE_BLOCK) || type.equals(Material.CHEST)
				|| type.equals(Material.FURNACE)
				|| type.equals(Material.DISPENSER)
				|| type.equals(Material.DIODE)
				|| type.equals(Material.WOODEN_DOOR)
				|| type.equals(Material.JUKEBOX)
				|| type.equals(Material.TRAPPED_CHEST)
				|| type.equals(Material.HOPPER)) {
			beaconplug.checkPlayerEvent(event, player, loc);
			if (event.isCancelled())
				return;
		}
		if (event.getMaterial() == Material.SLIME_BALL) {
			try {
				BWorld bworld = BeaconPlugin.manager.world(loc.getWorld());
				BChunk bchunk = bworld.chunk(
						loc.getBlockX() >> BeaconPlugin.manager.chunkshift,
						loc.getBlockZ() >> BeaconPlugin.manager.chunkshift);
				String b = "";
				String p = "";
				/*for (BBeacon beacon : bchunk.bbeacons) {
					b = b + " " + beacon.id + "(" + beacon.pr + ", "
							+ beacon.br + ")";
				}
				
				for (BBeacon beacon : bchunk.pbeacons) {
					p = p + " " + beacon.id + "(" + beacon.pr + ", "
							+ beacon.br + ")";
				}*/
				LinkedList<BBeacon> done = new LinkedList<BBeacon>();
				for (BBeacon beacon : bchunk.pbeacons) {
					if (beacon.inside(loc.getBlockX(), loc.getBlockZ(), 0)) {
						p = p + " " + beacon.id + "(" + beacon.pr + ", "
								+ beacon.br + ")";
						done.add(beacon);
					}
				}
				String bugged = "";
				for (BBeacon beacon : bchunk.bbeacons) {
					if (beacon.inside(loc.getBlockX(), loc.getBlockZ(), 0)) {
						bugged = bugged + " " + beacon.id + "(" + beacon.pr + ", "
								+ beacon.br + ")";
					}
					else if (beacon.inside(loc.getBlockX(), loc.getBlockZ(), 1)) {
						b = b + " " + beacon.id + "(" + beacon.pr + ", "
								+ beacon.br + ")";
					}
				}
				player.sendMessage((loc.getBlockX() >> BeaconPlugin.manager.chunkshift)
						+ ", "
						+ (loc.getBlockZ() >> BeaconPlugin.manager.chunkshift));
				player.sendMessage("This block is inside the:");
				player.sendMessage("  - protection of: " + p);
				player.sendMessage("  - border of: " + b);
				if (bugged.length() > 0)
					player.sendMessage("  - bugged?: " + bugged);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		try {
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
				beaconplug.bselection.put(player, beacon.id);
				player.sendMessage(ChatColor.YELLOW + "Beacon " + beacon.id
						+ " selected.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	@EventHandler(priority=EventPriority.HIGHEST)
	  public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
	    if (event.isCancelled()) {
	      return;
	    }
	    Player player = event.getPlayer();
	    Entity entity = event.getRightClicked();
	    Location loc = entity.getLocation();
	    if (entity.getType() != EntityType.HORSE) {
	      if ((entity.getType() != EntityType.MINECART) && (entity.getType() != EntityType.BOAT)) {
	        this.beaconplug.checkPlayerEvent(event, player, loc);
	      }
	    }
	  }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamageEntity(EntityDamageByEntityEvent event){//Stop our peaceful friends from dying :)
		Entity dmger = event.getDamager();
		Entity damaged = event.getEntity();
		Location loc = damaged.getLocation();
		if(damaged.getType() == EntityType.COW
		|| damaged.getType() == EntityType.HORSE
		|| damaged.getType() == EntityType.PIG
		|| damaged.getType() == EntityType.WOLF
		|| damaged.getType() == EntityType.VILLAGER
		|| damaged.getType() == EntityType.BAT
		|| damaged.getType() == EntityType.SHEEP
		|| damaged.getType() == EntityType.SQUID
	    || damaged.getType() == EntityType.OCELOT
	    || damaged.getType() == EntityType.MUSHROOM_COW){
		  if(dmger instanceof Player){
			beaconplug.checkPlayerEvent(event, (Player)dmger, loc);
		  }else{
			if(dmger.getType() == EntityType.ARROW){
			   Projectile arrow = (Projectile) dmger;

			   if(arrow.getShooter() instanceof Player){
				   beaconplug.checkPlayerEvent(event, (Player)dmger, loc);
			   }else{
				   BWorld bworld = BeaconPlugin.manager.world(dmger.getWorld());
					  try {
						if(bworld.inBeacon(dmger.getLocation().getBlockX(), dmger.getLocation().getBlockZ(), 1)) {
							event.setCancelled(true); 
						}
					  } catch (SQLException e) {
					   	//dinner is served
					  } 
			   }
			}else{
			  BWorld bworld = BeaconPlugin.manager.world(dmger.getWorld());
			  try {
				if(bworld.inBeacon(dmger.getLocation().getBlockX(), dmger.getLocation().getBlockZ(), 1)) {
					event.setCancelled(true); 
				}
			  } catch (SQLException e) {
			   	//eat it up
			  }
			}
		  }
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlockClicked().getRelative(event.getBlockFace(),
				1);
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		Block block = event.getBlockClicked().getRelative(event.getBlockFace(),
				1);
		Location loc = block.getLocation();
		beaconplug.checkPlayerEvent(event, player, loc);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled())
			return;

		Location loc = event.getLocation();
		World world = loc.getWorld();
		BWorld bworld = BeaconPlugin.manager.world(world);

		List<Block> blocks = event.blockList();
		try {
			for (Block block : blocks) {
				if (bworld.inBeacon(block.getX(), block.getZ(), 0)) {
					event.setCancelled(true);
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
    public void onCreateFrame(HangingPlaceEvent event) {
		Entity en = event.getPlayer();
		Hanging hanging = event.getEntity();
		if((hanging.getType() == EntityType.PAINTING && beaconplug.protectPaint)
		|| (hanging.getType() == EntityType.ITEM_FRAME && beaconplug.protectFrame)){
		   if(en instanceof Player){
			 beaconplug.checkPlayerEvent(event, ((Player) en), en.getLocation());
			 if (event.isCancelled())
				 return;
		   }else{
			   BWorld bworld = BeaconPlugin.manager.world(en.getWorld());
				try {
					if (bworld.inBeacon(en.getLocation().getBlockX(), en.getLocation().getBlockZ(), 1)) {
						event.setCancelled(true);
					}
				} catch (SQLException e) {
					//swallow it?
				}
		   }
		}
	  }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemBreak(HangingBreakByEntityEvent event){
		Entity en = event.getRemover();
		Hanging hanging = event.getEntity();
		if((hanging.getType() == EntityType.PAINTING && beaconplug.protectPaint)
		|| (hanging.getType() == EntityType.ITEM_FRAME && beaconplug.protectFrame)){
		   if(en instanceof Player){
			 beaconplug.checkPlayerEvent(event, ((Player) en), en.getLocation());
			 if (event.isCancelled())
				 return;
		   }else{
			   BWorld bworld = BeaconPlugin.manager.world(en.getWorld());
				try {
					if (bworld.inBeacon(en.getLocation().getBlockX(), en.getLocation().getBlockZ(), 1)) {
						event.setCancelled(true);
					}
				} catch (SQLException e) {
					//swallow it?
				}
		   }
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		// BeaconPlugin.beaconplug.checkEvent(event, player, loc);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (event.isCancelled())
			return;
		Player player = event.getPlayer();
		// BeaconPlugin.beaconplug.checkEvent(event, player, loc);
	}
}
