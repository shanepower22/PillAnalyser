package com.pillanalyser.pillanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainController {

    @FXML
    private ImageView mainImageView, analysisImageView;

    double imageScale;
    private Image loadedImage;
    private WritableImage adjustedImage;

    @FXML
    private BorderPane mainBorderPane, analysisBorderPane;

    @FXML
    private Slider toleranceSlider, hueSlider, saturationSlider, brightnessSlider;

    double tolerance = 0.08;
    private double hueValue = 0;

    private double saturationValue = 1;

    private double brightnessValue = 1;
    double selectedXCoord;
    double selectedYCoord;
    private int[] arrayForPixels;
    @FXML
    private void initialize() {
        mainImageView.setOnMouseClicked(e -> {
            selectedXCoord = e.getX() / imageScale;
            selectedYCoord = e.getY()/ imageScale;
            analyseImage();
        });


        toleranceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tolerance = (double) newValue;
            analyseImage();
        });

        hueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            hueValue = (double) newValue;
            Color hue = Color.hsb(hueValue, 1, brightnessValue);
            saturationSlider.setStyle("-track-color: linear-gradient(to right, #000000, rgb(" + hue.getRed() * 255 + ", " + hue.getGreen() * 255 + ", " + hue.getBlue() * 255 + "));");
            adjustColor();

            analyseImage();

        });
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            saturationValue = (double) newValue;
            adjustColor();
            analyseImage();
        });
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            brightnessValue = (double) newValue;
            adjustColor();
            analyseImage();

        });
    }
    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        fileChooser.setTitle("Open image");
        File imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            loadedImage = new Image(imageFile.toURI().toString());
            mainImageView.setImage(loadedImage);
            imageScale = calculateScale(loadedImage, mainBorderPane.getWidth(), mainBorderPane.getHeight());
            mainImageView.setFitWidth(loadedImage.getWidth() * imageScale);
            mainImageView.setFitHeight(loadedImage.getHeight() * imageScale);
        }
    }

    private void adjustColor() {
        if(loadedImage != null) {
            PixelReader pixelReader = loadedImage.getPixelReader();
            adjustedImage = new WritableImage((int) loadedImage.getWidth(), (int) loadedImage.getHeight());

            for (int x = 0; x < loadedImage.getWidth(); x++) {
                for (int y = 0; y < loadedImage.getHeight(); y++) {
                    Color color = pixelReader.getColor(x, y);
                    double adjustedHue = (color.getHue() + hueValue + 180) % 360;
                    double adjustedSaturation = Math.max(0.0, Math.min(color.getSaturation(), saturationValue));
                    double adjustedBrightness = Math.max(0.0, Math.min(color.getBrightness(), brightnessValue));
                    Color adjustedColor = Color.hsb(adjustedHue, adjustedSaturation, adjustedBrightness, color.getOpacity());
                    adjustedImage.getPixelWriter().setColor(x, y, adjustedColor);

                }
            }
            mainImageView.setImage(adjustedImage);
        }
    }
    private void analyseImage() {
        Image analysisImage;
        if (adjustedImage != null) {
            analysisImage = adjustedImage;
        } else {
            analysisImage = loadedImage;
        }
        arrayForPixels = new int[(int) loadedImage.getWidth() * (int) loadedImage.getHeight()];
        if (analysisImage != null) {
            PixelReader pixelReader = analysisImage.getPixelReader();
            WritableImage analysedImage = new WritableImage((int) analysisImage.getWidth(), (int) analysisImage.getHeight());
            Color targetColor = pixelReader.getColor((int) selectedXCoord, (int) selectedYCoord);
            for (int x = 0; x < analysisImage.getWidth(); x++) {
                for (int y = 0; y < analysisImage.getHeight(); y++) {
                    int index = y * (int) analysisImage.getWidth() + x ;
                    Color pixelColor = pixelReader.getColor(x, y);
                    if (checkPixelColor(targetColor, pixelColor, tolerance)) {
                        analysedImage.getPixelWriter().setColor(x, y, Color.WHITE);
                       arrayForPixels[index] = index;
                    } else {
                        analysedImage.getPixelWriter().setColor(x, y, Color.BLACK);
                        arrayForPixels[index]= 0;
                    }
                }
            }
            setAnalysedImage(analysedImage);
        }
    }

    private boolean checkPixelColor(Color a, Color b, double tolerance) {
    return Math.abs(a.getRed() - b.getRed()) < tolerance &&
            Math.abs(a.getGreen() - b.getGreen()) < tolerance &&
            Math.abs(a.getBlue() - b.getBlue()) < tolerance;
    }

    @FXML
    private void setAnalysedImage(Image analysedImage) {
        analysisImageView.setImage(analysedImage);
        double scale = calculateScale(analysedImage, analysisBorderPane.getWidth(), analysisBorderPane.getHeight());
        analysisImageView.setFitWidth(analysedImage.getWidth() * scale);
        analysisImageView.setFitHeight(analysedImage.getHeight() * scale);
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
    public void displayDisjointtext(){
        int width = (int) loadedImage.getWidth();
        Map<Integer, PillInfo> pillInfoMap = new HashMap<>();
       for (int i = 0; i < arrayForPixels.length; i++) {
            if (arrayForPixels[i] != 0) {

                if (i + 1 < arrayForPixels.length && arrayForPixels[i + 1] != 0) {
                    DisjointSet.union(arrayForPixels, i, i + 1);
                }
                if (i + width < arrayForPixels.length && arrayForPixels[i + width] != 0) {
                    DisjointSet.union(arrayForPixels, i, i + width);
                }
            }
        }
        for (int i = 0; i < arrayForPixels.length; i++) {
            System.out.print(DisjointSet.find  (arrayForPixels,i) + ((i+1) %width ==0 ? "\n": " "));
                }
            }
        }
    }
}