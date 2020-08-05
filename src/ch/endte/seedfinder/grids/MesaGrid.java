package ch.endte.seedfinder.grids;

/**
 * {@link Grid} made for {@link MesaPoint}
 *
 * @see Grid
 * @see MesaPoint
 */
public class MesaGrid extends Grid {
    public MesaGrid(int columns, int rows) {
        super(columns, rows);
    }

    @Override
    public MesaPoint getAt(int x, int z) throws NotWithinGridException {
        try {
            return (MesaPoint) grid[x][z];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NotWithinGridException();
        }
    }

    @Override
    public void initiateGrid() {
        for (int x = 0; x < this.columns; x++) {
            for (int z = 0; z < this.rows; z++) {
                this.grid[x][z] = new MesaPoint(x, z, this);
            }
        }
    }
}
