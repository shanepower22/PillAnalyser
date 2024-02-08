package com.pillanalyser.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {

    @FXML
    private ImageView mainImageView, analysisImageView;;

    private File imageFile;
    private Image loadedImage, analysisImage;
    private WritableImage adjustedImage;

    @FXML
    private BorderPane mainBorderPane, analysisBorderPane;

    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        fileChooser.setTitle("Open image");
        imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            loadedImage = new Image(imageFile.toURI().toString());
            mainImageView.setImage(loadedImage);
            double scale = calculateScale(loadedImage, mainBorderPane.getWidth(), mainBorderPane.getHeight());
            mainImageView.setFitWidth(loadedImage.getWidth() * scale);
            mainImageView.setFitHeight(loadedImage.getHeight() * scale);
        }
    }

    @FXML
    private void closeProgram() {
        Platform.exit();

    }
    private double calculateScale(Image image, double maxWidth, double maxHeight) {
        double widthRatio = maxWidth / image.getWidth();
        double heightRatio = maxHeight / image.getHeight();
        return Math.min(widthRatio, heightRatio);
    }
}