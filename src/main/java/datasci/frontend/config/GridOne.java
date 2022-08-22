package datasci.frontend.config;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The type FilterPane.
 */
public class GridOne extends GridPane {

    private static final Logger LOG = Logger.getLogger(GridOne.class.getName());
    // Save gridpane nodes in one-D array for access to a specific cell,
    // because GridPane has no method like getcell(int rowI, int colJ)
    // Array index = i*n + j to access cell(i, j) where i = row, j = column , n = number of columns
    protected Node[] cellArray = null;
    protected int size;
    protected int rows;
    protected int cols;

    /**
     * Create new grid
     */
    public GridOne() {
    }

    /**
     * Create new grid
     * @param rows number of rows in grid
     * @param cols number of columns in grid
     */
    public GridOne(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.size = rows * cols;
        cellArray = new Node[size];
    }

    /**
     * Create new filter GridPane
     */
    public void initCellGaps(int hgap, int vgap) {
        this.setHgap(hgap);
        this.setVgap(vgap);
    }

    /**
     * Get cell node at specific index.
     *
     * @param rowI row i, index is zero based
     * @param colJ col j, index is zero based
     * @return the node at index i,j
     */
    public Node getCell(int rowI, int colJ) {
        Node cell = null;
        try {
            if (rowI >= 0 && rowI < rows & colJ >= 0 && colJ < cols) {
                cell = cellArray[rowI * cols + colJ];
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return cell;
    }

    /**
     * Add cell node at specific index.
     *
     * @param rowI row i, index is zero based
     * @param colJ col j, index is zero based
     * @param cell node to add
     */
    public void setCell(int rowI, int colJ, Node cell) {
        try {
            if ((rowI >= 0 && rowI < rows) && (colJ >= 0 && colJ < cols)) {
                cellArray[rowI * cols + colJ] = cell;
                // note: for gridpane add method, indexes order is column, row
                this.add(cell, colJ, rowI);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Init grid with one-D cell array
     */
    public void initGrid(Node[] cellArray) {
        try {
            int len = cellArray.length;
            if (len == size) {
                // save cell array
                this.cellArray = cellArray;
                // init array of nodes for gridpane cells, index is zero based
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // one-D array index = i*n + j
                        // where i = row, j = column , n = number of columns
                        Node cell = cellArray[i * cols + j];
                        // add cell to grid
                        // note: for gridpane method, index order is column, row
                        this.add(cell, j, i);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        return "GridOne{" +
                "size=" + size +
                ", rows=" + rows +
                ", cols=" + cols +
                '}';
    }
}  //end class