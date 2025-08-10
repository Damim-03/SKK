package com.example.library.controller.sales;

import com.example.library.model.Sale;
import com.example.library.model.SaleItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class printFormController {

    @FXML
    private TableView<SaleItem> salesTable;

    // Header Section
    @FXML private Label arabicTitleLabel;
    @FXML private Label englishTitleLabel;
    @FXML private Label arabicAddressLabel;
    @FXML private Label englishAddressLabel;

    @FXML private AnchorPane anchorPaneId;

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
    @FXML private Button printButton;
    @FXML private Button closeButton;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupButtons();
    }

    public void initializeFormData(ObservableList<SaleItem> items,
                                   double subtotal,
                                   String discount,
                                   String debt,
                                   String customerName,
                                   String customerId) {
        // Set customer info
        customerNameField.setText(customerName != null ? customerName : "غير محدد");
        customerIdField.setText(customerId != null ? customerId : "غير محدد");

        // Set current date/time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        issueDateField.setText(LocalDate.now().format(dateFormatter));
        issueTimeField.setText(LocalTime.now().format(timeFormatter));

        // Set table data
        itemsTableView.setItems(items);

        // Calculate and set totals
        double total = subtotal - Double.parseDouble(discount) + Double.parseDouble(debt);
        subtotalField.setText(String.format("%,.2f DZ", subtotal));
        discountField.setText(String.format("%,.2f DZ", Double.parseDouble(discount)));
        debtField.setText(String.format("%,.2f DZ", Double.parseDouble(debt)));
        totalField.setText(String.format("%,.2f DZ", total));
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

    public void setSaleItems(ObservableList<SaleItem> items) {
        if (itemsTableView != null) {
            itemsTableView.setItems(items);
        }
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

        // Similarly for other buttons
        if (printButton != null) {
            printButton.setOnAction(event -> handlePrint());
        }
    }

    public void setSaleData(Sale sale, ObservableList<SaleItem> saleItems) {
        if (sale == null) return;

        // Set customer info
        customerNameField.setText(sale.getCustomerName());
        customerIdField.setText(sale.getCustomerId());

        // Set date/time
        issueDateField.setText(sale.getSaleDate().toString());
        issueTimeField.setText(sale.getSaleTime().toString());

        // Set totals
        subtotalField.setText(String.format("%.2f DZ", sale.getSubtotal()));
        discountField.setText(String.format("%.2f DZ", sale.getDiscount()));
        debtField.setText(String.format("%.2f DZ", sale.getDebt()));
        totalField.setText(String.format("%.2f DZ", sale.getTotal()));

        // Set table data
        itemsTableView.setItems(saleItems);
    }


    public void setSalesData(ObservableList<SaleItem> items, String subtotal, String discount,
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

    @FXML
    private void handlePrint() {
        if (itemsTableView.getItems() == null || itemsTableView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "تنبيه", "لا توجد عناصر للطباعة!");
            return;
        }

        try {
            Node rootNode = anchorPaneId;
            PrinterJob printerJob = PrinterJob.createPrinterJob();

            if (printerJob != null && printerJob.showPrintDialog(rootNode.getScene().getWindow())) {
                Paper a4Paper = Paper.A4;
                PageLayout pageLayout = printerJob.getPrinter().createPageLayout(
                        a4Paper, PageOrientation.PORTRAIT, 0, 0, 0, 0
                );

                double pageWidth = pageLayout.getPrintableWidth();
                double pageHeight = pageLayout.getPrintableHeight();

                double contentWidth = anchorPaneId.getPrefWidth();
                double contentHeight = anchorPaneId.getPrefHeight();

                double scaleX = pageWidth / contentWidth;
                double scaleY = pageHeight / contentHeight;
                double scale = Math.min(scaleX, scaleY);

                // Layout the node fully before snapshot
                anchorPaneId.applyCss();
                anchorPaneId.layout();

                // ✅ Increase DPI for high quality
                final double PRINTER_DPI = 300; // High-quality print resolution
                final double SCREEN_DPI = 96;   // Typical screen DPI
                double scaleFactor = PRINTER_DPI / SCREEN_DPI;

                SnapshotParameters params = new SnapshotParameters();
                params.setTransform(Transform.scale(scale * scaleFactor, scale * scaleFactor));

                // Create high-res image
                WritableImage image = new WritableImage(
                        (int) Math.round(contentWidth * scale * scaleFactor),
                        (int) Math.round(contentHeight * scale * scaleFactor)
                );
                anchorPaneId.snapshot(params, image);

                // Create image view for printing
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(pageWidth);
                imageView.setFitHeight(pageHeight);

                // Print
                boolean success = printerJob.printPage(pageLayout, imageView);
                if (success) {
                    printerJob.endJob();
                    showAlert(Alert.AlertType.INFORMATION, "نجاح", "تمت الطباعة بنجاح");
                    ((Stage) printButton.getScene().getWindow()).close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "خطأ", "فشل عملية الطباعة");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "تنبيه", "تم إلغاء الطباعة");
            }




        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "خطأ", "حدث خطأ أثناء الطباعة: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        alert.showAndWait();
    }

    // Getters for fields if needed by other controllers
    public TextField getCustomerIdField() {
        return customerIdField;
    }

    public TextField getCustomerNameField() {
        return customerNameField;
    }
}