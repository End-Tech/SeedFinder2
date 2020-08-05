package ch.endte.seedfinder.grids;

import ch.endte.seedfinder.MesaBiomeEvaluator;

/**
 * {@link Point} that stores wether it is a Mesa or not.
 *
 * @see Point
 * @see MesaGrid
 */
public class MesaPoint extends Point {
    private boolean isMesa = false;

    public MesaPoint(int x, int z, Grid grid) {
        super(x, z, grid);
    }

    public boolean isMesa() {
        return this.isMesa;
    }

    public void setIfMesa(boolean isMesa) {
        this.isMesa = isMesa;
    }

    @Override
    public String toString() {
        int xRealCoordinate = this.x * MesaBiomeEvaluator.DISTANCE_BETWEEN_POINTS - MesaBiomeEvaluator.DISTANCE_FROM_SPAWN;
        int zRealCoordinate = this.z * MesaBiomeEvaluator.DISTANCE_BETWEEN_POINTS - MesaBiomeEvaluator.DISTANCE_FROM_SPAWN;

        return "MesaPoint{" +
                "x=" + xRealCoordinate +
                ", z=" + zRealCoordinate +
                '}';
    }
}
