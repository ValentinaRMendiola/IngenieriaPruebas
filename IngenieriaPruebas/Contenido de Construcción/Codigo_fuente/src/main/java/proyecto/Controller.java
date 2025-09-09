package proyecto;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.text.DecimalFormat;
import java.util.Stack;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class Controller {
    private final DecimalFormat df = new DecimalFormat("0.00");

    //Filtro de teclado, solo permite numeros y simbolos autorizados
    private javafx.scene.control.TextFormatter<String> keybFormatter;

    //Ultima expresion antes de error
    private String lastValidExpression = "";

    //Array para guardar el historial
    private List<String> historial = new ArrayList<>();

    @FXML private TextField screen;

    @FXML private void typeCero() { append("0"); }
    @FXML private void typeUno() { append("1"); }
    @FXML private void typeDos() { append("2"); }
    @FXML private void typeTres() { append("3"); }
    @FXML private void typeCuatro() { append("4"); }
    @FXML private void typeCinco() { append("5"); }
    @FXML private void typeSeis() { append("6"); }
    @FXML private void typeSiete() { append("7"); }
    @FXML private void typeOcho() { append("8"); }
    @FXML private void typeNueve() { append("9"); }
    @FXML private void typePunto() { append("."); }

    @FXML private void sumar() { append("+"); }
    @FXML private void restar() { append("-"); }
    @FXML private void multiplicar() { append("*"); }
    @FXML private void dividir() { append("/"); }

    @FXML 
    private void clearAll() {
        screen.clear(); 
        lastValidExpression = "";
    }
    
    @FXML
    private void delete() {
        String text = screen.getText();

        if (text.equals("Error")) {
            screen.setText(lastValidExpression);
            return;
        }

        if (!text.isEmpty()){
            screen.setText(text.substring(0, text.length() - 1));
            lastValidExpression = screen.getText();
        }
    }

    @FXML
    private void igual() {
        try{
            String expr = screen.getText();
            double result = evaluarExpresion(expr);

            lastValidExpression = expr; // Guarda lo escrito antes del cálculo
            String resultado = df.format(result);

            screen.setText(df.format(result));
            historial.add(expr + " = " + resultado);
        } catch(Exception e){
            // Guarda lo último válido antes del error
            lastValidExpression = screen.getText();

            screen.setTextFormatter(null);
            screen.setText("Error");
            screen.setTextFormatter(keybFormatter);
        }
    }

    @FXML
    private void historial() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historial de Operaciones");
        alert.setHeaderText("Últimas operaciones realizadas");

        Stage historyStage = new Stage();
        historyStage.setTitle("Historial de Operaciones");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(historial);

        Scene scene = new Scene(listView, 300, 400);
        historyStage.setScene(scene);
        historyStage.show();
    }

    @FXML
    private void initialize() {
        keybFormatter = new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            
            //Solo permitir caracteres validos
            if (!newText.matches("[0-9+\\-*/.]*")){
                return null;
            }

            //Evitar doble punto
            // Divide la expresión en tokens (números) separados por operadores
            String[] tokens = newText.split("[+\\-*/]");

            for (String token : tokens) {
                // Si un número tiene más de un punto, es inválido
                if (token.chars().filter(ch -> ch == '.').count() > 1) {
                    return null;
                }
            }

            return change;
        });

        screen.setTextFormatter(keybFormatter);
    }

    // Helper para escribir
    private void append(String value) {
        if (screen.getText().equals("Error")) {
            screen.setText(""); // limpia error antes de escribir
        }
        screen.appendText(value);
        lastValidExpression = screen.getText();
    }

    //---------------------
    //PARSER DE EXPRESIONES
    //---------------------
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
            case '/': 
                if (b == 0){
                    throw new ArithmeticException("Division por cero");
                }
                return a / b;
        }
        return 0;
    }
    //----------------------
}
