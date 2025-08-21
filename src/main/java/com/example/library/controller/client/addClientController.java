package com.example.library.controller.client;

import com.example.library.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static com.example.library.Alert.alert.*;

public class addClientController {

    // FXML elements
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField idField;
    @FXML private ImageView imageView;
    @FXML private Button deleteImageButton;
    @FXML private Button generateIdButton;
    @FXML private Button uploadImageButton;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button cancelButton;

    private String imagePath = "/images/image.png";

    @FXML
    public void initialize() {
        // Initialize form
        handleGenerateIdButtonAction(null); // Generate 13-digit ID
        try {
            imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
        } catch (NullPointerException e) {
            showFailedAlert("خطأ", "فشل تحميل الصورة الافتراضية.");
        }
    }

    @FXML
    private void handleGenerateIdButtonAction(ActionEvent event) {
        String newId = generateUnique13DigitId();
        idField.setText(newId);
    }

    @FXML
    private void handleUploadImageButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة العميل");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("صور", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                // Save image to target/classes/images/customers/ for runtime access
                String fileName = idField.getText().isEmpty() ? "temp_" + System.currentTimeMillis() : idField.getText();
                fileName += file.getName().substring(file.getName().lastIndexOf('.'));
                Path destPath = Paths.get("target/classes/images/customers/" + fileName);
                Files.createDirectories(destPath.getParent());
                Files.copy(file.toPath(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                imagePath = "/images/customers/" + fileName;
                // Load image from file system using file URL
                imageView.setImage(new Image(destPath.toUri().toString()));
            } catch (IOException e) {
                showFailedAlert("خطأ", "فشل تحميل الصورة.");
            }
        }
    }

    @FXML
    private void handleDeleteImageButtonAction(ActionEvent event) {
        imagePath = "/images/image.png";
        try {
            imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
        } catch (NullPointerException e) {
            showFailedAlert("خطأ", "فشل تحميل الصورة الافتراضية.");
        }
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        String customerId = idField.getText().trim();
        String customerName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (customerId.isEmpty() || customerName.isEmpty()) {
            showWarningAlert("تنبيه", "يجب إدخال رقم المعرف واسم العميل.");
            return;
        }
        if (customerId.length() != 13 || !customerId.matches("\\d+")) {
            showWarningAlert("تنبيه", "رقم المعرف يجب أن يكون 13 رقمًا.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showWarningAlert("تنبيه", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            // Check for duplicate customer_id
            String checkSql = "SELECT COUNT(*) FROM client WHERE customer_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, customerId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlert("خطأ", "رقم المعرف موجود بالفعل.");
                return;
            }

            String sql = "INSERT INTO client (customer_id, customer_name, phone, address, image_path) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, customerId);
            stmt.setString(2, customerName);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setString(4, address.isEmpty() ? null : address);
            stmt.setString(5, imagePath.equals("/images/image.png") ? null : imagePath);
            stmt.executeUpdate();
            showSuccessAlert("نجاح", "تم حفظ العميل بنجاح.");
            handleClearButtonAction(null);
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل حفظ العميل." + e.getMessage());
        }
    }

    @FXML
    private void handleClearButtonAction(ActionEvent event) {
        nameField.clear();
        phoneField.clear();
        addressField.clear();
        idField.clear();
        imagePath = "/images/image.png";
        try {
            imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
        } catch (NullPointerException e) {
            showFailedAlert("خطأ", "فشل تحميل الصورة الافتراضية.");
        }
        handleGenerateIdButtonAction(null); // Generate new ID after clearing
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private String generateUnique13DigitId() {
        Random random = new Random();
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            id.append(random.nextInt(10));
        }
        String generatedId = id.toString();

        // Verify uniqueness
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                String sql = "SELECT COUNT(*) FROM client WHERE customer_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, generatedId);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    return generateUnique13DigitId(); // Recurse if ID exists
                }
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "فشل التحقق من رقم المعرف.");
        }
        return generatedId;
    }
}