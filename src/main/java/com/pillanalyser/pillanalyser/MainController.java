package com.pillanalyser.pillanalyser;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainController {

    @FXML
    private ImageView mainImageView, analysisImageView;

    double imageScale;
    private Image loadedImage;
    private WritableImage adjustedImage;

    @FXML
    private BorderPane mainBorderPane, analysisBorderPane;

    @FXML
    private Slider toleranceSlider, hueSlider, saturationSlider, brightnessSlider, pillRangeSlider;

    @FXML
    private Label rangeLabel, pillSizeLabel;

    double tolerance = 0.08;
    private double hueValue = 0;

    private double saturationValue = 1;

    private double brightnessValue = 1;
    double selectedXCoord;
    double selectedYCoord;


    double pillSizeRange = 20;
    private int[] arrayForPixels;

    Map<Integer, PillInfo> pillInfoMap = new HashMap<>();
    private Map<Integer, Color> rootColors = new HashMap<>();

    private Map<Integer, Color> rootRandomColors = new HashMap<>();

    @FXML
    private void initialize() {

        mainImageView.setOnMouseClicked(e -> {
            selectedXCoord = e.getX() / imageScale;
            selectedYCoord = e.getY() / imageScale;
        });


        toleranceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tolerance = (double) newValue;
            analysePill();
        });

        hueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            hueValue = (double) newValue;
            Color hue = Color.hsb(hueValue, 1, brightnessValue);
            saturationSlider.setStyle("-track-color: linear-gradient(to right, #000000, rgb(" + hue.getRed() * 255 + ", " + hue.getGreen() * 255 + ", " + hue.getBlue() * 255 + "));");
            adjustColor();

            analysePill();

        });
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            saturationValue = (double) newValue;
            adjustColor();
            analysePill();
        });
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            brightnessValue = (double) newValue;
            adjustColor();
            analysePill();
        });

        pillRangeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            pillSizeRange = (double) newValue;
            rangeLabel.setText("Range: ±" + newValue);
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
            adjustedImage = null;
        }
        pillInfoMap.clear();
        rootColors.clear();
        rootRandomColors.clear();
    }

    private void adjustColor() {
        if (loadedImage != null) {
            PixelReader pixelReader = loadedImage.getPixelReader();
            int width = (int) loadedImage.getWidth();
            int height = (int) loadedImage.getHeight();
            adjustedImage = new WritableImage(width,height);
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


    public BufferedImage convertToFxImage(Image inputImage) {
        return SwingFXUtils.fromFXImage(inputImage, null);
    }

    public Image convertToJavaFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public BufferedImage applyGaussianBlur(BufferedImage sourceImage) {
        float[] matrix = {
                0, 1 / 8f, 0,
                1 / 8f, 1 / 2f, 1 / 8f,
                0, 1 / 8f, 0,
        };

        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage destImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
        op.filter(sourceImage, destImage);
        return destImage;
    }

    private void analyseImage() {
        Image startImage;
        if (adjustedImage != null) {
            startImage = adjustedImage;
        } else {
            startImage = loadedImage;
        }

        BufferedImage bufferedImage = convertToFxImage(startImage);
        BufferedImage blurredImage = applyGaussianBlur(bufferedImage);
        Image analysisImage = convertToJavaFXImage(blurredImage);

        arrayForPixels = new int[(int) loadedImage.getWidth() * (int) loadedImage.getHeight()];
        PixelReader pixelReader = analysisImage.getPixelReader();
        WritableImage analysedImage = new WritableImage((int) analysisImage.getWidth(), (int) analysisImage.getHeight());
        Color targetColor = pixelReader.getColor((int) selectedXCoord, (int) selectedYCoord);
        for (int x = 0; x < analysisImage.getWidth(); x++) {
            for (int y = 0; y < analysisImage.getHeight(); y++) {
                int index = y * (int) analysisImage.getWidth() + x;
                Color pixelColor = pixelReader.getColor(x, y);
                if (checkPixelColor(targetColor, pixelColor, tolerance)) {
                    analysedImage.getPixelWriter().setColor(x, y, Color.WHITE);
                    arrayForPixels[index] = index;

                } else {
                    analysedImage.getPixelWriter().setColor(x, y, Color.BLACK);
                    arrayForPixels[index] = 0;
                }
            }
        }
        assignSetColors(arrayForPixels, targetColor);
        assignRandomColors(arrayForPixels);
        setAnalysedImage(analysedImage);
    }

    public void assignSetColors(int[] arrayForPixels, Color targetColor) {
        for (int i = 0; i < arrayForPixels.length; i++) {
            if (arrayForPixels[i] != 0) {
                int root = DisjointSet.find(arrayForPixels, i);
                if (!rootColors.containsKey(root)) {
                    rootColors.put(root, targetColor);
                }
            }
        }
    }

    public void assignRandomColors(int[] arrayForPixels) {
        for (int i = 0; i < arrayForPixels.length; i++) {
            if (arrayForPixels[i] != 0) {
                int root = DisjointSet.find(arrayForPixels, i);
                if (!rootRandomColors.containsKey(root)) {
                    Color randomColor = generateRandomColor();
                    rootRandomColors.put(root, randomColor);
                }
            }
        }

    }
    private Color generateRandomColor() {
        return new Color(Math.random(), Math.random(), Math.random(), 1.0);
    }
    @FXML
    private void coloredDisjointSets() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        WritableImage coloredImage = new WritableImage(width,height);
        PixelWriter pixelWriter = coloredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int root = DisjointSet.find(arrayForPixels, index);
                if (arrayForPixels[index] != 0 && rootColors.containsKey(root)) {
                    pixelWriter.setColor(x, y, rootColors.get(root));
                } else {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }

        analysisImageView.setImage(coloredImage);
    }
    @FXML
    private void randomColorDisjointSets() {
        int width = (int) loadedImage.getWidth();
        int height = (int) loadedImage.getHeight();

        WritableImage coloredImage = new WritableImage(width,height);
        PixelWriter pixelWriter = coloredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int root = DisjointSet.find(arrayForPixels, index);
                if (arrayForPixels[index] != 0 && rootRandomColors.containsKey(root)) {
                    pixelWriter.setColor(x, y, rootRandomColors.get(root));
                } else {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }

        analysisImageView.setImage(coloredImage);
    }

    private boolean checkPixelColor(Color a, Color b, double tolerance) {
        double deltaRed = Math.abs(a.getRed() - b.getRed());
        double deltaGreen = Math.abs(a.getGreen() - b.getGreen());
        double deltaBlue = Math.abs(a.getBlue() - b.getBlue());
        return (deltaRed + deltaGreen + deltaBlue) / 3 < tolerance;
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
    public void analysePill(){
        analyseImage();
        int width = (int) loadedImage.getWidth();

        int selectedIndex = (int) selectedYCoord * (int) loadedImage.getWidth() + (int) selectedXCoord;

        int selectedPillSize = 0;
       for (int i = 0; i < arrayForPixels.length; i++) {
            if (arrayForPixels[i] != 0) {
                int pixelCoord = i;
                if (i + 1 < arrayForPixels.length && arrayForPixels[i + 1] != 0) {
                    DisjointSet.union(arrayForPixels, i, i + 1);
                }
                if (i - 1 >= 0 && arrayForPixels[i - 1] != 0) {
                    DisjointSet.union(arrayForPixels, i, i - 1);
                }
                if (i + width < arrayForPixels.length && arrayForPixels[i + width] != 0) {
                    DisjointSet.union(arrayForPixels, i, i + width);
                }
                if (i - width >= 0 && arrayForPixels[i - width] != 0) {
                    DisjointSet.union(arrayForPixels, i, i - width);
                }
                int parentIndex = DisjointSet.find(arrayForPixels, arrayForPixels[i]);

                if (!pillInfoMap.containsKey(parentIndex)) {
                    pillInfoMap.put(parentIndex, new PillInfo());
                }
                pillInfoMap.get(parentIndex).addPixelIndex(pixelCoord);
                if (selectedIndex == i) {
                    pillInfoMap.get(parentIndex).setSelectedPill();

                }
            }
        }
        for (PillInfo pill:pillInfoMap.values()) {
            if(pill.isSelectedPill()){
                selectedPillSize = pill.getPixelCoords().size();
                pillSizeLabel.setText("Selected Pill Size: " + selectedPillSize);
            }
        }
        List<Integer> pillsToRemove = new ArrayList<>();
        for (Map.Entry<Integer, PillInfo>  entry : pillInfoMap.entrySet()) {
            PillInfo pill = entry.getValue();
            if (!pill.isSelectedPill() && (Math.abs(pill.getPixelCoords().size() - selectedPillSize) > pillSizeRange) || pill.getPixelCoords().size() < 10) {
                pillsToRemove.add(entry.getKey());
            }
        }
        for (Integer key : pillsToRemove) {
            pillInfoMap.remove(key);
        }
        int pillIndex = 1;
        for (PillInfo pill:pillInfoMap.values()) {
            pill.setPillIndex(pillIndex);
            pillIndex++;
            }



        for (PillInfo pill:pillInfoMap.values()) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int j = 0; j < pill.getPixelCoords().size(); j++) {
                int pixelIndex = (int) pill.getPixelCoords().get(j);
                int x = pixelIndex % width;
                int y = pixelIndex / width;

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }

            drawBox(minX*imageScale, minY*imageScale, maxX*imageScale, maxY*imageScale, pill);
            }

            }

    private void drawBox(double minX, double minY, double maxX, double maxY, PillInfo pill) {
        Rectangle rectangle = new Rectangle(minX, mainBorderPane.getHeight() / 2 - mainImageView.getFitHeight() / 2 + minY, maxX - minX, maxY - minY);
        rectangle.setStroke(Color.BLUE);
        rectangle.setFill(Color.TRANSPARENT);

        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(pill.toStringProperty());


        Tooltip.install(rectangle, tooltip);

        rectangle.setOnMouseClicked(event -> {
            showPillDetailsDialog();
        });


        mainBorderPane.getChildren().add(rectangle);
    }

    private void showPillDetailsDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Pill Details");
        dialog.setHeaderText("Add new pill");
        dialog.setContentText("Enter Pill Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            for (PillInfo pill : pillInfoMap.values()) {
                pill.setPillName(name);
            }

        });
    }

        @FXML
        private void clearBoxes() {
            mainBorderPane.getChildren().removeIf(node -> node instanceof Rectangle);
            pillInfoMap.clear();
        }
}
