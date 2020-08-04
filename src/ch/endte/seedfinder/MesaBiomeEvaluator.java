package ch.endte.seedfinder;

import ch.endte.seedfinder.EvaluationTask.Context;
import ch.endte.seedfinder.EvaluationTask.Result;
import kaptainwutax.biomeutils.Biome;

import java.util.ArrayList;

public class MesaBiomeEvaluator {
	private final static int REQUIRED_SIZE = 4;
	private final static int DISTANCE_BETWEEN_POINTS = 500;
	private final static int DISTANCE_FROM_SPAWN = 50_000;

	/**
	 * Finds the closes/largest mesa strip to spawn.
	 * Start and end points are put into r.
	 *
	 * @param g Context
	 * @param r Result
	 */
	public void evaluate(Context g, Result r) {

		ArrayList<int[]> alreadyUsedPoints = new ArrayList<int[]>();
		int gridLength = DISTANCE_FROM_SPAWN * 2 / DISTANCE_BETWEEN_POINTS;
		boolean[][] grid = new boolean[gridLength][gridLength];

		// Creates a 2D array of points, distanced by 50k blocks to each other, within `distance` blocks from 0, 0.
		// If a point is true, it is a Mesa.
		for (int x = 0; x < gridLength; x++) {
			for (int z = 0; z < gridLength; z++) {
				int scale = g.oSource.biomes.getScale();
				int biomeId = g.oSource.biomes.get(
						Math.floorDiv(x * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN, scale),
						0,
						Math.floorDiv(z * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN, scale)
				);

				grid[x][z] = (biomeId == Biome.BADLANDS.getId() || biomeId == Biome.BADLANDS_PLATEAU.getId() || biomeId == Biome.ERODED_BADLANDS.getId());
			}
		}

		// Checks for 2k+ long mesas
		for (int x = 0; x < gridLength; x++) {
			for (int z = 0; z < gridLength; z++) {

				if (grid[x][z] /* Is a Mesa */ && !isAlreadyUsed(x, z, alreadyUsedPoints)) {
					int tempX = x;
					int tempZ = z;
					int size = 0;

					try {
						while (grid[++tempX][tempZ]) {
							size++;
							alreadyUsedPoints.add(new int[]{tempX, tempZ});
						}
					} catch (ArrayIndexOutOfBoundsException e) { /* 😎 */ }

					tempX = x;

					try {
						while (grid[--tempX][tempZ]) {
							size++;
							alreadyUsedPoints.add(new int[]{tempX, tempZ});
						}
					} catch (ArrayIndexOutOfBoundsException e) { /* reeeeee */ }

					if (size < REQUIRED_SIZE) size = 0;
					else {
						itsAGudMesa(x, z, alreadyUsedPoints, r);
					}

					try {
						while (grid[tempX][++tempZ]) {
							size++;
							alreadyUsedPoints.add(new int[]{tempX, tempZ});
						}
					} catch (ArrayIndexOutOfBoundsException e) { /* haha error handling go brrrr */ }

					tempZ = z;

					try {
						while (grid[tempX][--tempZ]) {
							size++;
							alreadyUsedPoints.add(new int[]{tempX, tempZ});
						}
					} catch (ArrayIndexOutOfBoundsException e) { /* endtech besttech */ }

					if (size >= REQUIRED_SIZE)
						itsAGudMesa(x, z, alreadyUsedPoints, r);
				}
				;
			}
		}
	}

	private void itsAGudMesa(int x, int z, ArrayList<int[]> alreadyUsedPoints, Result r) {
		alreadyUsedPoints.add(new int[]{x, z});
		int xRealCoordinate = x * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN;
		int zRealCoordinate = z * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN;
		System.out.println("x" + xRealCoordinate + " z" + zRealCoordinate);
	}

	private boolean isAlreadyUsed(int x, int z, ArrayList<int[]> alreadyUsedPoints) {
		for (int[] coordinates : alreadyUsedPoints) {
			if (coordinates[0] /* x axis */ == x && coordinates[1] /* z axis */ == z)
				return true;
		}

		return false;
	}
}
