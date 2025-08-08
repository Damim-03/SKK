package com.example.library.controller.sales;

import com.example.library.model.SaleItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class billController {

    @FXML private Label arabicTitleLabel;
    @FXML private Label englishTitleLabel;
    @FXML private Label arabicAddressLabel;
    @FXML private Label englishAddressLabel;
    @FXML private Label customerNameLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerIdField;
    @FXML private Label issueDateLabel;
    @FXML private TextField issueDateField;
    @FXML private Label issueTimeLabel;
    @FXML private TextField issueTimeField;
    @FXML private TableView<SaleItem> itemsTableView;
    @FXML private TableColumn<SaleItem, String> idColumn;
    @FXML private TableColumn<SaleItem, String> productIdColumn;
    @FXML private TableColumn<SaleItem, String> productNameColumn;
    @FXML private TableColumn<SaleItem, String> quantityColumn;
    @FXML private TableColumn<SaleItem, String> priceColumn;
    @FXML private TableColumn<SaleItem, String> totalPriceColumn;
    @FXML private Label subtotalLabel;
    @FXML private TextField subtotalField;
    @FXML private Label discountLabel;
    @FXML private TextField discountField;
    @FXML private Label debtLabel;
    @FXML private TextField debtField;
    @FXML private Label totalLabel;
    @FXML private TextField totalField;
    @FXML private Label returnPolicyLabel;
    @FXML private Label returnDeadlineLabel;
    @FXML private Label copyrightLabel;

    @FXML
    public void initialize() {
        // Set up table columns
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (productIdColumn != null) productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        if (productNameColumn != null) productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        if (quantityColumn != null) quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        if (priceColumn != null) priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        if (totalPriceColumn != null) totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }

    public void setSalesData(ObservableList<SaleItem> items, String subtotal, String discount, String debt, String total, String date, String time, String customerName, String customerId) {
        if (itemsTableView != null) itemsTableView.setItems(items);
        if (subtotalField != null) subtotalField.setText(subtotal);
        if (discountField != null) discountField.setText(discount);
        if (debtField != null) debtField.setText(debt);
        if (totalField != null) totalField.setText(total);
        if (issueDateField != null) issueDateField.setText(date);
        if (issueTimeField != null) issueTimeField.setText(time);
        if (customerNameField != null) customerNameField.setText(customerName);
        if (customerIdField != null) customerIdField.setText(customerId);
    }
}