package com.example.library.controller.home;

import java.io.IOException;

import com.example.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ButtonController {

    @FXML
    public Label label; // Reference to the Label in the header

    @FXML
    public Button salesButton; // المبيعات button
    @FXML
    public Button inventoryButton; // المخزون button
    @FXML
    public Button expensesButton; // المصروفات button
    @FXML
    public Button purchasesButton; // المشتريات button
    @FXML
    public Button recordsButton; // السجلات button
    @FXML
    public Button customersButton; // العملاء button

    @FXML
    public void initialize() {
        // Optional: Initialize UI components or set default states
        label.setText("Soubirate Kamel Kir"); // Ensure label text is set
    }

    @FXML
    public void handleSalesButtonAction(ActionEvent event) {
        try{
            App.setRoot("interfaces/sales/sales");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleInventoryButtonAction(ActionEvent event) {
        try {
            App.setRoot("interfaces/inventory/inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExpensesButtonAction(ActionEvent event) {
        System.out.println("Expenses button clicked!");
        // Add logic for Expenses (المصروفات) button
    }

    @FXML
    public void handlePurchasesButtonAction(ActionEvent event) {
        System.out.println("Purchases button clicked!");
        // Add logic for Purchases (المشتريات) button
    }

    @FXML
    public void handleRecordsButtonAction(ActionEvent event) {
        System.out.println("Records button clicked!");
        // Add logic for Records (السجلات) button
    }

    @FXML
    public void handleCustomersButtonAction(ActionEvent event) {
        System.out.println("Customers button clicked!");
        // Add logic for Customers (العملاء) button
    }

}
