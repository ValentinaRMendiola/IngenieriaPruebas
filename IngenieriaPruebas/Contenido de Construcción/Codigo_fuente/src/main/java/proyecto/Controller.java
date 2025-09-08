package proyecto;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.text.DecimalFormat;

public class Controller {

    @FXML
    private TextField screen;

    @FXML
    private Button btnIgual;
    @FXML
    private Button btnSumar;
    @FXML
    private Button btnRestar;
    @FXML
    private Button btnMultiplicar;
    @FXML
    private Button btnDividir;

    @FXML
    private Button btnHistorial;
    @FXML
    private Button btnNueve;
    @FXML
    private Button btnSeis;
    @FXML
    private Button btnTres;

    @FXML
    private Button btnDelete;
    @FXML
    private Button btnOcho;
    @FXML
    private Button btnCinco;
    @FXML
    private Button btnDos;
    @FXML
    private Button btnPunto;

    @FXML
    private Button btnClearAll;
    @FXML
    private Button btnSiete;
    @FXML
    private Button btnCuatro;
    @FXML
    private Button btnUno;
    @FXML
    private Button btnCero;

    /*
    public int sumar(int a, int b) {
        return a + b;
    }

    public int restar(int a, int b) {
        return a - b;
    }

    public int multiplicar(int a, int b){
        return a * b;
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

    @FXML
    private void multiplicar(){
        try{
            int a = Integer.parseInt(inputA.getText());
            int b = Integer.parseInt(inputB.getText());
        lblResultado.setText("Resultado: " + multiplicar(a, b));
        } catch (NumberFormatException e) 
        {
            lblResultado.setText("Ingrese números válidos");
        }
    }*/
    private double evaluarExpresion(String expr) {
        try {
            java.util.Stack<Double> valores = new java.util.Stack<>();
            java.util.Stack<Character> ops = new java.util.Stack<>();

            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);

                // Si es espacio, ignorar
                if (c == ' ') continue;

                // Si es número (puede tener varios dígitos)
                if (Character.isDigit(c) || c == '.') {
                    StringBuilder sb = new StringBuilder();
                    while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                        sb.append(expr.charAt(i++));
                    }
                    valores.push(Double.parseDouble(sb.toString()));
                    i--; // retroceder
                }
                // Si es paréntesis
                else if (c == '(') {
                    ops.push(c);
                } else if (c == ')') {
                    while (ops.peek() != '(') {
                        valores.push(aplicarOp(ops.pop(), valores.pop(), valores.pop()));
                    }
                    ops.pop();
                }
                // Si es operador
                else if (c == '+' || c == '-' || c == '*' || c == '/') {
                    while (!ops.isEmpty() && precedencia(ops.peek()) >= precedencia(c)) {
                        valores.push(aplicarOp(ops.pop(), valores.pop(), valores.pop()));
                    }
                    ops.push(c);
                }
            }

            while (!ops.isEmpty()) {
                valores.push(aplicarOp(ops.pop(), valores.pop(), valores.pop()));
            }

            return valores.pop();
        } catch (Exception e) {
            throw new RuntimeException("Expresión inválida");
        }
    }

    private int precedencia(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private double aplicarOp(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
        }
        return 0;
    }
    
    @FXML
    private void igual() {
        try {
            double result = evaluarExpresion(screen.getText());
            screen.setText(df.format(result));
        } catch (Exception e) {
            screen.setText("Error");
    }
}
}
