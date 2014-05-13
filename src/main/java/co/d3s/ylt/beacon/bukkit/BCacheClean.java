package co.d3s.ylt.beacon.bukkit;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.World;

import co.d3s.ylt.beacon.beacon.BChunk;
import co.d3s.ylt.beacon.beacon.BWorld;

public class BCacheClean implements Runnable {
	BeaconPlugin beaconplug;

	public BCacheClean(BeaconPlugin beaconplug) {
		this.beaconplug = beaconplug;
	}

	@Override
	public void run() {
		int time = (int) (System.currentTimeMillis()/1000);
		for(Entry<World, BWorld> wentry : beaconplug.manager.worlds.entrySet()) {
			BWorld bworld = wentry.getValue();
			Iterator<Entry<Long, BChunk>> it = bworld.chunks.entrySet().iterator();
			//for(Entry<Long, BChunk> centry : bworld.chunks.entrySet()){
			while(it.hasNext()) {
				Entry<Long, BChunk> centry = it.next();
				BChunk bchunk = centry.getValue();
				if (bchunk.time-time > 5*60*1000) { //5 mins
					it.remove();
				}
			}
		}
	}

}
