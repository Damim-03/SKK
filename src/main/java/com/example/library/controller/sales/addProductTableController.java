package com.example.library.controller.sales;

import com.example.library.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.function.UnaryOperator;

public class addProductTableController {

    @FXML public TextField generatedBarcode;
    @FXML public Button generatedBarcodeButton;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private Button addButton;
    @FXML private Button cancelButton;

    private salesController mainController;
    private boolean isFromDatabase;

    public void setMainController(salesController controller) {
        this.mainController = controller;
    }

    @FXML
    private void initialize() {
        // Setup barcode generation
        generatedBarcodeButton.setOnAction(e -> {
            String uniqueBarcode = generateUniqueBarcode();
            generatedBarcode.setText(uniqueBarcode);
        });

        // Setup buttons
        addButton.setOnAction(e -> handleAddProduct());
        cancelButton.setOnAction(e -> ((Stage) cancelButton.getScene().getWindow()).close());

        // Price field validation (numbers with 2 decimal places)
        priceField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // Quantity field validation (only positive integers)
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        quantityField.setTextFormatter(new TextFormatter<>(integerFilter));

        // Format price when focus is lost
        priceField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                formatPriceField();
            }
        });
    }

    private void formatPriceField() {
        String priceText = priceField.getText();
        try {
            double price = Double.parseDouble(priceText);
            priceField.setText(String.format("%.2f", price));
        } catch (NumberFormatException e) {
            priceField.setText("0.00");
        }
    }

    private void handleAddProduct() {
        // Get values from fields
        String name = nameField.getText().trim();
        String barcode = generatedBarcode.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();

        // Validate required fields
        if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || barcode.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "All fields (including barcode) are required.");
            return;
        }

        // Parse and validate price
        double priceVal;
        try {
            priceVal = Double.parseDouble(priceText);
            if (priceVal <= 0.0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Price", "Price must be greater than 0.");
                return;
            }
            priceVal = Double.parseDouble(String.format("%.2f", priceVal));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Price must be a valid number.");
            return;
        }

        // Parse and validate quantity
        int quantityVal;
        try {
            quantityVal = Integer.parseInt(quantityText);
            if (quantityVal <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Quantity must be a whole number.");
            return;
        }

        // Calculate total
        double total = priceVal * quantityVal;
        total = Double.parseDouble(String.format("%.2f", total));

        // Add product to main table
        if (mainController != null) {
            mainController.addProductToTable(name, barcode, priceVal, quantityVal, total, isFromDatabase);
        }

        // Close the window
        ((Stage) addButton.getScene().getWindow()).close();
    }

    private String generateUniqueBarcode() {
        String barcode;
        int attempts = 0;
        do {
            barcode = generateValid13DigitBarcode();
            attempts++;
            if (attempts > 100) {
                showAlert(Alert.AlertType.ERROR, "Barcode Error", "Could not generate unique barcode after 100 attempts");
                return "ERROR";
            }
        } while (barcodeExistsInDatabase(barcode));
        return barcode;
    }

    private String generateValid13DigitBarcode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(13);

        // First digit (1-9 to avoid leading zero)
        sb.append(random.nextInt(9) + 1);

        // Remaining 12 digits (0-9)
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private boolean barcodeExistsInDatabase(String barcode) {
        String query = "SELECT COUNT(*) FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume exists if DB error
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}