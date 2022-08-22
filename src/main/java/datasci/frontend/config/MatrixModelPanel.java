package datasci.frontend.config;

import datasci.backend.model.ConvoConfig;
import datasci.backend.model.Matrix;
import datasci.frontend.util.Cert;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel for setting matrix models
 *
 */
public class MatrixModelPanel {
    private static final Logger LOG = Logger.getLogger(MatrixModelPanel.class.getName());
    //
    private final TextField filterSizeField = new TextField();
    private final TextField numFiltersField = new TextField();
    private ArrayList<Matrix> filterList;
    private Label listSizeLabel = new Label();
    private MessageFormat sizeFmt = new MessageFormat("Filter list size: {0}");


    public MatrixModelPanel() {
    }

    /**
     * Form for internal layer configuration parameters
     *
     * @return pane with internal configuration fields
     */
    public Pane createConvoPane() {
        // new vbox for grid and button bar
        VBox convo = new VBox(10);
        try {
            GridPane grid = new GridPane();
            grid.setVgap(10);
            grid.setHgap(10);
            // grid row index
            int row = 0;
            //
            Label convoLabel = new Label("Convolution layer config:");
            // grid cell index: col, row, colSpan, rowSpan
            grid.add(convoLabel, 0, row, 2, 1);

            //
            Label labelFilterSize = new Label("Filter Size:");
            // grid cell index: col, row
            grid.add(labelFilterSize, 0, ++row);
            grid.add(filterSizeField, 1, row);
            Cert.checkIntField(filterSizeField);
            //
            Label labelNum = new Label("Number of Filters:");
            // grid cell index: col, row
            grid.add(labelNum, 0, ++row);
            grid.add(numFiltersField, 1, row);
            Cert.checkIntField(numFiltersField);
            //

            //
            // add grid to vbox, which may contain several filter layer config grids
            convo.getChildren().add(grid);
            //
            // buttons for filter dialog box
            Button filtersBtn = new Button("Filters");
            //
            filtersBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "filters");
                    boolean isOK = validateFilterFields();
                    if(isOK){
                        int filterSize = Integer.parseInt(filterSizeField.getText());
                        LOG.log(Level.INFO, "filterSize: " + filterSize);
                        int numFilters = Integer.parseInt(numFiltersField.getText());
                        //
                        FilterDialog filterDialog = new FilterDialog(filterSize, numFilters, filterList);
                        Optional<ArrayList<Matrix>> result = filterDialog.showAndWait();
                        if (result.isPresent()) {
                            ArrayList<Matrix> filters = result.get();
                            //      LOG.log(Level.INFO, "showFontDialog, result: " + fontOption);
                            if (filters != null) {
                                filterList = filters;
                                updateSizeLabel();
                            } else {
                                //
                            }
                        }
                    }
                }
            });
            //
            HBox btnPanel = createFilterBtnBar(filtersBtn);
            convo.getChildren().add(btnPanel);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convo;
    }

    private void updateSizeLabel(){
        try{
            int oldSize = 0;
            if (filterList != null) {
                oldSize = filterList.size();
            }
            //
            Object[] objArray = {Integer.toString(oldSize)};
            String sizeMsg = sizeFmt.format(objArray);
            listSizeLabel.setText(sizeMsg);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

private HBox createFilterBtnBar(Button filtersBtn){
    HBox btnPanel = new HBox(20);
        try{
            updateSizeLabel();
            // filler to separate label and button
            Region filler = new Region();
            HBox.setHgrow(filler, Priority.ALWAYS);
            //
            btnPanel.getChildren().addAll(listSizeLabel, filler, filtersBtn);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return btnPanel;
}

    private boolean validateFilterFields(){
        boolean isOk = false;
        try{
            AtomicBoolean isValid1 = Cert.validateIntField(filterSizeField);
            AtomicBoolean isValid2 = Cert.validateIntField(numFiltersField);
            //
            if(isValid1.get() && isValid2.get()) {
                isOk = true;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return isOk;
    }
    //

    public ConvoConfig getConvoConfig() {
        ConvoConfig config = new ConvoConfig();
        try {
            // allow zero to edit later ?
            config.filterSize = 0;
            config.numFilters = 0;
            if(validateFilterFields()) {
                config.filterSize = Integer.parseInt(filterSizeField.getText());
                config.numFilters = Integer.parseInt(numFiltersField.getText());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setConvoConfig(ConvoConfig config) {
        try {
            filterSizeField.setText(Integer.toString(config.filterSize));
            numFiltersField.setText(Integer.toString(config.numFilters));
            updateSizeLabel();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /*
    public ConvoConfig getMatrixConfig() {
        ConvoConfig config = new ConvoConfig();
        try {
            //
            config.filterList = filterList;
            if(filterList == null){
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Matrix List is null",
                        ButtonType.OK);
                Optional<ButtonType> result = alert.showAndWait();
                //    LOG.log(Level.INFO, "alert result: " + result);
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // do nothing
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    public void setMatrixConfig(ConvoConfig config) {
        try {
            filterSizeField.setText(Integer.toString(config.filterSize));
            numFiltersField.setText(Integer.toString(config.numFilters));
            filterList = config.filterList;
            updateSizeLabel();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

     */

}  // end class
