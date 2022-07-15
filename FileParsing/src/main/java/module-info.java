module com.example.fileparsing {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fileparsing to javafx.fxml;
    exports com.example.fileparsing;
}