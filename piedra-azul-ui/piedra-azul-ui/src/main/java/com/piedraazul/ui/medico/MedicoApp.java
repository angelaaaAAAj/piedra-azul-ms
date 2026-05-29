package com.piedraazul.ui.medico;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.piedraazul.ui.agenda.Agenda;
import com.piedraazul.ui.historial.HistorialApp;
import com.piedraazul.ui.historial.HistorialEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MedicoApp extends Application {

    // ── Paleta ──
    private static final String AZUL_OSCURO = "#1E3A5F";
    private static final String AZUL_MEDIO  = "#1E88E5";
    private static final String FONDO       = "#F4F6F9";
    private static final String VERDE       = "#2E7D32";
    private static final String ROJO        = "#C62828";
    private static final String NARANJA     = "#F57C00";

    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient   http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Datos del médico autenticado
    private Long   medicoId;
    private String nombreMedico;

    private final ObservableList<Agenda> citasData = FXCollections.observableArrayList();
    private Label lblStatus;

    // Constructor para uso desde LoginController
    public MedicoApp(Long medicoId, String nombreMedico) {
        this.medicoId    = medicoId;
        this.nombreMedico = nombreMedico;
    }

    // Constructor vacío requerido por JavaFX Application
    public MedicoApp() {}

    @Override
    public void start(Stage stage) {

        // ══ HEADER ══
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + AZUL_OSCURO
                + "; -fx-padding: 14 20 14 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("👨‍⚕️  Portal del Médico — " + nombreMedico);
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px;"
                + " -fx-font-weight: bold;");
        header.getChildren().add(lblTitulo);

        // ══ TABS ══
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabs.getTabs().addAll(
                tabMisCitas(stage),
                tabHorarios(stage),
                tabHistorialPaciente()
        );

        // ══ STATUS BAR ══
        lblStatus = new Label("Listo.");
        lblStatus.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12px;");
        HBox statusBar = new HBox(lblStatus);
        statusBar.setStyle("-fx-background-color: #e8eaf6; -fx-padding: 5 16 5 16;");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FONDO + ";");
        root.setTop(header);
        root.setCenter(tabs);
        root.setBottom(statusBar);

        stage.setTitle("Portal Médico — Piedra Azul");
        stage.setScene(new Scene(root, 1000, 640));
        stage.show();

        cargarMisCitas();
    }

    // ══════════════════════════════════════════════════════
    //  TAB 1 — MIS CITAS
    // ══════════════════════════════════════════════════════
    private Tab tabMisCitas(Stage owner) {
        Tab tab = new Tab("📋  Mis Citas");

        // Barra superior
        HBox barra = new HBox(10);
        barra.setPadding(new Insets(10, 16, 10, 16));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setStyle("-fx-background-color: white; "
                + "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button btnRecargar = boton("↺  Recargar", AZUL_MEDIO);
        btnRecargar.setOnAction(e -> cargarMisCitas());

        // Filtro por estado
        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("TODAS", "PROGRAMADA", "CONFIRMADA",
                "CANCELADA", "REAGENDADA", "COMPLETADA");
        cbEstado.setValue("TODAS");
        cbEstado.setPrefWidth(150);
        cbEstado.setOnAction(e -> filtrarPorEstado(cbEstado.getValue()));

        barra.getChildren().addAll(
                etiqueta("Filtrar por estado:"), cbEstado, btnRecargar);

        // Tabla
        TableView<Agenda> tabla = new TableView<>(citasData);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPlaceholder(new Label("No tienes citas registradas."));

        TableColumn<Agenda, Long>   colId       = col("ID",         "id",         55);
        TableColumn<Agenda, Long>   colPaciente = col("Paciente ID","pacienteId", 100);
        TableColumn<Agenda, String> colFecha    = col("Fecha/Hora", "fechaHora",  170);
        TableColumn<Agenda, String> colMotivo   = col("Motivo",     "motivo",     160);
        TableColumn<Agenda, String> colEstado   = col("Estado",     "estado",     110);
        TableColumn<Agenda, String> colObs      = col("Observaciones","observaciones", 180);

        // Color por estado
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Agenda c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setStyle(""); return; }
                switch (c.getEstado() == null ? "" : c.getEstado()) {
                    case "CANCELADA"  -> setStyle("-fx-background-color: #ffcdd2;");
                    case "COMPLETADA" -> setStyle("-fx-background-color: #c8e6c9;");
                    case "REAGENDADA" -> setStyle("-fx-background-color: #fff9c4;");
                    case "CONFIRMADA" -> setStyle("-fx-background-color: #bbdefb;");
                    default           -> setStyle("");
                }
            }
        });

        tabla.getColumns().addAll(colId, colPaciente, colFecha, colMotivo, colEstado, colObs);

        VBox contenido = new VBox(barra, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        tab.setContent(contenido);
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  TAB 2 — MIS HORARIOS
    // ══════════════════════════════════════════════════════
    private Tab tabHorarios(Stage owner) {
        Tab tab = new Tab("🕐  Mis Horarios");

        Label lblInfo = new Label(
                "Aquí puedes definir o editar los bloques horarios en los que atiendes.");
        lblInfo.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 12px;");
        lblInfo.setWrapText(true);

        // Lista de horarios agregados (en memoria, para mostrar)
        ObservableList<String> horarios = FXCollections.observableArrayList();
        ListView<String> listaHorarios = new ListView<>(horarios);
        listaHorarios.setPrefHeight(220);

        // Formulario para agregar horario
        Label lblDia = etiqueta("Día de la semana:");
        ComboBox<String> cbDia = new ComboBox<>();
        cbDia.getItems().addAll(
                "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES");
        cbDia.setPromptText("Seleccionar día");
        cbDia.setPrefWidth(160);

        Label lblDesde = etiqueta("Desde (HH:mm):");
        TextField txtDesde = new TextField();
        txtDesde.setPromptText("08:00");
        txtDesde.setPrefWidth(90);

        Label lblHasta = etiqueta("Hasta (HH:mm):");
        TextField txtHasta = new TextField();
        txtHasta.setPromptText("17:00");
        txtHasta.setPrefWidth(90);

        Button btnAgregar = boton("+ Agregar horario", VERDE);
        Button btnEliminar = boton("✖ Eliminar seleccionado", ROJO);
        Button btnGuardar  = boton("💾  Guardar horarios", AZUL_OSCURO);

        btnAgregar.setOnAction(e -> {
            String dia   = cbDia.getValue();
            String desde = txtDesde.getText().trim();
            String hasta = txtHasta.getText().trim();
            if (dia == null || desde.isEmpty() || hasta.isEmpty()) {
                status("⚠  Completa día, hora inicio y hora fin.", ROJO);
                return;
            }
            // Validar formato HH:mm
            try {
                LocalTime.parse(desde, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime.parse(hasta, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ex) {
                status("⚠  Formato de hora inválido. Usa HH:mm (ej: 08:00).", ROJO);
                return;
            }
            horarios.add(dia + "  " + desde + " → " + hasta);
            cbDia.setValue(null);
            txtDesde.clear();
            txtHasta.clear();
            status("Horario agregado.", VERDE);
        });

        btnEliminar.setOnAction(e -> {
            String sel = listaHorarios.getSelectionModel().getSelectedItem();
            if (sel != null) {
                horarios.remove(sel);
                status("Horario eliminado.", NARANJA);
            } else {
                status("⚠  Selecciona un horario de la lista.", ROJO);
            }
        });

        // Guardar: actualiza disponibilidad del médico en el backend
        btnGuardar.setOnAction(e -> {
            if (medicoId == null) {
                status("⚠  No se encontró tu ID de médico.", ROJO);
                return;
            }
            // Marcar médico como disponible al guardar horarios
            new Thread(() -> {
                try {
                    String json = "{\"disponible\": true}";
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/api/medicos/"
                                    + medicoId + "/disponibilidad"))
                            .header("Content-Type", "application/json")
                            .method("PATCH",
                                    HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    HttpResponse<String> resp = http.send(req,
                            HttpResponse.BodyHandlers.ofString());
                    Platform.runLater(() -> {
                        if (resp.statusCode() == 200) {
                            status("✔  Horarios guardados y disponibilidad actualizada.", VERDE);
                        } else {
                            status("⚠  No se pudo actualizar disponibilidad.", ROJO);
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() ->
                            status("⚠  Error de conexión: " + ex.getMessage(), ROJO));
                }
            }).start();
        });

        // Layout del formulario
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(12, 0, 12, 0));
        form.add(lblDia,   0, 0); form.add(cbDia,   1, 0);
        form.add(lblDesde, 2, 0); form.add(txtDesde, 3, 0);
        form.add(lblHasta, 4, 0); form.add(txtHasta, 5, 0);
        form.add(btnAgregar, 6, 0);

        HBox botones = new HBox(10, btnEliminar, btnGuardar);

        VBox contenido = new VBox(14,
                lblInfo, form,
                etiqueta("Horarios configurados:"),
                listaHorarios, botones);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + FONDO + ";");

        tab.setContent(contenido);
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  TAB 3 — HISTORIAL DE PACIENTE
    // ══════════════════════════════════════════════════════
    private Tab tabHistorialPaciente() {
        Tab tab = new Tab("📁  Historial del Paciente");

        Label lblDesc = new Label(
                "Busca el historial clínico de un paciente ingresando su ID.");
        lblDesc.setStyle("-fx-text-fill: #546e7a; -fx-font-size: 12px;");

        HBox busqueda = new HBox(10);
        busqueda.setAlignment(Pos.CENTER_LEFT);
        busqueda.setPadding(new Insets(0, 0, 10, 0));

        TextField txtPacId = new TextField();
        txtPacId.setPromptText("ID del paciente");
        txtPacId.setPrefWidth(160);
        Button btnBuscar = boton("Buscar historial", AZUL_MEDIO);

        busqueda.getChildren().addAll(etiqueta("ID Paciente:"), txtPacId, btnBuscar);

        // Tabla de historial
        ObservableList<HistorialEntry> histData = FXCollections.observableArrayList();
        TableView<HistorialEntry> tablaH = new TableView<>(histData);
        tablaH.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaH.setPlaceholder(new Label("Ingresa un ID de paciente para buscar."));

        TableColumn<HistorialEntry, Number> hColId   = new TableColumn<>("ID");
        hColId.setCellValueFactory(c -> c.getValue().idProperty());
        hColId.setPrefWidth(55);

        TableColumn<HistorialEntry, String> hColTipo = new TableColumn<>("Tipo");
        hColTipo.setCellValueFactory(c -> c.getValue().tipoRegistroProperty());
        hColTipo.setPrefWidth(130);

        TableColumn<HistorialEntry, String> hColDesc = new TableColumn<>("Descripción");
        hColDesc.setCellValueFactory(c -> c.getValue().descripcionProperty());
        hColDesc.setPrefWidth(240);

        TableColumn<HistorialEntry, String> hColFecha = new TableColumn<>("Fecha");
        hColFecha.setCellValueFactory(c -> c.getValue().fechaRegistroProperty());
        hColFecha.setPrefWidth(130);

        TableColumn<HistorialEntry, String> hColReg = new TableColumn<>("Registrado por");
        hColReg.setCellValueFactory(c -> c.getValue().registradoPorProperty());
        hColReg.setPrefWidth(130);

        tablaH.getColumns().addAll(hColId, hColTipo, hColDesc, hColFecha, hColReg);

        Label lblResultH = new Label();

        btnBuscar.setOnAction(e -> {
            String txt = txtPacId.getText().trim();
            if (txt.isEmpty()) {
                lblResultH.setText("⚠  Ingresa el ID del paciente.");
                lblResultH.setStyle("-fx-text-fill: " + ROJO + ";");
                return;
            }
            try {
                long id = Long.parseLong(txt);
                new com.piedraazul.ui.historial.HistorialController()
                        .cargarPorPacienteId(id, tablaH, lblResultH, null);
                lblResultH.setStyle("-fx-text-fill: " + VERDE + ";");
            } catch (NumberFormatException ex) {
                lblResultH.setText("⚠  El ID debe ser un número.");
                lblResultH.setStyle("-fx-text-fill: " + ROJO + ";");
            }
        });

        VBox contenido = new VBox(14,
                lblDesc, busqueda, lblResultH, tablaH);
        VBox.setVgrow(tablaH, Priority.ALWAYS);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + FONDO + ";");

        tab.setContent(contenido);
        return tab;
    }

    // ══════════════════════════════════════════════════════
    //  BACKEND
    // ══════════════════════════════════════════════════════
    private void cargarMisCitas() {
        if (medicoId == null) {
            status("⚠  No se encontró tu ID de médico.", ROJO);
            return;
        }
        status("Cargando citas...", AZUL_MEDIO);
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/citas/medico/" + medicoId))
                        .GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                List<Agenda> lista = mapper.readValue(resp.body(),
                        new TypeReference<>() {});
                Platform.runLater(() -> {
                    citasData.setAll(lista);
                    status("✔  " + lista.size() + " citas cargadas.", VERDE);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        status("⚠  Error al cargar citas: " + e.getMessage(), ROJO));
            }
        }).start();
    }

    private void filtrarPorEstado(String estado) {
        if ("TODAS".equals(estado)) {
            cargarMisCitas();
            return;
        }
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/citas/medico/" + medicoId))
                        .GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());
                List<Agenda> todas = mapper.readValue(resp.body(),
                        new TypeReference<>() {});
                List<Agenda> filtradas = todas.stream()
                        .filter(c -> estado.equals(c.getEstado()))
                        .toList();
                Platform.runLater(() -> {
                    citasData.setAll(filtradas);
                    status("Filtro \"" + estado + "\": "
                            + filtradas.size() + " citas.", AZUL_MEDIO);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        status("⚠  Error al filtrar: " + e.getMessage(), ROJO));
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════
    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold;"
                + " -fx-cursor: hand; -fx-background-radius: 4;");
        return b;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: " + AZUL_OSCURO + "; -fx-font-weight: bold;");
        return l;
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Agenda, T> col(String titulo, String prop, double ancho) {
        TableColumn<Agenda, T> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(ancho);
        return c;
    }

    private void status(String msg, String color) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
    }

    public static void main(String[] args) { launch(); }
}