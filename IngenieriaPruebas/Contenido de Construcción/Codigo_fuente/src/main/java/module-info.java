module proyecto.calculadora {
    requires javafx.controls;
    requires javafx.fxml;

    exports proyecto;
    opens proyecto to javafx.fxml;
}
