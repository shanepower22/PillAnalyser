package com.pillanalyser.pillanalyser;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PillInfo {
    private List<Integer> pixelCoords;

    private String pillName;
    private int pillIndex;
    private boolean selectedPill = false;

    private StringProperty toStringProperty = new SimpleStringProperty();

    public PillInfo() {
        pixelCoords = new ArrayList<>();
        updateToStringProperty();
    }

    public int getPillSize() {
        return pixelCoords.size();
    }




    public String getPillName() {
        return pillName;
    }

    public void setPillName(String pillName) {
        this.pillName = pillName;
        updateToStringProperty();
    }

    public int getPillIndex() {
        return pillIndex;
    }

    public void setPillIndex(int pillIndex) {
        this.pillIndex = pillIndex;
        updateToStringProperty();
    }

    public void addPixelIndex(int pixelIndex) {
        pixelCoords.add(pixelIndex);
    }

    public List getPixelCoords(){
        return pixelCoords;
    }

    public void setSelectedPill() {
        selectedPill = true;
    }
    public boolean isSelectedPill(){
        return selectedPill;
    }

    public StringProperty toStringProperty() {
        return toStringProperty;
    }

    private void updateToStringProperty() {
        toStringProperty.set(toString());
    }
    @Override
    public String toString() {
        return "Pill Name: " + getPillName() + "\n" +
                "Pill Number: " + getPillIndex() + "\n" +
                "Pill size(pixel units): " + getPillSize();
    }
}
