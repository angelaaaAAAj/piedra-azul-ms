module com.piedraazul {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    exports com.piedraazul.ui.auditoria;
    exports com.piedraazul.ui.pacientes;
}