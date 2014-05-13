package co.d3s.ylt.beacon.beacon;

import java.lang.ref.WeakReference;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.World;

public class BWorld {
	HashMap<Integer, WeakReference<BBeacon>> beacons = new HashMap<Integer, WeakReference<BBeacon>>();
	World world;
	BManager manager;

	public BWorld(World world, BManager manager) {
		this.world = world;
		this.manager = manager;
	}

	public BWorld(BManager manager) {
		this.manager = manager;
	}

	public BBeacon beacon(int id) throws SQLException {
		WeakReference<BBeacon> ref = beacons.get(id);
		BBeacon beacon = null;
		if (ref != null)
			beacon = beacons.get(id).get();

		if (beacon != null) {
			return beacon;
		}
		beacon = new BBeacon(this, id);
		beacons.put(id, new WeakReference<BBeacon>(beacon));
		return beacon;
	}

	public HashMap<Long, BChunk> chunks = new HashMap<Long, BChunk>();

	static long toLong(int msw, int lsw) {
		return (((long) msw) << 32) + lsw;
	}

	public BChunk lchunk(int x, int z) {
		return chunks.get(toLong(x, z));
	}

	public BChunk chunk(int x, int z) throws SQLException {
		// (05:15) <ylt> does it cast like ((long)x)<<32 or (long)(x<<32) with
		// the way i've written it?
		BChunk chunk = lchunk(x, z);
		if (chunk != null) {
			return chunk;
		}
		chunk = new BChunk(this, x, z);
		chunks.put(toLong(x, z), chunk);
		return chunk;
	}

	public LinkedList<BBeacon> getBeacons(int x, int z) throws SQLException {
		return chunk(x >> manager.chunkshift, z >> manager.chunkshift)
				.getBeacons(x, z);
	}

	public boolean inBeacon(int x, int z, int type) throws SQLException {
		return chunk(x >> manager.chunkshift, z >> manager.chunkshift)
				.inBeacon(x, z, type);
	}

	public static PreparedStatement addchunk;

	public void add(BBeacon beacon) throws SQLException {
		// if (beacons.containsValue(beacon)) {
		//del(beacon);
		// }
		beacon.add();

		if (addchunk == null) {
			addchunk = manager.db
					.prepareStatement("INSERT INTO `chunks` (`x`,`z`,`id`,`type`,`world`) VALUES (?,?,?,?,?);");
		}

		// all to ALL chunks
		/*
		 * int pradius = (int) Math.ceil(beacon.pr/16); int bradius = (int)
		 * Math.ceil(beacon.br/16); for(int ix=-bradius; ix<=bradius; ix++) {
		 * for(int iz=-bradius; iz<=bradius; iz++) { int mode = 1; if
		 * (ix>=-pradius && ix <=pradius && iz >=-pradius && iz <=pradius) {
		 * mode = 0; }
		 * 
		 * int x = (int) (Math.floor(beacon.x/16)+ix); int z = (int)
		 * (Math.floor(beacon.z/16)+iz);
		 * 
		 * BChunk chunk = lchunk(x,z); if (chunk != null) { chunk.add(beacon,
		 * mode, true); }
		 * 
		 * addchunk.setInt(1, x); addchunk.setInt(2, z); addchunk.setInt(3,
		 * beacon.id); addchunk.setInt(4, mode); addchunk.executeUpdate(); } }
		 */
		int pradius = beacon.pr;
		int bradius = beacon.br;
		for (int ix = -bradius; ix <= bradius; ix += Math.pow(2,
				manager.chunkshift)) {
			for (int iz = -bradius; iz <= bradius; iz += Math.pow(2,
					manager.chunkshift)) {
				int mode = 1;
				/*
				 * if (Math.abs(ix)<=pradius && Math.abs(iz)<=pradius) { mode =
				 * 0; }
				 */

				int x = beacon.x + ix;
				int z = beacon.z + iz;
				int cx = x >> manager.chunkshift;
				int cz = z >> manager.chunkshift;

				if (cx >= (beacon.x - pradius) >> manager.chunkshift
						&& cx <= (beacon.x + pradius) >> manager.chunkshift
						&& cz >= (beacon.z - pradius) >> manager.chunkshift
						&& cz <= (beacon.z + pradius) >> manager.chunkshift) {
					mode = 0;
				}
				// cx<<5, cz<<5

				BChunk chunk = lchunk(cx, cz);
				if (chunk != null) {
					chunk.add(beacon, mode, true);
				}
				addchunk.setInt(1, cx);
				addchunk.setInt(2, cz);
				addchunk.setInt(3, beacon.id);
				addchunk.setInt(4, mode);
				addchunk.setString(5, world.getName());
				//addchunk.setString(5, "world_nether");
				addchunk.executeUpdate();
			}
		}
	}

	public static PreparedStatement delchunk;

	public void del(BBeacon beacon) throws SQLException {
		// delete from DB
		if (delchunk == null) {
			delchunk = manager.db
					.prepareStatement("DELETE FROM `chunks`  WHERE `id`=?;");
		}

		delchunk.setInt(1, beacon.id);
		delchunk.executeUpdate();

		// delete from cache in LOADED chunks
		/*
		 * int radius = (int) Math.ceil(beacon.br/16); for(int x=-radius;
		 * x<=radius; x++) { for(int z=-radius; z<=radius; z++) { BChunk chunk =
		 * lchunk(x,z); if (chunk != null) { chunk.del(beacon, false); } } }
		 */

		int bradius = beacon.br;
		for (int ix = -bradius; ix <= bradius; ix += Math.pow(2,
				manager.chunkshift)) {
			for (int iz = -bradius; iz <= bradius; iz += Math.pow(2,
					manager.chunkshift)) {
				int x = beacon.x + ix;
				int z = beacon.z + iz;
				// System.out.print(x+"="+(x>>5)+", "+z+"="+(z>>5));
				// >>5 = 32
				int cx = x >> manager.chunkshift;
				int cz = z >> manager.chunkshift;

				// if (beacon.inside(x, z, true)) { mode = 2; }

				BChunk chunk = lchunk(cx, cz);
				if (chunk != null) {
					chunk.del(beacon, true);
				}
			}
		}

		beacon.del();
	}
}
