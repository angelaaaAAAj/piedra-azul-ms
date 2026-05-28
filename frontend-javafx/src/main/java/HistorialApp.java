import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Arrays;

public class HistorialApp extends Application {

    private final TableView<Historial> tabla = new TableView<>();

    private final ObservableList<Historial> historiales =
            FXCollections.observableArrayList();

    private final TextField txtPaciente = new TextField();

    private final TextField txtDiagnostico = new TextField();

    private final TextField txtTratamiento = new TextField();

    private final DatePicker dpFecha = new DatePicker();

    @Override
    public void start(Stage stage) {

        Label titulo =
                new Label("Historial Clínico - Piedra Azul");

        titulo.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1E3A5F;
                """);

        TableColumn<Historial, Long> colId =
                new TableColumn<>("ID");

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        TableColumn<Historial, String> colPaciente =
                new TableColumn<>("Paciente");

        colPaciente.setCellValueFactory(
                new PropertyValueFactory<>("paciente")
        );

        TableColumn<Historial, String> colDiagnostico =
                new TableColumn<>("Diagnóstico");

        colDiagnostico.setCellValueFactory(
                new PropertyValueFactory<>("diagnostico")
        );

        TableColumn<Historial, String> colTratamiento =
                new TableColumn<>("Tratamiento");

        colTratamiento.setCellValueFactory(
                new PropertyValueFactory<>("tratamiento")
        );

        TableColumn<Historial, String> colFecha =
                new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha")
        );

        tabla.getColumns().addAll(Arrays.asList(
                colId,
                colPaciente,
                colDiagnostico,
                colTratamiento,
                colFecha
        ));

        tabla.setItems(historiales);

        txtPaciente.setPromptText("Paciente");

        txtDiagnostico.setPromptText("Diagnóstico");

        txtTratamiento.setPromptText("Tratamiento");

        dpFecha.setPromptText("Fecha");

        Button btnAgregar =
                new Button("Agregar historial");

        btnAgregar.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);

        btnAgregar.setOnAction(e -> agregarHistorial());

        GridPane formulario = new GridPane();

        formulario.setHgap(10);
        formulario.setVgap(10);

        formulario.add(txtPaciente, 0, 0);

        formulario.add(txtDiagnostico, 1, 0);

        formulario.add(txtTratamiento, 0, 1);

        formulario.add(dpFecha, 1, 1);

        formulario.add(btnAgregar, 1, 2);

        Button btnCargar =
                new Button("Cargar historial");
        btnCargar.setOnAction(e -> tabla.refresh());

        btnCargar.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);

        VBox root = new VBox(
                15,
                titulo,
                formulario,
                btnCargar,
                tabla
        );

        root.setStyle("""
                -fx-padding: 20;
                -fx-background-color: #F4F6F9;
                """);

        Scene scene = new Scene(root, 1000, 550);

        stage.setTitle("Historial Clínico");

        stage.setScene(scene);

        stage.show();
    }

    private void agregarHistorial() {

        Historial historial = new Historial();

        historial.setId(
                (long) (historiales.size() + 1)
        );

        historial.setPaciente(
                txtPaciente.getText()
        );

        historial.setDiagnostico(
                txtDiagnostico.getText()
        );

        historial.setTratamiento(
                txtTratamiento.getText()
        );

        historial.setFecha(
                dpFecha.getValue() != null
                        ? dpFecha.getValue().toString()
                        : ""
        );

        historiales.add(historial);

        limpiarFormulario();
    }

    private void limpiarFormulario() {

        txtPaciente.clear();

        txtDiagnostico.clear();

        txtTratamiento.clear();

        dpFecha.setValue(null);
    }

    public static void main(String[] args) {
        launch();
    }
}