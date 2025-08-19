package com.example.library.controller.client;

import com.example.library.model.Client;
import com.example.library.util.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import static com.example.library.Alert.alert.showFailedAlert;

public class updateClientController {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField idField;
    @FXML private Button generateIdButton;
    @FXML private Button uploadImageButton;
    @FXML private Button deleteImageButton;
    @FXML private ImageView imageView;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button cancelButton;

    private Client client;
    private String selectedImagePath;

    public void setClient(Client client) {
        this.client = client;
        nameField.setText(client.getCustomerName());
        phoneField.setText(client.getPhone());
        addressField.setText(client.getAddress());
        idField.setText(client.getCustomerId());
        String imagePath = client.getImagePath() != null ? client.getImagePath() : "/images/image.png";
        try {
            if (imagePath.startsWith("/images/customers/")) {
                File file = new File("target/classes" + imagePath);
                imageView.setImage(new Image(file.toURI().toString()));
            } else {
                imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
            }
        } catch (Exception e) {
            showFailedAlert("خطأ", "فشل تحميل صورة العميل: " + e.getMessage());
            imageView.setImage(null);
        }
        generateIdButton.setVisible(false); // Hide for updates
        clearButton.setVisible(false); // Hide for updates
    }

    @FXML
    private void handleGenerateIdButton(ActionEvent event) {
        String newId = generateUniqueCustomerId();
        idField.setText(newId);
    }

    @FXML
    private void handleUploadImageButton(ActionEvent event) {
        if (idField.getText().isEmpty()) {
            showFailedAlert("خطأ", "يرجى توليد أو تحديد رقم المعرف أولاً.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة العميل");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("صور", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (file != null) {
            try {
                String fileName = idField.getText() + file.getName().substring(file.getName().lastIndexOf('.'));
                Path destPath = Paths.get("target/classes/images/customers/" + fileName);
                Files.createDirectories(destPath.getParent());
                Files.copy(file.toPath(), destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = "/images/customers/" + fileName;
                imageView.setImage(new Image(destPath.toUri().toString()));
            } catch (IOException e) {
                showFailedAlert("خطأ", "فشل تحميل الصورة: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteImageButton(ActionEvent event) {
        selectedImagePath = null;
        imageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        String customerId = idField.getText().trim();
        String customerName = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (customerId.isEmpty() || customerName.isEmpty()) {
            showFailedAlert("خطأ", "رقم المعرف واسم العميل مطلوبان.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                return;
            }
            String sql;
            PreparedStatement stmt;
            if (client == null) { // Add new client
                sql = "INSERT INTO client (customer_id, customer_name, phone, address, image_path) VALUES (?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, customerId);
                stmt.setString(2, customerName);
                stmt.setString(3, phone.isEmpty() ? null : phone);
                stmt.setString(4, address.isEmpty() ? null : address);
                stmt.setString(5, selectedImagePath != null ? selectedImagePath : null);
            } else { // Update existing client
                sql = "UPDATE client SET phone = ?, address = ?, image_path = ? WHERE customer_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, phone.isEmpty() ? null : phone);
                stmt.setString(2, address.isEmpty() ? null : address);
                stmt.setString(3, selectedImagePath != null ? selectedImagePath : null);
                stmt.setString(4, customerId);
            }
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showFailedAlert("نجاح", client == null ? "تم إضافة العميل بنجاح: " + customerName : "تم تحديث العميل بنجاح: " + customerName);
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            } else {
                showFailedAlert("خطأ", client == null ? "فشل إضافة العميل." : "فشل تحديث العميل.");
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", (client == null ? "فشل إضافة العميل: " : "فشل تحديث العميل: ") + e.getMessage());
        }
    }

    @FXML
    private void handleClearButton(ActionEvent event) {
        nameField.clear();
        phoneField.clear();
        addressField.clear();
        idField.clear();
        selectedImagePath = null;
        imageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private String generateUniqueCustomerId() {
        Random random = new Random();
        String newId;
        boolean exists;

        do {
            newId = String.format("%013d", random.nextInt(1000000000) + 1000000000);
            exists = false;
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات.");
                    return null;
                }
                String sql = "SELECT COUNT(*) FROM client WHERE customer_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            } catch (SQLException e) {
                showFailedAlert("خطأ", "فشل التحقق من رقم المعرف: " + e.getMessage());
                return null;
            }
        } while (exists);

        return newId;
    }
}