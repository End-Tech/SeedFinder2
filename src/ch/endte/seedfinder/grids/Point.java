package ch.endte.seedfinder.grids;

/**
 * Class representing a point in a {@link Grid}.
 * Provides methods to naviguate in that grid of points.
 *
 * @see Grid
 */
public class Point {
    public int x;
    public int z;
    private Grid grid;

    public Point(int x, int z, Grid grid) {
        this.x = x;
        this.z = z;
        this.grid = grid;
    }

    public Point north() throws NotWithinGridException {
        try {
            return this.grid.getAt(x - 1, z);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    public Point south() throws NotWithinGridException {
        try {
            return this.grid.getAt(x + 1, z);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    public Point west() throws NotWithinGridException {
        try {
            return this.grid.getAt(x, z - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    public Point east() throws NotWithinGridException {
        try {
            return this.grid.getAt(x, z + 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + this.x +
                ", z=" + this.z +
                '}';
    }
}