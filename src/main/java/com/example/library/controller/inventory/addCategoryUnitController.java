package com.example.library.controller.inventory;

import com.example.library.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class addCategoryUnitController {

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextField unitNameField;

    @FXML
    private void handleSaveCategory() {
        String name = categoryNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "يرجى إدخال اسم التصنيف!");
            return;
        }

        String insertSQL = "INSERT INTO category(name_of_category) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "تم حفظ التصنيف بنجاح!");
            categoryNameField.clear();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "فشل في حفظ التصنيف: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveUnit() {
        String name = unitNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "يرجى إدخال اسم الوحدة!");
            return;
        }

        String insertSQL = "INSERT INTO unit(unit_name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "تم حفظ الوحدة بنجاح!");
            unitNameField.clear();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "فشل في حفظ الوحدة: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearunit() {
        unitNameField.clear(); // ✅ استخدم unitNameField هنا
    }

    @FXML
    private void handleClear() {
        categoryNameField.clear(); // ✅ استخدم unitNameField هنا
    }

    @FXML
    private void handleCloseUnit() {
        Stage stage = (Stage) unitNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) categoryNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
