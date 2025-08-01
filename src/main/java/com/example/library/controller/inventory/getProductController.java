package com.example.library.controller.inventory;

import com.example.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class getProductController {

    @FXML
    public Button backButton;
    @FXML
    private TextField barcodeField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField productionDateField;

    @FXML
    private TextField expiryDateField;

    @FXML
    private ImageView productImage;

    @FXML
    private Button editButton;

    @FXML
    private Button detailsButton;

    @FXML
    private Button printButton;

    @FXML
    private Button deleteButton;

    @FXML
    public void handleBackButtonAction (ActionEvent event) {
        try {
            App.setRoot("interfaces/inventory/inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEditButtonAction (ActionEvent event) {

    }
    @FXML
    public void handleDetailsButtonAction (ActionEvent event) {

    }
    @FXML
    public void handlePrintButtonAction (ActionEvent event) {

    }
    @FXML
    public void handleDeleteButtonAction (ActionEvent event) {

    }

}
