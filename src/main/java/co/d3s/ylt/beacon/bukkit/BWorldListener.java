package co.d3s.ylt.beacon.bukkit;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import co.d3s.ylt.beacon.beacon.BChunk;
import co.d3s.ylt.beacon.beacon.BWorld;

public class BWorldListener implements Listener {
	BeaconPlugin beaconplug;

	public BWorldListener(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		World world = event.getWorld();
		Chunk chunk = event.getChunk();

		int cx = chunk.getX();
		int cz = chunk.getZ();
		try {
			BWorld bworld = BeaconPlugin.manager.world(world);

			BChunk bchunk = bworld.chunk(
					cx >> (BeaconPlugin.manager.chunkshift - 4),
					cz >> (BeaconPlugin.manager.chunkshift - 4)); // 32 chunks
																// rather than
																// 16

			for (int ix = 0; ix < 16; ix++) {
				// int x = (cx<<4)+ix;
				for (int iz = 0; iz < 16; iz++) {
					// int z = (cz<<4)+iz;
					Block block = chunk.getBlock(ix, 127, iz);
					block.setType(Material.AIR);
				}
			}
			// System.out.print("Chunk loaded: "+chunk.getX()+","+chunk.getZ()+".");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkUnload(ChunkUnloadEvent event) {

	}
}
