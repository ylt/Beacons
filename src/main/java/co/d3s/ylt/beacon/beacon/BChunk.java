package co.d3s.ylt.beacon.beacon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class BChunk {
	public int time = (int) (System.currentTimeMillis()/1000);
	
	public LinkedList<BBeacon> pbeacons = new LinkedList<BBeacon>();
	public LinkedList<BBeacon> bbeacons = new LinkedList<BBeacon>();

	public static PreparedStatement getBeacons;
	
	public BChunk(BWorld world, int x, int z) throws SQLException {
		// System.out.print("Construct "+x+", "+z);
		if (getBeacons == null) {
			getBeacons = world.manager.db
					.prepareStatement("SELECT `type`,`id` FROM `chunks`  WHERE `x`=? AND `z`=? AND `world`=?");
		}
		getBeacons.setInt(1, x);
		getBeacons.setInt(2, z);
		getBeacons.setString(3, world.world.getName());
		ResultSet results = getBeacons.executeQuery();
		while (results.next()) {
			if (results.getInt(1) == 0) {
				pbeacons.add(world.beacon(results.getInt(2)));
			} else {
				bbeacons.add(world.beacon(results.getInt(2)));
			}
		}
	}

	public LinkedList<BBeacon> getBeacons(int x, int z) {
		LinkedList<BBeacon> b = new LinkedList<BBeacon>();
		for (BBeacon beacon : pbeacons) {
			if (beacon.inside(x, z, 0)) {
				b.add(beacon);
			}
		}
		return b;
	}

	public boolean inBeacon(int x, int z, int type) {
		for (BBeacon beacon : pbeacons) {
			if (beacon.inside(x, z, type)) {
				return true;
			}
		}
		if (type == 1) {
			for (BBeacon beacon : bbeacons) {
				if (beacon.inside(x, z, type)) {
					return true;
				}
			}
		}
		return false;
	}

	public void add(BBeacon beacon, int type, boolean query) {
		if (type == 0) {
			pbeacons.add(beacon);
			// X add to db
		} else if (!pbeacons.contains(beacon)) {
			bbeacons.add(beacon);
			// X add to db
		}
	}

	public void del(BBeacon beacon, boolean query) {
		if (pbeacons.contains(beacon)) {
			pbeacons.remove(beacon);
			// remove from db
		} else if (bbeacons.contains(beacon)) {
			bbeacons.remove(beacon);
			// remove from db
		}
	}
}
