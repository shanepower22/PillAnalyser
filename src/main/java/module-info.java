module com.pillanalyser.pillanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.pillanalyser.pillanalyser to javafx.fxml;
    exports com.pillanalyser.pillanalyser;
}