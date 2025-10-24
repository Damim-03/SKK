package com.example.library.controller.home;

import java.io.IOException;
import java.util.Objects;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static com.example.library.Alert.alert.showFailedAlert;

public class ButtonController {

    @FXML
    public Label label; // Reference to the Label in the header

    @FXML
    public Button salesButton; // المبيعات button
    @FXML
    public Button inventoryButton; // المخزون button
    @FXML
    public Button ExitButton; // الخروج button
    @FXML
    public Button purchasesButton; // المشتريات button
    @FXML
    public Button recordsButton; // السجلات button
    @FXML
    public Button inquiriesButton;
    @FXML
    public Button PrintBarcodeButton;
    @FXML
    public Button customersButton; // العملاء button

    @FXML
    public void initialize() {
        // Optional: Initialize UI components or set default states
    }

    @FXML
    public void handleSalesButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/sales/Form/sales.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("المبيعات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/purchase.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر فتح شاشة المبيعات.");
        }
    }

    @FXML
    public void handleInventoryButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/password/passwordGate.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("التحقق من كلمة المرور");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // تمرير Stage إلى الكنترولر
            PasswordGateController controller = loader.getController();
            controller.setCurrentStage(stage);

            stage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر فتح نافذة التحقق من كلمة المرور.");
        }
    }

    @FXML
    public void handleCustomersButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/clientForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("العملاء");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/client.png"))));

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
    public void handleProductGatewayAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/Product/ProductScannerui.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("بوابة المنتجات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/product.png"))));

            newStage.setMaximized(true);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة بوابة المنتجات.");
        }
    }

    @FXML
    public void handlePurchasesButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/Form/purchases_Form.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("المشتريات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/sales.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة المشتريات.");
        }
    }

    @FXML
    public void handleRecordsButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/records/Form/all_records.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("السجلات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/records.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة السجلات.");
        }
    }

    @FXML
    public void handleinquiriesButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inquiries/Form/inquiries_Form.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("الاستعلامات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/positive-dynamic.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة الاستعلامات.");
        }
    }

    @FXML
    public void handlePrintBarcodeButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/barcode/Form/Barcode_Form.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("باركود");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/barcode.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "تعذر الانتقال الى شاشة باركود.");
        }
    }

}
