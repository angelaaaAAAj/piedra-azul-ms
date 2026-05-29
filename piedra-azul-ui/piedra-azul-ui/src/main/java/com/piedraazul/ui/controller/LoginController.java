package com.piedraazul.ui.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piedraazul.ui.agenda.AgendaApp;
import com.piedraazul.ui.auditoria.AuditoriaApp;
import com.piedraazul.ui.historial.HistorialApp;
import com.piedraazul.ui.medico.MedicoApp;
import com.piedraazul.ui.pacientes.PacienteApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnLogin;

    private final HttpClient   http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private void onLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("⚠  Usuario y contraseña son obligatorios.");
            return;
        }

        lblError.setText("");
        btnLogin.setDisable(true);
        btnLogin.setText("Verificando...");

        new Thread(() -> {
            try {
                String json = """
                    {"username": "%s", "password": "%s"}
                    """.formatted(usuario, password);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> resp = http.send(req,
                        HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("Iniciar sesión");

                    if (resp.statusCode() == 200) {
                        try {
                            Map<?, ?> body = mapper.readValue(resp.body(), Map.class);
                            String rol = body.getOrDefault("rol", "").toString();
                            String nombre = body.getOrDefault("nombre", usuario).toString();
                            // Guardar medicoId si viene en la respuesta
                            Object medicoIdObj = body.get("medicoId");
                            Long medicoId = medicoIdObj != null
                                    ? Long.parseLong(medicoIdObj.toString()) : null;

                            abrirVistaPorRol(rol, nombre, medicoId);
                        } catch (Exception e) {
                            lblError.setText("Error al procesar respuesta del servidor.");
                        }
                    } else {
                        try {
                            Map<?, ?> err = mapper.readValue(resp.body(), Map.class);
                            lblError.setText("⚠  " + err.getOrDefault("error",
                                    "Credenciales incorrectas."));
                        } catch (Exception e) {
                            lblError.setText("⚠  Credenciales incorrectas.");
                        }
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("Iniciar sesión");
                    lblError.setText("⚠  No se pudo conectar con el servidor.");
                });
            }
        }).start();
    }

    private void abrirVistaPorRol(String rol, String nombre, Long medicoId) {
        // Cierra la ventana del login
        Stage loginStage = (Stage) btnLogin.getScene().getWindow();
        loginStage.close();

        try {
            switch (rol.toUpperCase()) {

                case "MEDICO_TERAPISTA" -> {
                    MedicoApp app = new MedicoApp(medicoId, nombre);
                    Stage s = new Stage();
                    app.start(s);
                }

                case "ADMINISTRADOR" -> {
                    // El administrador ve todas las pestañas en un TabPane
                    abrirPanelAdmin(nombre);
                }

                case "AGENDADOR" -> {
                    Stage s = new Stage();
                    new AgendaApp().start(s);
                }

                case "PACIENTE" -> {
                    // Paciente solo ve su propia agenda e historial
                    Stage s = new Stage();
                    new AgendaApp().start(s);
                }

                default -> {
                    lblError.setText("Rol no reconocido: " + rol);
                    loginStage.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirPanelAdmin(String nombre) throws Exception {
        // Administrador: abre todas las apps en ventanas separadas
        new AgendaApp().start(new Stage());
        new PacienteApp().start(new Stage());
        new HistorialApp().start(new Stage());
        new AuditoriaApp().start(new Stage());
    }
}