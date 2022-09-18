package datasci.frontend.config;

import datasci.backend.model.BackPropConfig;
import datasci.frontend.util.Cert;
import javafx.scene.Group;
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
 * User interface form for entering network hyperparameters
 */
public class BackPropConfigPanel {
    private static final Logger LOG = Logger.getLogger(BackPropConfigPanel.class.getName());
    public static final DecimalFormat SIZE_FMT = new DecimalFormat("##0.0#####");

    // gradient descent rate table
    private final GradientRatePanel ratePanel = new GradientRatePanel();
    // lambda: L2 regularization parameter
    private final TextField lambdaField = new TextField();
    // mu:  momentum parameter
    private final TextField muField = new TextField();
    private final TextField batchField = new TextField();
    /**
     * Instantiates a new Hyper config panel.
     */
    public BackPropConfigPanel() {
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
            Label labelRate = new Label("Gradient descent rate:");
            Group gRate = createRatePanel();
            // grid cell index: col, row
            grid.add(labelRate, 0, row);
            grid.add(gRate, 1, row);
            //
            Label labelLambda = new Label("L2 Regularization parameter:");
            // grid cell index: col, row
            grid.add(labelLambda, 0, ++row);
            grid.add(lambdaField, 1, row);
            Cert.checkDblField(lambdaField);
            //
            Label labelMu = new Label("Momentum parameter:");
            // grid cell index: col, row
            grid.add(labelMu, 0, ++row);
            grid.add(muField, 1, row);
            Cert.checkDblField(muField);
            //
            Label labelBatch = new Label("Batch Size:");
            // grid cell index: col, row
            grid.add(labelBatch, 0, ++row);
            grid.add(batchField, 1, row);
            Cert.checkIntField(batchField);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return grid;
    }

    /**
     * Init gradient descent rate panel.
     */
    public Group createRatePanel() {
        // Advantage of Group: it cannot be resized
        Group g = new Group();
        try {
            Pane p = ratePanel.init();
            g.getChildren().add(p);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return g;
    }

    private boolean validateNumberFields() {
        boolean isOk = false;
        try {
            AtomicBoolean isValid4 = Cert.validateDoubleField(lambdaField);
            AtomicBoolean isValid5 = Cert.validateDoubleField(muField);
            AtomicBoolean isValid3 = Cert.validateIntField(batchField);

            // check if all fields are valid
            if (isValid3.get() &&isValid4.get() && isValid5.get()) {
                isOk = true;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }

    /**
     * Gets hyper parameters config.
     *
     * @return the config
     */
    public BackPropConfig getConfig() {
        BackPropConfig backPropConfig = new BackPropConfig();
        try {
            backPropConfig.batchSize = 1;
            if (validateNumberFields()) {
                backPropConfig.lambda = Double.parseDouble(lambdaField.getText());
                backPropConfig.mu = Double.parseDouble(muField.getText());
                backPropConfig.batchSize = Integer.parseInt(batchField.getText());
            } else{
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Fix error in BackProp params",
                            ButtonType.OK);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // do nothing
                    }
            }
            //
            backPropConfig.rateModel = ratePanel.getRateModel();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return backPropConfig;
    }

    /**
     * Sets hyper parameters config.
     *
     * @param backPropConfig network hyper parameters
     */
    public void setConfig(BackPropConfig backPropConfig) {
        try {
            //
            ratePanel.setRateModel(backPropConfig.rateModel);
            //
            lambdaField.setText(SIZE_FMT.format(backPropConfig.lambda));
            muField.setText(SIZE_FMT.format(backPropConfig.mu));
            batchField.setText(Integer.toString(backPropConfig.batchSize));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}  //end class
