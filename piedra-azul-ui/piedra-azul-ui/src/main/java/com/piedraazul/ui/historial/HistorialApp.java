package com.piedraazul.ui.historial;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HistorialApp extends Application {

    @Override
    public void start(Stage stage) {

        Label titulo = new Label("Historial Clínico - Piedra Azul");
        titulo.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1E3A5F;
                """);

        // Buscar por paciente
        TextField txtPacienteId = new TextField();
        txtPacienteId.setPromptText("ID Paciente");
        Button btnCargarPaciente = new Button("Buscar por Paciente");
        btnCargarPaciente.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);

        // Buscar por cita
        TextField txtCitaId = new TextField();
        txtCitaId.setPromptText("ID Cita");
        Button btnCargarCita = new Button("Buscar por Cita");
        btnCargarCita.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);

        Label lblFeedback = new Label();
        Label lblTotal = new Label("Registros: 0");

        GridPane busqueda = new GridPane();
        busqueda.setHgap(10);
        busqueda.setVgap(10);
        busqueda.add(txtPacienteId, 0, 0);
        busqueda.add(btnCargarPaciente, 1, 0);
        busqueda.add(txtCitaId, 0, 1);
        busqueda.add(btnCargarCita, 1, 1);

        TableView<HistorialEntry> tabla = new TableView<>();

        TableColumn<HistorialEntry, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());

        TableColumn<HistorialEntry, Number> colPaciente = new TableColumn<>("Paciente ID");
        colPaciente.setCellValueFactory(c -> c.getValue().pacienteIdProperty());

        TableColumn<HistorialEntry, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoRegistroProperty());

        TableColumn<HistorialEntry, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());

        TableColumn<HistorialEntry, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> c.getValue().fechaRegistroProperty());

        TableColumn<HistorialEntry, String> colRegistrado = new TableColumn<>("Registrado por");
        colRegistrado.setCellValueFactory(c -> c.getValue().registradoPorProperty());

        tabla.getColumns().addAll(colId, colPaciente, colTipo, colDesc, colFecha, colRegistrado);

        HistorialController controller = new HistorialController();

        btnCargarPaciente.setOnAction(e -> {
            try {
                controller.cargarPorPacienteId(
                        Long.parseLong(txtPacienteId.getText().trim()), tabla, lblFeedback, lblTotal);
            } catch (NumberFormatException ex) {
                lblFeedback.setText("ID inválido");
                lblFeedback.setStyle("-fx-text-fill: red;");
            }
        });

        btnCargarCita.setOnAction(e -> {
            try {
                controller.cargarPorCitaId(
                        Long.parseLong(txtCitaId.getText().trim()), tabla, lblFeedback, lblTotal);
            } catch (NumberFormatException ex) {
                lblFeedback.setText("ID inválido");
                lblFeedback.setStyle("-fx-text-fill: red;");
            }
        });

        VBox root = new VBox(15, titulo, busqueda, lblFeedback, lblTotal, tabla);
        root.setStyle("""
                -fx-padding: 20;
                -fx-background-color: #F4F6F9;
                """);

        stage.setTitle("Historial Clínico");
        stage.setScene(new Scene(root, 900, 500));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
