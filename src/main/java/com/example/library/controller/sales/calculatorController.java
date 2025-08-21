package com.example.library.controller.sales;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Controller for the calculator UI, handling arithmetic operations and display updates.
 */
public class calculatorController {

    @FXML
    private TextField displayField;

    @FXML
    private Text historyText;

    private String firstOperand = "";
    private String currentOperand = "";
    private String operator = "";

    /**
     * Sets up the calculation by storing the first operand and operator.
     * @param operator The arithmetic operator (+, -, *, /)
     */
    private void setupCalculation(String operator) {
        if (!currentOperand.isEmpty()) {
            this.operator = operator;
            firstOperand = currentOperand;
            currentOperand = "";
            updateHistory(firstOperand + " " + operator);
        }
    }

    @FXML
    void addAction(ActionEvent event) {
        setupCalculation("+");
    }

    @FXML
    void minusAction(ActionEvent event) {
        setupCalculation("-");
    }

    @FXML
    void divideAction(ActionEvent event) {
        setupCalculation("/");
    }

    @FXML
    void multiplicationAction(ActionEvent event) {
        setupCalculation("*");
    }

    /**
     * Performs the calculation based on the stored operator and operands.
     */
    @FXML
    void calculate(ActionEvent event) {
        if (firstOperand.isEmpty() || currentOperand.isEmpty() || operator.isEmpty()) {
            return; // Prevent calculation with incomplete data
        }

        try {
            double firstNum = Double.parseDouble(firstOperand);
            double secondNum = Double.parseDouble(currentOperand);
            double result;

            switch (operator) {
                case "+":
                    result = firstNum + secondNum;
                    break;
                case "-":
                    result = firstNum - secondNum;
                    break;
                case "/":
                    if (secondNum == 0) {
                        displayField.setText("Error: Division by zero");
                        return;
                    }
                    result = firstNum / secondNum;
                    break;
                case "*":
                    result = firstNum * secondNum;
                    break;
                default:
                    return;
            }

            updateHistory(firstOperand + " " + operator + " " + currentOperand + " = " + result);
            displayField.setText(String.valueOf(result));
            firstOperand = String.valueOf(result);
            currentOperand = "";
        } catch (NumberFormatException e) {
            displayField.setText("Error: Invalid input");
        }
    }

    /**
     * Clears all operands, operator, and display fields.
     */
    @FXML
    void clearTextField(ActionEvent event) {
        firstOperand = "";
        currentOperand = "";
        operator = "";
        displayField.setText("0");
        historyText.setText("");
    }

    // --- Number buttons ---
    @FXML void button0Clicked(ActionEvent e) { addDigit("0"); }
    @FXML void button1Clicked(ActionEvent e) { addDigit("1"); }
    @FXML void button2Clicked(ActionEvent e) { addDigit("2"); }
    @FXML void button3Clicked(ActionEvent e) { addDigit("3"); }
    @FXML void button4Clicked(ActionEvent e) { addDigit("4"); }
    @FXML void button5Clicked(ActionEvent e) { addDigit("5"); }
    @FXML void button6Clicked(ActionEvent e) { addDigit("6"); }
    @FXML void button7Clicked(ActionEvent e) { addDigit("7"); }
    @FXML void button8Clicked(ActionEvent e) { addDigit("8"); }
    @FXML void button9Clicked(ActionEvent e) { addDigit("9"); }

    /**
     * Updates the display field with the current operand.
     */
    private void updateDisplay() {
        displayField.setText(currentOperand.isEmpty() ? "0" : currentOperand);
    }

    /**
     * Adds a digit to the current operand and updates the display.
     * @param digit The digit to add (0-9)
     */
    private void addDigit(String digit) {
        if (digit.equals("0") && currentOperand.equals("0")) {
            return; // avoid multiple leading zeros
        }
        currentOperand += digit;
        updateDisplay();
    }

    /**
     * Updates the history text with the current calculation state.
     * @param text The text to display in the history
     */
    private void updateHistory(String text) {
        historyText.setText(text);
    }
}
