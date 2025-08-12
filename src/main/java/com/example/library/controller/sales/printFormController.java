package com.example.library.controller.sales;

import com.example.library.model.Sale;
import com.example.library.model.SaleItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
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
            showAlert(Alert.AlertType.WARNING, "تنبيه", null, "لا توجد عناصر للطباعة!");
            return;
        }

        // Create a new Stage for the print preview
        Stage printStage = new Stage();
        VBox printLayout = createPrintLayout();

        // Wrap in a centered container
        StackPane centeredLayout = new StackPane();
        centeredLayout.setPadding(new Insets(20));
        centeredLayout.getChildren().add(printLayout);
        StackPane.setAlignment(printLayout, Pos.TOP_CENTER);

        Scene printScene = new Scene(centeredLayout);
        printStage.setScene(printScene);

        // Show the stage and wait for it to be rendered
        printStage.show();

        // Print after the stage is shown
        Platform.runLater(() -> {
            printHighQualityNode(printLayout, printStage);
            printStage.close();
        });
    }

    private VBox createPrintLayout() {
        VBox mainContainer = new VBox(5); // تقليل المسافة بين العناصر
        mainContainer.setPadding(new Insets(10));
        mainContainer.setMaxWidth(300);
        mainContainer.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-width: 1;");

        // Header Section
        GridPane headerGrid = new GridPane();
        headerGrid.setAlignment(Pos.CENTER);
        headerGrid.setHgap(10);
        headerGrid.setVgap(5);

        Label titleEng = new Label("Soubirate Kamel kir");
        titleEng.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        Label titleAr = new Label("سوبيرات كمال كير");
        titleAr.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-alignment: center-right;");
        Label addressEng = new Label("Quartier Chouhada Gummar-El Oued");
        addressEng.setStyle("-fx-font-size: 12;");
        Label addressAr = new Label("حي الشهداء -x- قمار-الوادي");
        addressAr.setStyle("-fx-font-size: 12; -fx-alignment: center-right;");

        headerGrid.add(titleEng, 0, 0);
        headerGrid.add(titleAr, 1, 0);
        headerGrid.add(addressEng, 0, 1);
        headerGrid.add(addressAr, 1, 1);

        // Customer Info Section
        GridPane customerInfoGrid = new GridPane();
        customerInfoGrid.setHgap(10);
        customerInfoGrid.setVgap(5);
        customerInfoGrid.addRow(0, new Label("Customer Name:"), new Label(getSafeText(customerNameField)));
        customerInfoGrid.addRow(1, new Label("Customer ID:"), new Label(getSafeText(customerIdField)));
        customerInfoGrid.addRow(2, new Label("Date:"), new Label(getSafeText(issueDateField)));
        customerInfoGrid.addRow(3, new Label("Time:"), new Label(getSafeText(issueTimeField)));
        customerInfoGrid.setStyle("-fx-font-size: 12;");

        // Items Table - Improved with null checks
        GridPane itemsGrid = new GridPane();
        itemsGrid.setHgap(5);
        itemsGrid.setVgap(2);
        itemsGrid.setGridLinesVisible(true);
        itemsGrid.setStyle("-fx-font-size: 12;");

        ColumnConstraints productCol = new ColumnConstraints(150); // المنتج (زاد من 120 إلى 150)
        ColumnConstraints qtyCol = new ColumnConstraints(45);     // الكمية
        ColumnConstraints priceCol = new ColumnConstraints(100);  // السعر (زاد من 80 إلى 100)
        ColumnConstraints totalCol = new ColumnConstraints(120);  // الإجمالي (زاد من 90 إلى 120)

        itemsGrid.getColumnConstraints().addAll(productCol, qtyCol , priceCol, totalCol);

        // Headers (right-to-left)
        itemsGrid.add(new Label("منتج"), 0, 0);     // Product
        itemsGrid.add(new Label("كمية"), 1, 0);     // Quantity
        itemsGrid.add(new Label("سعر"), 2, 0);      // Price
        itemsGrid.add(new Label("إجمالي"), 3, 0);   // Total

        // Add items data with null checks
        int row = 1;
        for (SaleItem item : itemsTableView.getItems()) {
            // الحصول على القيم مع معالجة القيم الفارغة
            String productName = item.getProductName() != null ? item.getProductName() : "";
            String quantity = item.getQuantity() != null ? item.getQuantity() : "";
            String price = item.getPrice() != null ? formatCurrency(item.getPrice()) : "0.00";
            String total = item.getTotalPrice() != null ? formatCurrency(item.getTotalPrice()) : "0.00";

            // إضافة البيانات إلى الجدول
            itemsGrid.add(createDataLabel(productName), 0, row);
            itemsGrid.add(createDataLabel(quantity), 1, row);
            itemsGrid.add(createDataLabel(price), 2, row);
            itemsGrid.add(createDataLabel(total), 3, row);
            row++;
        }

        VBox totalsBox = new VBox(5);
        totalsBox.setAlignment(Pos.CENTER_LEFT);

        // استخراج القيم من الحقول مع معالجة القيم الفارغة
        String subtotal = (subtotalField.getText() != null && !subtotalField.getText().trim().isEmpty()) ?
                subtotalField.getText().replace(" DZ", "").trim() : "0.00 DZ";
        String discount = (discountField.getText() != null && !discountField.getText().trim().isEmpty()) ?
                discountField.getText().replace(" DZ", "").trim() : "0.00 DZ";
        String debt = (debtField.getText() != null && !debtField.getText().trim().isEmpty()) ?
                debtField.getText().replace(" DZ", "").trim() : "0.00 DZ";
        String total = (totalField.getText() != null && !totalField.getText().trim().isEmpty()) ?
                totalField.getText().replace(" DZ", "").trim() : "0.00 DZ";

        // إنشاء التسميات مع تنسيق العملة
        totalsBox.getChildren().addAll(
                new Label("المجموع الفرعي: " + formatCurrency(subtotal)),
                new Label("مبلغ الخصم: " + formatCurrency(discount)),
                new Label("الديون: " + formatCurrency(debt)),
                new Label("المبلغ الإجمالي الكلي: " + formatCurrency(total))
        );
        totalsBox.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        // Footer Section
        VBox footerBox = new VBox(5);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.getChildren().addAll(
                new Label("في حالة طلب ارجاع المنتجات الرجاء التأكد من عدم التأخر في ارجاعها"),
                new Label("المدة لاخر الاجال هي 24 ساعة و لاتقبل في حالة استهلاكها"),
                new Label("S.K.K All rights reserved")
        );
        footerBox.setStyle("-fx-font-size: 10;");

        // Assemble all sections
        mainContainer.getChildren().addAll(
                headerGrid,
                new Separator(),
                customerInfoGrid,
                new Separator(),
                itemsGrid,
                new Separator(),
                totalsBox,
                new Separator(),
                footerBox,
                new Label("شكرا!!!")
        );

        return mainContainer;
    }

    private String getSafeText(TextField field) {
        return field != null && field.getText() != null ? field.getText() : "";
    }

    private Label createDataLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-alignment: center; -fx-padding: 0 5px;");
        return label;
    }

    private void printHighQualityNode(Node node, Stage ownerWindow) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showAlert(Alert.AlertType.ERROR, "Error", null, "Cannot create print job");
            return;
        }

        if (job.showPrintDialog(ownerWindow)) {
            try {
                // 1. Force layout calculation
                node.snapshot(null, null); // Ensures proper layout

                // 2. Get node dimensions including footer
                double nodeWidth = node.getBoundsInParent().getWidth();
                double nodeHeight = node.getBoundsInParent().getHeight();

                // 3. Create paper size (80mm width, dynamic height with footer space)
                double paperWidth = 80; // 80mm standard width
                double paperHeight = (nodeHeight * 0.35) + 20; // Extra 20mm for footer

                // 4. Create custom paper
                Paper thermalPaper = createCustomPaper("RECEIPT", paperWidth, paperHeight);

                // 5. Create layout with minimal margins
                PageLayout layout = job.getPrinter().createPageLayout(
                        thermalPaper,
                        PageOrientation.PORTRAIT,
                        Printer.MarginType.HARDWARE_MINIMUM
                );

                // 6. Calculate scaling (width-only to maintain proportions)
                double scale = layout.getPrintableWidth() / nodeWidth;

                // 7. Create high-quality snapshot
                SnapshotParameters params = new SnapshotParameters();
                params.setTransform(Transform.scale(scale, scale));

                WritableImage image = new WritableImage(
                        (int)(nodeWidth * scale),
                        (int)(nodeHeight * scale)
                );
                node.snapshot(params, image);

                // 8. Prepare print content
                ImageView imageView = new ImageView(image);
                imageView.setSmooth(false); // Better for thermal text

                StackPane printContent = new StackPane(imageView);
                printContent.setStyle("-fx-background-color: white;");

                // 9. Print
                if (job.printPage(layout, printContent)) {
                    job.endJob();
                    showAlert(Alert.AlertType.INFORMATION, "Success", null, "Receipt printed");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", null, "Print failed");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Print Exception", e.getMessage());
            }
        }
    }

    private Paper createCustomPaper(String name, double width, double height) {
        try {
            // Java 8u60+ method
            Constructor<Paper> constructor = Paper.class.getDeclaredConstructor(
                    String.class, double.class, double.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, width, height);
        } catch (Exception e) {
            // Fallback to A4 if custom paper can't be created
            return Paper.A4;
        }
    }


    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.show();
        });
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