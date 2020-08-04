package ch.endte.seedfinder;

import java.util.Comparator;
import java.util.HashMap;

import kaptainwutax.biomeutils.Biome;

public class BiomeComposition {
	
	private final HashMap<Biome, Integer> composition = new HashMap<>();
	private int entryCounts = 0;
	
	public BiomeComposition() {}
	
	public String toString() {
		return "";
	}
	
	public void fromString() {
		
	}
	
	public void addBiome(Biome b) {
		addBiome(b, 1);
	}
	
	public void addBiome(Biome b, int bCount) {
		composition.compute(b, (bio ,i) -> (i==null) ? bCount : i+bCount);
		entryCounts+= bCount;
	}
	
	private class EntrySortComparator implements Comparator<Biome> {

		@Override
		public int compare(Biome arg0, Biome arg1) {
			return Integer.compare(composition.getOrDefault(arg0, 0), composition.getOrDefault(arg1, 0));
		}
		
	}
}
