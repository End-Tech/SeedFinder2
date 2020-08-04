package ch.endte.seedfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import kaptainwutax.biomeutils.Biome;

public class BiomeComposition {
	
	private final HashMap<Biome, Integer> composition = new HashMap<>();
	
	public BiomeComposition() {}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ArrayList<Biome> bl = new ArrayList<Biome>(composition.keySet());
		Collections.sort(bl, new EntrySortComparator());
		// sort output for no reason whatsoever
		boolean bb = false;
		for (Biome b: bl) {
			if (bb) {sb.append("|");}
			else {bb = true;}
			sb.append(b.getName()+":"+composition.get(b));
		}
		return sb.toString();
	}
	
	public void fromString(String s) {
		
	}
	
	public void addBiome(Biome b) {
		addBiome(b, 1);
	}
	
	public void addBiome(Biome b, int bCount) {
		composition.compute(b, (bio ,i) -> (i==null) ? bCount : i+bCount);
	}
	
	private class EntrySortComparator implements Comparator<Biome> {

		// sort from highest to lowest
		@Override
		public int compare(Biome arg0, Biome arg1) {
			return Integer.compare(composition.getOrDefault(arg1, 0), composition.getOrDefault(arg0, 0));
		}
		
	}
}
