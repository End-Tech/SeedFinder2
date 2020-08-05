package ch.endte.seedfinder;

import ch.endte.seedfinder.EvaluationTask.Context;
import ch.endte.seedfinder.EvaluationTask.Result;
import ch.endte.seedfinder.grids.*;
import kaptainwutax.biomeutils.Biome;
import kaptainwutax.seedutils.mc.pos.CPos;

import java.util.ArrayList;

public class MesaBiomeEvaluator {
	public final static int DISTANCE_BETWEEN_POINTS = 500; // The lower this number is, the more accurate the finder will be
	public final static int REQUIRED_SIZE = 4; // 4 * 500 = 2000 blocks
	public final static int DISTANCE_FROM_SPAWN = 50_000;

	/**
	 * Finds the closes/largest mesa strip to spawn.
	 * Start and end points are put into r.
	 * Takes around 0.8 second to compute.
	 *
	 * @param g Context
	 * @param r Result
	 */
	public void evaluate(Context g, Result r) {

		ArrayList<MesaPoint> alreadyUsedPoints = new ArrayList<MesaPoint>();
		int gridLength = DISTANCE_FROM_SPAWN * 2 / DISTANCE_BETWEEN_POINTS;
		MesaGrid biomeGrid = new MesaGrid(gridLength, gridLength);
		ArrayList<MesaPoint> confirmedMesas = new ArrayList<MesaPoint>(); // Just not to loop through unecessary points

		// Creates a 2D array of points, distanced by 50k blocks to each other, within `distance` blocks from 0, 0.
		// If a point is true, it is a Mesa.
		int scale = g.oSource.biomes.getScale();
		biomeGrid.initiateGrid();

		for (int x = 0; x < biomeGrid.getRows(); x++) {
			for (int z = 0; z < biomeGrid.getColumns(); z++) {

				MesaPoint mesaPoint;

				try {
					mesaPoint = biomeGrid.getAt(x, z);
				} catch (NotWithinGridException e) {
					e.printStackTrace();
					continue;
				}

				int biomeId = g.oSource.biomes.get(
						Math.floorDiv(x * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN, scale),
						0,
						Math.floorDiv(z * DISTANCE_BETWEEN_POINTS - DISTANCE_FROM_SPAWN, scale)
				);

				if (biomeId == Biome.BADLANDS.getId() || biomeId == Biome.BADLANDS_PLATEAU.getId() || biomeId == Biome.ERODED_BADLANDS.getId()) {
					confirmedMesas.add(mesaPoint);
					mesaPoint.setIfMesa(true);
				}
			}
		}

		// Checks for 2k+ long mesas
		for (MesaPoint mesaPoint : confirmedMesas) {

			// NOTE: We shouldn't need to check for that, as it can only create duplicates
			// when a Mesa is 2500+ block long, which are very rare. It slows down the process.
			// Feel free to add this check back if needed.

//			if (alreadyUsedPoints.contains(mesaPoint)) continue; // To prevent duplicates

			MesaPoint tempMesaPoint = mesaPoint;
			int size = 0;

			/*
			We only need to check for long mesas in these directions,
			as we will already have had checked for long mesas in previous points

			+  +  +  +  +
			+  +  +  +  +
			+  +  o––––––
			+  +  |  +  +
			+  +  |  +  +
			 */

			try {
				while ((tempMesaPoint = (MesaPoint) tempMesaPoint.north()).isMesa()) {
					size++;
//					alreadyUsedPoints.add(tempMesaPoint);
				}
			} catch (NotWithinGridException e) { /* 😎 */ }

			tempMesaPoint = mesaPoint;

			if (size < REQUIRED_SIZE) size = 0;
			else {
//				alreadyUsedPoints.add(mesaPoint);
				handleLongMesa(mesaPoint, size, r);
				continue;
			}

			try {
				while ((tempMesaPoint = (MesaPoint) tempMesaPoint.west()).isMesa()) {
					size++;
//					alreadyUsedPoints.add(tempMesaPoint);
				}
			} catch (NotWithinGridException e) { /* haha error handling go brrrr */ }

			if (size >= REQUIRED_SIZE) {
//				alreadyUsedPoints.add(mesaPoint);
				handleLongMesa(mesaPoint, size, r);
			}
		}
	}

	private void handleLongMesa(MesaPoint mesaPoint, int size, Result r) {
		System.out.println(mesaPoint);

		r.longMesaLength = size * DISTANCE_BETWEEN_POINTS;

		r.longMesaStart = new CPos(
				mesaPoint.x >> 4,
				mesaPoint.z >> 4
		);
		r.longMesaStart = new CPos(
				(mesaPoint.x + r.longMesaLength) >> 4,
				(mesaPoint.z + r.longMesaLength) >> 4
		);
	}
}
