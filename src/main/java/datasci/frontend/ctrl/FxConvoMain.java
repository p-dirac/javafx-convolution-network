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

package datasci.frontend.ctrl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Graphical interface for convolutional network for image classification on MNIST dataset
 * <p>
 * JavaFX javadocs:   https://openjfx.io/javadoc/18/
 *
 * @author r cook
 */
public class FxConvoMain extends Application {
    private static Logger LOG = Logger.getLogger(FxConvoMain.class.getName());

    public static final String APP_TITLE = "Convolution Network for Image Classification";
    private final BorderPane scenePanel = new BorderPane();
    private final static double SCENE_WIDTH = 1000;
    private final static double SCENE_HEIGHT = 750;
    //use border pane to put menu at top, content in center, status at bottom
    private final BorderPane rootPanel = new BorderPane();
    //app tab pane
    private final TabPane appTabPane = new TabPane();
    //dummy status panel at startup
    private final HBox statusPane = new HBox();
    //

    public static void main(String[] args) {
        // launch calls start
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("ConvoDigitsFx App");
        initLogging();
        try {
            LOG.log(Level.INFO, "start");
            //open client view window
            initComponents(primaryStage);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(0);
        }
    }
    /**
     * Init logging to file
     */
    public void initLogging() {
        try {
            System.out.println("initLogging");
            //   System.setProperty("java.util.logging.config.file", ".\\resources\\clientlog.properties");
            //   System.out.println("initLogging prop: " + logmgr.getProperty("java.util.logging.config.file"));
            String logPropFile = "clientlog.properties";
            InputStream fis = getClass().getClassLoader().getResourceAsStream(logPropFile);
          //  FileInputStream fis = new FileInputStream(logPropFile);
            if (fis != null) {
                LogManager logmgr = LogManager.getLogManager();
                logmgr.readConfiguration(fis);
                LOG.log(Level.INFO, "logPropFile: " + logPropFile);
            } else {
                System.out.println("initLogging error, log propertis file: " + logPropFile + " not found");
            }
        } catch (Exception e) {
            System.out.println("initLogging error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void initComponents(Stage stage) {
        try {
            //
            AppTabs tabs = new AppTabs();
            tabs.initPanel(rootPanel);

            AppMenu fxMenu = new AppMenu();
            // init view panel
            fxMenu.initViewer(tabs);
            //init menu
            MenuBar menuBar = fxMenu.initMenuBar();

            rootPanel.setTop(menuBar);


            LOG.log(Level.INFO, "show stage");
            //
            rootPanel.setBottom(statusPane);
            //
            Scene scene = new Scene(rootPanel, SCENE_WIDTH, SCENE_HEIGHT);
            //
            //   String css = "tools/account/resources/stylesAcc.css";
            //   scene.getStylesheets().add(css);
            //    LOG.log(Level.INFO, "scene getStylesheets:" + scene.getStylesheets());
            stage.setTitle(APP_TITLE);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }

    }


}  //end class
