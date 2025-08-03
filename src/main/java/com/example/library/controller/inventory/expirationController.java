package com.example.library.controller.inventory;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class expirationController {

    // FXML elements from the view
    @FXML private TextField barcodeField;
    @FXML private Button ReadBarcode;
    @FXML private TableView<?> tableView;
    @FXML private TableColumn<?, ?> selectColumn;
    @FXML private TableColumn<?, ?> barcodeColumn;
    @FXML private TableColumn<?, ?> nameColumn;
    @FXML private TableColumn<?, ?> productionDateColumn;
    @FXML private TableColumn<?, ?> expiryDateColumn;

    @FXML private ImageView productImage;
    @FXML private TextField nameField;
    @FXML private TextField productionDateField;
    @FXML private TextField expiryDateField;
    @FXML private TextField price1Field;
    @FXML private TextField price2Field;
    @FXML private TextField price3Field;
    @FXML private TextField unitField;

    @FXML private Button exitButton;
    @FXML private Button updateListButton;

    // Initialize method (called after FXML loading)
    @FXML
    public void initialize() {
        // Initialization code here
        setupTableColumns();
        disableFields();
    }

    private void setupTableColumns() {
        // Configure table columns if needed
    }

    private void disableFields() {
        // All detail fields are non-editable as per FXML
    }

    // Event handler methods
    @FXML
    private void handleReadBarcode() {
        // Handle barcode reading action
    }

    @FXML
    private void handleExitButton() {
        // Handle exit button action
    }

    @FXML
    private void handleUpdateButton() {
        // Handle update list button action
    }
}