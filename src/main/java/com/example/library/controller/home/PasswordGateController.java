package com.example.library.controller.home;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PasswordGateController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label lblStatus;

    private Stage currentStage; // نافذة كلمة المرور الحالية

    // Setter لإرسال Stage من الزر الأصلي
    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
    }

    @FXML
    public void handleUnlock() {
        String password = passwordField.getText();

        if ("0778184259".equals(password)) {
            lblStatus.setText("تم السماح بالدخول ✅");
            lblStatus.setStyle("-fx-text-fill: #00ff88;");

            // غلق نافذة كلمة المرور وفتح نافذة المخزون
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/inventory.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("المخزون");
                stage.setScene(new Scene(root));
                stage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/goods.png"))));
                stage.setResizable(false);
                stage.show();

                currentStage.close(); // غلق نافذة كلمة المرور

            } catch (IOException e) {
                lblStatus.setText("حدث خطأ أثناء الفتح ❌");
                lblStatus.setStyle("-fx-text-fill: #ff6666;");
            }

        } else {
            lblStatus.setText("كلمة المرور غير صحيحة ❌");
            lblStatus.setStyle("-fx-text-fill: #ff6666;");
        }
    }
}
