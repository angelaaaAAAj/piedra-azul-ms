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

public class AgendaApp extends Application {

    private final TableView<Agenda> tabla = new TableView<>();

    private final ObservableList<Agenda> citas =
            FXCollections.observableArrayList();

    private final TextField txtPaciente = new TextField();

    private final DatePicker dpFecha = new DatePicker();

    private final TextField txtHora = new TextField();

    private final ComboBox<String> cbEstado = new ComboBox<>();

    @Override
    public void start(Stage stage) {

        Label titulo = new Label("Gestión de Agenda - Piedra Azul");

        titulo.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1E3A5F;
                """);

        TableColumn<Agenda, Long> colId =
                new TableColumn<>("ID");

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        TableColumn<Agenda, String> colPaciente =
                new TableColumn<>("Paciente");

        colPaciente.setCellValueFactory(
                new PropertyValueFactory<>("paciente")
        );

        TableColumn<Agenda, String> colFecha =
                new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha")
        );

        TableColumn<Agenda, String> colHora =
                new TableColumn<>("Hora");

        colHora.setCellValueFactory(
                new PropertyValueFactory<>("hora")
        );

        TableColumn<Agenda, String> colEstado =
                new TableColumn<>("Estado");

        colEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        tabla.getColumns().addAll(Arrays.asList(
                colId,
                colPaciente,
                colFecha,
                colHora,
                colEstado
        ));

        tabla.setItems(citas);

        txtPaciente.setPromptText("Paciente");

        dpFecha.setPromptText("Fecha");

        txtHora.setPromptText("Hora");

        cbEstado.getItems().addAll(
                "PROGRAMADA",
                "CONFIRMADA",
                "CANCELADA",
                "FINALIZADA"
        );

        cbEstado.setPromptText("Estado");

        Button btnAgregar = new Button("Agregar cita");

        btnAgregar.setStyle("""
                -fx-background-color: #1E88E5;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                """);

        btnAgregar.setOnAction(e -> agregarCita());

        GridPane formulario = new GridPane();

        formulario.setHgap(10);
        formulario.setVgap(10);

        formulario.add(txtPaciente, 0, 0);

        formulario.add(dpFecha, 1, 0);

        formulario.add(txtHora, 0, 1);

        formulario.add(cbEstado, 1, 1);

        formulario.add(btnAgregar, 1, 2);

        Button btnCargar = new Button("Cargar citas");

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

        Scene scene = new Scene(root, 900, 500);

        stage.setTitle("Agenda");

        stage.setScene(scene);

        stage.show();
    }

    private void agregarCita() {

        Agenda agenda = new Agenda();

        agenda.setId((long) (citas.size() + 1));

        agenda.setPaciente(txtPaciente.getText());

        agenda.setFecha(
                dpFecha.getValue() != null
                        ? dpFecha.getValue().toString()
                        : ""
        );

        agenda.setHora(txtHora.getText());

        agenda.setEstado(cbEstado.getValue());

        citas.add(agenda);

        limpiarFormulario();
    }

    private void limpiarFormulario() {

        txtPaciente.clear();

        dpFecha.setValue(null);

        txtHora.clear();

        cbEstado.setValue(null);
    }

    public static void main(String[] args) {
        launch();
    }
}