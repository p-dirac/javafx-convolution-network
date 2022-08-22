package datasci.frontend.config;

import datasci.backend.model.Matrix;
import datasci.frontend.util.ViewUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Scroll pane containing list of filter grids which can be edited
 */
public class FilterView {

    private static final Logger LOG = Logger.getLogger(FilterView.class.getName());
    //
    private final VBox formPanel = new VBox();
    private final GridOne gridForm = new GridOne();
    private final HBox btnPanel = new HBox();
    private final ScrollPane scroller = new ScrollPane();
    private final static double PORT_MIN_H = 200;
    // vertical panel for filter grids
    private final VBox filtersPanel = new VBox();
    // filter props
    private int numFilters;
    private int rows;
    private int cols;
    private final RadioButton btnManual = new RadioButton("Manual");
    private final RadioButton btnRandom = new RadioButton("Random");
    private boolean isManual;
    // list of filters converted to matrix format
    private ArrayList<Matrix> filterList;
    // initial filters input, if any
    private ArrayList<Matrix> initFilters;


    /**
     * Instantiates a new Filter view.
     */
    public FilterView(ArrayList<Matrix> initFilters) {
        this.initFilters = initFilters;
    }

    /**
     * Gets scroller.
     *
     * @return the scroller
     */
    public ScrollPane getScroller() {
        return scroller;
    }

    /**
     * Init parameters
     */
    public Pane initFormPanel() {
        try {
            formPanel.setAlignment(Pos.TOP_CENTER);
            formPanel.setSpacing(10);
            formPanel.setStyle("-fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            //
            ToggleGroup groupHow = new ToggleGroup();
            btnManual.setToggleGroup(groupHow);
            btnManual.setSelected(true);
            btnRandom.setToggleGroup(groupHow);
            //
            HBox btnHowPanel = new HBox();
            btnHowPanel.getChildren().addAll(btnManual, btnRandom);
            ViewUtil.compact(btnHowPanel);
            //
            formPanel.getChildren().add(btnHowPanel);
            //
            Button createBtn = new Button("Create new filters");
            Button useOldBtn = new Button("Use old filters");
            Button deleteBtn = new Button("Delete all filters");
            btnPanel.getChildren().addAll(createBtn, useOldBtn, deleteBtn);
            ViewUtil.compact(btnPanel);
            //
            createBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    isManual = btnManual.isSelected();
                    LOG.log(Level.INFO, "New Filters");
                    if (rows > 1 && numFilters > 0) {
                        initFilters = null;
                        initFilterFields();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Number of filters and Filter size must be set");
                        alert.show();
                    }
                }
            });
            //
            useOldBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Use old Filters");
                    if (initFilters == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("No old filters");
                        alert.show();
                    } else {
                        if (initFilters != null) {
                            setMatrixList(initFilters);
                            int oldsize = initFilters.size();
                            if (numFilters > oldsize) {
                                int deltaNum = numFilters - oldsize;
                                if (deltaNum > 0) {
                                    updateFilters(deltaNum);
                                }
                            }
                        }
                        filterList = getMatrixList();

                    }
                }
            });
            //
            deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "delete Filters");
                    filtersPanel.getChildren().clear();
                    filterList = getMatrixList();
                }
            });

            //
            formPanel.getChildren().add(btnPanel);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return formPanel;
    }

    /**
     * Init filtersPanel and put in scroll pane
     *
     * @param rows       number of rows in each filter
     * @param cols       number of columns in each filter
     * @param numFilters number of filers
     */
    public ScrollPane initFiltersPanel(int rows, int cols, int numFilters) {
        try {
            this.rows = rows;
            this.cols = cols;
            this.numFilters = numFilters;
            //
            filtersPanel.setSpacing(5);
            //   filtersPanel.getChildren().clear();
            filtersPanel.setAlignment(Pos.TOP_CENTER);
            filtersPanel.setStyle("-fx-border-color: cyan; -fx-border-width: 1px;-fx-border-style: solid;");
            ViewUtil.compactW(filtersPanel);
            //

            //
            // add filtersPanel (VBox) to StackPane for center alignment within ScrollPane
            StackPane filtersHolder = new StackPane(filtersPanel);
            //   filtersHolder.setMinHeight(250);
            // make StackPane same width as ScrollPane
            filtersHolder.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                    scroller.getViewportBounds().getWidth(), scroller.viewportBoundsProperty()));
            scroller.setMinViewportHeight(PORT_MIN_H);

            //    scroller.setStyle("-fx-border-color: red; -fx-border-width: 1px;-fx-border-style: solid;");
            scroller.setContent(filtersHolder);
            scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            //
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return scroller;
    }

    /**
     * Init filtersPanel with matrix grid for each filter
     */
    public void initFilterFields() {
        try {
            filtersPanel.getChildren().clear();
            for (int k = 0; k < numFilters; k++) {
                // either blank or random filters
                FilterPane filterPane = createFilterPane(rows, cols);
                filtersPanel.getChildren().add(filterPane);
                // note: margin is outside border
                // note: insets top,  right,  bottom,  left
                VBox.setMargin(filterPane, new Insets(5, 5, 5, 10));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Add more filter panels
     *
     * @param deltaNum increase in number of filters
     */
    public void updateFilters(int deltaNum) {
        try {
            for (int k = 0; k < deltaNum; k++) {
                // either blank or random filters
                FilterPane filterPane = createFilterPane(rows, cols);
                filtersPanel.getChildren().add(filterPane);
                // note: margin is outside border
                // note: insets top,  right,  bottom,  left
                VBox.setMargin(filterPane, new Insets(5, 5, 5, 10));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create one filter grid.
     *
     * @param rows number of rows in one filter
     * @param cols number of columns in one filter
     * @return the filter grid
     */
    public FilterPane createFilterPane(int rows, int cols) {
        FilterPane pane = new FilterPane(rows, cols);
        try {
            isManual = btnManual.isSelected();
            if (isManual) {
                pane.initManual();
            } else {
                pane.initRandom();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return pane;
    }

    public ArrayList<Matrix> getFilterList() {
        filterList = getMatrixList();
        return filterList;
    }

    /**
     * Set list of filtersPanel with string values
     *
     * @param filterList filtersPanel with string values
     */
    public void setMatrixList(ArrayList<Matrix> filterList) {
        try {
            ObservableList<Node> nodeList = filtersPanel.getChildren();
            nodeList.clear();
            for (Matrix f : filterList) {
                FilterPane pane = new FilterPane(rows, cols);
                pane.setFilterMatrix(f);
                nodeList.add(pane);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get list of filter matrix containing double arrays
     *
     * @return list of filter matrix with double values
     */
    public ArrayList<Matrix> getMatrixList() {
        ArrayList<Matrix> matrxList = new ArrayList<>();
        try {
            ObservableList<Node> nodeList = filtersPanel.getChildren();
            for (Node n : nodeList) {
                // node is pane
                FilterPane pane = (FilterPane) n;
                // get filter matrix
                Matrix m = pane.getFilterMatrix();
                matrxList.add(m);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return matrxList;
    }

}  //end class