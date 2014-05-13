package co.d3s.ylt.beacon.beacon;

import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class BBeacon {
	public int id = -1;

	BWorld world;
	String sworld;

	public int x;
	public int y;
	public int z;

	public int pr; // protect radius
	public int br; // border radius

	public LinkedList<String> players = new LinkedList<String>();

	public BBeacon(BWorld world, int x, int y, int z, int pradius, int bradius) {
		this.world = world;
		this.sworld = world.world.getName();
		this.x = x;
		this.y = y;
		this.z = z;
		this.pr = pradius;
		this.br = bradius;
	}

	public static PreparedStatement getbeacon;
	public static PreparedStatement getusers;

	public BBeacon(BWorld world, int id) throws SQLException {
		this.id = id;
		if (getbeacon == null) {
			getbeacon = world.manager.db
					.prepareStatement("SELECT `world`,`x`,`y`,`z`,`pradius`,`bradius` FROM `beacons` WHERE `id`=? LIMIT 1;");
		}
		if (getusers == null) {
			getusers = world.manager.db
					.prepareStatement("SELECT `user` FROM `users` WHERE `id`=?;");
		}
		this.world = world;

		getbeacon.setInt(1, id);
		ResultSet results = getbeacon.executeQuery();
		if (results.next()) {
			sworld = results.getString(1);
			x = results.getInt(2);
			y = results.getInt(3);
			z = results.getInt(4);
			pr = results.getInt(5);
			br = results.getInt(6);
		}

		getusers.setInt(1, id);
		results = getusers.executeQuery();
		while (results.next()) {
			players.add(results.getString(1));
		}

	}

	public boolean inside(int cx, int cz, int type) {
		if (x - pr <= cx && x + pr >= cx && z - pr <= cz && z + pr >= cz) {
			// System.out.print("within protection.");
			return true;
		}
		if (type == 1 && x - br <= cx && x + br >= cx && z - br <= cz
				&& z + br >= cz) {
			// System.out.print("within border.");
			return true;
		}
		// System.out.print("outside.");
		return false;
	}

	public static PreparedStatement addbeacon;
	public static PreparedStatement addplayer;
	public static PreparedStatement delplayer;

	public void add() throws SQLException {
		if (addbeacon == null) {
			addbeacon = world.manager.db
					.prepareStatement(
							"INSERT INTO `beacons` (`world`,`x`,`y`,`z`,`pradius`,`bradius`) VALUES (?,?,?,?,?,?);",
							new String[] { "id" }
					// Statement.RETURN_GENERATED_KEYS
					);
		}
		if (addplayer == null) {
			addplayer = world.manager.db
					.prepareStatement("INSERT INTO `users` (`id`,`user`) VALUES (?,?);");
		}
		addbeacon.setString(1, sworld);
		addbeacon.setInt(2, x);
		addbeacon.setInt(3, y);
		addbeacon.setInt(4, z);
		addbeacon.setInt(5, pr);
		addbeacon.setInt(6, br);
		addbeacon.executeUpdate();
		ResultSet keys = addbeacon.getGeneratedKeys();
		System.out.print(keys);
		System.out.print(keys.next());
		id = keys.getInt(1);
		System.out.print(id);

		addplayer.setInt(1, id);
		for (String player : players) {
			addplayer.setString(2, player);
			addplayer.executeUpdate();
		}

		world.beacons.put(id, new WeakReference<BBeacon>(this));
	}

	public static PreparedStatement delbeacon;
	public static PreparedStatement delplayers;

	public void del() throws SQLException {
		if (delbeacon == null) {
			delbeacon = world.manager.db
					.prepareStatement("DELETE FROM `beacons` WHERE `id`=?");
		}
		delbeacon.setInt(1, id);
		delbeacon.executeUpdate();

		if (delplayers == null) {
			delplayers = world.manager.db
					.prepareStatement("DELETE FROM `users` WHERE `id`=?");
		}
		delplayers.setInt(1, id);
		delplayers.executeUpdate();

		world.beacons.remove(this.id);
		id = -1;
	}

	@Override
	public String toString() {

		return null;
	}

	public void addPlayer(String name) throws SQLException {
		if (addplayer == null) {
			addplayer = world.manager.db
					.prepareStatement("INSERT INTO `users` (`id`,`user`) VALUES (?,?)");
		}
		name = name.toLowerCase();
		if (!players.contains(name)) {
			players.add(name);
			if (id != -1) {
				addplayer.setInt(1, id);
				addplayer.setString(2, name);
				addplayer.executeUpdate();
			}
		}
	}

	public void delPlayer(String name) throws SQLException {
		if (delplayer == null) {
			delplayer = world.manager.db
					.prepareStatement("DELETE FROM `users` WHERE `id`=? AND `user`=?;");
		}
		name = name.toLowerCase();
		if (players.contains(name)) {
			players.remove(name);
			if (id != -1) {
				delplayer.setInt(1, id);
				delplayer.setString(2, name);
				delplayer.executeUpdate();
			}
		}

	}
}
