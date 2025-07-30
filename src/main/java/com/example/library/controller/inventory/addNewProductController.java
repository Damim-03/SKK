package com.example.library.controller.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import com.example.library.util.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class addNewProductController {

    @FXML
    private TextField barcodeTextField;
    @FXML
    private Button readButton;
    @FXML
    private TextField productNameTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private MenuButton unitMenuButton;
    @FXML
    private Button backButton;
    @FXML
    private TextField price1TextField;
    @FXML
    private TextField price2TextField;
    @FXML
    private TextField price3TextField;
    @FXML
    private TextField quantityTextField;
    @FXML
    private ImageView productImageView;
    @FXML
    private StackPane imageDisplayPane;
    @FXML
    private Button uploadImageButton;
    @FXML
    private Button deleteImageButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button clearButton;
    @FXML
    private DatePicker productionDatePicker; // Added for production date
    @FXML
    private DatePicker expirationDatePicker; // Added for expiration date

    public void initialize() {
        // Initialize unit menu
        unitMenuButton.getItems().clear();
        MenuItem item1 = new MenuItem("Piece");
        MenuItem item2 = new MenuItem("Kilogram");

        item1.setOnAction(e -> unitMenuButton.setText("Piece"));
        item2.setOnAction(e -> unitMenuButton.setText("Kilogram"));

        unitMenuButton.getItems().addAll(item1, item2);
        unitMenuButton.setText("Select Unit");

        // Allow only numeric input in the barcode field
        barcodeTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                barcodeTextField.setText(newText.replaceAll("[^\\d]", ""));
            }
        });

        // When scanner presses Enter, move focus to next field
        barcodeTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String scannedBarcode = barcodeTextField.getText().trim();
                if (!scannedBarcode.isEmpty()) {
                    productNameTextField.requestFocus(); // Move to next input
                }
            }
        });

        // Format price and quantity fields
        setupPriceFieldFormatting(price1TextField);
        setupPriceFieldFormatting(price2TextField);
        setupPriceFieldFormatting(price3TextField);
        setupQuantityFieldFormatting(quantityTextField);
    }

    // Extract the longest consecutive numeric sequence
    private String extractNumericBarcode(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder numericSeq = new StringBuilder();
        StringBuilder currentSeq = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                currentSeq.append(c);
            } else {
                if (currentSeq.length() > numericSeq.length()) {
                    numericSeq = new StringBuilder(currentSeq);
                }
                currentSeq.setLength(0);
            }
        }
        if (currentSeq.length() > numericSeq.length()) {
            numericSeq = currentSeq;
        }
        return numericSeq.toString();
    }

    private void setupPriceFieldFormatting(TextField priceField) {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                String cleanValue = newValue.replaceAll("[^0-9.]", "");

                String[] parts = cleanValue.split("\\.");
                if (parts.length > 2) {
                    cleanValue = parts[0] + "." + parts[1];
                }

                if (cleanValue.startsWith(".")) {
                    cleanValue = "0" + cleanValue;
                }

                if (cleanValue.contains(".")) {
                    String[] decimalParts = cleanValue.split("\\.");
                    String integerPart = decimalParts[0];
                    String decimalPart = decimalParts.length > 1 ? decimalParts[1] : "";
                    decimalPart = decimalPart.length() > 2 ? decimalPart.substring(0, 2) : decimalPart;
                    cleanValue = integerPart + "." + String.format("%-2s", decimalPart).replace(' ', '0');
                } else {
                    cleanValue = cleanValue + ".00";
                }

                priceField.setText(cleanValue);
                priceField.positionCaret(cleanValue.length());
            } else {
                priceField.setText("0.00");
            }
        });
    }

    private void setupQuantityFieldFormatting(TextField quantityField) {
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                quantityField.setText(newValue.replaceAll("[^0-9]", ""));
            }
        });
    }

    @FXML
    public void handleUploadImageAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            Stage stage = (Stage) uploadImageButton.getScene().getWindow();
            java.io.File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                Image image = new Image(selectedFile.toURI().toString());
                productImageView.setImage(image);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load image: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleDeleteImageAction(ActionEvent event) {
        try {
            productImageView.setImage(new Image(getClass().getResourceAsStream("/images/image.png")));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to load default image.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleReadButtonAction(ActionEvent event) {
        barcodeTextField.requestFocus();
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> barcodeTextField.requestFocus());
            }
        }, 100);
    }

    @FXML
    private void handleUnitSelection(ActionEvent event) {
        MenuItem selectedItem = (MenuItem) event.getSource();
        unitMenuButton.setText(selectedItem.getText());
    }

    @FXML
    public void handleBackButtonAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void handleSaveProductAction(ActionEvent event) {
        String uuid = UUID.randomUUID().toString();
        String barcode = barcodeTextField.getText().trim();
        String name = productNameTextField.getText().trim();
        String desc = descriptionTextField.getText().trim();
        String selectedUnit = unitMenuButton.getText();
        String price1 = price1TextField.getText().trim();
        String price2 = price2TextField.getText().trim();
        String price3 = price3TextField.getText().trim();
        String quantity = quantityTextField.getText().trim();

        String productionDate = productionDatePicker.getValue() != null
                ? productionDatePicker.getValue().toString()
                : java.time.LocalDate.now().toString();

        String expirationDate = expirationDatePicker.getValue() != null
                ? expirationDatePicker.getValue().toString()
                : java.time.LocalDate.now().plusYears(1).toString();

        String imagePath = (productImageView.getImage() != null && productImageView.getImage().getUrl() != null)
                ? productImageView.getImage().getUrl()
                : null;

        if (barcode.isEmpty() || name.isEmpty() || desc.isEmpty() || selectedUnit.equals("Select Unit") ||
                price1.isEmpty() || price2.isEmpty() || price3.isEmpty() || quantity.isEmpty()) {
            showAlert("Missing Fields", "Please fill all required fields and select a unit.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "INSERT INTO products (id, barcode, product_name, description, price1, price2, price3, unit, quantity, production_date, expiration_date, image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            pstmt.setString(2, barcode);
            pstmt.setString(3, name);
            pstmt.setString(4, desc);
            pstmt.setString(5, price1);
            pstmt.setString(6, price2);
            pstmt.setString(7, price3);
            pstmt.setString(8, selectedUnit);
            pstmt.setInt(9, Integer.parseInt(quantity));
            pstmt.setDate(10, java.sql.Date.valueOf(productionDate));
            pstmt.setDate(11, java.sql.Date.valueOf(expirationDate));
            pstmt.setString(12, imagePath);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("تنبيه");
                alert.setHeaderText("✅ تم حفظ المنتح بنجاح");
                alert.showAndWait();
                clearFields();
            } else {
                showAlert("Warning", "No rows were affected", Alert.AlertType.WARNING);
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save product: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid quantity.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void clearFields() {
        barcodeTextField.clear();
        productNameTextField.clear();
        descriptionTextField.clear();
        unitMenuButton.setText("Select Unit");
        price1TextField.clear();
        price2TextField.clear();
        price3TextField.clear();
        quantityTextField.clear();
        productionDatePicker.setValue(null);
        expirationDatePicker.setValue(null);
        try {
            productImageView.setImage(new Image(getClass().getResourceAsStream("/images/image.png")));
        } catch (Exception e) {
            // Silently handle exception, image remains unchanged if load fails
        }
    }

    private void showAlert(String success, String product_saved_successfully, Alert.AlertType alertType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}