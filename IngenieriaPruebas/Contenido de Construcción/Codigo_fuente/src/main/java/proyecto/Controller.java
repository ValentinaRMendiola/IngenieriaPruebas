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

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class Controller {
    private final DecimalFormat df = new DecimalFormat("0.00");
    private double memoryValue = 0.0;

    //Filtro de teclado, solo permite numeros y simbolos autorizados
    private javafx.scene.control.TextFormatter<String> keybFormatter;

    //Ultima expresion antes de error
    private String lastValidExpression = "";

    //Array para guardar el historial
    private List<String> historial = new ArrayList<>();

    final int MAX_DIGITS = 28;

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
    @FXML private void multiplicar() { screen.appendText("×"); }
    @FXML private void dividir() { screen.appendText("÷"); }

    @FXML 
    private void percentage() {
        String text = screen.getText();

        // Evitar doble porcentaje consecutivo
        if (!text.endsWith("%")) {
            screen.appendText("%");
        }
    }

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
            expr = expr.replace("÷", "/").replace("×", "*");

            // Manejar porcentajes
            // Reemplaza "n%" por "(n/100)"
            expr = expr.replaceAll("(-?\\d+(?:\\.\\d+)?)%", "($1/100)");

            double result = evaluarExpresion(expr);

            String resultado = df.format(result);
            // Verificar límite de caracteres
            if (resultado.length() > MAX_DIGITS) {
                throw new RuntimeException("Excede el limite de digitos.");
            }

            lastValidExpression = expr; // Guarda lo escrito antes del cálculo
            
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
        Stage historyStage = new Stage();
        historyStage.setTitle("Historial de operaciones");
        historyStage.setResizable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setAlignment(Pos.TOP_LEFT);

        for (String entry : historial) {
            Label lbl = new Label(entry);
            lbl.setStyle("-fx-font-size: 14px;");
            content.getChildren().add(lbl);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 300, 400);
        historyStage.setScene(scene);

        // 👇 Hace que el historial sea modal
        historyStage.initModality(Modality.APPLICATION_MODAL);
        historyStage.showAndWait(); // Bloquea la calculadora hasta cerrar
    }

    @FXML
    private void memoryAdd() {
        try {
            double current;
            try {
                current = evaluarExpresion(screen.getText());
            } catch (Exception e) {
                current = Double.parseDouble(screen.getText()); // fallback directo
            }
            memoryValue += current;
        } catch (Exception e) {
            // ignorar error
        }
    }

    @FXML
    private void memorySubs() {
        try {
            double current;
            try {
                current = evaluarExpresion(screen.getText());
            } catch (Exception e) {
                current = Double.parseDouble(screen.getText()); // fallback directo
            }
            memoryValue -= current;
        } catch (Exception e) {
            // ignorar error
        }
    }

    @FXML
    private void memoryClear() {
        memoryValue = 0.0;
    }

    @FXML
    private void memoryRecov() {
        // Recupera el valor almacenado y lo muestra
        screen.setText(df.format(memoryValue));
        lastValidExpression = screen.getText();
    }

    @FXML
    private void initialize() {
        keybFormatter = new javafx.scene.control.TextFormatter<>(change -> {
            
            if (change.getText().equals("/")) {
                change.setText("÷");
            }
            if (change.getText().equals("*")) {
                change.setText("×");
            }

            String newText = change.getControlNewText();
            
            // Limitar cantidad de caracteres
            if (newText.length() > MAX_DIGITS) {
                return null;
            }

            //Solo permitir caracteres validos
            if (!newText.matches("[0-9+\\-*/÷×%.]*")){
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

            // Evitar operadores repetidos (++, --, **, //)
            if (newText.matches(".*([+\\-*/÷×%])\\1.*")) {
                return null;
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
            expr = expr.replace("×", "*").replace("÷", "/");
            java.util.Stack<Double> valores = new java.util.Stack<>();
            java.util.Stack<Character> ops = new java.util.Stack<>();

            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);

                // Si es espacio, ignorar
                if (c == ' ') continue;

                // Si es número (puede tener varios dígitos)
                if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || "+-*/(".indexOf(expr.charAt(i - 1)) != -1))) {
                    StringBuilder sb = new StringBuilder();
                    if (c == '-') sb.append('-'); // signo negativo
                    if (c == '-') i++;
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
