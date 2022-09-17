package datasci.frontend.config;

import datasci.backend.model.GeneralConfig;
import datasci.frontend.util.Cert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type GeneralConfigPanel.
 */
public class GeneralConfigPanel {
    private static final Logger LOG = Logger.getLogger(GeneralConfigPanel.class.getName());
    public static final DecimalFormat SIZE_FMT = new DecimalFormat("##0.0#####");

    //
    private final TextField trainDirField = new TextField();
    private final TextField testDirField = new TextField();
    //
    private final TextField totalTrainField = new TextField();
    private final TextField totalTestField = new TextField();


    /**
     * Instantiates a new General config panel.
     */
    public GeneralConfigPanel() {
    }

    /**
     * Form for network configuration parameters
     *
     * @return pane with general configuration fields
     */
    public Pane init() {
        GridPane grid = new GridPane();
        try {
            grid.setVgap(10);
            grid.setHgap(10);
            int row = 0;
            //
            Label labelTrainDir = new Label("Training directory:");
            // grid cell index: col, row
            grid.add(labelTrainDir, 0, row);
            grid.add(trainDirField, 1, row);
            trainDirField.setPrefColumnCount(30);
            //
            Label labelTestDir = new Label("Testing directory:");
            // grid cell index: col, row
            grid.add(labelTestDir, 0, ++row);
            grid.add(testDirField, 1, row);
            // DEFAULT_PREF_COLUMN_COUNT = 12, need wider field for path
            testDirField.setPrefColumnCount(30);
            //
            Label labelTotalTrain = new Label("Total Training Samples to load:");
            // grid cell index: col, row
            grid.add(labelTotalTrain, 0, ++row);
            grid.add(totalTrainField, 1, row);
            Cert.checkIntField(totalTrainField);
            //
            Label labelTotalTest = new Label("Total Testing Samples to load:");
            // grid cell index: col, row
            grid.add(labelTotalTest, 0, ++row);
            grid.add(totalTestField, 1, row);
            Cert.checkIntField(totalTestField);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return grid;
    }

    private boolean validateNumberFields() {
        boolean isOk = false;
        try {
            AtomicBoolean isValid1 = Cert.validateIntField(totalTrainField);
            AtomicBoolean isValid2 = Cert.validateIntField(totalTestField);

            // check if all fields are valid
            if (isValid1.get() && isValid2.get() ) {
                isOk = true;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }

    /**
     * Gets config.
     *
     * @return the config
     */
    public GeneralConfig getConfig() {
        GeneralConfig generalConfig = new GeneralConfig();
        try {
            generalConfig.trainingDir = trainDirField.getText();
            generalConfig.testingDir = testDirField.getText();
            //
            // allow zero to edit later ?
            generalConfig.totalTrainingSamples = 0;

            if (validateNumberFields()) {
                generalConfig.totalTrainingSamples = Integer.parseInt(totalTrainField.getText());
                generalConfig.totalTestingSamples = Integer.parseInt(totalTestField.getText());
            } else{
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Fix error in General params",
                            ButtonType.OK);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // do nothing
                    }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return generalConfig;
    }

    /**
     * Sets config.
     *
     * @param generalConfig the general config
     */
    public void setConfig(GeneralConfig generalConfig) {
        try {
            trainDirField.setText(generalConfig.trainingDir);
            testDirField.setText(generalConfig.testingDir);
            //
            totalTrainField.setText(Integer.toString(generalConfig.totalTrainingSamples));
            totalTestField.setText(Integer.toString(generalConfig.totalTestingSamples));

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class
