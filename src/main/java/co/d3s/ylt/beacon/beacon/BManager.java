package co.d3s.ylt.beacon.beacon;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BManager {
	public HashMap<World, BWorld> worlds = new HashMap<World, BWorld>();
	Connection db;
	public int chunkshift = 6;

	public BManager(Connection db) {
		this.db = db;
	}

	public BWorld world(World world) {
		BWorld bworld = worlds.get(world);
		if (bworld != null) {
			return bworld;
		}
		bworld = new BWorld(world, this);
		worlds.put(world, bworld);
		return bworld;
	}

	public boolean hasPermission(Player player, int x, int z)
			throws SQLException {
		return hasPermission(player, player.getWorld(), x, z);
	}

	public boolean hasPermission(Player player, World world, int x, int z)
			throws SQLException {
		BWorld bworld = world(world);
		String name = player.getName().toLowerCase();
		LinkedList<BBeacon> beacons = bworld.getBeacons(x, z);
		if (beacons.isEmpty())
			return true;

		for (BBeacon beacon : beacons) {
			if (beacon.players.contains(name)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasPermission(Player player, Location loc)
			throws SQLException {
		return hasPermission(player, loc.getWorld(), loc.getBlockX(),
				loc.getBlockZ());
	}
}
