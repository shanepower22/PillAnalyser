module com.pillanalyser.pillanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;


    opens com.pillanalyser.pillanalyser to javafx.fxml;
    exports com.pillanalyser.pillanalyser;
}