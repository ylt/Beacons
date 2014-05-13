package co.d3s.ylt.beacon.dev;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import co.d3s.ylt.beacon.beacon.BBeacon;
import co.d3s.ylt.beacon.beacon.BManager;
import co.d3s.ylt.beacon.beacon.BWorld;

public class ResetChunks {
	public static Connection db;
	public static BManager manager;

	public static void main(String[] args) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://direct.d3s.co/mcosurvival_beacon";
			db = DriverManager.getConnection(url, "mco", "***password***");
		} catch (Exception e) {
			// except e;
			e.printStackTrace();
		}
		manager = new BManager(db);
		BWorld bworld = new BWorld(manager);

		PreparedStatement delChunks = db
				.prepareStatement("TRUNCATE TABLE `chunks`");
		//delChunks.executeUpdate();

		PreparedStatement getBeacons = db
				.prepareStatement("SELECT `id` FROM `beacons`  WHERE `world`='world_nether'");
		ResultSet results = getBeacons.executeQuery();
		while (results.next()) {
			BBeacon beacon = bworld.beacon(results.getInt(1));
			beacon.del();
			bworld.add(beacon);
		}
	}

}