package datasci.frontend.ctrl;
/*******************************************************************************
 *
 * Copyright 2022 Ronald Cook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

import datasci.backend.model.FileUtil;
import datasci.backend.model.FitParams;
import datasci.backend.model.JsonUtil;
import datasci.backend.model.MTX;
import datasci.backend.model.Matrix;
import datasci.backend.model.NetConfig;
import datasci.frontend.util.SciFmtR;
import datasci.frontend.util.ViewUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 The type TaskView.
 */
public class TaskView {

    // private static final Logger LOG = Logger.getLogger(TaskView.class.getName());
    private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    private static final DecimalFormat fmtAsInt = new DecimalFormat("##0");
    private final BorderPane pane;
    private final VBox topPanel = new VBox();
    private final HBox ctrPanel = new HBox();
    //
    private final Button runBtn = new Button("Run task");
    private final Button stopBtn = new Button("Cancel task");
    private final Button snapBtn = new Button("Snapshot");
    private Pane snapPane;
    private final HBox btnPanel = new HBox();
    private final GridPane infoGrid = new GridPane();
    private final ProgressBar progressBar = new ProgressBar();
    //
    private final TabPane resultsTabPane = new TabPane();
    private final ObservableList<Tab> tabList = FXCollections.observableArrayList();
    //
    private final StackPane togPaneAll = new StackPane();
    private final String tog1 = "Cumulative\nPerformance";
    private final String tog2 = "Batch\nPerformance";
    private final String tog3 = "Summary";
    private final StackPane togPane1 = new StackPane();
    private final StackPane togPane2 = new StackPane();
    private final StackPane togPane3 = new StackPane();
    private final ToggleButton tb1 = new ToggleButton(tog1);
    private final ToggleButton tb2 = new ToggleButton(tog2);
    private final ToggleButton tb3 = new ToggleButton(tog3);

    //
    private final XYChart.Series<Number, Number> plotSeries = new XYChart.Series<>();
    private LineChart<Number, Number> performanceChart;
    //
    private final XYChart.Series<Number, Number> barSeries = new XYChart.Series<>();
    private LineChart<Number, Number> histoChart;

    //
    private final VBox summaryPanel = new VBox();
    // confusion matrix: column is predicted index, row is actual index
    private final GridPane summaryGrid = new GridPane();
    //
    private NetTask task;
    private TaskResult taskResult;
    private final HBox statusPanel = new HBox();
    private final TextArea statusField = new TextArea();
    private String statusMsg;
    //
    private int totalSamples;
    private int batchSize;
    private int subsetSize;
    private NetConfig netConfig;
    //
    private String netOption;
    private static final String TRAIN_NET = "Train Net";
    private static final String TEST_NET = "Test Net";
    // readyFitParams: true when FitParams is defined, otherwise false
    private final BooleanProperty readyFitParams = new SimpleBooleanProperty();
    // completedProp: true after task is completed, otherwise false
    private final BooleanProperty completedProp = new SimpleBooleanProperty();

    /**
     Creates new Task view.

     @param pane the pane to control the task and show results
     */
    public TaskView(BorderPane pane) {
        this.pane = pane;

    }

    public BorderPane getPane() {
        return pane;
    }

    public void setNetOption(String netOption) {
        this.netOption = netOption;
    }

    public boolean isReadyFitParams() {
        return readyFitParams.get();
    }

    public BooleanProperty readyFitParamsProperty() {
        return readyFitParams;
    }

    public void setReadyFitParams(boolean readyFitParams) {
        this.readyFitParams.set(readyFitParams);
    }

    public boolean isCompletedProp() {
        return completedProp.get();
    }

    public BooleanProperty completedPropProperty() {
        return completedProp;
    }

    public void setCompletedProp(boolean completedProp) {
        this.completedProp.set(completedProp);
    }

    /**
     Init network configuration.
     */
    public void initConfig(NetConfig netConfig) {
        try {
            this.netConfig = netConfig;
            setCompletedProp(false);
            //
            if (TRAIN_NET.equalsIgnoreCase(netOption)) {
                // total data samples to load for training network
                totalSamples = netConfig.generalConfig.totalTrainingSamples;
                // fit params not required before training run
                runBtn.setDisable(false);
            } else if (TEST_NET.equalsIgnoreCase(netOption)) {
                // total data samples to load for testing network
                totalSamples = netConfig.generalConfig.totalTestingSamples;
                // fit params must be loaded before test run
                runBtn.setDisable(true);
            } else {
                throw new RuntimeException("Error in netOption: " + netOption);
            }
            LOG.info("totalSamples: " + totalSamples);
            if (totalSamples == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Total samples is zero",
                        ButtonType.OK);
                Optional<ButtonType> result = alert.showAndWait();
                LOG.log(Level.INFO, "alert result: " + result);
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // do nothing
                }
            }
            subsetSize = subsetSize(totalSamples);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init panels.
     */
    public void initPanels() {
        try {
            initTopPanel();
            initCtrPanel();
            //
            pane.setTop(topPanel);
            pane.setCenter(ctrPanel);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init top panel.
     */
    public void initTopPanel() {
        try {
            topPanel.getChildren().clear();
            // let topPanel fill the width of the outer pane
            //    topPanel.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            topPanel.setSpacing(4);
            topPanel.setAlignment(Pos.CENTER);
            // //insets: top, right, bottom, left
            //     topPanel.setStyle("-fx-padding: 5 10 5 10;");
            topPanel.setStyle("-fx-padding: 5 10 5 10; -fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            initBtnPanel();
            /*
            Note: topPanel will center its child nodes if they are not expanded to fill space
             Why is btnPanel is added to a Group?
             Without the Group, topPanel will expand the btnPanel size to the width of topPanel, and the buttons will
             not appear to be centered. With the Group, topPanel will not resize it. The Group will have the
              exact size of the buttons, and will be centered within the topPanel.
             "Group is not directly resizable"
             "a Group will "auto-size" its managed resizable children to their preferred sizes
             during the layout pass"
             See initBtnPanel for an alternative way to prevent btnPanel from expanding in width.
             */
            Group btnG = new Group(btnPanel);
            topPanel.getChildren().add(btnG);
            initProgressPanel();
            topPanel.getChildren().add(progressBar);
            initStatusPanel();
            topPanel.getChildren().add(statusPanel);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init button panel.
     */
    public void initBtnPanel() {
        try {
            btnPanel.getChildren().clear();
            runBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "run task");
                    NetConfig config = ConfigCache.getInstance().getConfig();
                    progressBar.setProgress(0);
                    initCtrPanel();
                    initConfig(config);
                    runTask(netOption);
                }
            });
            //
            stopBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "stop task");
                    cancelTask();
                }
            });
            //
            snapBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "snapshot");
                    takeSnap();
                }
            });

            //
            //   btnPanel.setStyle("-fx-border-color: red; -fx-border-width: 1px;-fx-border-style: solid;");
          /*
            See initTopPanel, why is btnPanel added to a Group?
            Alternative to adding btnPanel to a Group:
            btnPanel.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            btnPanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            btnPanel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
           */
            btnPanel.setSpacing(10);
            btnPanel.getChildren().addAll(runBtn, stopBtn, snapBtn);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init status panel.
     */
    public void initProgressPanel() {
        try {
            progressBar.setProgress(0.0);
            progressBar.setPrefSize(300, 20);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init status panel.
     */
    public void initStatusPanel() {
        try {
            statusPanel.setStyle("-fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            statusPanel.setSpacing(5);
            Label statusHeading = new Label("Status");
            statusPanel.getChildren().add(statusHeading);
            statusField.setPrefColumnCount(50);
            statusField.setPrefRowCount(2);
            statusPanel.getChildren().add(statusField);
            ViewUtil.compact(statusPanel);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init center panel.
     */
    public void initCtrPanel() {
        try {
            ctrPanel.getChildren().clear();
            togPane1.getChildren().clear();
            togPane2.getChildren().clear();
            togPane3.getChildren().clear();
            togPaneAll.getChildren().clear();
            //
            initPlotPanel();
            togPane1.getChildren().add(performanceChart);
            StackPane.setAlignment(performanceChart, Pos.TOP_CENTER);
            //
            initHistoPanel();
            togPane2.getChildren().add(histoChart);
            StackPane.setAlignment(histoChart, Pos.TOP_CENTER);
            //
            initSummaryPanel();
            togPane3.getChildren().add(summaryPanel);
            StackPane.setAlignment(summaryPanel, Pos.TOP_CENTER);
            //
            ToggleGroup group = new ToggleGroup();
            tb1.setToggleGroup(group);
            tb2.setToggleGroup(group);
            tb3.setToggleGroup(group);
            /*
            tb1.setOnAction(e -> {
                if (tb1.isSelected()) {
                    setToggleStyle(tb1.getText());
                }
            });
            tb2.setOnAction(e -> {
                if (tb2.isSelected()) {
                    setToggleStyle(tb2.getText());
                }
            });
            tb3.setOnAction(e -> {
                if (tb3.isSelected()) {
                    setToggleStyle(tb3.getText());
                }
            });

             */

            VBox togList = new VBox();
            togList.setSpacing(10);
            togList.getChildren().addAll(tb1, tb2, tb3);
            //
            // Create a ChangeListener for the ToggleGroup
            group.selectedToggleProperty().addListener(
                    new ChangeListener<Toggle>() {
                        public void changed(ObservableValue<? extends Toggle> ov,
                                            Toggle oldToggle, Toggle newToggle) {
                            LOG.info("newToggle: " + newToggle);
                            if (newToggle != null) {
                                String toggleName = ((ToggleButton) newToggle).getText();
                                // Switches visible pane to match selected toggle button
                                LOG.info("toggleName: " + toggleName);
                                setTogglePane(toggleName);
                                setToggleStyle(toggleName);
                            }
                        }
                    });

            //
            // must set only one pane to be visible within StackPane
            setTogglePane(tog2);
            setToggleStyle(tb2.getText());
            LOG.info("set tb2: " + tb2.getText());
            group.selectToggle(tb1);
            //  tb1.setSelected(true);
            togPaneAll.getChildren().addAll(togPane1, togPane2, togPane3);
            ScrollPane resultsPanel = new ScrollPane();
            resultsPanel.setContent(togPaneAll);
            //
            ctrPanel.getChildren().addAll(togList, resultsPanel);
            //insets: top, right, bottom, left
            //   ctrPanel.setStyle("-fx-padding: 5 10 5 10;");
            ctrPanel.setStyle("-fx-padding: 5 10 5 10; -fx-border-color: blue; -fx-border-width: 1px;-fx-border-style: solid;");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private void setToggleStyle(String toggleName) {
        try {
            switch (toggleName) {
                case tog1:
                    tb1.setStyle("-fx-background-color:white");
                    tb2.setStyle("-fx-background-color:GAINSBORO");
                    tb3.setStyle("-fx-background-color:GAINSBORO");
                    break;
                case tog2:
                    tb1.setStyle("-fx-background-color:GAINSBORO");
                    tb2.setStyle("-fx-background-color:white");
                    tb3.setStyle("-fx-background-color:GAINSBORO");
                    break;
                case tog3:
                    tb1.setStyle("-fx-background-color:GAINSBORO");
                    tb2.setStyle("-fx-background-color:GAINSBORO");
                    tb3.setStyle("-fx-background-color:white");
                    break;

                default:
                    break;
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     Switches visible pane to match selected toggle button

     @param toggleName selected toggle name
     */
    private void setTogglePane(String toggleName) {
        try {
            switch (toggleName) {
                case tog1:
                    togPane1.setVisible(true);
                    togPane2.setVisible(false);
                    togPane3.setVisible(false);
                    snapPane = togPane1;
                    break;
                case tog2:
                    togPane1.setVisible(false);
                    togPane2.setVisible(true);
                    togPane3.setVisible(false);
                    snapPane = togPane2;
                    break;
                case tog3:
                    togPane1.setVisible(false);
                    togPane2.setVisible(false);
                    togPane3.setVisible(true);
                    snapPane = togPane3;
                    break;

                default:
                    break;
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init plot panel to display network performance
     */
    public void initPlotPanel() {
        try {
            double upperBound = totalSamples;
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Samples");
            xAxis.setLowerBound(0.0);
            xAxis.setUpperBound(Math.rint(upperBound));
            xAxis.setTickUnit(Math.rint(upperBound / 10.0));
            xAxis.setMinorTickCount(2);
            xAxis.setAutoRanging(false);
            //
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("% Accuracy");
            yAxis.setLowerBound(0.0);
            yAxis.setUpperBound(100.0);
            yAxis.setTickUnit(10.0);
            yAxis.setMinorTickCount(2);
            yAxis.setAutoRanging(false);
            //
            performanceChart = new LineChart(xAxis, yAxis);
            // setAnimated to false to allow snapshot
            performanceChart.setAnimated(false);
            performanceChart.setTitle("Network Performance");
            performanceChart.setTitleSide(Side.TOP);
            performanceChart.setLegendVisible(false);
            performanceChart.setCreateSymbols(false);
            performanceChart.setHorizontalGridLinesVisible(true);
            performanceChart.setVerticalGridLinesVisible(true);
            //
            ViewUtil.compact(performanceChart);
            //
            plotSeries.getData().clear();
            performanceChart.getData().add(plotSeries);
            //
            Set<Node> nodes = performanceChart.lookupAll(".series" + 0);
            for (Node n : nodes) {
                if (plotSeries.getNode() == n) {
                    n.setStyle("-fx-stroke: green; -fx-background-color: green, white; ");
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init plot panel to display network performance
     */
    public void initHistoPanel() {
        try {
            double upperBound = totalSamples;
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Sample Count");
            xAxis.setLowerBound(0.0);
            xAxis.setUpperBound(Math.rint(upperBound));
            xAxis.setTickUnit(Math.rint(upperBound / 10.0));
            xAxis.setMinorTickCount(2);
            xAxis.setAutoRanging(false);
            //
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("% Accuracy");
            yAxis.setLowerBound(0.0);
            yAxis.setUpperBound(100.0);
            yAxis.setTickUnit(10.0);
            yAxis.setMinorTickCount(2);
            yAxis.setAutoRanging(false);
            //
            histoChart = new LineChart<Number, Number>(xAxis, yAxis);
            // setAnimated to false to allow snapshot
            histoChart.setAnimated(false);
            histoChart.setTitle("Performance Histogram");
            histoChart.setTitleSide(Side.TOP);
            histoChart.setLegendVisible(false);
            histoChart.setCreateSymbols(false);
            histoChart.setHorizontalGridLinesVisible(true);
            histoChart.setVerticalGridLinesVisible(true);

            //
            //   ViewUtil.compact(histoChart);
            //
            barSeries.getData().clear();
            histoChart.getData().add(barSeries);
            //

            Set<Node> nodes = histoChart.lookupAll(".series" + 0);
            for (Node n : nodes) {
                if (barSeries.getNode() == n) {
                    n.setStyle("-fx-stroke-width: 1px; -fx-stroke: blue; -fx-background-color: blue, white; ");
                }
            }


        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init summary panel grid (i.e. confusion matrix) with row and column labels
     Rows are actual (known) values, columns are predicted (computed) values
     */
    public void initSummaryPanel() {
        try {
            summaryGrid.getChildren().clear();
            summaryPanel.getChildren().clear();
            int numClasses = netConfig.outputConfig.numOutputNodes;
            summaryGrid.setHgap(5);
            summaryGrid.setVgap(4);
            // set size since initially it will be empty with no computed size
            summaryGrid.setPrefSize(600, 400);
            summaryGrid.setMinSize(600, 400);
            if (numClasses < 6) {
                summaryGrid.setPrefSize(400, 300);
            }
            // summaryGrid is mostly empty here, don't use computed size
            //   summaryGrid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            //   summaryGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            //   summaryGrid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            summaryGrid.setStyle("-fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            //
            Label rowTitle = new Label("Actual");
            rowTitle.setStyle("-fx-rotate: -90;");
            //   rowTitle.setStyle("-fx-rotate: -90; -fx-border-color: blue; -fx-border-width: 1px;-fx-border-style: solid;");
            /*
            Why do we need to add rowTitle to a Group?
            Without the Group, because rowTitle is a rotated Label, it's layout is clipped. By adding
            rowTitle to a Group, the Label will be displayed correctly with no clipping.
            From javadocs:
            "If transforms and effects are set directly on children of this Group, those will be
            included in this Group's layout bounds."
             */
            ColumnConstraints colCon = null;
            RowConstraints rowCon = null;
            //
            // rowTitle in column 0
            Group rowG = new Group(rowTitle);
            // need new ColumnConstraints for rowTitle column
            colCon = new ColumnConstraints();
            colCon.setPercentWidth(5.0);
            summaryGrid.getColumnConstraints().add(colCon);
            // add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan)
            summaryGrid.add(rowG, 0, 3);
            // row span is down
            GridPane.setRowSpan(rowG, 5);
            //
            int labelCol = 1;
            // need new ColumnConstraints for row labels column
            colCon = new ColumnConstraints();
            colCon.setPercentWidth(5.0);
            summaryGrid.getColumnConstraints().add(colCon);
            //
            // colTitle in row 0
            Label colTitle = new Label("Predicted");
            // need new RowConstraints for colTitle row
            rowCon = new RowConstraints();
            rowCon.setPercentHeight(5.0);
            summaryGrid.getRowConstraints().add(rowCon);
            summaryGrid.add(colTitle, 4, 0);
            // column span is to the right
            GridPane.setColumnSpan(colTitle, 5);
            int labelRow = 1;
            // need new ColumnConstraints for column labels row
            rowCon = new RowConstraints();
            rowCon.setPercentHeight(5.0);
            summaryGrid.getRowConstraints().add(rowCon);
            // create row labels (skip colTitle in row 0 , column labels in row 1)
            int k = 0;
            double rowPercent = 90.0 / numClasses;
            LOG.info("numClasses: " + numClasses + ", rowPercent: " + rowPercent);
            for (int row = 2; row < 2 + numClasses; row++) {
                // need new RowConstraints for each row
                rowCon = new RowConstraints();
                rowCon.setPercentHeight(rowPercent);
            //    summaryGrid.getRowConstraints().add(rowCon);
                // actual labels
                Label rowLabel = new Label(String.valueOf(k));
                summaryGrid.add(rowLabel, labelCol, row);
                k++;
            }
            // create column labels (skip rowTitle in col 0, row labels in col 1)
            int c = 0;
            double colPercent = 90.0 / numClasses;
            for (int col = 2; col < 2 + numClasses; col++) {
                // need new ColumnConstraints for each column
                colCon = new ColumnConstraints();
                colCon.setPercentWidth(colPercent);
             //   summaryGrid.getColumnConstraints().add(colCon);
                // predicted labels
                Label colLabel = new Label(String.valueOf(c));
                summaryGrid.add(colLabel, col, labelRow);
                c++;
            }
            LOG.info("summaryGrid pref W: " + summaryGrid.getPrefWidth() );
            //
            Label summaryTitle = new Label("Confusion Matrix");
            summaryPanel.getChildren().add(summaryTitle);
            summaryPanel.getChildren().add(summaryGrid);
            summaryPanel.setPrefSize(800, 500);
            summaryPanel.setSpacing(5);
            // alignment of children within the vbox's width and height
            summaryPanel.setAlignment(Pos.CENTER);
            //insets: top, right, bottom, left
            summaryPanel.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    /**
     Run task.
     */
    public void runTask(String netOption) {
        task = new NetTask(netConfig, netOption);
        task.setSubsetSize(subsetSize);
        Thread th = new Thread(task);
        try {
            // send network configuration to background task
            //  task = new NetTask(netConfig, netOption);
            //
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                public void handle(WorkerStateEvent event) {
                    // do something when task finishes
                    TaskResult finalResult = task.getValue();
                    // display confusion matrix
                    Matrix confusion = finalResult.netResult.summaryResults;
                    //
                    showSummary(confusion);
                    if (TRAIN_NET.equalsIgnoreCase(netOption)) {
                        FitParams soln = task.prepFitParams();
                        setReadyFitParams(true);
                        if (isReadyFitParams()) {
                            LOG.log(Level.INFO, "open alert");
                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                    "FitParams ready for export",
                                    ButtonType.OK);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                // do nothing
                            }
                        }

                    }
                    statusField.setText("Task completed");
                    setCompletedProp(true);
                }
            });

            task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                public void handle(WorkerStateEvent event) {
                    // do something when task finishes
                    TaskResult finalResult = task.getValue();
                    if (finalResult != null && finalResult.netResult != null) {
                        // display confusion matrix
                        Matrix confusion = finalResult.netResult.summaryResults;
                        //
                        showSummary(confusion);
                    }
                    statusField.setText("Task cancelled");
                    setCompletedProp(true);
                }
            });

            // listen for message changes:
            ChangeListener<String> msgListener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                                    String oldValue,
                                    String newValue) {
                    // status update
                    statusField.setText(newValue);
                }
            };
            // add the message Listener
            task.messageProperty().addListener(msgListener);
            //
            // Before starting a task, bind UI values to the task properties
            //     progress.progressProperty().bind(task.progressProperty());
            //
            // alternative to bind: listen for progress changes:
            // listener also allows task calls getValue, getMessage
            ChangeListener<Number> progListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                    //
                    double progress = (Double) newValue;
                    LOG.log(Level.INFO, "progress: " + progress);
                    // update progress bar
                    progressBar.setProgress(progress);
                    // update status
                    String msg = task.getMessage();
                    statusField.setText(msg);
                    // check other values when progress changes
                    taskResult = task.getValue();
                    double completed = taskResult.netResult.samplesCompleted;
                    double correct = taskResult.netResult.samplesCorrect;
                    LOG.log(Level.INFO, "samplesCompleted: " + completed + ", samplesCorrect: " + correct);
                    //
                    // update plot of overall sample performance
                    updatePerformancePlot(completed, correct);
                    //
                    // update plot of sub sample performance
                    updateHistoChart(completed, taskResult.subsetCompleted, taskResult.subsetCorrect);

                }
            };
            // add the progress listener
            task.progressProperty().addListener(progListener); //.addListener(progListener);
            //
            // background thread, separate from JavaFX main thread
            //   Thread th = new Thread(task);
            th.setDaemon(true);
            // thread start calls task call() method
            th.start();
            statusField.setText("Task running");
            // note: task may be cancelled by calling task.cancel()
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            setCompletedProp(true);
            // ??
            ex.setStackTrace(th.getStackTrace());
            throw new RuntimeException(ex);
        }
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }

    /**
     Update plot of network prediction accuracy

     @param completed
     @param correct
     */
    public void updatePerformancePlot(double completed, double correct) {
        try {
            double x = completed;
            // plot accuracy = cumulative percent correct
            double y = (correct / completed) * 100.0;
            XYChart.Data data = new XYChart.Data<>(x, y);
            plotSeries.getData().add(data);
            Node node = data.getNode();
            if (node != null) {
                node.setScaleX(0.25);
                node.setScaleY(0.25);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Update bar chart of network prediction accuracy for one batch of samples

     @param completedBatch
     @param correctBatch
     */
    public void updateHistoChart(double completed, double completedBatch, double correctBatch) {
        try {
            double xLo = completed - completedBatch;
            double xHi = completed;
            // plot accuracy = percent correct
            double y = (correctBatch / completedBatch) * 100.0;
            XYChart.Data[] data = new XYChart.Data[4];
            // form rectangular vertical bar
            data[0] = new XYChart.Data<>(xLo, 0);
            data[1] = new XYChart.Data<>(xLo, y);
            data[2] = new XYChart.Data<>(xHi, y);
            data[3] = new XYChart.Data<>(xHi, 0);
            //
            for (int k = 0; k < 4; k++) {
                barSeries.getData().add(data[k]);
                Node node = data[k].getNode();
                if (node != null) {
                    node.setScaleX(0.25);
                    node.setScaleY(0.25);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Show summary matrix of correct and incorrect predictions

     @param confusion confusion matrix
     */
    public void showSummary(Matrix confusion) {
        try {
            int rows = confusion.rows;
            int cols = confusion.cols;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // number of correct or incorrect predictions
                    double d = MTX.getCell(confusion, i, j);
                    String n = fmtAsInt.format(d);
                    Pane pane = new Pane();
                    Label val = new Label(n);
                    pane.setStyle("-fx-background-color: white; ");
                    if (i == j) {
                        pane.setStyle("-fx-background-color: lime; ");
                    }
                    pane.setPrefHeight(24);
                    pane.getChildren().add(val);

                    // grid already has labels in col 0, 1 and row 0, 1
                    // data goes in other gridd cells
                    summaryGrid.add(pane, j + 2, i + 2);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void cancelTask() {
        try {
            // note: task may be cancelled by calling task.cancel()
            task.cancel();
            statusField.setText("Task cancelled");
            setCompletedProp(true);
        } catch (Exception ex) {
            setCompletedProp(true);
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void takeSnap() {
        try {
            // snapPane is reset in setTogglePane
            WritableImage image = snapPane.snapshot(new SnapshotParameters(), null);
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("Snapshot");
            File file = fileDialog.showSaveDialog(null);
            //
            if (file != null) {
                // fileName does not include path
                String fileName = file.getName();
                LOG.log(Level.INFO, "fileName: " + fileName);
                int dotIndex = fileName.indexOf(".");
                if (dotIndex > 0) {
                    // make file ext png
                    String ext = Arrays.stream(fileName.split("\\.")).reduce((a, b) -> b).orElse(null);
                    LOG.log(Level.INFO, "ext: " + ext);
                    if (!ext.equalsIgnoreCase("png")) {
                        // extension not png, change it to png
                        String name = file.getAbsolutePath();
                        LOG.log(Level.INFO, "name: " + name);
                        int index = name.indexOf("." + ext);
                        // pre excludes '.'
                        String pre = name.substring(0, index);
                        LOG.log(Level.INFO, "pre: " + pre);
                        file = new File(pre + ".png");
                    }
                } else {
                    // need whole path for new File
                    String name = file.getAbsolutePath();
                    // no extension, add png
                    file = new File(name + ".png");
                }
                //
                BufferedImage bim = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bim, "png", file);
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void importFitParams() {
        try {
            File file = ViewUtil.openFileDialog("Import Training FitParams");
            if (file != null) {
                InputStream src = FileUtil.getInputStream(file);
                FitParams fitParams = new FitParams();
                if(taskResult != null) {
                    fitParams = JsonUtil.jsonToFitParams(src);
                } else {
                    fitParams = JsonUtil.jsonToFitParams(src);
                }
                FitParamsCache fitParamsCache = FitParamsCache.getInstance();
                fitParamsCache.setFitParams(fitParams);
                // train and test allowed to run
                runBtn.setDisable(false);
                //
                setReadyFitParams(true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void exportFitParams() {
        try {
            LOG.log(Level.INFO, "Export FitParams");
            File file = ViewUtil.openCreateFileDialog("Export Training FitParams");
            if (file != null) {
                OutputStream fos = FileUtil.getOutputStream(file);
                JsonUtil.FitParamsToJson(taskResult.netResult.fitParams, fos);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public int subsetSize(int totalSamples) {
        int subsetSize = 100;
        try {
            // want roughly 50 subsets
            subsetSize = totalSamples / 50;
            if(subsetSize == 0){
                subsetSize = totalSamples;
            } else {
                if (subsetSize < 50) {
                    subsetSize = 50;
                }
            }
            SciFmtR sciFmt = getSciFmt(subsetSize);
            int exp = sciFmt.exponent();
            double mant = sciFmt.mantissa();
            double mm = Math.rint(mant);
            int n = 1;
            if (mm > 5) {
                n = 10;
            } else if (mm > 2) {
                n = 5;
            } else if (mm > 1) {
                n = 2;
            } else {
                n = 1;
            }
            subsetSize = n * (int) Math.rint(Math.pow(10, exp));
            LOG.info("totalSamples: " + totalSamples + ", sciFmt: " + sciFmt + ", n: " + n
                    + ", subsetSize: " + subsetSize);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return subsetSize;
    }

    public SciFmtR getSciFmt(int x) {
        double logten = Math.log10(x);
        int expI = (int) logten;
        double mant = x / Math.pow(10, expI);
        LOG.info("x: " + x + ", logten: " + logten + ", expI: " + expI + ", mant: " + mant);
        SciFmtR sciFmt = new SciFmtR(mant, expI);
        return sciFmt;
    }

}