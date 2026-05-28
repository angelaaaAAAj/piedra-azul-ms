module com.piedraazul {

    requires javafx.controls;
    requires javafx.fxml;

    opens com.piedraazul.controller to javafx.fxml;

    exports com.piedraazul.app;
}