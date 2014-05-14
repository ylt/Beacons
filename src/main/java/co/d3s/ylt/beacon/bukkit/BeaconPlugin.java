package co.d3s.ylt.beacon.bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.java.JavaPlugin;

import co.d3s.ylt.beacon.beacon.BBeacon;
import co.d3s.ylt.beacon.beacon.BManager;
import co.d3s.ylt.beacon.beacon.BWorld;
import co.d3s.ylt.util.separator.Separator;

public class BeaconPlugin extends JavaPlugin {

	@Override
	public void onDisable() {

	}

	public static Connection db;
	public static BeaconPlugin beaconplug;
	public static BManager manager;

	BBlockListener bl;
	BPlayerListener pl;
	BEntityListener el;
	BBorderTimer bt;
	BCacheClean cc;

	class AdminExcept {
		public AdminExcept(Player player, int r) {
			Location loc = player.getLocation();
			x = loc.getBlockX();
			y = loc.getBlockY();
			z = loc.getBlockZ();

			this.r = r;

			time = System.currentTimeMillis();
		}

		public int x;
		public int y;
		public int z;

		public int r;

		public long time;
	}

	public WeakHashMap<Player, AdminExcept> admins = new WeakHashMap<Player, AdminExcept>();
	public WeakHashMap<Player, Integer> bselection = new WeakHashMap<Player, Integer>();
	public boolean protectMinecart = false;
	public boolean protectFrame = true;
	public boolean protectPaint = true;
	public Material btool = Material.SLIME_BALL;

	@Override
	public void onEnable() {		
		saveDefaultConfig();
	    String user = getConfig().getString("user");
	    String url = getConfig().getString("url");
	    String password = getConfig().getString("password");
	    
	    this.protectMinecart = getConfig().getBoolean("protectMinecart");
	    this.protectFrame = getConfig().getBoolean("protectFrame");
	    this.protectPaint = getConfig().getBoolean("protectPaint");
	    this.btool = Material.getMaterial(getConfig().getInt("btool")); //TODO swap this later I guess
	    try
	    {
	      Class.forName("com.mysql.jdbc.Driver");
	      db = DriverManager.getConnection(url, user, 
	        password);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    beaconplug = this;
	    manager = new BManager(db);

		// Not used (#P)
		// World world = getServer().getWorld("world");
		// BWorld bworld = manager.world(world);

		bl = new BBlockListener(this);
		this.getServer().getPluginManager().registerEvents(bl, this);
		/*getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, bl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_DAMAGE, bl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_PLACE, bl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.BLOCK_IGNITE, bl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.SIGN_CHANGE, bl,
				Priority.Highest, this);*/

		pl = new BPlayerListener(this);
		this.getServer().getPluginManager().registerEvents(pl, this);
		/*getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, pl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(
				Type.PLAYER_INTERACT_ENTITY, pl, Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_EMPTY,
				pl, Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_BUCKET_FILL,
				pl, Priority.Highest, this);
		// getServer().getPluginManager().registerEvent(Type.PLAYER_DROP_ITEM,
		// pl, Priority.Highest, this);
		// getServer().getPluginManager().registerEvent(Type.PLAYER_PICKUP_ITEM,
		// pl, Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, pl,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_TELEPORT, pl,
				Priority.Highest, this);

		el = new BEntityListener(this);
		getServer().getPluginManager().registerEvent(Type.ENDERMAN_PICKUP, el,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.ENDERMAN_PLACE, el,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.ENTITY_EXPLODE, el,
				Priority.Highest, this);
		getServer().getPluginManager().registerEvent(Type.ENTITY_DEATH, el,
				Priority.Highest, this);*/

		// BWorldListener wl = new BWorldListener(this);
		// getServer().getPluginManager().registerEvent(Type.CHUNK_LOAD, wl,
		// Priority.Highest, this);

		/*bt = new BBorderTimer(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, bt, 5, 5);*/
		
		cc = new BCacheClean(this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, cc, 5000, 5000);
		
		/*
		 * Useless code (#P) try { BBeacon beacon = new BBeacon(bworld, 60, 67,
		 * 249, 34, 400); //beacon.addPlayer(player.getName().toLowerCase());
		 * bworld.add(beacon); } catch (Exception e) {
		 * 
		 * }
		 */

		getCommand("build").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command,
					String label, String[] args) {
				if (!sender.hasPermission("beacon.admin.build")) {
					sender.sendMessage(ChatColor.YELLOW
							+ "Error: No permission.");
					return true;
				}
				Player player = (Player) sender;

				AdminExcept ae = new AdminExcept(player, 32);
				player.setCompassTarget(player.getLocation());

				admins.put(player, ae);
				sender.sendMessage(ChatColor.YELLOW + "You can now build.");
				return true;
			}
		});
		getCommand("beacon").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command,
					String label, String[] args) {
				int selection = 0;
				int offset = 0;
				if (args.length == 0) {
					sender.sendMessage(ChatColor.YELLOW + "## /beacon usage:-");
					sender.sendMessage(ChatColor.RED
							+ "  *Command acts on a selected beacon*");
					sender.sendMessage(ChatColor.YELLOW
							+ " - Beacon Selection:- (any)");
					sender.sendMessage(ChatColor.YELLOW
							+ "   - Right click plate or stand on beacon");
					sender.sendMessage(ChatColor.YELLOW + "   - /beacon [id]");
					sender.sendMessage(ChatColor.YELLOW
							+ "   - /beacon [id] [sub-command]");
					sender.sendMessage(ChatColor.YELLOW + " - Sub-Commands:-");
					sender.sendMessage(ChatColor.YELLOW
							+ "   - info > Display basic information.");
					sender.sendMessage(ChatColor.YELLOW
							+ "   - owner(s) > List owners of this beacon");
					sender.sendMessage(ChatColor.YELLOW
							+ "   - owner(s) [+-][player]... > Add/remove owners");
					return true;
				}
				try {
					selection = Integer.parseInt(args[0]);
					offset = 1;
				} catch (NumberFormatException e) {

					Integer sel = bselection.get(sender);
					if (sel == null) {
						sender.sendMessage(ChatColor.YELLOW
								+ "No ID passed or no beacon selection.");
						return true;
					}
					selection = sel;
				}
				try {
					BWorld bworld = manager.world(((Player) sender).getWorld());
					BBeacon beacon = bworld.beacon(selection);

					if (args.length <= offset) {
						// player selected beacon
						bselection.put((Player) sender, selection);
						sender.sendMessage(ChatColor.YELLOW + "Beacon "
								+ selection + " selected.");
					} else if (args[0 + offset].equals("info")) {
						sender.sendMessage(ChatColor.YELLOW + "Beacon info:- ");
						sender.sendMessage(ChatColor.YELLOW + " - Location: ("
								+ beacon.x + ", " + beacon.y + ", " + beacon.z
								+ ")");
						sender.sendMessage(ChatColor.YELLOW
								+ " - Radii:- Protective: " + beacon.pr
								+ ", Border: " + beacon.br);

						Server server = beaconplug.getServer();
						Separator sep = new Separator(ChatColor.YELLOW + ", ");
						StringBuffer players = new StringBuffer(
								ChatColor.YELLOW + "Beacon owners: ");
						for (String player : beacon.players) {
							Player oplayer = server.getPlayerExact(player);
							players.append(sep
									+ (oplayer == null ? ChatColor.RED + player
											: ChatColor.GREEN
													+ oplayer.getName()));
						}
						sender.sendMessage(players.toString());
					} else if (args[0 + offset].equals("owner")
							|| args[0 + offset].equals("owners")) {
						if (args.length <= offset + 1) {
							Server server = beaconplug.getServer();
							Separator sep = new Separator(ChatColor.YELLOW
									+ ", ");
							StringBuffer players = new StringBuffer(
									ChatColor.YELLOW + "Beacon owners: ");
							for (String player : beacon.players) {
								Player oplayer = server.getPlayerExact(player);
								players.append(sep
										+ (oplayer == null ? ChatColor.RED
												+ player : ChatColor.GREEN
												+ oplayer.getName()));
							}
							sender.sendMessage(players.toString());
						} else {
							if (sender.hasPermission("beacons.admin.modify")
									|| beacon.players.contains(sender.getName()
											.toLowerCase())) {
								for (int i = offset + 1; i < args.length; i++) {
									String arg = args[i];
									if (arg.startsWith("+")) {
										beacon.addPlayer(arg.substring(1));
										sender.sendMessage(ChatColor.YELLOW
												+ "Added " + arg.substring(1));
									} else if (arg.startsWith("-")) {
										beacon.delPlayer(arg.substring(1));
										sender.sendMessage(ChatColor.YELLOW
												+ "Removed " + arg.substring(1));
									} else {
										sender.sendMessage(ChatColor.YELLOW
												+ "What to do with \"" + arg
												+ "\"? :P");
									}
								}
							} else {
								sender.sendMessage(ChatColor.YELLOW
										+ "No permission to modify beacons.");
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return true;
			}
		});
	}

	public void checkPlayerEvent(Cancellable event, Player player, Location loc) {
		// System.out.print("type: "+event.toString());
		if (admins.containsKey(player)) {
			AdminExcept ae = admins.get(player);
			// Location aloc = player.getLocation();
			if (ae.x - ae.r < loc.getX() && ae.x + ae.r > loc.getX()
					&& ae.y - ae.r < loc.getY() && ae.y + ae.r > loc.getY()) {
				return;
			}
		}
		try {
			if (!manager.hasPermission(player, loc)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.YELLOW
						+ "You have no permission to do this action in this area.");
			}
		} catch (Exception e) {

		}
	}

	public static HashMap<Material, BeaconType> types = new HashMap<Material, BeaconType>();
	static {
		/*types.put(Material.DIAMOND_BLOCK, new BeaconType(0x40, 0x80));
		types.put(Material.GOLD_BLOCK, new BeaconType(0x30, 0x60));
		types.put(Material.LAPIS_BLOCK, new BeaconType(0x20, 0x50));
		types.put(Material.IRON_BLOCK, new BeaconType(0x10, 0x30));
		types.put(Material.GOLD_ORE, new BeaconType(0x0B, 0x20));
		types.put(Material.IRON_ORE, new BeaconType(0x08, 0x10));*/
		
		types.put(Material.DIAMOND_BLOCK, new BeaconType(0x40, 2*0x80));
		types.put(Material.GOLD_BLOCK, new BeaconType(0x30, 2*0x60));
		types.put(Material.LAPIS_BLOCK, new BeaconType(0x20, 2*0x50));
		types.put(Material.IRON_BLOCK, new BeaconType(0x10, 2*0x30));
		types.put(Material.GOLD_ORE, new BeaconType(0x0B, 2*0x20));
		types.put(Material.IRON_ORE, new BeaconType(0x0B, 2*0x10));
	}

}

class BeaconType {
	public int protection;
	public int border;

	public BeaconType(int protection, int border) {
		this.protection = protection;
		this.border = border;
	}

}