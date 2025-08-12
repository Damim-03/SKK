package com.example.library.controller.inventory;

import java.io.IOException;
import java.util.Objects;

import com.example.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import static com.example.library.Alert.alert.showFailedAlert;

public class inventoryButtonController {

    @FXML
    public Button backButton; // back button
    @FXML
    public Button addNewProductButton; // اضافة منتج جديد
    @FXML
    public Button viewProductsButton; // عرض المنتجات
    @FXML
    public Button addNewCategoryButton; // اضافة تصنيف جديد
    @FXML
    public Button editPricesButton; // تعديل اسعار المنتجات
    @FXML
    public Button importDataButton; // استيراد بيانات المنتجات
    @FXML
    public Button productExpirationButton; // صلاحية المنتجات
    @FXML
    public Button categoryButton;
    @FXML
    public Button unitButton;

    @FXML
    public void initialize() {
        // Optional: Initialize UI components or set default states
    }

    @FXML
    public void handleBackButtonAction(ActionEvent event) {
        try {
            App.setRoot("interfaces/home/home");
        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة الرئيسية.");
        }
    }

    @FXML
    public void handleAddNewProductButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/CRUD/addnewproduct.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("اضافة منتج جديد");
            newStage.setScene(new javafx.scene.Scene(root));



            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة اضافة منتج جديد.");
        }
    }

    @FXML
    public void handleViewProductsButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/CRUD/getproduct.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("عرض المنتجات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/zoom.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة عرض المنتجات."+ e.getMessage());
        }
    }

    @FXML
    public void handleAddNewCategoryButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/categoryForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("اضافة تصنيف جديد");
            newStage.setScene(new javafx.scene.Scene(root));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة اضافة تصنيفات."+ e.getMessage());
        }
    }

    @FXML
    public void handleEditPricesButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/CRUD/updateproduct.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("تعديل اسعار المنتجات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/cycle.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة تعديل المنتجات."+ e.getMessage());
        }
    }
    @FXML
    public void handleCategoryButtonAction(ActionEvent event){
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/addcategory.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("اضافة تصنيف جديد");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/category.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة اضافة تصنيفات."+ e.getMessage());
        }
    }

    @FXML
    public void handleUnitButtonAction(ActionEvent event){
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/addunit.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("اضافة وحدة جديدة");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/unit.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة اضافة الوحدات."+ e.getMessage());
        }
    }

    @FXML
    public void handleImportDataButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/importDataForm.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("استيراد البيانات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/excel.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة استيراد البيانات."+ e.getMessage());
        }
    }

    @FXML
    public void handleProductExpirationButtonAction(ActionEvent event) {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/Form/expiration.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("صلاحية المنتجات");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/date.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            showFailedAlert("خطأ", "فشل في فتح نافذة صلاحيات المنتج."+ e.getMessage());
        }
    }
}