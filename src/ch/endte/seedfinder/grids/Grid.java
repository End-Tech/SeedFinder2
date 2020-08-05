package ch.endte.seedfinder.grids;

/**
 * Class representing a grid of {@link Point},
 * for easy access.
 *
 * @see Point
 */
public class Grid {
    protected Point[][] grid;
    protected final int columns;
    protected final int rows;

    public Grid(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        this.grid = new Point[columns][rows];
    }

    public void initiateGrid() {
        for (int x = 0; x < this.columns; x++) {
            for (int z = 0; z < this.rows; z++) {
                this.grid[x][z] = new Point(x, z, this);
            }
        }
    }

    public Point getAt(int x, int z) throws NotWithinGridException {
        try {
            return grid[x][z];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }
}
