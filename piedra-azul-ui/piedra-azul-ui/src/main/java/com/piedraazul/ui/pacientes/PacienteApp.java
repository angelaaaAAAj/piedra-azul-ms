package com.piedraazul.ui.pacientes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PacienteApp extends Application {

    private final TableView<Paciente> tabla = new TableView<>();
    private final ObservableList<Paciente> pacientes = FXCollections.observableArrayList();
    private final TextField txtNombre = new TextField();
    private final TextField txtApellido = new TextField();
    private final TextField txtDocumento = new TextField();
    private final TextField txtTelefono = new TextField();
    private final ComboBox<String> cbGenero = new ComboBox<>();
    private final DatePicker dpFechaNacimiento = new DatePicker();
    private final TextField txtEmail = new TextField();
    private final TextField txtDireccion = new TextField();
    private final TextField txtEps = new TextField();
    private final TextField txtEstadoId = new TextField();
    private final ComboBox<String> cbNuevoEstado = new ComboBox<>();
    private Long pacienteEditandoId = null;
    @Override
    public void start(Stage stage) {
        TableColumn<Paciente, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Paciente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));

        TableColumn<Paciente, String> colDocumento = new TableColumn<>("Documento");
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));

        TableColumn<Paciente, String> colTelefono = new TableColumn<>("Celular");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<Paciente, String> colGenero = new TableColumn<>("Género");
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));

        TableColumn<Paciente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Paciente, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tabla.getColumns().addAll(
                colId,
                colNombre,
                colDocumento,
                colTelefono,
                colGenero,
                colEmail,
                colEstado
        );

        tabla.setItems(pacientes);
        tabla.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) {
                cargarPacienteParaEditar(tabla.getSelectionModel().getSelectedItem());
            }
        });

        Button btnCargar = new Button("Cargar pacientes");
        btnCargar.setOnAction(e -> cargarPacientes());
        btnCargar.setStyle("""
        -fx-background-color: #1E88E5;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);
        txtBuscarDocumento.setPromptText("Buscar por documento");

        Button btnBuscar = new Button("Buscar");
        btnBuscar.setOnAction(e -> buscarPorDocumento());
        btnBuscar.setStyle("""
        -fx-background-color: #1E88E5;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);

        Button btnMostrarTodos = new Button("Mostrar todos");
        btnMostrarTodos.setOnAction(e -> cargarPacientes());
        btnMostrarTodos.setStyle("""
        -fx-background-color: #1E88E5;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);

        GridPane buscador = new GridPane();
        buscador.setHgap(10);
        buscador.add(txtBuscarDocumento, 0, 0);
        buscador.add(btnBuscar, 1, 0);
        buscador.add(btnMostrarTodos, 2, 0);
        cbGenero.getItems().addAll("HOMBRE", "MUJER", "OTRO");

        txtEstadoId.setPromptText("ID del paciente");

        cbNuevoEstado.getItems().addAll("ACTIVO", "INACTIVO");
        cbNuevoEstado.setPromptText("Nuevo estado");

        Button btnCambiarEstado = new Button("Cambiar estado");
        btnCambiarEstado.setOnAction(e -> cambiarEstado());
        btnCambiarEstado.setStyle("""
        -fx-background-color: #1E88E5;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);
        GridPane panelEstado = new GridPane();
        panelEstado.setHgap(10);
        panelEstado.add(txtEstadoId, 0, 0);
        panelEstado.add(cbNuevoEstado, 1, 0);
        panelEstado.add(btnCambiarEstado, 2, 0);

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);

        formulario.add(txtNombre, 0, 0);
        txtNombre.setPromptText("Nombre");

        formulario.add(txtApellido, 1, 0);
        txtApellido.setPromptText("Apellido");

        formulario.add(txtDocumento, 0, 1);
        txtDocumento.setPromptText("Documento");

        formulario.add(txtTelefono, 1, 1);
        txtTelefono.setPromptText("Celular");

        formulario.add(cbGenero, 0, 2);
        cbGenero.setPromptText("Género");

        formulario.add(dpFechaNacimiento, 1, 2);
        dpFechaNacimiento.setPromptText("Fecha nacimiento");

        formulario.add(txtEmail, 0, 3);
        txtEmail.setPromptText("Email");

        Button btnGuardar = new Button("Registrar paciente");
        btnGuardar.setOnAction(e -> registrarPaciente());
        btnGuardar.setStyle("""
        -fx-background-color: #1E88E5;
        -fx-text-fill: white;
        -fx-font-weight: bold;
    """);

        formulario.add(btnGuardar, 1, 3);

        Label titulo = new Label("Gestión de Pacientes - Piedra Azul");
        titulo.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #1E3A5F;
        """);

        VBox root = new VBox(
                15,
                titulo,
                formulario,
                buscador,
                panelEstado,
                btnCargar,
                tabla
        );
        root.setStyle("""
        -fx-padding: 20;
        -fx-background-color: #F4F6F9;
    """);

        Scene scene = new Scene(root, 900, 500);

        stage.setTitle("Gestión de Pacientes");
        stage.setScene(scene);
        stage.show();

        cargarPacientes();
    }

    private void cargarPacientes() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pacientes"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ObjectMapper mapper = new ObjectMapper();
            List<Paciente> lista = mapper.readValue(
                    response.body(),
                    new TypeReference<List<Paciente>>() {}
            );

            pacientes.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registrarPaciente() {
        try {

            String json = """
            {
            "nombre": "%s",
            "apellido": "%s",
            "numeroDocumento": "%s",
            "telefono": "%s",
            "genero": "%s",
            "fechaNacimiento": "%s",
            "email": "%s"
        }
        """.formatted(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtDocumento.getText().trim(),
                    txtTelefono.getText().trim(),
                    cbGenero.getValue() != null ? cbGenero.getValue() : "",
                    dpFechaNacimiento.getValue() != null
                            ? dpFechaNacimiento.getValue().toString()
                            : "",
                    txtEmail.getText().trim()
            );

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json");

            if (pacienteEditandoId == null) {
                requestBuilder
                        .uri(URI.create("http://localhost:8080/api/pacientes/registro"))
                        .POST(HttpRequest.BodyPublishers.ofString(json));
            } else {
                requestBuilder
                        .uri(URI.create("http://localhost:8080/api/pacientes/" + pacienteEditandoId))
                        .PUT(HttpRequest.BodyPublishers.ofString(json));
            }

            HttpResponse<String> response = client.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                mostrarAlerta(
                        "Éxito",
                        pacienteEditandoId == null
                                ? "Paciente registrado correctamente"
                                : "Paciente actualizado correctamente"
                );

                pacienteEditandoId = null;
                limpiarFormulario();
                cargarPacientes();
            } else {
                mostrarAlerta("Error", response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar el paciente");
        }
    }
    private void buscarPorDocumento() {
        try {
            String documento = txtBuscarDocumento.getText().trim();

            if (documento.isBlank()) {
                mostrarAlerta("Error", "Ingrese un número de documento");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pacientes/documento/" + documento))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Paciente paciente = mapper.readValue(response.body(), Paciente.class);
                pacientes.setAll(paciente);
            } else {
                pacientes.clear();
                mostrarAlerta("No encontrado", "No existe un paciente con ese documento");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo buscar el paciente");
        }
    }

    private void cambiarEstado() {
        try {
            String id = txtEstadoId.getText();
            String estado = cbNuevoEstado.getValue();

            if (id.isBlank() || estado == null) {
                mostrarAlerta("Error", "Ingrese el ID y seleccione un estado");
                return;
            }

            String json = """
                {
                  "estado": "%s"
                }
                """.formatted(estado);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pacientes/" + id + "/estado"))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                mostrarAlerta("Éxito", "Estado actualizado correctamente");
                txtEstadoId.clear();
                cbNuevoEstado.setValue(null);
                cargarPacientes();
            } else {
                mostrarAlerta("Error", response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cambiar el estado");
        }
    }
    private void cargarPacienteParaEditar(Paciente paciente) {
        pacienteEditandoId = paciente.getId();

        txtNombre.setText(paciente.getNombre());
        txtApellido.setText(paciente.getApellido());
        txtDocumento.setText(paciente.getNumeroDocumento());
        txtTelefono.setText(paciente.getTelefono());
        cbGenero.setValue(paciente.getGenero());
        txtEmail.setText(paciente.getEmail());

        mostrarAlerta("Edición", "Paciente cargado para editar");
    }
    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellido.clear();
        txtDocumento.clear();
        txtTelefono.clear();
        cbGenero.setValue(null);
        dpFechaNacimiento.setValue(null);
        txtEmail.clear();
        txtDireccion.clear();
        txtEps.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    private final TextField txtBuscarDocumento = new TextField();

    public static void main(String[] args) {
        launch();
    }
}