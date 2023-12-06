module com.example.spacing {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.json;


    opens com.example.spacing to javafx.fxml;
    exports com.example.spacing;
}