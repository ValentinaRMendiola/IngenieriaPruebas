package proyecto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/proyecto/calculadora.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Calculadora");

        // Fijar tamaño
    primaryStage.setResizable(false);   // Bloquea el redimensionamiento
    primaryStage.setWidth(375);         // opcional, ancho exacto
    primaryStage.setHeight(515);        // opcional, alto exacto

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
