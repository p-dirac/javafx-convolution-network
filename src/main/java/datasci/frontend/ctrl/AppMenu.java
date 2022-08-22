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

import datasci.backend.model.NetConfig;
import datasci.frontend.config.ConfigView;
import datasci.frontend.config.MatrixView;
import datasci.frontend.config.PixelView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Application menu bar

 @author r cook */
public class AppMenu {


    //private static final Logger LOG = Logger.getLogger(AppMenu.class.getName());
    private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    //same menu for all panels
    private final MenuBar menuBar = new MenuBar();
    //
    private MenuItem importFitParamsItem;
    private MenuItem exportFitParamsItem;
    //
    private MenuItem importMatrixItem;
    private MenuItem exportMatrixItem;
    //
    private AppTabs tabs;
    private ObservableList<Tab> tabList;
    private SingleSelectionModel<Tab> tabSelector;
    //
    private final PixelView pixelView = new PixelView();
    //
    private final ConfigView configView = new ConfigView();
    //
    private final MatrixView matrixView = new MatrixView();
    //
    private TaskView taskTrainView;
    private TaskView taskTestView;
    //
    private String netOption;
    private static final String TRAIN_NET = "Train Net";
    private static final String TEST_NET = "Test Net";

    /**
     Create new application menu.
     */
    public AppMenu() {
    }

    /**
     Init viewer.

     @param tabs
     */
    public void initViewer(AppTabs tabs) {
        try {
            this.tabs = tabs;
            tabList = tabs.getTabList();
            tabSelector = tabs.getSelectionModel();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     Init menu bar.

     @return the menu bar
     */
    public MenuBar initMenuBar() {
        try {
            List<Menu> menuList = new ArrayList<>();
            // --- App menu
            Menu appMenu = initMenuApp();
            menuList.add(appMenu);
            //
            // --- Settings menu
            Menu settingMenu = initSettingsMenu();
            menuList.add(settingMenu);
            //
            // --- Options menu
            Menu opMenu = initOptionMenu();
            menuList.add(opMenu);
            //
            // --- Help menu
            Menu helpMenu = initHelpMenu();
            menuList.add(helpMenu);
            //
            menuBar.getMenus().addAll(menuList);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return menuBar;
    }

    /**
     Init application menu

     @return application menu
     */
    public Menu initMenuApp() {
        // --- Menu File
        Menu myMenu = new Menu("App");
        try {
            List<MenuItem> menuItemList = new ArrayList<>();
            //
            /*
            MenuItem aItem = new MenuItem("Create  Data");
            menuItemList.add(aItem);
            aItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Create  Data");
                }
            });
        */

            //
            MenuItem bItem = new MenuItem("Save  Data");
            menuItemList.add(bItem);
            bItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Save  Data");
                    //delegate tab view
                }
            });
            //
            //separator
            menuItemList.add(new SeparatorMenuItem());
            //
            MenuItem cItem = new MenuItem("Close App");
            menuItemList.add(cItem);
            cItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Close  App");
                    //delegate tab view
                    exitApp();
                }
            });
            myMenu.getItems().addAll(menuItemList);
            // myMenu.getItems().addAll(aItem, bItem, new SeparatorMenuItem(), cItem);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return myMenu;

    }

    /**
     Init settings menu

     @return settings menu
     */
    public Menu initSettingsMenu() {
        // --- Menu
        Menu settingsMenu = new Menu("Setup");
        try {
            List<MenuItem> menuItemList = new ArrayList<>();
            //
            MenuItem aItem = new MenuItem("Import Network Configuration");
            menuItemList.add(aItem);
            aItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Network Configuration");
                    Tab tab = tabs.addOrSelectTab("Import Config");
                    // read config from json file into netConfig object
                    configView.importConfig();
                    // allow user to edit the config, and then save to replace netConfig object
                    // pane with label for each layer config
                    configView.setAllLayers((BorderPane) tab.getContent());
                }
            });

            //
            MenuItem bItem = new MenuItem("Create Network Configuration");
            menuItemList.add(bItem);
            bItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Network Configuration");
                    Tab tab = tabs.addOrSelectTab("Create Config");
                    // user must enter all config options, and then save to create netConfig object
                    // pane with label for each layer config
                    configView.createAllLayers((BorderPane) tab.getContent());
                }
            });
            //
            MenuItem cItem = new MenuItem("Export Network Configuration");
            menuItemList.add(cItem);
            cItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Export Configuration");
                    //   Tab tab = tabs.addOrSelectTab("Export Config");
                    // pane with horizontal sub tab for each layer config
                    configView.exportConfig();
                }
            });
            // disable menu item, until netConfig is created
            cItem.setDisable(true);
            configView.readyConfigProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                    //   LOG.log(Level.INFO, "prop change: old: " + oldVal + ", new: " + newVal);
                    Boolean readyProp = (Boolean) newVal;
                    // enable menu item
                    // disable menu item
                    cItem.setDisable(!readyProp.booleanValue());
                }
            });


            SeparatorMenuItem separator = new SeparatorMenuItem();
            menuItemList.add(separator);
            //
            importFitParamsItem = new MenuItem("Import Training Model Fit Parameters");
            menuItemList.add(importFitParamsItem);
            importFitParamsItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Import FitParams");
                    Tab tab = tabs.addOrSelectTab("Import FitParams");
                    if (TRAIN_NET.equalsIgnoreCase(netOption)) {
                        // training network
                        if (taskTrainView != null) {
                            taskTrainView.importFitParams();
                        }
                    } else if (TEST_NET.equalsIgnoreCase(netOption)) {
                        // testing network
                        if (taskTestView != null) {
                            taskTestView.importFitParams();
                        }
                    }
                }
            });
            // disable menu item, until taskview is created
            importFitParamsItem.setDisable(true);
            //
            exportFitParamsItem = new MenuItem("Export Training Model Fit Parameters");
            menuItemList.add(exportFitParamsItem);
            exportFitParamsItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Export FitParams");
                    LOG.log(Level.INFO, "netOption: " + netOption);
                    //  Tab tab = tabs.addOrSelectTab("Export FitParams");
                    if (TRAIN_NET.equalsIgnoreCase(netOption)) {
                        // training network
                        if (taskTrainView != null) {
                            taskTrainView.exportFitParams();
                        }
                    }
                }
            });
            // disable menu item, until FitParams is created
            exportFitParamsItem.setDisable(true);
/*
            //
            SeparatorMenuItem separator2 = new SeparatorMenuItem();
            menuItemList.add(separator2);
            //
            importMatrixItem = new MenuItem("Import Matrix Model");
            menuItemList.add(importMatrixItem);
            importMatrixItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Import Matrix Model");
                    Tab tab = tabs.addOrSelectTab("Import Matrix Model");
                            matrixView.importMatrixModel();
                }
            });
            // disable menu item, until taskview is created
            importMatrixItem.setDisable(true);
            //
            exportMatrixItem = new MenuItem("Export Matrix Model");
            menuItemList.add(exportMatrixItem);
            exportMatrixItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Export Matrix Model");
                            matrixView.exportMatrixModel();
                }
            });
            // disable menu item, until FitParams is created
            exportMatrixItem.setDisable(true);
            */

            //
            settingsMenu.getItems().addAll(menuItemList);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return settingsMenu;

    }

    /**
     Init options menu

     @return options menu
     */
    public Menu initOptionMenu() {
        // --- Menu
        Menu bMenu = new Menu("Process");
        try {
            List<MenuItem> menuItemList = new ArrayList<>();
            //
            MenuItem aItem = new MenuItem("Display sample");
            menuItemList.add(aItem);
            aItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Display digit");
                    Tab tab = tabs.addOrSelectTab("Digit");
                    pixelView.displayImage((BorderPane) tab.getContent());
                }
            });

            //
            MenuItem bItem = new MenuItem("Train the Network");
            menuItemList.add(bItem);
            bItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Run");
                    Tab tab = tabs.addOrSelectTab("Train");
                    taskTrainView = new TaskView((BorderPane) tab.getContent());
                    NetConfig config = configView.getNetConfig();
                    if (config != null) {
                        netOption = TRAIN_NET;
                        taskTrainView.setNetOption(TRAIN_NET);
                        taskTrainView.initConfig(config);
                        taskTrainView.initPanels();
                        taskTrainView.readyFitParamsProperty().addListener(new ChangeListener() {
                            @Override
                            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                                //   LOG.log(Level.INFO, "prop change: old: " + oldVal + ", new: " + newVal);
                                Boolean readyProp = (Boolean) newVal;
                                // FitParams is ready, enable menu item
                                // FitParams not ready, disable menu item
                                exportFitParamsItem.setDisable(!readyProp.booleanValue());
                            }
                        });
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Configuration is null",
                                ButtonType.OK);
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // do nothing
                        }

                    }
                }
            });
            // disable menu item, until netConfig is created
            bItem.setDisable(true);
            configView.readyConfigProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                    //  LOG.log(Level.INFO, "prop change: old: " + oldVal + ", new: " + newVal);
                    Boolean netProp = (Boolean) newVal;
                    // enable menu item
                    // disable menu item
                    bItem.setDisable(!netProp.booleanValue());
                }
            });

            //
            //
            MenuItem gItem = new MenuItem("Test the Network");
            menuItemList.add(gItem);
            gItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "Run test");
                    Tab tab = tabs.addOrSelectTab("Test");
                    taskTestView = new TaskView((BorderPane) tab.getContent());
                    NetConfig config = configView.getNetConfig();
                    if (config != null) {
                        netOption = TEST_NET;
                        taskTestView.setNetOption(TEST_NET);
                        taskTestView.initConfig(config);
                        taskTestView.initPanels();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Configuration is null",
                                ButtonType.OK);
                        Optional<ButtonType> result = alert.showAndWait();
                        LOG.log(Level.INFO, "alert result: " + result);
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // do nothing
                        }

                    }
                }
            });
            // disable menu item, until netConfig is created
            gItem.setDisable(true);
            configView.readyConfigProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                    //  LOG.log(Level.INFO, "prop change: old: " + oldVal + ", new: " + newVal);
                    Boolean netProp = (Boolean) newVal;
                    // enable menu item
                    // disable menu item
                    gItem.setDisable(!netProp.booleanValue());
                }
            });


            bMenu.getItems().addAll(menuItemList);
            // myMenu.getItems().addAll(aItem, bItem, new SeparatorMenuItem(), cItem);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return bMenu;

    }


    /**
     Init help menu

     @return help menu
     */
    public Menu initHelpMenu() {
        // --- Menu
        Menu bMenu = new Menu("Help");
        try {
            List<MenuItem> menuItemList = new ArrayList<>();
            //
            MenuItem aItem = new MenuItem("License");
            menuItemList.add(aItem);
            aItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "License");
                }
            });

            //
            MenuItem bItem = new MenuItem("About");
            menuItemList.add(bItem);
            bItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    LOG.log(Level.INFO, "About");
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("About Dialog");
                    dialog.setContentText("ConvoMainFx Application, version 2022.5.1");
                    dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                    dialog.showAndWait();
                }
            });


            bMenu.getItems().addAll(menuItemList);
            // myMenu.getItems().addAll(aItem, bItem, new SeparatorMenuItem(), cItem);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return bMenu;

    }

    /**
     Exit application
     */
//
    public void exitApp() {
        System.exit(0);
    }


}  //end class
