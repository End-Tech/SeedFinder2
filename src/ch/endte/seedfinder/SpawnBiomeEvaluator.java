package ch.endte.seedfinder;

import ch.endte.seedfinder.EvaluationTask.Context;

public class SpawnBiomeEvaluator {
	
	public static int SIZE = 555;
	public static int RIM_SIZE = 10;
	public static int SIZE_SQ = SIZE*SIZE;
	public static int RIM_OUTERSIZE_SQ = (SIZE+RIM_SIZE)*(SIZE+RIM_SIZE);
	public static int RIM_INNERSIZE_SQ = (SIZE-RIM_SIZE)*(SIZE-RIM_SIZE);
	
	public void evaluate(Context g, Result r) {
		// in this method the spawn chunk inside and the border of the spawn chunks
		// are evaluated according to their composition
		// the results are stored in r
		r.spawnCenterBiomes.addBiome(g.oSource.getBiome(0, 0, 0));
		for (int x = 1; x<=SIZE; x++) {
			r.spawnCenterBiomes.addBiome(g.oSource.getBiome(x, 0, 0));
			r.spawnCenterBiomes.addBiome(g.oSource.getBiome(-x, 0, 0));
			r.spawnCenterBiomes.addBiome(g.oSource.getBiome(0, 0, x));
			r.spawnCenterBiomes.addBiome(g.oSource.getBiome(0, 0, -x));
			int zLim = (int) Math.round(Math.sqrt(SIZE_SQ-x*x));
			for (int z = 1; z<=zLim; z++) {
				r.spawnCenterBiomes.addBiome(g.oSource.getBiome(x, 0, z));
				r.spawnCenterBiomes.addBiome(g.oSource.getBiome(-x, 0, z));
				r.spawnCenterBiomes.addBiome(g.oSource.getBiome(-x, 0, -z));
				r.spawnCenterBiomes.addBiome(g.oSource.getBiome(x, 0, -z));
			}
		}
		for (int x = SIZE-RIM_SIZE;x<=SIZE+RIM_SIZE;x++) {
			r.spawnRimBiomes.addBiome(g.oSource.getBiome(x, 0, 0));
			r.spawnRimBiomes.addBiome(g.oSource.getBiome(-x, 0, 0));
			r.spawnRimBiomes.addBiome(g.oSource.getBiome(0, 0, x));
			r.spawnRimBiomes.addBiome(g.oSource.getBiome(0, 0, -x));
		}
		for (int x = 1; x<=SIZE+RIM_SIZE; x++) {
			int zLim = (int) Math.round(Math.sqrt(RIM_OUTERSIZE_SQ-x*x));
			int z = (x < SIZE-RIM_SIZE)? (int) Math.round(Math.sqrt(RIM_INNERSIZE_SQ-x*x)) : 1;
			for (; z<=zLim; z++) {
				r.spawnRimBiomes.addBiome(g.oSource.getBiome(-x, 0, z));
				r.spawnRimBiomes.addBiome(g.oSource.getBiome(x, 0, z));
				r.spawnRimBiomes.addBiome(g.oSource.getBiome(-x, 0, -z));
				r.spawnRimBiomes.addBiome(g.oSource.getBiome(x, 0, -z));
			}
		}
		
	}

}
