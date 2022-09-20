package datasci.frontend.config;

import datasci.backend.model.ImageDataUtil;
import datasci.backend.model.Matrix;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PixelView {

    private static final Logger LOG = Logger.getLogger(PixelView.class.getName());
    private BorderPane pane;
    private ScrollPane scroller = new ScrollPane();
    private File imageFile;
    private FileInputStream fis;

    public PixelView() {
    }

    public BorderPane getPane() {
        return pane;
    }

    public void displayImage(BorderPane pane) {
        try {
            this.pane = pane;
            // sample: path = "/digitImage/training/3/74.png";
            BufferedInputStream bufferIS = imageFileDialog();
            if(bufferIS != null) {
                LOG.log(Level.INFO, "dialog bufferIS:" + bufferIS);
                Image img = initData(bufferIS);
                Matrix imageMatrix = ImageDataUtil.loadImageData(imageFile);
                LOG.info("imageMatrix: " + imageMatrix);
                if (img != null) {
                    LOG.log(Level.INFO, "viewData");
                    viewData(img, imageMatrix);
                }
            } else{
                LOG.log(Level.INFO, "bufferIS is null");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public BufferedInputStream imageFileDialog() {
      //  String path = null;
        BufferedInputStream bufferIS = null;
                FileInputStream fis = null;
        try {
            FileChooser fileDialog = new FileChooser();
                fileDialog.setTitle("Image file");
                // Show dialog
                imageFile = fileDialog.showOpenDialog(null);
                    LOG.log(Level.INFO, "imageFile: " + imageFile);
                if (imageFile != null) {
                    fis = new FileInputStream(imageFile);
                    if(fis != null) {
                        bufferIS = new BufferedInputStream(fis);
                    }
                } else{
                    LOG.log(Level.INFO, "open alert");
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Image file is null",
                            ButtonType.OK);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // do nothing
                    }
                }
                if(bufferIS == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Error loading image file",
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
        return bufferIS;
    }


        public Image initData(BufferedInputStream bis) {
        Image image = null;
        try {
            if (bis != null) {
                image = new Image(bis);
                bis.close();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return image;
    }

    public void viewData(Image img, Matrix imageMatrix) {
        try {
            LOG.log(Level.INFO, "viewData");
            ImageView imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setImage(img);
            //
            // font size: point size of the font
            Font titleFont = Font.font("Arial", FontWeight.BOLD, 18.0);
            //
            LOG.log(Level.INFO, "set imageView");
            VBox imageBox = new VBox();
            imageBox.setAlignment(Pos.CENTER);
            imageBox.setSpacing(10);
            imageBox.setPrefSize(250, 300);
            Label imageTitle = new Label("Actual Image");
            imageTitle.setFont(titleFont);
            imageBox.getChildren().addAll(imageTitle, imageView);
            // insets top, right, bottom, left
            VBox.setMargin(imageTitle, new Insets(0,50,0,50));
            VBox.setMargin(imageView, new Insets(20,50,20,50));
            pane.setLeft(imageBox);
            //
            if(imageMatrix != null) {
                ImagePane imagePane = new ImagePane(imageMatrix);
                VBox matrixBox = new VBox();
                matrixBox.setAlignment(Pos.CENTER);
                matrixBox.setSpacing(10);
                Label gridTitle = new Label("Normalized Image Grid");
                gridTitle.setFont(titleFont);
                matrixBox.getChildren().addAll(gridTitle, imagePane);
                scroller.setContent(matrixBox);
              //  scroller.setFitToWidth(true);
                scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                pane.setCenter(scroller);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

}