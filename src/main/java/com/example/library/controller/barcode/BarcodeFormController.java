package com.example.library.controller.barcode;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import static com.example.library.Alert.alert.showFailedAlert;

public class BarcodeFormController {

    @FXML
    private void handlePrintA4(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/barcode/Form/Barcode_A4.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("طباعة على ورق من قياس A4");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/a4.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة العملاء.");
        }
    }

    @FXML
    private void handlePrintBarcodePaper(ActionEvent event) {
        try {
            // استخدم getResourceAsStream للتحقق من وجود الملف
            InputStream fxmlStream = getClass().getResourceAsStream("/com/example/interfaces/barcode/Form/barcode_Printerbc.fxml");
            if (fxmlStream == null) {
                showFailedAlert("خطأ", "لم يتم العثور على ملف الواجهة: Barcode_Printerbc.fxml");
                return;
            }
            fxmlStream.close();

            // تحميل الملف
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/barcode/Form/barcode_Printerbc.fxml"));
            Parent root = loader.load();

            // إنشاء النافذة الجديدة
            Stage newStage = new Stage();
            newStage.setTitle("طباعة على ورق من قياس A4");
            newStage.setScene(new Scene(root));

            // إضافة الأيقونة
            try {
                Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/barcode-paper.png")));
                newStage.getIcons().add(icon);
            } catch (Exception e) {
                showFailedAlert("خطأ", "لم يتم العثور على الأيقونة");
            }

            // خصائص النافذة
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // عرض النافذة
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال إلى شاشة طباعة الباركود.");
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Close the window
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
