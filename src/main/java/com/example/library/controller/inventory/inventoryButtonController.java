package com.example.library.controller.inventory;

import java.io.IOException;

import com.example.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

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
    public void initialize() {
        // Optional: Initialize UI components or set default states
    }

    @FXML
    public void handleBackButtonAction(ActionEvent event) {
        try {
            App.setRoot("interfaces/home/home");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddNewProductButtonAction(ActionEvent event) {
        System.out.println("Add New Product button clicked!");
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/inventory/addnewproduct.fxml"));
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
            e.printStackTrace();
            System.out.println("Error opening add new product window: " + e.getMessage());
        }
    }

    @FXML
    public void handleViewProductsButtonAction(ActionEvent event) {
        try{
            App.setRoot("interfaces/inventory/getproduct");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddNewCategoryButtonAction(ActionEvent event) {
        System.out.println("Add New Category button clicked!");
        // Add logic for adding a new category
    }

    @FXML
    public void handleEditPricesButtonAction(ActionEvent event) {
        System.out.println("Edit Prices button clicked!");
        // Add logic for editing product prices
    }

    @FXML
    public void handleImportDataButtonAction(ActionEvent event) {
        System.out.println("Import Data button clicked!");
        // Add logic for importing product data
    }

    @FXML
    public void handleProductExpirationButtonAction(ActionEvent event) {
        System.out.println("Product Expiration button clicked!");
        // Add logic for checking product expiration
    }
}