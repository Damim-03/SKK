package com.example.library.controller.Purchases;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class calculatorPurchasesController {
    @FXML
    private Label display;

    private double currentNumber = 0;
    private double storedNumber = 0;
    private String currentOperator = "";
    private boolean newInput = true;
    private boolean decimalEntered = false;

    @FXML
    private void handleNumber(ActionEvent event) {
        Button button = (Button) event.getSource();
        String digit = button.getText();

        if (display.getText().length() >= 15) return; // Limit input length

        if (newInput) {
            display.setText(digit);
            newInput = false;
        } else {
            display.setText(display.getText() + digit);
        }
    }

    @FXML
    private void handleDecimal(ActionEvent event) {
        if (!display.getText().contains(".")) {
            if (newInput) {
                display.setText("0.");
                newInput = false;
            } else {
                display.setText(display.getText() + ".");
            }
            decimalEntered = true;
        }
    }

    @FXML
    private void handleOperator(ActionEvent event) {
        Button button = (Button) event.getSource();
        String operator = button.getText();

        if (!newInput) {
            calculate();
            currentNumber = Double.parseDouble(display.getText());
            currentOperator = operator;
            newInput = true;
            decimalEntered = false;
        } else {
            currentOperator = operator;
        }
    }

    @FXML
    private void handleEquals(ActionEvent event) {
        if (!newInput) {
            calculate();
            newInput = true;
            decimalEntered = false;
            currentOperator = "";
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        display.setText("0");
        currentNumber = 0;
        storedNumber = 0;
        currentOperator = "";
        newInput = true;
        decimalEntered = false;
    }

    @FXML
    private void handleClearEntry(ActionEvent event) {
        display.setText("0");
        newInput = true;
        decimalEntered = false;
    }

    @FXML
    private void handleNegate(ActionEvent event) {
        double value = Double.parseDouble(display.getText());
        value *= -1;
        display.setText(formatNumber(value));
    }

    @FXML
    private void handlePercentage(ActionEvent event) {
        double value = Double.parseDouble(display.getText());
        value /= 100;
        display.setText(formatNumber(value));
    }

    @FXML
    private void handleSquare(ActionEvent event) {
        double value = Double.parseDouble(display.getText());
        value *= value;
        display.setText(formatNumber(value));
    }

    @FXML
    private void handleSquareRoot(ActionEvent event) {
        double value = Double.parseDouble(display.getText());
        if (value < 0) {
            display.setText("Error");
            newInput = true;
            return;
        }
        value = Math.sqrt(value);
        display.setText(formatNumber(value));
    }

    @FXML
    private void handleReciprocal(ActionEvent event) {
        double value = Double.parseDouble(display.getText());
        if (value == 0) {
            display.setText("Error");
            newInput = true;
            return;
        }
        value = 1 / value;
        display.setText(formatNumber(value));
    }

    @FXML
    private void handleMemoryStore(ActionEvent event) {
        storedNumber = Double.parseDouble(display.getText());
    }

    @FXML
    private void handleMemoryRecall(ActionEvent event) {
        display.setText(formatNumber(storedNumber));
        newInput = true;
    }

    @FXML
    private void handleMemoryAdd(ActionEvent event) {
        storedNumber += Double.parseDouble(display.getText());
    }

    @FXML
    private void handleMemorySubtract(ActionEvent event) {
        storedNumber -= Double.parseDouble(display.getText());
    }

    @FXML
    private void handleMemoryClear(ActionEvent event) {
        storedNumber = 0;
    }

    @FXML
    private void handleMemorySwap(ActionEvent event) {
        double temp = storedNumber;
        storedNumber = Double.parseDouble(display.getText());
        display.setText(formatNumber(temp));
        newInput = true;
    }

    private void calculate() {
        double secondNumber = Double.parseDouble(display.getText());
        if (currentOperator.equals("/") && secondNumber == 0) {
            display.setText("Error");
            newInput = true;
            currentOperator = "";
            return;
        }
        switch (currentOperator) {
            case "+":
                currentNumber += secondNumber;
                break;
            case "-":
                currentNumber -= secondNumber;
                break;
            case "×":
                currentNumber *= secondNumber;
                break;
            case "/":
                currentNumber /= secondNumber;
                break;
            default:
                currentNumber = secondNumber;
                break;
        }
        display.setText(formatNumber(currentNumber));
    }

    private String formatNumber(double value) {
        return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
    }
}
