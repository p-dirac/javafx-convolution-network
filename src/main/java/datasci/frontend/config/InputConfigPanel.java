package datasci.frontend.config;

import datasci.backend.model.InputConfig;
import datasci.frontend.util.Cert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel for internal layer configuration parameters
 */
public class InputConfigPanel extends GridPane {
    private static final Logger LOG = Logger.getLogger(InputConfigPanel.class.getName());
    //
    private TextField rowsField = new TextField();
    private TextField colsField = new TextField();

    public InputConfigPanel() {
    }

    /**
     * Initialize form for internal layer configuration parameters
     *
     */
    public void init() {
      //  GridPane grid = new GridPane();
        try {
            this.setVgap(10);
            this.setHgap(10);
            // grid row index
            int row = 0;
            //
            //
            Label labelRows = new Label("Image rows (pixel height):");
            // grid cell index: col, row
            this.add(labelRows, 0, row);
            this.add(rowsField, 1, row);
            Cert.checkIntField(rowsField);
            //
            Label labelCols = new Label("Image cols (pixel width):");
            // grid cell index: col, row
            this.add(labelCols, 0, ++row);
            this.add(colsField, 1, row);
            Cert.checkIntField(colsField);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
    private boolean validateNumberFields() {
        boolean isOk = false;
        try {
            AtomicBoolean isValid1 = Cert.validateIntField(rowsField);
            AtomicBoolean isValid2 = Cert.validateIntField(colsField);
            //
            if (isValid1.get() && isValid2.get()) {
                isOk = true;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }

    public InputConfig getConfig() {
        InputConfig config = new InputConfig();
        try {
            // allow zero to edit later ?
            config.rows = 0;
            config.cols = 0;
            if (validateNumberFields()) {
                config.rows = Integer.parseInt(rowsField.getText());
                config.cols = Integer.parseInt(colsField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setConfig(InputConfig config) {
        try {
            rowsField.setText(Integer.toString(config.rows));
            colsField.setText(Integer.toString(config.cols));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class
