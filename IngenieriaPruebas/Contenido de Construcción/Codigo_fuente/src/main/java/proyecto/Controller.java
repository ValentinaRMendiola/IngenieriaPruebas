package proyecto;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Controller {

    @FXML
    private TextField inputA;

    @FXML
    private TextField inputB;

    @FXML
    private Label lblResultado;

    public int sumar(int a, int b) {
        return a + b;
    }

    public int restar(int a, int b) {
        return a - b;
    }

    @FXML
    private void sumar() {
        try {
            int a = Integer.parseInt(inputA.getText());
            int b = Integer.parseInt(inputB.getText());
            lblResultado.setText("Resultado: " + sumar(a, b));
        } catch (NumberFormatException e) {
            lblResultado.setText("Ingrese números válidos");
        }
    }

    @FXML
    private void restar() {
        try {
            int a = Integer.parseInt(inputA.getText());
            int b = Integer.parseInt(inputB.getText());
            lblResultado.setText("Resultado: " + restar(a, b));
        } catch (NumberFormatException e) {
            lblResultado.setText("Ingrese números válidos");
        }
    }
}
