package com.piedraazul.ui.agenda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AgendaApp extends Application {

    // ── Paleta idéntica al login ──
    private static final String AZUL_OSCURO  = "#1E3A5F";
    private static final String AZUL_MEDIO   = "#1E88E5";
    private static final String FONDO        = "#F4F6F9";
    private static final String BLANCO       = "#FFFFFF";
    private static final String ROJO         = "#C62828";
    private static final String NARANJA      = "#F57C00";
    private static final String VERDE        = "#2E7D32";

    private static final String BASE_URL     = "http://localhost:8080";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final HttpClient http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private final ObservableList<Agenda>        citasData   = FXCollections.observableArrayList();
    private final ObservableList<Agenda.Medico> medicosData = FXCollections.observableArrayList();

    private TableView<Agenda> tablaCitas;
    private ComboBox<Agenda.Medico> cmbMedicoFiltro;
    private Label lblStatus;

    @Override
    public void start(Stage stage) {

        // ══ HEADER ══
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + AZUL_OSCURO + "; -fx-padding: 14 20 14 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("🗓  Gestión de Agenda — Piedra Azul");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(lblTitulo);

        // ══ BARRA DE ACCIONES ══
        HBox barraAcciones = new HBox(10);
        barraAcciones.setPadding(new Insets(10, 20, 6, 20));
        barraAcciones.setAlignment(Pos.CENTER_LEFT);
        barraAcciones.setStyle("-fx-background-color: " + BLANCO + ";");

        // Buscar por paciente
        TextField txtBuscarPaciente = new TextField();
        txtBuscarPaciente.setPromptText("ID paciente...");
        txtBuscarPaciente.setPrefWidth(130);

        Button btnBuscarPaciente = boton("Buscar paciente", AZUL_MEDIO);
        btnBuscarPaciente.setOnAction(e -> buscarPorPaciente(txtBuscarPaciente.getText().trim()));

        // Filtro por médico
        cmbMedicoFiltro = new ComboBox<>(medicosData);
        cmbMedicoFiltro.setPromptText("Filtrar por médico...");
        cmbMedicoFiltro.setPrefWidth(220);

        Button btnFiltrarMedico = boton("Filtrar", AZUL_MEDIO);
        btnFiltrarMedico.setOnAction(e -> {
            if (cmbMedicoFiltro.getValue() != null)
                buscarPorMedico(cmbMedicoFiltro.getValue().getId());
        });

        Button btnTodas  = boton("↺  Todas", AZUL_OSCURO);
        btnTodas.setOnAction(e -> cargarCitas());

        Button btnNueva  = boton("+ Nueva cita", VERDE);
        btnNueva.setOnAction(e -> abrirDialogoNuevaCita(stage));

        barraAcciones.getChildren().addAll(
                txtBuscarPaciente, btnBuscarPaciente,
                new Separator(), cmbMedicoFiltro, btnFiltrarMedico,
                new Separator(), btnTodas,
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                btnNueva
        );

        // ══ TABLA ══
        tablaCitas = new TableView<>(citasData);
        tablaCitas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Agenda, Long>   colId       = col("ID",        "id",           60);
        TableColumn<Agenda, Long>   colPaciente = col("Paciente",  "pacienteId",   90);
        TableColumn<Agenda, String> colFecha    = col("Fecha/Hora","fechaHora",   170);
        TableColumn<Agenda, String> colMotivo   = col("Motivo",    "motivo",      150);
        TableColumn<Agenda, String> colEstado   = col("Estado",    "estado",      110);

        // Columna médico (objeto anidado)
        TableColumn<Agenda, String> colMedico = new TableColumn<>("Médico");
        colMedico.setPrefWidth(180);
        colMedico.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombreMedico()));

        // Columna acciones (cancelar / reagendar inline)
        TableColumn<Agenda, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(200);
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            private final Button btnCancelar   = boton("✖ Cancelar", ROJO);
            private final Button btnReagendar  = boton("📅 Reagendar", NARANJA);
            private final HBox   box           = new HBox(6, btnCancelar, btnReagendar);

            {
                btnCancelar.setOnAction(e -> {
                    Agenda cita = getTableView().getItems().get(getIndex());
                    cancelarCita(cita);
                });
                btnReagendar.setOnAction(e -> {
                    Agenda cita = getTableView().getItems().get(getIndex());
                    abrirDialogoReagendar(stage, cita);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Agenda cita = getTableView().getItems().get(getIndex());
                    // Ocultar acciones si ya está cancelada o completada
                    boolean bloqueada = "CANCELADA".equals(cita.getEstado())
                            || "COMPLETADA".equals(cita.getEstado());
                    btnCancelar.setDisable(bloqueada);
                    btnReagendar.setDisable(bloqueada);
                    setGraphic(box);
                }
            }
        });

        // Color de fila según estado
        tablaCitas.setRowFactory(tv -> new TableRow<>() {
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

        tablaCitas.getColumns().addAll(
                colId, colPaciente, colMedico, colFecha, colMotivo, colEstado, colAcciones);

        // ══ STATUS BAR ══
        lblStatus = new Label("Listo.");
        HBox statusBar = new HBox(lblStatus);
        statusBar.setStyle("-fx-background-color: #e8eaf6; -fx-padding: 5 16 5 16;");
        lblStatus.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12px;");

        // ══ LAYOUT RAÍZ ══
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FONDO + ";");
        root.setTop(new VBox(header, barraAcciones));
        root.setCenter(tablaCitas);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1000, 620);
        stage.setTitle("Gestión de Agenda — Piedra Azul");
        stage.setScene(scene);
        stage.show();

        cargarMedicos();
        cargarCitas();
    }

    // ══════════════════════════════════════════════════════
    //  DIALOGO — Nueva cita (buscar paciente primero)
    // ══════════════════════════════════════════════════════
    private void abrirDialogoNuevaCita(Stage owner) {
        Stage dlg = new Stage();
        dlg.initOwner(owner);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Nueva cita");

        // ── Paso 1: buscar paciente ──
        Label lblDoc = new Label("Número de identificación del paciente:");
        lblDoc.setStyle("-fx-font-weight: bold; -fx-text-fill: " + AZUL_OSCURO + ";");
        TextField txtDoc = new TextField();
        txtDoc.setPromptText("Ej: 1001234567");
        Button btnBuscar = boton("Buscar", AZUL_MEDIO);
        Label lblResultado = new Label();
        lblResultado.setStyle("-fx-text-fill: " + AZUL_OSCURO + ";");

        // ── Paso 2: formulario cita (oculto hasta encontrar paciente) ──
        VBox panelCita = new VBox(10);
        panelCita.setVisible(false);
        panelCita.setManaged(false);

        ComboBox<Agenda.Medico> cmbMedico = new ComboBox<>(medicosData);
        cmbMedico.setPromptText("Seleccionar médico");
        cmbMedico.setMaxWidth(Double.MAX_VALUE);

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivo de la consulta");

        // DatePicker con restricción: solo hoy en adelante
        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha de la cita");
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        TextField txtHora = new TextField();
        txtHora.setPromptText("Hora (HH:mm) — ej: 10:30");

        ComboBox<String> cmbEstrategia = new ComboBox<>();
        cmbEstrategia.getItems().addAll("primerDisponible", "horarioCercano");
        cmbEstrategia.setValue("primerDisponible");
        cmbEstrategia.setMaxWidth(Double.MAX_VALUE);

        Button btnAgendar = boton("✔  Agendar cita", VERDE);
        btnAgendar.setMaxWidth(Double.MAX_VALUE);

        panelCita.getChildren().addAll(
                separadorLabel("Médico *"), cmbMedico,
                separadorLabel("Motivo"),   txtMotivo,
                separadorLabel("Fecha *"),  dpFecha,
                separadorLabel("Hora (dejar vacío = automático)"), txtHora,
                separadorLabel("Estrategia"), cmbEstrategia,
                btnAgendar
        );

        // ID del paciente encontrado
        final long[] pacienteIdRef = {-1};

        btnBuscar.setOnAction(e -> {
            String doc = txtDoc.getText().trim();
            if (doc.isEmpty()) return;
            try {
                HttpResponse<String> resp = get("/api/pacientes/documento/" + doc);
                if (resp.statusCode() == 200) {
                    Map<?, ?> pac = mapper.readValue(resp.body(), Map.class);
                    pacienteIdRef[0] = Long.parseLong(pac.get("id").toString());
                    lblResultado.setText("✔  " + pac.get("nombre") + " " + pac.get("apellido")
                            + "  (ID: " + pacienteIdRef[0] + ")");
                    lblResultado.setStyle("-fx-text-fill: " + VERDE + "; -fx-font-weight: bold;");
                    panelCita.setVisible(true);
                    panelCita.setManaged(true);
                    dlg.sizeToScene();
                } else {
                    lblResultado.setText("✖  Paciente no encontrado.");
                    lblResultado.setStyle("-fx-text-fill: " + ROJO + "; -fx-font-weight: bold;");
                    panelCita.setVisible(false);
                    panelCita.setManaged(false);
                }
            } catch (Exception ex) {
                lblResultado.setText("Error al buscar: " + ex.getMessage());
            }
        });

        btnAgendar.setOnAction(e -> {
            if (pacienteIdRef[0] < 0) return;
            if (cmbMedico.getValue() == null) { alerta("Médico requerido", "Selecciona un médico."); return; }
            if (dpFecha.getValue() == null && txtHora.getText().isBlank() ) {
                // sin fecha manual → estrategia automática
            }

            try {
                String fechaHoraManual = "";
                if (dpFecha.getValue() != null && !txtHora.getText().isBlank()) {
                    fechaHoraManual = dpFecha.getValue().toString() + "T" + txtHora.getText().trim();
                }

                String json = """
                    {
                      "pacienteId": %d,
                      "medicoId": %d,
                      "motivo": "%s",
                      "estrategia": "%s"%s
                    }
                    """.formatted(
                        pacienteIdRef[0],
                        cmbMedico.getValue().getId(),
                        txtMotivo.getText().trim(),
                        cmbEstrategia.getValue(),
                        fechaHoraManual.isBlank() ? ""
                                : ",\n  \"fechaHoraManual\": \"" + fechaHoraManual + "\""
                );

                HttpResponse<String> resp = post("/api/citas", json);
                if (resp.statusCode() == 201) {
                    Agenda nueva = mapper.readValue(resp.body(), Agenda.class);
                    citasData.add(nueva);
                    status("Cita agendada — ID: " + nueva.getId());
                    dlg.close();
                } else {
                    Map<?, ?> err = mapper.readValue(resp.body(), Map.class);
                    alerta("Error al agendar", err.getOrDefault("error", resp.body()).toString());
                }
            } catch (Exception ex) {
                alerta("Error", ex.getMessage());
            }
        });

        VBox contenido = new VBox(12,
                lblDoc,
                new HBox(8, txtDoc, btnBuscar),
                lblResultado,
                panelCita
        );
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + FONDO + ";");
        contenido.setPrefWidth(420);

        dlg.setScene(new Scene(contenido));
        dlg.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  DIALOGO — Reagendar
    // ══════════════════════════════════════════════════════
    private void abrirDialogoReagendar(Stage owner, Agenda cita) {
        Stage dlg = new Stage();
        dlg.initOwner(owner);
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Reagendar cita #" + cita.getId());

        Label lbl = new Label("Cita de paciente " + cita.getPacienteId()
                + " con " + cita.getNombreMedico());
        lbl.setStyle("-fx-text-fill: " + AZUL_OSCURO + "; -fx-font-weight: bold;");

        DatePicker dpNueva = new DatePicker();
        dpNueva.setPromptText("Nueva fecha");
        // Solo fechas desde hoy
        dpNueva.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        TextField txtHora = new TextField();
        txtHora.setPromptText("Nueva hora (HH:mm) — ej: 14:00");

        Button btnConfirmar = boton("✔  Confirmar reagendamiento", NARANJA);
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setOnAction(e -> {
            if (dpNueva.getValue() == null || txtHora.getText().isBlank()) {
                alerta("Campos requeridos", "Ingresa fecha y hora.");
                return;
            }
            String nuevaFechaHora = dpNueva.getValue().toString() + "T" + txtHora.getText().trim();
            try {
                String json = "{\"fechaHora\": \"" + nuevaFechaHora + "\"}";
                HttpResponse<String> resp = patch("/api/citas/" + cita.getId() + "/reagendar", json);
                if (resp.statusCode() == 200) {
                    Agenda actualizada = mapper.readValue(resp.body(), Agenda.class);
                    int idx = citasData.indexOf(cita);
                    if (idx >= 0) citasData.set(idx, actualizada);
                    status("Cita #" + cita.getId() + " reagendada → " + nuevaFechaHora);
                    dlg.close();
                } else {
                    Map<?, ?> err = mapper.readValue(resp.body(), Map.class);
                    alerta("Error", err.getOrDefault("error", resp.body()).toString());
                }
            } catch (Exception ex) {
                alerta("Error", ex.getMessage());
            }
        });

        VBox contenido = new VBox(12, lbl,
                separadorLabel("Nueva fecha *"), dpNueva,
                separadorLabel("Nueva hora *"),  txtHora,
                btnConfirmar);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + FONDO + ";");
        contenido.setPrefWidth(360);

        dlg.setScene(new Scene(contenido));
        dlg.showAndWait();
    }

    // ══════════════════════════════════════════════════════
    //  LLAMADAS AL BACKEND
    // ══════════════════════════════════════════════════════
    private void cargarCitas() {
        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/citas");
                List<Agenda> lista = mapper.readValue(resp.body(), new TypeReference<>() {});
                Platform.runLater(() -> { citasData.setAll(lista); status("Citas cargadas: " + lista.size()); });
            } catch (Exception e) {
                Platform.runLater(() -> status("Error al cargar citas: " + e.getMessage()));
            }
        }).start();
    }

    private void cargarMedicos() {
        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/medicos");
                List<Agenda.Medico> lista = mapper.readValue(resp.body(), new TypeReference<>() {});
                Platform.runLater(() -> medicosData.setAll(lista));
            } catch (Exception ignored) {}
        }).start();
    }

    private void buscarPorPaciente(String idTxt) {
        if (idTxt.isEmpty()) return;
        try {
            long id = Long.parseLong(idTxt);
            new Thread(() -> {
                try {
                    HttpResponse<String> resp = get("/api/citas/paciente/" + id);
                    List<Agenda> lista = mapper.readValue(resp.body(), new TypeReference<>() {});
                    Platform.runLater(() -> { citasData.setAll(lista); status("Citas del paciente " + id + ": " + lista.size()); });
                } catch (Exception e) {
                    Platform.runLater(() -> status("Error: " + e.getMessage()));
                }
            }).start();
        } catch (NumberFormatException e) {
            alerta("Error", "El ID debe ser un número.");
        }
    }

    private void buscarPorMedico(Long medicoId) {
        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/citas/medico/" + medicoId);
                List<Agenda> lista = mapper.readValue(resp.body(), new TypeReference<>() {});
                Platform.runLater(() -> { citasData.setAll(lista); status("Citas del médico: " + lista.size()); });
            } catch (Exception e) {
                Platform.runLater(() -> status("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void cancelarCita(Agenda cita) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cancelar la cita #" + cita.getId() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        HttpResponse<String> resp = patch("/api/citas/" + cita.getId() + "/cancelar", "{}");
                        if (resp.statusCode() == 200) {
                            Agenda actualizada = mapper.readValue(resp.body(), Agenda.class);
                            int idx = citasData.indexOf(cita);
                            Platform.runLater(() -> {
                                if (idx >= 0) citasData.set(idx, actualizada);
                                status("Cita #" + cita.getId() + " cancelada.");
                            });
                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> alerta("Error", e.getMessage()));
                    }
                }).start();
            }
        });
    }

    // ══════════════════════════════════════════════════════
    //  HTTP HELPERS
    // ══════════════════════════════════════════════════════
    private HttpResponse<String> get(String path) throws Exception {
        return http.send(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> patch(String path, String body) throws Exception {
        return http.send(
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + path))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    // ══════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════
    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        return b;
    }

    private Label separadorLabel(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: " + AZUL_OSCURO + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        return l;
    }

    private <T> TableColumn<T, ?> col(String titulo, String prop, double ancho) {
        TableColumn<T, Object> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(ancho);
        return c;
    }

    private void alerta(String titulo, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
            a.showAndWait();
        });
    }

    private void status(String msg) {
        Platform.runLater(() -> lblStatus.setText(msg));
    }

    public static void main(String[] args) { launch(); }
}