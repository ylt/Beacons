package co.d3s.ylt.beacon.bukkit;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import co.d3s.ylt.beacon.beacon.BWorld;

public class BBorderTimer implements Runnable {
	BeaconPlugin beaconplug;

	public BBorderTimer(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}

	public HashMap<Player, BorderPlayer> players = new HashMap<Player, BorderPlayer>();

	@Override
	public void run() {
		// System.out.print("checked.");
		Server server = beaconplug.getServer();
		for (Player player : server.getOnlinePlayers()) {
			try {
				Location loc = player.getLocation();
				BWorld world = BeaconPlugin.manager.world(loc.getWorld());
				BorderPlayer bp = players.get(player);
				if (player.isDead()) {
					if (bp != null) {
						players.remove(player);
						player.sendMessage(ChatColor.RED
								+ "You have been killed for remaining outside border.");
					}
					continue;
				}
				if (!world.inBeacon(loc.getBlockX(), loc.getBlockZ(), 1)) {
					if (bp == null) {
						player.sendMessage(ChatColor.RED
								+ "You have exited the border; please re-enter to avoid death.");
						bp = new BorderPlayer(player.getName());
						players.put(player, bp);
					} else {
						bp.steps += 1;
						bp.rot += 0.5;
						if (bp.steps == 1) {
							bp.steps = 0;
							if (player.getHealth() - 1 >= 0) {
								player.damage(1);
								bp.healthdrop += 1;
							}
						}

						player.playEffect(
								new Location(loc.getWorld(), loc.getX()
										+ (Math.sin(bp.rot) * 2), loc.getY(),
										loc.getZ() + (Math.cos(bp.rot) * 2)),
								Effect.BOW_FIRE, 0);
					}
				} else if (bp != null) {
					player.sendMessage(ChatColor.GREEN
							+ "You have re-entered the border, your health has been refunded.");
					player.setHealth(Math.min(player.getHealth()
							+ bp.healthdrop, 20));
					players.remove(player);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class BorderPlayer {
	public BorderPlayer(String name) {
		this.name = name;
	}

	public String name;
	// public int time;w
	public int steps;
	public int healthdrop;
	public double rot;
}