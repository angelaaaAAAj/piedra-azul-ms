package com.piedraazul.mshistorial.ui;

import com.piedraazul.mshistorial.MsHistorialApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class HistorialApp extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        // Nota: para pruebas locales de la interfaz existe un perfil "ui"
        // con H2 en memoria en src/main/resources/application-ui.properties.
        // Aquí se arranca sin perfil específico, por lo tanto se usa
        // la configuración por defecto de application.properties (PostgreSQL).
        springContext = new SpringApplicationBuilder(MsHistorialApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/HistorialView.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Historial Médico");
        stage.setScene(scene);
        stage.setWidth(1100);
        stage.setHeight(720);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (springContext != null) {
            springContext.close();
        }
        super.stop();
    }
}
