package ch.endte.seedfinder;

import ch.endte.seedfinder.EvaluationTask.Context;
import ch.endte.seedfinder.EvaluationTask.Result;
import kaptainwutax.biomeutils.Biome;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MesaBiomeEvaluator {
	private final static int REQUIRED_SIZE = 4;
	private final static int DISTANCE_BETWEEN_POINTS = 500;

	/**
	 * Finds the closes/largest mesa strip to spawn.
	 * Start and end points are put into r.
	 *
	 * @param g Context
	 * @param r Result
	 * @param distance Distance from spawn the Mesa will be in
	 */
	public void evaluate(Context g, Result r, int distance) {

		ArrayList<int[]> alreadyUsedPoints = new ArrayList<int[]>();
		int gridLength = distance / DISTANCE_BETWEEN_POINTS;
		boolean[][] grid = new boolean[gridLength][gridLength];

		// Creates a 2D array of points, distanced by `DISTANCE_BETWEEN_POINTS` blocks to each other, within `distance` blocks from 0, 0.
		// If a point is true, it is a Mesa.
		for (int x = 0; x < gridLength; x++) {
			for (int z = 0; z < gridLength; z++) {
				int biomeId = g.oSource.biomes.get(x*DISTANCE_BETWEEN_POINTS, 0, z*DISTANCE_BETWEEN_POINTS);
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
						itsAGudMesa(x, z, alreadyUsedPoints);
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
						itsAGudMesa(x, z, alreadyUsedPoints);
				};
			}
		}
	}

	private void itsAGudMesa(int x, int z, ArrayList<int[]> alreadyUsedPoints) {
		alreadyUsedPoints.add(new int[]{x, z});
		System.out.println("x" + x*DISTANCE_BETWEEN_POINTS + " z" + z*DISTANCE_BETWEEN_POINTS);
	}

	private boolean isAlreadyUsed(int x, int z, ArrayList<int[]> alreadyUsedPoints) {
		for (int[] coordinates : alreadyUsedPoints) {
			if (coordinates[0] /* x axis */ == x && coordinates[1] /* z axis */ == z)
				return true;
		}

		return false;
	}
}
