package com.example.library.controller.sales;

import com.example.library.model.Sale;
import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class rapotsController {
    @FXML
    private TableView<SaleItem> salesTable;

    // Current sale information
    private int currentSaleId;
    private ObservableList<SaleItem> currentSaleItems;

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

    @FXML private Button printButton;

    // Footer Section
    @FXML private Label returnPolicyLabel;
    @FXML private Label returnDeadlineLabel;
    @FXML private Label copyrightLabel;

    @FXML private AnchorPane anchorPaneId;

    // Action Buttons
    @FXML private Button closeButton;
    @FXML private Button deleteButton;
    @FXML private Button cancelButton;

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
        if (closeButton != null) {
            closeButton.setOnAction(event -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });
        }

        if (printButton != null) {
            printButton.setOnAction(event -> handlePrint());
        }

        if (deleteButton != null) {
            deleteButton.setOnAction(event -> handleDelete());
        }

        if (cancelButton != null) {
            cancelButton.setOnAction(event -> handleCancel());
        }
    }

    public void setSaleData(Sale sale, ObservableList<SaleItem> saleItems) {
        if (sale == null) return;

        this.currentSaleId = sale.getSaleId();
        this.currentSaleItems = saleItems;

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

    @FXML
    private void handleDelete() {
        if (currentSaleId == 0) {
            showAlert(Alert.AlertType.WARNING, "خطأ", null, "لا يوجد فاتورة محددة للحذف.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد أنك تريد حذف هذه الفاتورة؟");

        ButtonType approveButton = new ButtonType("حذف", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(approveButton, cancelButton);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == approveButton) {
            try {
                // Delete from database
                deleteSaleFromDatabase(currentSaleId);

                // Refresh the table by clearing and reloading data if needed
                itemsTableView.getItems().clear();
                itemsTableView.refresh();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "تم الحذف", null,
                        "تم حذف الفاتورة بنجاح.");

                // Close the current window
                Stage stage = (Stage) deleteButton.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "خطأ", "فشل في الحذف",
                        "حدث خطأ أثناء حذف الفاتورة: " + e.getMessage());
            }
        }
    }

    private void deleteSaleFromDatabase(int saleId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete sale items first
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM sale_items WHERE sale_id = ?")) {
                    stmt.setInt(1, saleId);
                    stmt.executeUpdate();
                }

                // Then delete the sale
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM sales WHERE sale_id = ?")) {
                    stmt.setInt(1, saleId);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void clearSaleData() {
        currentSaleId = 0;
        if (currentSaleItems != null) {
            currentSaleItems.clear();
        }
        itemsTableView.getItems().clear();
        customerNameField.clear();
        customerIdField.clear();
        issueDateField.clear();
        issueTimeField.clear();
        subtotalField.clear();
        discountField.clear();
        debtField.clear();
        totalField.clear();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePrint() {
        if (itemsTableView.getItems() == null || itemsTableView.getItems().isEmpty()) {
            showWarningAlert("تنبيه", "لا توجد عناصر للطباعة!");
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
                    showSuccessAlert("نجاح", "تمت الطباعة بنجاح");
                    ((Stage) printButton.getScene().getWindow()).close();
                } else {
                    showFailedAlert("خطأ", "فشل عملية الطباعة");
                }
            } else {
                showWarningAlert("تنبيه", "تم إلغاء الطباعة");
            }




        } catch (Exception e) {
            showFailedAlert( "خطأ", "حدث خطأ أثناء الطباعة: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/success.png")));
        alert.getDialogPane().setStyle("-fx-background-color: #e8f5e9;");
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/warning.png")));
        alert.getDialogPane().setStyle("-fx-background-color: #fff8e1;");
        alert.showAndWait();
    }

    private void showFailedAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/fail.png")));
        alert.getDialogPane().setStyle("-fx-background-color: #ffebee;");
        alert.showAndWait();
    }
}