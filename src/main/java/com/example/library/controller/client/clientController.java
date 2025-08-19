package com.example.library.controller.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.util.Objects;
import static com.example.library.Alert.alert.showFailedAlert;

public class clientController {

    @FXML
    private Button addClientButton;
    @FXML
    private Button purchaseClientButton;
    @FXML
    private Button searchClientButton;
    @FXML
    private Button payButton;
    @FXML
    private Button settingsClientButton;
    @FXML
    private Button recordsClientButton;

    @FXML
    private void handleaddClientButtonAction(ActionEvent event) {
        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/CRUD/add_client.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("اضافة العميل");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/add_client.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("فشل", "فتح نافذة اضافة عميل جديد");
        }
    }

    @FXML
    private void handlepurchaseClientButtonAction(ActionEvent event) {

        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/sales_Client.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("مبيعات العملاء");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/purchase.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("عمليات شراء العميل", "عرض سجل مشتريات العميل");
        }
    }

    @FXML
    private void handlesearchClientButtonAction(ActionEvent event) {

        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/CRUD/get_client.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("عرض العملاء");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/search-client.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("بحث العملاء", "فتح واجهة بحث العملاء.");
        }
    }

    @FXML
    private void handlepayClientButtonAction(ActionEvent event) {

        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/client_Pay_DebtForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("تسديد الدين");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/pay.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("سداد الدين", " تعذر فتح نموذج سداد الدين.");
        }
    }

    @FXML
    private void handlesettingsClientButtonAction(ActionEvent event) {

        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/settings_Client.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("خصائص العميل");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/settings.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("إعدادات العميل", "تعذر فتح واجهة إعدادات العميل.");
        }
    }

    @FXML
    private void handlerecordsClientButtonAction(ActionEvent event) {
        try{
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/recordes_client.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("سجلات المبيعات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/records.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();
        } catch (Exception e) {
            showFailedAlert("سجلات المبيعات", "تعذر عرض سجلات المبيعات.");
        }
    }

}