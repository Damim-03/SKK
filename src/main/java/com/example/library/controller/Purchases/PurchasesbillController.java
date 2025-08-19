package com.example.library.controller.Purchases;

import com.example.library.model.SaleItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PurchasesbillController {

    // Header Section
    @FXML private Label arabicTitleLabel;
    @FXML private Label englishTitleLabel;
    @FXML private Label arabicAddressLabel;
    @FXML private Label englishAddressLabel;

    // Customer Information
    @FXML private Label customerNameLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerIdField;

    // Date/Time
    @FXML private Label issueDateLabel;
    @FXML private TextField issueDateField;
    @FXML private Label issueTimeLabel;
    @FXML private TextField issueTimeField;

    // Items Table
    @FXML private TableView<SaleItem> itemsTableView;
    @FXML private TableColumn<SaleItem, String> idColumn;
    @FXML private TableColumn<SaleItem, String> productIdColumn;
    @FXML private TableColumn<SaleItem, String> productNameColumn;
    @FXML private TableColumn<SaleItem, String> quantityColumn;
    @FXML private TableColumn<SaleItem, String> priceColumn;
    @FXML private TableColumn<SaleItem, String> totalPriceColumn;

    // Totals Section
    @FXML private Label subtotalLabel;
    @FXML private TextField subtotalField;
    @FXML private Label discountLabel;
    @FXML private TextField discountField;
    @FXML private Label debtLabel;
    @FXML private TextField debtField;
    @FXML private Label totalLabel;
    @FXML private TextField totalField;

    // Footer Section
    @FXML private Label returnPolicyLabel;
    @FXML private Label returnDeadlineLabel;
    @FXML private Label copyrightLabel;

    // Action Buttons
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupButtons();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Format currency columns
        priceColumn.setCellFactory(col -> formatCurrencyCell());
        totalPriceColumn.setCellFactory(col -> formatCurrencyCell());
    }

    private TableCell<SaleItem, String> formatCurrencyCell() {
        return new TableCell<SaleItem, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    try {
                        double amount = Double.parseDouble(value);
                        setText(String.format("%.2f DZ", amount));
                    } catch (NumberFormatException e) {
                        setText(value);
                    }
                }
            }
        };
    }

    private void setupButtons() {
        if (closeButton != null) {  // Add null check
            closeButton.setOnAction(event -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });
        }
    }


    public void setPurchaseData(ObservableList<SaleItem> items, String subtotal, String discount,
                             String debt, String total, String date, String time,
                             String customerName, String customerId) {
        itemsTableView.setItems(items);

        // Format currency values
        subtotalField.setText(formatCurrency(subtotal));
        discountField.setText(formatCurrency(discount));
        debtField.setText(formatCurrency(debt));
        totalField.setText(formatCurrency(total));

        // Set date/time (use current if not provided)
        issueDateField.setText(date != null ? date : LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        issueTimeField.setText(time != null ? time : LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Set customer info
        customerNameField.setText(customerName != null ? customerName : "غير محدد");
        customerIdField.setText(customerId != null ? customerId : "غير محدد");
    }

    private String formatCurrency(String value) {
        try {
            double amount = Double.parseDouble(value);
            return String.format("%.2f DZ", amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    // Getters for fields if needed by other controllers
    public TextField getCustomerIdField() {
        return customerIdField;
    }

    public TextField getCustomerNameField() {
        return customerNameField;
    }
}
