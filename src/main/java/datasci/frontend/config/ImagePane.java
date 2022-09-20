package datasci.frontend.config;

import datasci.backend.model.Matrix;
import datasci.backend.model.MTX;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * ImagePane.
 */
public class ImagePane extends GridOne {

    private static final Logger LOG = Logger.getLogger(ImagePane.class.getName());
    private static final DecimalFormat fmtTwo = new DecimalFormat("0.00");
    // normalized image pixel 0 to 1
    // see ImageDataUtil.loadImageData in back end
    // rescaled pixel value for plot
    private static final double minPlotPix = 0.0;
    private static final double maxPlotPix = 1.0;

    /**
     * Create new grid from matrix
     *
     * @param m matrix to initialize the grid
     */
    public ImagePane(Matrix m) {
        super(m.rows, m.cols);
        setMatrix(m);
        initCellGaps(2, 2);
        this.setStyle("-fx-border-style: solid; -fx-border-width: 1; -fx-border-color: green;");
    }


    /**
     * Set this grid from a matrix with same number of rows and columns
     *
     * @param m input matrix
     */
    public void setMatrix(Matrix m) {
        try {
            LOG.info("image matrix: " + m);
            // rows and cols set from matrix m in constructor
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // scale 0 to 1
                    double d = MTX.getCell(m, i, j);
                    // new pane for each cell to set background color
                    Pane pane = new Pane();
                    String s = fmtTwo.format(d);
                    // user not allowed to edit matrix values
                    Label t = new Label();
                    t.setText(s);
                    pane.setStyle("-fx-background-color: white; ");
                    if (d > 0.75) {
                        pane.setStyle("-fx-background-color: yellow; ");
                    } else if (d > 0.5) {
                        pane.setStyle("-fx-background-color: PAPAYAWHIP; ");
                    } else if (d > 0.25) {
                        pane.setStyle("-fx-background-color:  #F1F1F1; ");
                    } else {
                        pane.setStyle("-fx-background-color: white; ");
                        // must set something to create cell width, since zero width will not display
                        t.setText("       ");
                    }
                    pane.setPrefHeight(24);
                    pane.getChildren().add(t);
                    // add pane label to grid
                    setCell(i, j, pane);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class