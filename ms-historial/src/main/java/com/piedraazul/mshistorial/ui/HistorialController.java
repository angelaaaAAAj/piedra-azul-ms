package com.piedraazul.mshistorial.ui;

import com.piedraazul.mshistorial.dto.HistorialDTO;
import com.piedraazul.mshistorial.model.HistorialClinico;
import com.piedraazul.mshistorial.model.TipoRegistro;
import com.piedraazul.mshistorial.service.HistorialService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("historialUIController")
public class HistorialController {

    private final HistorialService historialService;

    @FXML
    private TableView<HistorialEntry> tablaRegistros;

    @FXML
    private TableColumn<HistorialEntry, Number> colId;

    @FXML
    private TableColumn<HistorialEntry, Number> colPacienteId;

    @FXML
    private TableColumn<HistorialEntry, Number> colMedicoId;

    @FXML
    private TableColumn<HistorialEntry, Number> colCitaId;

    @FXML
    private TableColumn<HistorialEntry, String> colTipoRegistro;

    @FXML
    private TableColumn<HistorialEntry, String> colDescripcion;

    @FXML
    private TableColumn<HistorialEntry, String> colFechaRegistro;

    @FXML
    private TableColumn<HistorialEntry, String> colRegistradoPor;

    @FXML
    private TextField txtBusqueda;

    @FXML
    private TextField txtPacienteId;

    @FXML
    private TextField txtCitaId;

    @FXML
    private TextField txtPacienteIdNew;

    @FXML
    private TextField txtMedicoId;

    @FXML
    private TextField txtCitaIdNew;

    @FXML
    private ComboBox<TipoRegistro> cmbTipoRegistro;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private TextArea txtDiagnostico;

    @FXML
    private TextArea txtTratamiento;

    @FXML
    private TextField txtRegistradoPor;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblFeedback;

    private final ObservableList<HistorialEntry> registros = FXCollections.observableArrayList();

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPacienteId.setCellValueFactory(new PropertyValueFactory<>("pacienteId"));
        colMedicoId.setCellValueFactory(new PropertyValueFactory<>("medicoId"));
        colCitaId.setCellValueFactory(new PropertyValueFactory<>("citaId"));
        colTipoRegistro.setCellValueFactory(new PropertyValueFactory<>("tipoRegistro"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colFechaRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
        colRegistradoPor.setCellValueFactory(new PropertyValueFactory<>("registradoPor"));

        cmbTipoRegistro.setItems(FXCollections.observableArrayList(TipoRegistro.values()));
        cmbTipoRegistro.getSelectionModel().selectFirst();

        tablaRegistros.setItems(registros);
        updateStatus();
    }

    @FXML
    private void cargarPorPaciente() {
        try {
            Long pacienteId = Long.parseLong(txtPacienteId.getText().trim());
            List<HistorialClinico> resultados = historialService.listarPorPaciente(pacienteId);
            showHistoriales(resultados);
            feedback("Cargados " + resultados.size() + " registros para paciente " + pacienteId, false);
        } catch (NumberFormatException e) {
            feedback("Ingrese un pacienteId válido.", true);
        } catch (Exception e) {
            feedback("Error al cargar registros: " + e.getMessage(), true);
        }
    }

    @FXML
    private void cargarPorCita() {
        try {
            Long citaId = Long.parseLong(txtCitaId.getText().trim());
            List<HistorialClinico> resultados = historialService.listarPorCita(citaId);
            showHistoriales(resultados);
            feedback("Cargados " + resultados.size() + " registros para cita " + citaId, false);
        } catch (NumberFormatException e) {
            feedback("Ingrese un citaId válido.", true);
        } catch (Exception e) {
            feedback("Error al cargar registros: " + e.getMessage(), true);
        }
    }

    @FXML
    private void crearRegistro() {
        try {
            HistorialDTO dto = new HistorialDTO();
            dto.setPacienteId(Long.parseLong(txtPacienteIdNew.getText().trim()));
            dto.setMedicoId(Long.parseLong(txtMedicoId.getText().trim()));
            dto.setCitaId(Long.parseLong(txtCitaIdNew.getText().trim()));
            dto.setTipoRegistro(cmbTipoRegistro.getSelectionModel().getSelectedItem());
            dto.setDescripcion(txtDescripcion.getText().trim());
            dto.setDiagnostico(txtDiagnostico.getText().trim());
            dto.setTratamiento(txtTratamiento.getText().trim());
            dto.setRegistradoPor(txtRegistradoPor.getText().trim());

            historialService.registrar(dto);
            feedback("Registro creado correctamente.", false);
            limpiarFormulario();
        } catch (NumberFormatException e) {
            feedback("Los campos de ID deben ser numéricos.", true);
        } catch (Exception e) {
            feedback("No se pudo crear el registro: " + e.getMessage(), true);
        }
    }

    @FXML
    private void buscarRegistros() {
        String criterio = txtBusqueda.getText().trim().toLowerCase();
        if (criterio.isEmpty()) {
            tablaRegistros.setItems(registros);
        } else {
            tablaRegistros.setItems(registros.filtered(item -> item.matches(criterio)));
        }
        updateStatus();
    }

    @FXML
    private void limpiarBusqueda() {
        txtBusqueda.clear();
        tablaRegistros.setItems(registros);
        updateStatus();
    }

    private void showHistoriales(List<HistorialClinico> historiales) {
        registros.setAll(historiales.stream().map(this::toEntry).toList());
        tablaRegistros.setItems(registros);
        updateStatus();
    }

    private HistorialEntry toEntry(HistorialClinico historial) {
        return new HistorialEntry(
                historial.getId(),
                historial.getPacienteId(),
                historial.getMedicoId(),
                historial.getCitaId(),
                historial.getTipoRegistro().name(),
                historial.getDescripcion(),
                historial.getFechaRegistro(),
                historial.getRegistradoPor());
    }

    private void updateStatus() {
        lblTotal.setText("Registros mostrados: " + tablaRegistros.getItems().size());
    }

    private void feedback(String message, boolean error) {
        lblFeedback.setText(message);
        lblFeedback.getStyleClass().removeAll("feedback-error", "feedback-ok");
        lblFeedback.getStyleClass().add(error ? "feedback-error" : "feedback-ok");
    }

    private void limpiarFormulario() {
        txtPacienteIdNew.clear();
        txtMedicoId.clear();
        txtCitaIdNew.clear();
        cmbTipoRegistro.getSelectionModel().selectFirst();
        txtDescripcion.clear();
        txtDiagnostico.clear();
        txtTratamiento.clear();
        txtRegistradoPor.clear();
    }
}
