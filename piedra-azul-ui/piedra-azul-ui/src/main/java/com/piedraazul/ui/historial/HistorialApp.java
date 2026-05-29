package com.piedraazul.ui.historial;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HistorialApp extends Application {

    // ── Paleta idéntica al login ──
    private static final String AZUL_OSCURO = "#1E3A5F";
    private static final String AZUL_MEDIO  = "#1E88E5";
    private static final String FONDO       = "#F4F6F9";
    private static final String VERDE       = "#2E7D32";
    private static final String ROJO        = "#C62828";

    @Override
    public void start(Stage stage) {

        // ══ HEADER ══
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + AZUL_OSCURO + "; -fx-padding: 14 20 14 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("📋  Historial Clínico — Piedra Azul");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(lblTitulo);

        // ══ PANEL DE BÚSQUEDA ══
        HBox panelBusqueda = new HBox(10);
        panelBusqueda.setPadding(new Insets(14, 20, 14, 20));
        panelBusqueda.setAlignment(Pos.CENTER_LEFT);
        panelBusqueda.setStyle("-fx-background-color: white; "
                + "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Buscar por ID de paciente
        Label lblPac = etiqueta("ID Paciente:");
        TextField txtPacienteId = new TextField();
        txtPacienteId.setPromptText("Ej: 1");
        txtPacienteId.setPrefWidth(100);
        Button btnPaciente = boton("Buscar paciente", AZUL_MEDIO);

        // Separador visual
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Buscar por ID de cita
        Label lblCita = etiqueta("ID Cita:");
        TextField txtCitaId = new TextField();
        txtCitaId.setPromptText("Ej: 5");
        txtCitaId.setPrefWidth(100);
        Button btnCita = boton("Buscar por cita", AZUL_MEDIO);

        panelBusqueda.getChildren().addAll(
                lblPac, txtPacienteId, btnPaciente,
                sep,
                lblCita, txtCitaId, btnCita
        );

        // ══ TABLA ══
        TableView<HistorialEntry> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-font-size: 13px;");
        tabla.setPlaceholder(new Label("Ingresa un ID de paciente o cita para buscar."));

        TableColumn<HistorialEntry, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colId.setPrefWidth(55);

        TableColumn<HistorialEntry, Number> colPaciente = new TableColumn<>("Paciente ID");
        colPaciente.setCellValueFactory(c -> c.getValue().pacienteIdProperty());
        colPaciente.setPrefWidth(95);

        TableColumn<HistorialEntry, Number> colMedico = new TableColumn<>("Médico ID");
        colMedico.setCellValueFactory(c -> c.getValue().medicoIdProperty());
        colMedico.setPrefWidth(90);

        TableColumn<HistorialEntry, Number> colCita = new TableColumn<>("Cita ID");
        colCita.setCellValueFactory(c -> c.getValue().citaIdProperty());
        colCita.setPrefWidth(75);

        TableColumn<HistorialEntry, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(c -> c.getValue().tipoRegistroProperty());
        colTipo.setPrefWidth(130);

        TableColumn<HistorialEntry, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        colDesc.setPrefWidth(220);

        TableColumn<HistorialEntry, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> c.getValue().fechaRegistroProperty());
        colFecha.setPrefWidth(130);

        TableColumn<HistorialEntry, String> colRegistrado = new TableColumn<>("Registrado por");
        colRegistrado.setCellValueFactory(c -> c.getValue().registradoPorProperty());
        colRegistrado.setPrefWidth(130);

        tabla.getColumns().addAll(
                colId, colPaciente, colMedico, colCita,
                colTipo, colDesc, colFecha, colRegistrado);

        // ══ STATUS BAR ══
        Label lblStatus = new Label("Listo.");
        lblStatus.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12px;");
        HBox statusBar = new HBox(lblStatus);
        statusBar.setStyle("-fx-background-color: #e8eaf6; -fx-padding: 5 16 5 16;");

        // ══ CONTROLLER ══
        HistorialController controller = new HistorialController();

        btnPaciente.setOnAction(e -> {
            String txt = txtPacienteId.getText().trim();
            if (txt.isEmpty()) {
                lblStatus.setText("⚠  Ingresa el ID del paciente.");
                lblStatus.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-size: 12px;");
                return;
            }
            try {
                long id = Long.parseLong(txt);
                Label dummy = new Label();
                controller.cargarPorPacienteId(id, tabla, dummy, null);
                lblStatus.setStyle("-fx-text-fill: " + VERDE + "; -fx-font-size: 12px;");
                lblStatus.setText("✔  Historial del paciente " + id
                        + " — " + tabla.getItems().size() + " registros.");
            } catch (NumberFormatException ex) {
                lblStatus.setText("⚠  El ID debe ser un número.");
                lblStatus.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-size: 12px;");
            }
        });

        btnCita.setOnAction(e -> {
            String txt = txtCitaId.getText().trim();
            if (txt.isEmpty()) {
                lblStatus.setText("⚠  Ingresa el ID de la cita.");
                lblStatus.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-size: 12px;");
                return;
            }
            try {
                long id = Long.parseLong(txt);
                Label dummy = new Label();
                controller.cargarPorCitaId(id, tabla, dummy, null);
                lblStatus.setStyle("-fx-text-fill: " + VERDE + "; -fx-font-size: 12px;");
                lblStatus.setText("✔  Historial de la cita " + id
                        + " — " + tabla.getItems().size() + " registros.");
            } catch (NumberFormatException ex) {
                lblStatus.setText("⚠  El ID debe ser un número.");
                lblStatus.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-size: 12px;");
            }
        });

        // ══ LAYOUT RAÍZ ══
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FONDO + ";");
        root.setTop(new VBox(header, panelBusqueda));
        root.setCenter(tabla);
        root.setBottom(statusBar);

        stage.setTitle("Historial Clínico — Piedra Azul");
        stage.setScene(new Scene(root, 980, 580));
        stage.show();
    }


    // ── Helpers de estilo ──
    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        return b;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: " + AZUL_OSCURO + "; -fx-font-weight: bold;");
        return l;
    }

    public static void main(String[] args) {
        launch();
    }
}