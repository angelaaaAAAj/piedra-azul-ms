package com.piedraazul.ui.auditoria;

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
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AuditoriaApp extends Application {

    // ── Paleta idéntica al login ──
    private static final String AZUL_OSCURO = "#1E3A5F";
    private static final String AZUL_MEDIO  = "#1E88E5";
    private static final String FONDO       = "#F4F6F9";
    private static final String VERDE       = "#2E7D32";
    private static final String ROJO        = "#C62828";

    private static final String API_URL = "http://localhost:8080/api/auditoria";

    private final HttpClient     http    = HttpClient.newHttpClient();
    private final ObjectMapper   mapper  = new ObjectMapper();

    // Lista maestra (todos los registros cargados del servidor)
    private final ObservableList<Auditoria> todosDatos   = FXCollections.observableArrayList();
    // Lista que ve la tabla (puede ser filtrada localmente)
    private final ObservableList<Auditoria> datosTabla   = FXCollections.observableArrayList();

    private TableView<Auditoria> tabla;
    private Label lblStatus;

    @Override
    public void start(Stage stage) {

        // ══ HEADER ══
        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + AZUL_OSCURO + "; -fx-padding: 14 20 14 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label("🔍  Auditoría del Sistema — Piedra Azul");
        lblTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(lblTitulo);

        // ══ PANEL DE FILTROS ══
        HBox panelFiltros = new HBox(10);
        panelFiltros.setPadding(new Insets(12, 20, 12, 20));
        panelFiltros.setAlignment(Pos.CENTER_LEFT);
        panelFiltros.setStyle("-fx-background-color: white; "
                + "-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // -- Búsqueda por número de identificación (entidadId) --
        Label lblId = etiqueta("N° identificación:");
        TextField txtIdentificacion = new TextField();
        txtIdentificacion.setPromptText("Ej: 1001234567");
        txtIdentificacion.setPrefWidth(140);
        Button btnBuscarId = boton("Buscar", AZUL_MEDIO);

        Separator sep1 = separadorV();

        // -- Filtro por tipo de evento --
        Label lblTipo = etiqueta("Tipo de evento:");
        ComboBox<String> cbTipoEvento = new ComboBox<>();
        cbTipoEvento.getItems().addAll(
                "PACIENTE_REGISTRADO", "CITA_AGENDADA", "CITA_CANCELADA",
                "CITA_REAGENDADA", "LOGIN_EXITOSO", "LOGIN_FALLIDO",
                "USUARIO_CREADO", "USUARIO_DESACTIVADO",
                "HISTORIAL_CREADO", "HISTORIAL_MODIFICADO"
        );
        cbTipoEvento.setPromptText("Seleccionar...");
        cbTipoEvento.setPrefWidth(200);
        Button btnFiltrarTipo = boton("Filtrar", AZUL_MEDIO);

        Separator sep2 = separadorV();

        // -- Filtro por microservicio --
        Label lblMs = etiqueta("Microservicio:");
        TextField txtMicroservicio = new TextField();
        txtMicroservicio.setPromptText("ej: ms-agenda");
        txtMicroservicio.setPrefWidth(130);
        Button btnFiltrarMs = boton("Filtrar", AZUL_MEDIO);

        Separator sep3 = separadorV();

        // -- Recargar todos --
        Button btnTodos = boton("↺  Todos", AZUL_OSCURO);

        panelFiltros.getChildren().addAll(
                lblId, txtIdentificacion, btnBuscarId,
                sep1,
                lblTipo, cbTipoEvento, btnFiltrarTipo,
                sep2,
                lblMs, txtMicroservicio, btnFiltrarMs,
                sep3,
                btnTodos
        );

        // ══ TABLA ══
        tabla = new TableView<>(datosTabla);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setStyle("-fx-font-size: 13px;");
        tabla.setPlaceholder(new Label("No hay registros de auditoría."));

        TableColumn<Auditoria, Long>   colId        = col("ID",            "id",                  55);
        TableColumn<Auditoria, String> colTipo      = col("Tipo evento",   "tipoEvento",          170);
        TableColumn<Auditoria, String> colDesc      = col("Descripción",   "descripcion",         260);
        TableColumn<Auditoria, Long>   colEntidad   = col("Entidad ID",    "entidadId",            90);
        TableColumn<Auditoria, String> colUsuario   = col("Realizado por", "realizadoPor",        140);
        TableColumn<Auditoria, String> colMs        = col("Microservicio", "microservicioOrigen", 130);
        TableColumn<Auditoria, String> colFecha     = col("Fecha",         "fechaEvento",         160);

        tabla.getColumns().addAll(colId, colTipo, colDesc, colEntidad, colUsuario, colMs, colFecha);

        // ══ STATUS BAR ══
        lblStatus = new Label("Listo.");
        lblStatus.setStyle("-fx-text-fill: #37474f; -fx-font-size: 12px;");
        HBox statusBar = new HBox(lblStatus);
        statusBar.setStyle("-fx-background-color: #e8eaf6; -fx-padding: 5 16 5 16;");

        // ══ ACCIONES ══

        // Buscar por identificación → filtro local sobre entidadId
        btnBuscarId.setOnAction(e -> {
            String txt = txtIdentificacion.getText().trim();
            if (txt.isEmpty()) {
                status("⚠  Ingresa un número de identificación.", ROJO);
                return;
            }
            try {
                long idBuscado = Long.parseLong(txt);
                List<Auditoria> filtrados = todosDatos.stream()
                        .filter(a -> a.getEntidadId() != null
                                && a.getEntidadId().equals(idBuscado))
                        .toList();
                datosTabla.setAll(filtrados);
                status("✔  Registros para identificación " + idBuscado
                        + ": " + filtrados.size(), VERDE);
            } catch (NumberFormatException ex) {
                status("⚠  El número de identificación debe ser numérico.", ROJO);
            }
        });

        // Filtrar por tipo → llamada al backend
        btnFiltrarTipo.setOnAction(e -> {
            if (cbTipoEvento.getValue() == null) {
                status("⚠  Selecciona un tipo de evento.", ROJO);
                return;
            }
            consumirLista(API_URL + "/tipo/" + cbTipoEvento.getValue(), true);
        });

        // Filtrar por microservicio → llamada al backend
        btnFiltrarMs.setOnAction(e -> {
            String ms = txtMicroservicio.getText().trim();
            if (ms.isEmpty()) {
                status("⚠  Ingresa el nombre del microservicio.", ROJO);
                return;
            }
            consumirLista(API_URL + "/microservicio/" + ms, true);
        });

        // Recargar todos
        btnTodos.setOnAction(e -> {
            txtIdentificacion.clear();
            cbTipoEvento.setValue(null);
            txtMicroservicio.clear();
            consumirLista(API_URL, false);
        });

        // ══ LAYOUT RAÍZ ══
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FONDO + ";");
        root.setTop(new VBox(header, panelFiltros));
        root.setCenter(tabla);
        root.setBottom(statusBar);

        stage.setTitle("Auditoría — Piedra Azul");
        stage.setScene(new Scene(root, 1080, 600));
        stage.show();

        consumirLista(API_URL, false);
    }

    // ══════════════════════════════════════════════════════
    //  HTTP
    // ══════════════════════════════════════════════════════
    private void consumirLista(String url, boolean soloTabla) {
        status("Cargando...", AZUL_MEDIO);
        new Thread(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url)).GET().build();
                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) {
                    Platform.runLater(() -> status("Error del servidor: " + resp.body(), ROJO));
                    return;
                }

                List<Auditoria> lista = mapper.readValue(
                        resp.body(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    if (!soloTabla) todosDatos.setAll(lista); // actualiza lista maestra
                    datosTabla.setAll(lista);
                    status("✔  " + lista.size() + " registros cargados.", VERDE);
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        status("⚠  No se pudo conectar con ms-auditoria: " + e.getMessage(), ROJO));
            }
        }).start();
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

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: " + AZUL_OSCURO + "; -fx-font-weight: bold;");
        return l;
    }

    private Separator separadorV() {
        Separator s = new Separator();
        s.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return s;
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Auditoria, T> col(String titulo, String prop, double ancho) {
        TableColumn<Auditoria, T> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(ancho);
        return c;
    }

    private void status(String msg, String color) {
        lblStatus.setText(msg);
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
    }

    public static void main(String[] args) {
        launch();
    }
}