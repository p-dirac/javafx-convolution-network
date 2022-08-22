package datasci.frontend.config;
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

import datasci.backend.model.ConvoConfig;
import datasci.backend.model.ConvoPoolConfig;
import datasci.backend.model.FileUtil;
import datasci.backend.model.GeneralConfig;
import datasci.backend.model.InputConfig;
import datasci.backend.model.InternalConfig;
import datasci.backend.model.MatrixModel;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetResult;
import datasci.backend.model.OutputConfig;
import datasci.backend.model.PoolConfig;
import datasci.frontend.ctrl.ConfigCache;
import datasci.frontend.util.ViewUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Network matrix fit model editor
 */
public class MatrixView {

    private static final Logger LOG = Logger.getLogger(MatrixView.class.getName());
    private BorderPane outerPane;
    //
    private final Button saveBtn = new Button("Save");
    private final Button cancelBtn = new Button("Cancel");
    private final HBox btnPanel = new HBox();
    private final Group btnGroup = new Group(btnPanel);

    //
    private final GridPane gridForm = new GridPane();
    private final ScrollPane scroller = new ScrollPane();
    // filter props
    private int numFilters;
    private int rows;
    private int cols;
    //
    private MatrixModel matrixModel;
    private NetConfig netConfig;
    // NetResult will hold the overall summary of network performance, and also the network training FitParams
    private NetResult netResult;
    //
    private NetConfigPanels netConfigPanels;
    // readyProp: true when all config fields are defined, otherwise false
    private final BooleanProperty readyConfig = new SimpleBooleanProperty();
    //
    private Node convoPoolCell;
    private Node internalCell;

    /**
     * Instantiates a new Matrix view.
     */
    public MatrixView() {
        init();
    }

    public void init() {
        try {
            readyConfig.setValue(false);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public boolean isReadyConfig() {
        return readyConfig.get();
    }

    public BooleanProperty readyConfigProperty() {
        return readyConfig;
    }

    public void setReadyConfig(boolean readyConfig) {
        this.readyConfig.set(readyConfig);
    }

    /**
     * Gets scroller.
     *
     * @return the scroller
     */
    public ScrollPane getScroller() {
        return scroller;
    }

    public NetConfig getNetConfig() {
        return netConfig;
    }






    /**
     * Form for network configuration parameters
     */
    public void createAllLayers(BorderPane outerPane) {
        this.outerPane = outerPane;
        try {
            netConfig = new NetConfig();
            netConfigPanels = new NetConfigPanels();
            //
            ScrollPane scroller = new ScrollPane();
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            scroller.setPadding(new Insets(10, 10, 10, 10));

            //  scroller.setStyle("-fx-border-color: red; -fx-border-width: 1px;-fx-border-style: solid;");
            VBox allPane = new VBox(20);
            //  allPane.setStyle("-fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            allPane.setAlignment(Pos.TOP_CENTER);
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            allPane.setPadding(new Insets(10, 10, 10, 10));
            //
            GridPane grid = new GridPane();
            //    grid.setStyle("-fx-border-color: cyan; -fx-border-width: 1px;-fx-border-style: solid;");
            //
            grid.setVgap(10);
            grid.setHgap(10);
            int row = 0;
            int labelCol = 0;
            int panelCol = 1;
            //
            Label labelGeneral = new Label("General params:");
            grid.add(labelGeneral, labelCol, row);
            Pane generalPane = createGeneralConfig();
            grid.add(generalPane, panelCol, row);
            //
            Label labelInput = new Label("Input layer:");
            grid.add(labelInput, labelCol, ++row);
            Pane inputPane = createInputConfig();
            grid.add(inputPane, panelCol, row);
            //
            Label labelConvoPool = new Label("Convolution/Pool layer pairs:");
            grid.add(labelConvoPool, labelCol, ++row);
            Pane convPoolPane = createConvoPoolConfig();
            grid.add(convPoolPane, panelCol, row);
            //
            Label labelInternal = new Label("Internal layers:");
            grid.add(labelInternal, labelCol, ++row);
            Pane internalPane = createInternalConfig();
            grid.add(internalPane, panelCol, row);
            //
            Label labelOutput = new Label("Output layer:");
            grid.add(labelOutput, labelCol, ++row);
            Pane outputPane = createOutputConfig();
            grid.add(outputPane, panelCol, row);
            //
            ViewUtil.compact(grid);
            allPane.getChildren().add(grid);
            //
            Button saveBtn = new Button("Save configuration");
            Button cancelBtn = new Button("Cancel");
            //
            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "save");
                    saveAllLayers();
                }
            });
            cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "cancel");
                    readyConfig.setValue(false);
                    netConfig = null;
                    netConfigPanels = null;
                }
            });
            ButtonBar buttonBar = new ButtonBar();
            buttonBar.getButtons().addAll(saveBtn, cancelBtn);
            allPane.getChildren().add(buttonBar);
            /*
            HBox btnPanel = new HBox();
            btnPanel.setSpacing(20);
            btnPanel.getChildren().addAll(saveBtn, cancelBtn);
            Group saveGroup = new Group(btnPanel);
            //
            allPane.getChildren().add(saveGroup);

             */
            //
            ViewUtil.compact(allPane);
            // StackPane centers its content
            StackPane stp = new StackPane(allPane);
            scroller.setContent(stp);

            //   scroller.setFitToWidth(true);
            ViewUtil.waitForScrollSkin(scroller, 20);
            scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            ViewUtil.compactW(scroller);
            // Set alignment for child node when contained by border pane
            BorderPane.setAlignment(scroller, Pos.TOP_CENTER);
            // Set margin for child node when contained by border pane
            // Insets: top, right, bottom, left (pixel units)
            BorderPane.setMargin(scroller, new Insets(10, 10, 10, 10));
            outerPane.setCenter(scroller);
            //   outerPane.setStyle("-fx-border-color: orange; -fx-border-width: 1px;-fx-border-style: dashed;");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Form for network configuration parameters
     */
    public void setAllLayers(BorderPane outerPane) {
        this.outerPane = outerPane;
        try {
            if (netConfig == null) {
                return;
            }
            netConfigPanels = new NetConfigPanels();
            //
            ScrollPane scroller = new ScrollPane();
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            scroller.setPadding(new Insets(10, 10, 10, 10));

            //  scroller.setStyle("-fx-border-color: red; -fx-border-width: 1px;-fx-border-style: solid;");
            VBox allPane = new VBox(20);
            //  allPane.setStyle("-fx-border-color: green; -fx-border-width: 1px;-fx-border-style: solid;");
            allPane.setAlignment(Pos.TOP_CENTER);
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            allPane.setPadding(new Insets(10, 10, 10, 10));
            //
            GridPane grid = new GridPane();
            //    grid.setStyle("-fx-border-color: cyan; -fx-border-width: 1px;-fx-border-style: solid;");
            //
            grid.setVgap(10);
            grid.setHgap(10);
            int row = 0;
            int labelCol = 0;
            int panelCol = 1;
            //
            Label labelGeneral = new Label("General params:");
            grid.add(labelGeneral, labelCol, row);
            Pane generalPane = createGeneralConfig(netConfig.generalConfig);
            grid.add(generalPane, panelCol, row);
            //
            Label labelInput = new Label("Input layer:");
            grid.add(labelInput, labelCol, ++row);
            Pane inputPane = createInputConfig(netConfig.inputConfig);
            grid.add(inputPane, panelCol, row);
            //
            Label labelConvoPool = new Label("Convolution/Pool layer pairs:");
            grid.add(labelConvoPool, labelCol, ++row);
            Pane convPoolPane = createConvoPoolConfig(netConfig.convoPoolList);
            grid.add(convPoolPane, panelCol, row);
            //
            Label labelInternal = new Label("Internal layers:");
            grid.add(labelInternal, labelCol, ++row);
            Pane internalPane = createInternalConfig(netConfig.internalList);
            grid.add(internalPane, panelCol, row);
            //
            Label labelOutput = new Label("Output layer:");
            grid.add(labelOutput, labelCol, ++row);
            Pane outputPane = createOutputConfig(netConfig.outputConfig);
            grid.add(outputPane, panelCol, row);
            //
            ViewUtil.compact(grid);
            allPane.getChildren().add(grid);
            //
            Button saveBtn = new Button("Save configuration");
            Button cancelBtn = new Button("Cancel");
            //
            saveBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "save");
                    saveAllLayers();
                }
            });
            cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "cancel");
                    readyConfig.setValue(false);
                    //    netConfig = null;
                    netConfigPanels = null;
                }
            });
            ButtonBar buttonBar = new ButtonBar();
            buttonBar.getButtons().addAll(saveBtn, cancelBtn);
            allPane.getChildren().add(buttonBar);
            //
            ViewUtil.compact(allPane);
            // StackPane centers its content
            StackPane stp = new StackPane(allPane);
            scroller.setContent(stp);

            //   scroller.setFitToWidth(true);
            ViewUtil.waitForScrollSkin(scroller, 20);
            scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

            ViewUtil.compactW(scroller);
            // Set alignment for child node when contained by border pane
            BorderPane.setAlignment(scroller, Pos.TOP_CENTER);
            // Set margin for child node when contained by border pane
            // Insets: top, right, bottom, left (pixel units)
            BorderPane.setMargin(scroller, new Insets(10, 10, 10, 10));
            outerPane.setCenter(scroller);
            //   outerPane.setStyle("-fx-border-color: orange; -fx-border-width: 1px;-fx-border-style: dashed;");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createGeneralConfig() {
        Pane generalPane = null;
        try {
            //
            GeneralConfigPanel generalPanel = new GeneralConfigPanel();
            generalPane = generalPanel.init();
            generalPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            generalPane.setPadding(new Insets(10, 10, 10, 10));
            netConfigPanels.generalPanel = generalPanel;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return generalPane;
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createGeneralConfig(GeneralConfig generalConfig) {
        Pane generalPane = null;
        try {
            //
            GeneralConfigPanel generalPanel = new GeneralConfigPanel();
            netConfigPanels.generalPanel = generalPanel;
            generalPane = generalPanel.init();
            generalPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            generalPane.setPadding(new Insets(10, 10, 10, 10));
            //
            generalPanel.setConfig(generalConfig);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return generalPane;
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createInternalConfig() {
        VBox pane = new VBox(10);
        try {
            pane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            pane.setPadding(new Insets(10, 10, 10, 10));
            // need initial size for empty pane
            pane.setMinSize(200, 30);
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            internalContext(pane);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return pane;
    }

    /**
     * Form for network configuration parameters
     */
    public void internalContext(VBox pane) {
        try {
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            MenuItem aboveItem = new MenuItem("Add Above");
            aboveItem.setOnAction(e -> {
                LOG.log(Level.INFO, "above");
                int prev = 0;
                int index = pane.getChildren().indexOf(internalCell);
                prev = index;
                if (index < 0) {
                    index = 0;
                    prev = 0;
                }
                Pane p = addInternal(prev);
                //Inserts element at index, and shifts elements right (adds one to their indices).
                pane.getChildren().add(prev, p);
            });
            //
            MenuItem belowItem = new MenuItem("Add Below");
            belowItem.setOnAction(e -> {
                LOG.log(Level.INFO, "below");
                int index = 0;
                int len = pane.getChildren().size();
                int next = 0;
                if (len > 0) {
                    index = pane.getChildren().indexOf(internalCell);
                    next = index + 1;
                }
                Pane p = addInternal(next);
                //Inserts element at index, and shifts elements right (adds one to their indices).
                pane.getChildren().add(next, p);
            });
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                LOG.log(Level.INFO, "Delete");
                int index = pane.getChildren().indexOf(internalCell);
                pane.getChildren().remove(index);
                netConfigPanels.internalPanels.remove(index);
            });

            ContextMenu ctxMenu = new ContextMenu(aboveItem, belowItem, deleteItem);
            pane.setOnContextMenuRequested(e -> {
                ctxMenu.show(pane, e.getScreenX(), e.getScreenY());
                ctxMenu.setAutoHide(true);
                // event X,Y: position of event relative to origin of ContextMenuEvent source
                internalCell = getCellatXY(pane, e.getX(), e.getY());
                e.consume();
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createInternalConfig(List<InternalConfig> internalList) {
        VBox pane = new VBox(10);
        try {

            pane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            pane.setPadding(new Insets(10, 10, 10, 10));
            pane.setMinSize(200, 30);
            //
            //
            for (InternalConfig ic : internalList) {
                Pane p = addInternal(ic);
                pane.getChildren().add(p);
            }
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            internalContext(pane);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return pane;
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createConvoPoolConfig() {
        VBox pane = new VBox(10);
        try {
            pane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            pane.setPadding(new Insets(10, 10, 10, 10));
            pane.setMinSize(200, 30);
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            convoPoolContext(pane);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return pane;
    }

    /**
     * Form for network configuration parameters
     */
    public void convoPoolContext(VBox pane) {
        try {
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            MenuItem aboveItem = new MenuItem("Add Above");
            aboveItem.setOnAction(e -> {
                LOG.log(Level.INFO, "above");
                int prev = 0;
                int index = pane.getChildren().indexOf(convoPoolCell);
                prev = index;
                if (index < 0) {
                    index = 0;
                    prev = 0;
                }
                Pane p = addConvoPool(prev);
                //Inserts element at index, and shifts elements right (adds one to their indices).
                pane.getChildren().add(prev, p);
            });
            //
            MenuItem belowItem = new MenuItem("Add Below");
            belowItem.setOnAction(e -> {
                LOG.log(Level.INFO, "below");
                int index = 0;
                int len = pane.getChildren().size();
                int next = 0;
                if (len > 0) {
                    index = pane.getChildren().indexOf(convoPoolCell);
                    next = index + 1;
                }
                Pane p = addConvoPool(next);
                //Inserts element at index, and shifts elements right (adds one to their indices).
                pane.getChildren().add(next, p);
            });
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> {
                LOG.log(Level.INFO, "Delete");
                int index = pane.getChildren().indexOf(convoPoolCell);
                pane.getChildren().remove(index);
                netConfigPanels.convoPoolPanels.remove(index);
            });
            //
            ContextMenu ctxMenu = new ContextMenu(aboveItem, belowItem, deleteItem);
            pane.setOnContextMenuRequested(e -> {
                ctxMenu.show(pane, e.getScreenX(), e.getScreenY());
                ctxMenu.setAutoHide(true);
                // event X,Y: position of event relative to origin of ContextMenuEvent source
                convoPoolCell = getCellatXY(pane, e.getX(), e.getY());
                e.consume();
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createConvoPoolConfig(List<ConvoPoolConfig> convoPoolList) {
        VBox pane = new VBox(10);
        try {
            pane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            pane.setPadding(new Insets(10, 10, 10, 10));
            pane.setMinSize(200, 30);
            //
            for (ConvoPoolConfig cp : convoPoolList) {
                Pane p = addConvoPool(cp);
                pane.getChildren().add(p);
            }
            //
            // create ContextMenu: mouse right click pane area to activate
            //
            convoPoolContext(pane);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return pane;
    }
    /**
     * Form for network configuration parameters
     */
    public InputConfigPanel createInputConfig() {
        InputConfigPanel inputPanel = null;
        try {
            //
            inputPanel = new InputConfigPanel();
            netConfigPanels.inputPanel = inputPanel;
            inputPanel.init();
            inputPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            inputPanel.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return inputPanel;
    }
    /**
     * Form for network configuration parameters
     */
    public Pane createInputConfig(InputConfig inputConfig) {
        InputConfigPanel inputPanel = null;
        try {
            //
            inputPanel = new InputConfigPanel();
            netConfigPanels.inputPanel = inputPanel;
            inputPanel.init();
            inputPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            inputPanel.setPadding(new Insets(10, 10, 10, 10));
            //
            inputPanel.setConfig(inputConfig);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return inputPanel;
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createOutputConfig() {
        Pane outputPane = null;
        try {
            //
            OutputConfigPanel outputPanel = new OutputConfigPanel();
            netConfigPanels.outputPanel = outputPanel;
            outputPane = outputPanel.init();
            outputPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            outputPane.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return outputPane;
    }

    /**
     * Form for network configuration parameters
     */
    public Pane createOutputConfig(OutputConfig outputConfig) {
        Pane outputPane = null;
        try {
            //
            OutputConfigPanel outputPanel = new OutputConfigPanel();
            netConfigPanels.outputPanel = outputPanel;
            outputPane = outputPanel.init();
            outputPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            outputPane.setPadding(new Insets(10, 10, 10, 10));
            //
            outputPanel.setConfig(outputConfig);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return outputPane;
    }

    public Pane addConvoPool(int index) {
        VBox convoPool = new VBox(10);
        try {
            ConvoPoolConfigPanel convoPoolPanel = new ConvoPoolConfigPanel();
            netConfigPanels.convoPoolPanels.add(index, convoPoolPanel);
            Pane convoPane = convoPoolPanel.createConvoPane();
            convoPool.getChildren().add(convoPane);
            //
            Pane poolPane = convoPoolPanel.createPoolPane();
            convoPool.getChildren().add(poolPane);
            //
            convoPool.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            convoPool.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convoPool;
    }

    public Pane addConvoPool(ConvoPoolConfig convoPoolConfig) {
        VBox convoPool = new VBox(10);
        try {
            ConvoPoolConfigPanel convoPoolPanel = new ConvoPoolConfigPanel();
            netConfigPanels.convoPoolPanels.add(convoPoolPanel);
            Pane convoPane = convoPoolPanel.createConvoPane();
            convoPool.getChildren().add(convoPane);
            convoPoolPanel.setConvoConfig(convoPoolConfig.convoConfig);
            //
            Pane poolPane = convoPoolPanel.createPoolPane();
            convoPool.getChildren().add(poolPane);
            convoPoolPanel.setPoolConfig(convoPoolConfig.poolConfig);
            //
            convoPool.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            convoPool.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return convoPool;
    }

    public Pane addInternal(int index) {
        InternalConfigPanel internalPanel = null;
        try {
            internalPanel = new InternalConfigPanel();
            netConfigPanels.internalPanels.add(index, internalPanel);
            internalPanel.init();
            internalPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            internalPanel.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return internalPanel;
    }

    public Pane addInternal(InternalConfig internalConfig) {
        InternalConfigPanel internalPanel = null;
        try {
            internalPanel = new InternalConfigPanel();
            netConfigPanels.internalPanels.add(internalPanel);
            internalPanel.init();
            internalPanel.setConfig(internalConfig);
            //
            internalPanel.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px;-fx-border-style: solid;");
            // Insets: top, right, bottom, left (pixel units)
            // padding is inside the border, (margin is outside border)
            internalPanel.setPadding(new Insets(10, 10, 10, 10));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return internalPanel;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public Node getCellatXY(Pane p, double x, double y) {
        Node cell = null;
        try {
            for (Node node : p.getChildren()) {
                // check if cell node contains the point x,y in parent of node; local coords
                if (node.getBoundsInParent().contains(x, y)) {
                    cell = node;
                    break;
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return cell;
    }


    /**
     * Form for network configuration parameters
     * User must press save button to replace netConfig object
     */
    public void saveAllLayers() {
        try {
            //
            GeneralConfigPanel generalPanel = netConfigPanels.generalPanel;
            GeneralConfig generalConfig = generalPanel.getConfig();
            netConfig.generalConfig = generalConfig;
            //
            InputConfigPanel inputPanel = netConfigPanels.inputPanel;
            InputConfig inputConfig = inputPanel.getConfig();
            netConfig.inputConfig = inputConfig;
            //
            List<ConvoPoolConfigPanel> convoPoolPanels = netConfigPanels.convoPoolPanels;
       //     int numConvoPool = convoPoolPanels.size();
            //
            netConfig.convoPoolList.clear();
            for (ConvoPoolConfigPanel convoPool : convoPoolPanels) {
                ConvoConfig convoConfig = convoPool.getConvoConfig();
                PoolConfig poolConfig = convoPool.getPoolConfig();
                //
                ConvoPoolConfig convoPoolConfig = new ConvoPoolConfig(convoConfig, poolConfig);
                netConfig.convoPoolList.add(convoPoolConfig);
            }
            //
            List<InternalConfigPanel> internalPanels = netConfigPanels.internalPanels;
       //     int numInternal = internalPanels.size();
            //
            netConfig.internalList.clear();
            for (InternalConfigPanel internalPanel : internalPanels) {
                InternalConfig internalConfig = internalPanel.getConfig();
                netConfig.internalList.add(internalConfig);
            }
            //
            OutputConfigPanel outputPanel = netConfigPanels.outputPanel;
            OutputConfig outputConfig = outputPanel.getConfig();
            netConfig.outputConfig = outputConfig;
            //
            LOG.log(Level.INFO, "netConfig saved");
            setReadyConfig(true);
            // also save config in cache for other panels to use
            ConfigCache.getInstance().setConfig(netConfig);
            if (isReadyConfig()) {
                LOG.log(Level.INFO, "open alert");
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Configuration ready for task or export",
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
        return;
    }


    public void importMatrixModel() {
        try {
            File file = ViewUtil.openFileDialog("Import Matrix Model");
            if (file != null) {
                InputStream src = FileUtil.getInputStream(file);
           //     matrixModel = JsonUtil.jsonToConfig(src);
                //
                readyConfig.setValue(true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public void exportMatrixModel() {
        try {
            File file = ViewUtil.openCreateFileDialog("Export Matrix Model");
            if (file != null) {
                OutputStream fos = FileUtil.getOutputStream(file);
          //      JsonUtil.configToJson(matrixModel, fos);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }



}  //end class