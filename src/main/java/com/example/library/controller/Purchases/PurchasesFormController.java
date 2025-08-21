package com.example.library.controller.Purchases;

import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import static com.example.library.Alert.alert.*;

public class PurchasesFormController {
    @FXML
    private TableView<SaleItem> salesTable;
    @FXML private TableColumn<SaleItem, String> colNumber;
    @FXML private TableColumn<SaleItem, String> colBarcode;
    @FXML private TableColumn<SaleItem, String> colProductName;
    @FXML private TableColumn<SaleItem, String> colQuantity;
    @FXML private TableColumn<SaleItem, String> colPrice;
    @FXML private TableColumn<SaleItem, String> colTotalPrice;

    @FXML private TextField productSearchField;
    @FXML private TextField subtotalField, discountField, debtField, totalField;
    @FXML private TextField dateField, timeField;
    @FXML private ImageView productImageView;

    @FXML private Button addButton, billButton, printButton, saveButton, clearListButton, refreshButton;
    @FXML private Button billsReportButton, searchButton, helpButton, calculButton, deleteButton, editButton, payButton;
    @FXML private Button barcodeButton;

    @FXML private TextField debtField2;

    @FXML private MenuButton taxMenuButton;

    @FXML private Label discountRateLabel;
    @FXML private MenuButton discountMenuButton;

    @FXML private TextField ArabicTotalField;
    @FXML private TextField quantityField;


    @FXML private MenuItem taxMenuItem1, taxMenuItem2, discountMenuItem1, discountMenuItem2;

    private final ObservableList<SaleItem> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        salesTable.setItems(productList);

        // Setup event handlers
        setupEventHandlers();

        // Initialize UI components
        initializeUIComponents();

        // Setup listeners
        setupListeners();

        // Update initial display
        updateDateTime();
        setupTaxMenu();
        setupDiscountMenu();
        setupDebtField();
    }

    private void setupEventHandlers() {
        if (productSearchField != null) productSearchField.setOnAction(e -> handleBarcodeScan());
        if (addButton != null) addButton.setOnAction(e -> handleAddButtonAction());
        if (billButton != null) billButton.setOnAction(e -> handleBill());
        if (printButton != null) printButton.setOnAction(e -> handlePrintButton());
        if (saveButton != null) saveButton.setOnAction(e -> handleSave());
        if (clearListButton != null) clearListButton.setOnAction(e -> handleClearList());
        if (refreshButton != null) refreshButton.setOnAction(e -> handleRefresh());
        if (billsReportButton != null) billsReportButton.setOnAction(e -> handleBillsReport());
        if (searchButton != null) searchButton.setOnAction(e -> handleSearch());
        if (helpButton != null) helpButton.setOnAction(e -> handleHelp());
        if (calculButton != null) calculButton.setOnAction(e -> handleCalculator());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete());
        if (editButton != null) editButton.setOnAction(e -> handleEdit());
        if (payButton != null) payButton.setOnAction(e -> handleconfigPurchase());
        if (barcodeButton != null) barcodeButton.setOnAction(e -> handleBarcodeButtonAction());

        if (taxMenuItem1 != null) taxMenuItem1.setOnAction(e -> handleTaxSelection(5));
        if (taxMenuItem2 != null) taxMenuItem2.setOnAction(e -> handleTaxSelection(10));
        if (discountMenuItem1 != null) discountMenuItem1.setOnAction(e -> handleDiscountSelection(5));
        if (discountMenuItem2 != null) discountMenuItem2.setOnAction(e -> handleDiscountSelection(10));
    }

    private void initializeUIComponents() {
        ArabicTotalField.setStyle("-fx-font-weight: bold; -fx-background-color: #faa2ea; -fx-font-size: 17px;");
        ArabicTotalField.setText("صفر دينار جزائري");
        productSearchField.requestFocus();
    }

    private void setupListeners() {
        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                quantityField.setText(newSelection.getQuantity());
                productSearchField.setText(newSelection.getBarcode());
                loadProductImage(newSelection.getBarcode());
            } else {
                quantityField.clear();
                productSearchField.clear();
                productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
            }
        });

        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            SaleItem selectedItem = salesTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && newVal.matches("\\d+")) {
                try {
                    int newQuantity = Integer.parseInt(newVal);
                    double price = Double.parseDouble(selectedItem.getPrice());
                    selectedItem.setQuantity(String.valueOf(newQuantity));
                    selectedItem.setTotal(String.format("%.2f", price * newQuantity));
                    salesTable.refresh();
                    updateTotals();
                } catch (NumberFormatException e) {
                    quantityField.setText(oldVal);
                }
            }
        });

        debtField2.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
        debtField.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
        discountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
    }

    private void loadProductImage(String barcode) {
        String query = "SELECT image_path FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String imagePath = rs.getString("image_path");
                if (imagePath != null && !imagePath.isEmpty()) {
                    File file = new File(imagePath);
                    productImageView.setImage(file.exists() ?
                            new Image(file.toURI().toString()) :
                            new Image(getClass().getResource("/images/image_not_found.png").toExternalForm()));
                } else {
                    productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
                }
            }
        } catch (SQLException e) {
            showFailedAlert("فشل", "تعذر تحميل الصورة!");
        }
    }

    private void setupDebtField() {
        debtField2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("\\d*(\\.\\d{0,2})?")) {
                debtField2.setText(oldValue != null ? oldValue : "");
            } else {
                try {
                    double value = newValue.isEmpty() ? 0.0 : Double.parseDouble(newValue);
                    debtField.setText(String.format("%.2f DZ", value)); // Update debtField with formatted value
                    updateTotals(); // Recalculate totals
                } catch (NumberFormatException e) {
                    debtField.setText("0.00 DZ");
                    debtField2.setText("0.00");
                }
            }
        });

        debtField2.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                formatDebtField();
            }
        });
    }

    private void formatDebtField() {
        if (debtField2.getText().isEmpty()) {
            debtField2.setText("0.00");
            debtField.setText("0.00 DZ");
        } else {
            try {
                double value = Double.parseDouble(debtField2.getText());
                debtField2.setText(String.format("%.2f", value));
                debtField.setText(String.format("%.2f DZ", value));
            } catch (NumberFormatException e) {
                debtField2.setText("0.00");
                debtField.setText("0.00 DZ");
            }
        }
        updateTotals(); // Ensure totals reflect the formatted value
    }

    private void setupTaxMenu() {
        taxMenuButton.getItems().clear();
        MenuItem tax5 = new MenuItem("5%");
        tax5.setOnAction(e -> handleTaxSelection(5));
        MenuItem tax10 = new MenuItem("10%");
        tax10.setOnAction(e -> handleTaxSelection(10));
        MenuItem custom = new MenuItem("ادخال...");
        custom.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("نسبة الشحن");
            dialog.setHeaderText("ادخل نسبة الشحن");
            dialog.setContentText("الشحن %:");
            dialog.showAndWait().ifPresent(value -> {
                try {
                    // استبدال أي فاصلة بنقطة لضمان التحليل الصحيح
                    String normalizedValue = value.replace(',', '.');
                    double tax = Double.parseDouble(normalizedValue);
                    handleTaxSelection(tax);
                } catch (NumberFormatException ex) {
                    showWarningAlert("تنبيه", "يرجى إدخال نسبة الشحن صحيحة (مثال: 5.5 أو 5,5)!");
                }
            });
        });
        taxMenuButton.getItems().addAll(tax5, tax10, custom);
        taxMenuButton.setText("0%");
    }

    private void setupDiscountMenu() {
        discountMenuButton.getItems().clear();
        MenuItem discount5 = new MenuItem("5%");
        discount5.setOnAction(e -> handleDiscountSelection(5));
        MenuItem discount10 = new MenuItem("10%");
        discount10.setOnAction(e -> handleDiscountSelection(10));
        MenuItem custom = new MenuItem("ادخال...");
        custom.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("اختر الخصم");
            dialog.setHeaderText("ادخل نسبة الخصم");
            dialog.setContentText("الخصم %:");
            dialog.showAndWait().ifPresent(value -> {
                try {
                    // استبدال الفواصل بنقاط إذا كانت مدخلة بالعربية
                    String normalizedValue = value.replace(',', '.');
                    double discount = Double.parseDouble(normalizedValue);
                    handleDiscountSelection(discount);
                } catch (NumberFormatException ex) {
                    showWarningAlert("تنبيه", "يرجى إدخال قيمة خصم صحيحة!");
                }
            });
        });
        discountMenuButton.getItems().addAll(discount5, discount10, custom);
        discountMenuButton.setText("0%");
    }

    private void handleTaxSelection(double percent) {
        taxMenuButton.setText(percent + "%");
        updateTotals();
    }

    private void handleDiscountSelection(double percent) {
        discountMenuButton.setText(percent + "%");
        discountRateLabel.setText("معدل الخصم :");
        updateTotals();
    }

    public void refreshTable() {
        try {
            salesTable.refresh();
            showSuccessAlert("نجاح", "تم تحديث القائمة!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر تحديث القائمة!");
        }
    }

    public void updateTotals() {
        double subtotal = 0.0;
        for (SaleItem item : productList) {
            try {
                subtotal += Double.parseDouble(item.getTotalPrice());
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }

        double flatDiscount = parseDoubleOrZero(discountField.getText());
        flatDiscount = Math.min(flatDiscount, subtotal);

        double debt = parseDoubleOrZero(debtField.getText());
        double shipping = parseDoubleOrZero(debtField2.getText());

        double taxRate = parseDoubleOrZero(taxMenuButton.getText().replace("%", ""));
        taxRate = Math.min(taxRate, 100);
        double taxAmount = subtotal * taxRate / 100;

        double discountRate = parseDoubleOrZero(discountMenuButton.getText().replace("%", ""));
        discountRate = Math.min(discountRate, 100);
        double discountAmount = subtotal * discountRate / 100;

        double total = subtotal - flatDiscount - discountAmount + debt + shipping + taxAmount;
        total = Math.max(total, 0);

        discountField.setText(String.format("%.2f DZ", discountAmount));
        subtotalField.setText(String.format("%.2f DZ", subtotal));
        totalField.setText(String.format("%.2f DZ", total));
        ArabicTotalField.setText(amountToArabicWords(total));
    }

    private double parseDoubleOrZero(String text) {
        try {
            return text.isEmpty() ? 0.0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String amountToArabicWords(double amount) {
        long dinars = (long) amount;
        int centimes = (int) Math.round((amount - dinars) * 100);

        String dinarsWords = (dinars == 0) ? "صفر" : integerToArabicWords(dinars);
        String result = dinarsWords + " " + getCurrencyWord(dinars, "دينار جزائري", "ديناران جزائري", "دنانير جزائري", "دينارا جزائريً");

        if (centimes > 0) {
            String centWords = integerToArabicWords(centimes);
            result += " و " + centWords + " " + getCurrencyWord(centimes, "سنتيم", "سنتيمان", "سنتيمات", "سنتيماً");
        }

        return result;
    }

    private String integerToArabicWords(long number) {
        if (number == 0) return "صفر";

        String[] units = {"", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
                "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"};
        String[] tens = {"", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"};
        String[] hundreds = {"", "مائة", "مئتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"};

        StringBuilder words = new StringBuilder();

        long billion = number / 1_000_000_000L;
        if (billion > 0) {
            words.append(threeDigitsToWords((int) billion, units, tens, hundreds))
                    .append(" ")
                    .append(billion == 1 ? "مليار" : (billion == 2 ? "ملياران" : "مليارات"))
                    .append(" ");
            number %= 1_000_000_000L;
        }

        long million = number / 1_000_000L;
        if (million > 0) {
            words.append(threeDigitsToWords((int) million, units, tens, hundreds))
                    .append(" ")
                    .append(million == 1 ? "مليون" : (million == 2 ? "مليونان" : "ملايين"))
                    .append(" ");
            number %= 1_000_000L;
        }

        long thousand = number / 1000L;
        if (thousand > 0) {
            words.append(thousand == 1 ? "ألف" :
                            thousand == 2 ? "ألفان" :
                                    threeDigitsToWords((int) thousand, units, tens, hundreds) + " ألف")
                    .append(" ");
            number %= 1000L;
        }

        if (number > 0) {
            words.append(threeDigitsToWords((int) number, units, tens, hundreds));
        }

        return words.toString().trim().replaceAll("\\s+", " ");
    }

    private String threeDigitsToWords(int num, String[] units, String[] tens, String[] hundreds) {
        StringBuilder part = new StringBuilder();
        if (num >= 100) {
            part.append(hundreds[num / 100]);
            num %= 100;
            if (num > 0) part.append(" و ");
        }

        if (num > 0) {
            if (num < 20) {
                part.append(units[num]);
            } else {
                int t = num / 10;
                int u = num % 10;
                if (u > 0) {
                    part.append(units[u]).append(" و ").append(tens[t]);
                } else {
                    part.append(tens[t]);
                }
            }
        }

        return part.toString().trim();
    }

    private String getCurrencyWord(long number, String singular, String dual, String plural, String accusativeSingular) {
        if (number == 0) return plural;
        if (number == 1) return accusativeSingular;
        if (number == 2) return dual;
        if (number >= 3 && number <= 10) return plural;
        return singular;
    }

    @FXML
    private void handleBarcodeScan() {
        String barcode = productSearchField.getText();
        if (barcode == null || barcode.trim().isEmpty()) {
            showWarningAlert("تنبيه", "يرجى إدخال رمز الباركود!");
            return;
        }

        SaleItem scannedItem = getProductByBarcode(barcode);
        if (scannedItem != null) {
            for (SaleItem item : productList) {
                if (item.getProductId().equals(scannedItem.getProductId())) {
                    int currentQty = Integer.parseInt(item.getQuantity());
                    double unitPrice = Double.parseDouble(item.getPrice());
                    currentQty++;
                    item.setQuantity(String.valueOf(currentQty));
                    item.setTotal(String.format("%.2f", currentQty * unitPrice));
                    salesTable.refresh();
                    updateTotals();
                    productSearchField.clear();
                    return;
                }
            }
            productList.add(scannedItem);
            updateTotals();
        } else {
            showFailedAlert("فشل", "لم يتم العثور على المنتج!");
        }
        productSearchField.clear();
        quantityField.clear();
    }

    private SaleItem getProductByBarcode(String barcode) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String number = String.valueOf(productList.size() + 1);
                return new SaleItem(
                        number,
                        rs.getString("id"),
                        rs.getString("product_name"),
                        rs.getString("barcode"),
                        "1",
                        String.format("%.2f", rs.getDouble("price1")),
                        String.format("%.2f", rs.getDouble("price1"))
                );
            }
        } catch (SQLException e) {
            showFailedAlert("فشل", "خطأ في قاعدة البيانات: " + e.getMessage());
        }
        return null;
    }

    public void addProductToTable(String name, String barcode, double price, int quantity, double total, boolean isFromDatabase) {
        String number = String.valueOf(productList.size() + 1);

        String productId = isFromDatabase ? getProductIdFromDatabase(barcode) : barcode;

        SaleItem newItem = new SaleItem(
                number,
                productId,  // Use the correct ID based on source
                name,
                barcode,    // Always preserve the original barcode
                String.valueOf(quantity),
                String.format("%.2f", price),
                String.format("%.2f", total)
        );

        productList.add(newItem);
        updateTotals();
        productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
    }

    private String getProductIdFromDatabase(String barcode) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM products WHERE barcode = ?")) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("id");  // Return the UUID from database
            }
            return barcode; // Fallback if not found
        } catch (SQLException e) {
            e.printStackTrace();
            return barcode; // Fallback on error
        }
    }

    @FXML
    private void handleBarcodeButtonAction() {
        try {
            productSearchField.clear();
            productSearchField.requestFocus();
            showSuccessAlert("نجاح", "تم إفراغ حقل الباركود!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر إفراغ حقل الباركود!");
        }
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/CRUD/addproductpurchases.fxml"));
            Parent root = loader.load();
            addProductTablePurchasesController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("إضافة منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/addproduct.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة إضافة المنتج: " + e.getMessage());
        }
    }

    @FXML
    private void handleBill() {
        try {
            // Prompt for customer details if not already set
            String customerName = showInputDialog("اسم العميل");
            String customerId = showInputDialog("رقم العميل");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/Form/bill.fxml"));
            Parent root = loader.load();

            // Get the bill controller
            PurchasesbillController controller = loader.getController();

            // Prepare data to pass
            ObservableList<SaleItem> items = productList; // Current items from salesTable
            String subtotal = subtotalField.getText();    // Current subtotal
            String discount = discountField.getText();    // Current discount
            String debt = debtField.getText();            // Current debt
            String total = totalField.getText();          // Current total
            String date = dateField.getText();            // Current date (e.g., "2025-08-09")
            String time = timeField.getText();            // Current time (e.g., "00:56")

            // Set the data in the bill controller
            controller.setPurchaseData(items, subtotal, discount, debt, total, date, time, customerName, customerId);

            Stage stage = new Stage();
            stage.setTitle("الفاتورة");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/bill.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة الفاتورة: " + e.getMessage());
        }
    }

    private String generate13DigitBarcode() {
        // Use current timestamp and append a random digit to ensure 13 digits
        long timestamp = System.currentTimeMillis();
        int randomDigit = (int) (Math.random() * 10); // Random digit 0-9
        String barcode = String.format("%012d%d", timestamp % 1000000000000L, randomDigit); // 12 digits from timestamp + 1 random digit
        return barcode.substring(0, 13); // Ensure exactly 13 digits
    }

    private String showInputDialog(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("إدخال بيانات");
        dialog.setHeaderText(null);

        // Create custom layout
        VBox content = new VBox(10);
        Label label = new Label(prompt + ":");
        TextField editor = dialog.getEditor();

        // Add barcode button only for "رقم العميل"
        if (prompt.equals("رقم العميل")) {
            Button generateBarcodeButton = new Button("توليد باركود");
            generateBarcodeButton.setOnAction(e -> {
                String generatedBarcode = generate13DigitBarcode();
                editor.setText(generatedBarcode);
            });
            content.getChildren().addAll(label, editor, generateBarcodeButton);
        } else {
            content.getChildren().addAll(label, editor);
        }

        // Set the custom content, replacing the default
        dialog.getDialogPane().setContent(content);

        return dialog.showAndWait().orElse("غير محدد");
    }

    @FXML
    private void handlePrintButton() {
        try {
            // Prompt for customer details if not already set
            String customerName = showInputDialog("اسم العميل");
            String customerId = showInputDialog("رقم العميل");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/Form/printPurchasesForm.fxml"));
            Parent root = loader.load();

            // Get the bill controller
            printFormPurchasesController controller = loader.getController();

            // Prepare data to pass
            ObservableList<SaleItem> items = productList; // Current items from salesTable
            String subtotal = subtotalField.getText();    // Current subtotal
            String discount = discountField.getText();    // Current discount
            String debt = debtField.getText();            // Current debt
            String total = totalField.getText();          // Current total
            String date = dateField.getText();            // Current date (e.g., "2025-08-09")
            String time = timeField.getText();            // Current time (e.g., "00:56")

            // Set the data in the bill controller
            controller.setSalesData(items, subtotal, discount, debt, total, date, time, customerName, customerId);

            Stage stage = new Stage();
            stage.setTitle("الفاتورة");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/bill.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة الطباعة: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (productList.isEmpty()) {
            showWarningAlert("تنبيه", "لا توجد منتجات لحفظها!");
            return;
        }

        try {
            // Prompt for customer details
            String customerName = showInputDialog("اسم العميل");
            String customerId = showInputDialog("رقم العميل");

            if (customerName == null || customerName.isEmpty() ||
                    customerId == null || customerId.isEmpty()) {
                showWarningAlert("تنبيه", "الرجاء إدخال جميع بيانات العميل!");
                return;
            }

            // Get and format values from UI fields
            String subtotalText = subtotalField.getText().replace(" DZ", "").trim();
            String discountText = discountField.getText().replace(" DZ", "").trim();
            String debtText = debtField.getText().replace(" DZ", "").trim();
            String totalText = totalField.getText().replace(" DZ", "").trim();

            // Parse values safely
            double subtotal = parseDoubleSafely(subtotalText);
            double discount = parseDoubleSafely(discountText);
            double debt = parseDoubleSafely(debtText);
            double total = parseDoubleSafely(totalText);

            // Get current date/time
            String date = LocalDate.now().toString();
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Show bill preview before saving
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/Form/bill.fxml"));
            Parent root = loader.load();

            // Get controller and set data with formatted values
            PurchasesbillController controller = loader.getController();
            controller.setPurchaseData(
                    productList,
                    formatCurrency(subtotal),
                    formatCurrency(discount),
                    formatCurrency(debt),
                    formatCurrency(total),
                    date,
                    time,
                    customerName,
                    customerId
            );

            // Create and show the bill stage
            Stage billStage = new Stage();
            billStage.setTitle("معاينة الفاتورة");
            billStage.setScene(new Scene(root));
            billStage.initModality(Modality.APPLICATION_MODAL);
            billStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/bill.png")));
            billStage.setResizable(false);

            // Add confirmation button to the bill view
            Button confirmButton = new Button("تأكيد الحفظ");
            confirmButton.setStyle("-fx-font-size: 16; -fx-padding: 10 20;");
            confirmButton.setOnAction(e -> {
                try {
                    savepurchaseToDatabase(
                            customerId,
                            customerName,
                            subtotal,
                            discount,
                            debt,
                            total,
                            date,
                            time
                    );
                    showSuccessAlert("نجاح", "تم حفظ الفاتورة بنجاح!");
                    billStage.close();
                } catch (Exception ex) {
                    showFailedAlert("خطأ", "تعذر حفظ الفاتورة: " + ex.getMessage());
                }
            });

            // Add button to bill view - safer approach
            if (root instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) root;
                Parent content = (Parent) scrollPane.getContent();

                if (content instanceof AnchorPane) {
                    AnchorPane anchorContent = (AnchorPane) content;
                    anchorContent.getChildren().add(confirmButton);
                    AnchorPane.setBottomAnchor(confirmButton, 20.0);
                    AnchorPane.setRightAnchor(confirmButton, 20.0);
                } else {
                    // Fallback if content isn't AnchorPane
                    VBox container = new VBox(content, confirmButton);
                    scrollPane.setContent(container);
                }
            } else if (root instanceof AnchorPane) {
                ((AnchorPane) root).getChildren().add(confirmButton);
                AnchorPane.setBottomAnchor(confirmButton, 20.0);
                AnchorPane.setRightAnchor(confirmButton, 20.0);
            } else {
                throw new RuntimeException("Unsupported root container type in bill.fxml");
            }

            billStage.showAndWait();

        } catch (Exception e) {
            showFailedAlert("خطأ", "تعذر حفظ الفاتورة: " + e.getMessage());
        }
    }


    private String formatCurrency(double value) {
        return String.format(Locale.US, "%.2f DZ", value);
    }


    private double parseDoubleSafely(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseIntSafely(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void savepurchaseToDatabase(String customerId, String customerName,
                                    double subtotal, double discount,
                                    double debt, double total,
                                    String date, String time) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Insert sale header
            String salesQuery = "INSERT INTO purchases (purchase_date, purchase_time, subtotal, discount, debt, total, customer_name, customer_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement salesStmt = conn.prepareStatement(salesQuery, Statement.RETURN_GENERATED_KEYS)) {
                // Parse date and time from strings
                LocalDate saleDate = LocalDate.parse(date);
                LocalTime saleTime = LocalTime.parse(time);

                salesStmt.setDate(1, Date.valueOf(saleDate));
                salesStmt.setTime(2, Time.valueOf(saleTime));
                salesStmt.setDouble(3, subtotal);
                salesStmt.setDouble(4, discount);
                salesStmt.setDouble(5, debt);
                salesStmt.setDouble(6, total);
                salesStmt.setString(7, customerName);
                salesStmt.setString(8, customerId);

                salesStmt.executeUpdate();

                // 2. Get generated ID and insert items
                try (ResultSet rs = salesStmt.getGeneratedKeys();
                     PreparedStatement itemsStmt = conn.prepareStatement(
                             "INSERT INTO purchase_items (purchase_id, product_id, number, product_name, quantity, price, total_price) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?)")) {  // Fixed: 7 parameters now

                    if (!rs.next()) throw new SQLException("Failed to get sale ID");
                    int purchaseId = rs.getInt(1);

                    int itemNum = 1;
                    for (SaleItem item : productList) {
                        itemsStmt.setInt(1, purchaseId);
                        itemsStmt.setString(2, item.getProductId());  // product_id
                        itemsStmt.setString(3, String.valueOf(itemNum++));  // number
                        itemsStmt.setString(4, item.getProductName());  // product_name
                        itemsStmt.setInt(5, parseIntSafely(item.getQuantity()));  // quantity
                        itemsStmt.setDouble(6, parseDoubleSafely(item.getPrice()));  // price
                        itemsStmt.setDouble(7, parseDoubleSafely(item.getTotalPrice()));  // total_price
                        itemsStmt.addBatch();
                    }
                    itemsStmt.executeBatch();
                }
                conn.commit();
            }
        } catch (SQLException e) {
            showFailedAlert("فشل", "تعذر حفظ الفاتورة: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showFailedAlert("خطأ", "حدث خطأ غير متوقع: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearList() {
        try {
            productList.clear();
            updateTotals();
            productSearchField.clear();
            quantityField.clear();
            productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
            productSearchField.requestFocus();
            debtField.clear();
            debtField2.clear();

            // Reset labels
            // Reset discount and tax to 0%
            discountMenuButton.setText("0%");
            taxMenuButton.setText("0%");


            showSuccessAlert("نجاح", "تم مسح القائمة بنجاح!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر مسح القائمة: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        salesTable.refresh();
        // Reset discount and tax to 0%
        discountMenuButton.setText("0%");
        taxMenuButton.setText("0%");
        debtField.clear();
        debtField2.clear();
        updateTotals();
        productSearchField.clear();
        quantityField.clear();
        salesTable.getSelectionModel().clearSelection();
        productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
        updateDateTime();
        productSearchField.requestFocus();

        showSuccessAlert("نجاح", "تم تحديث الصفحة بنجاح!");
    }

    @FXML
    private void handleBillsReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/CRUD/getPurchasesRaports.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("كشف الفواتير");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/report-file.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح تقرير الفواتير: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/CRUD/searchproductpurchases.fxml"));
            javafx.scene.Parent root = loader.load();

            // Create a new stage (window)
            javafx.stage.Stage newStage = new javafx.stage.Stage();
            newStage.setTitle("البحث");
            newStage.setScene(new javafx.scene.Scene(root));

            // Add icon to the stage
            newStage.getIcons().add(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/search.png"))));

            // Set window properties
            newStage.setResizable(false);
            newStage.setMaximized(false);

            // Show the new window
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showFailedAlert("فشل", "تعذر فتح  شاشة البحث عن المنتج.");
        }
    }

    @FXML
    private void handleHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/sales/Form/help.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("مساعدة");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/help.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة المساعدة: " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculator() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/example/interfaces/sales/Form/Calculator.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Calculator");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Set icon if available
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/calculator.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                showFailedAlert("فشل", "تعذر تحميل الايقونة");
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open calculator");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        ObservableList<SaleItem> selectedItems = salesTable.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showWarningAlert("تنبيه", "يرجى اختيار منتج أو أكثر للحذف!");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("تأكيد الحذف");
        confirmation.setContentText("هل أنت متأكد من حذف " + selectedItems.size() + " منتج(ات)؟");
        Stage stage = (Stage) confirmation.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/warning.png")));

        if (confirmation.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            productList.removeAll(selectedItems);
            for (int i = 0; i < productList.size(); i++) {
                productList.get(i).setNumber(String.valueOf(i + 1));
            }
            salesTable.refresh();
            updateTotals();
            productSearchField.clear();
            quantityField.clear();
            productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
            showSuccessAlert("نجاح", "تم حذف المنتج بنجاح!");
        }
    }

    @FXML
    private void handleEdit() {
        SaleItem selectedItem = salesTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showWarningAlert("تنبيه", "يرجى اختيار منتج لتعديله!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/purchases/CRUD/updateproductPurchase.fxml"));
            Parent root = loader.load();
            updateProductTablePurchasesController controller = loader.getController();
            controller.setMainController(this);
            controller.setProductData(selectedItem);

            Stage stage = new Stage();
            stage.setTitle("تعديل منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/addproduct.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة تعديل المنتج: " + e.getMessage());
        }
    }

    @FXML
    private void handleconfigPurchase() {
        if (productList.isEmpty()) {
            showWarningAlert("تنبيه", "لا توجد منتجات للدفع!");
            return;
        }

        try {
            // 1️⃣ Prompt for customer details
            String customerName = showInputDialog("اسم العميل");
            String customerId = showInputDialog("رقم العميل");

            // 2️⃣ Validate customer details
            if (customerName == null || customerId == null ||
                    customerName.trim().isEmpty() || customerId.trim().isEmpty()) {
                showWarningAlert("تنبيه", "الرجاء إدخال اسم العميل ورقم العميل بشكل صحيح!");
                return;
            }

            // 3️⃣ Get total from UI
            double total = parseDoubleOrZero(totalField.getText().replace(" DZ", ""));
            if (total <= 0) {
                showWarningAlert("تنبيه", "المبلغ الإجمالي غير صحيح!");
                return;
            }

            // 4️⃣ Set paid amount to total (no prompt, assuming full payment)
            double paidAmount = total; // Automatically set to total, no user input

            // 5️⃣ Calculate change
            double change = paidAmount - total; // Will be 0.0 since paidAmount = total
            if (change < 0) {
                showWarningAlert("تنبيه", "المبلغ المدفوع أقل من المبلغ الإجمالي!"); // Redundant but kept for consistency
                return;
            }

            // 6️⃣ Save the sale and get sale_id
            int purchaseId = saveSaleAndGetId(customerName, customerId);

            // 7️⃣ Save the payment
            savePurchaseToDatabase(purchaseId, total, paidAmount, change);

            // 8️⃣ Show success and clear the list
            handleClearList();

        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر معالجة الدفع: " + e.getMessage());
        }
    }

    private void savePurchaseToDatabase(int purchaseId, double total, double paid, double change) {

        // Validate input parameters
        if (purchaseId <= 0) {
            showFailedAlert("خطأ في البيانات", "رقم عملية البيع غير صالح");
            return;
        }
        if (Double.isNaN(total) || Double.isNaN(paid) || Double.isNaN(change) ||
                Double.isInfinite(total) || Double.isInfinite(paid) || Double.isInfinite(change)) {
            showFailedAlert("خطأ في البيانات", "القيم المدخلة غير صالحة");
            return;
        }

        // Format monetary values to 2 decimal places
        BigDecimal formattedTotal = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
        BigDecimal formattedPaid = BigDecimal.valueOf(paid).setScale(2, RoundingMode.HALF_UP);
        BigDecimal formattedChange = BigDecimal.valueOf(change).setScale(2, RoundingMode.HALF_UP);

        String sql = "INSERT INTO purchase_payments (purchase_id, total_amount, paid_amount, change_amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ في الاتصال", "فشل الاتصال بقاعدة البيانات");
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, purchaseId);
                pstmt.setBigDecimal(2, formattedTotal); // Use BigDecimal to preserve scale
                pstmt.setBigDecimal(3, formattedPaid);  // Use BigDecimal to preserve scale
                pstmt.setBigDecimal(4, formattedChange); // Use BigDecimal to preserve scale

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    showWarningAlert("تنبيه", "لم يتم إدراج الدفع في قاعدة البيانات");
                } else {
                    showSuccessAlert("نجاح", "تم حفظ الدفع بنجاح");
                }
            }
        } catch (SQLException e) {
            String errorMessage = "تعذر حفظ الدفع: " + (e.getMessage() != null ? e.getMessage() : "خطأ غير محدد");
            showFailedAlert("خطأ في قاعدة البيانات", errorMessage);
        }
    }

    private int saveSaleAndGetId(String customerName, String customerId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection failed");
            }
            conn.setAutoCommit(false);

            // 1️⃣ Get and format values from UI fields
            String subtotalText = subtotalField.getText().replace(" DZ", "").trim();
            String discountText = discountField.getText().replace(" DZ", "").trim();
            String debtText = debtField.getText().replace(" DZ", "").trim();
            String totalText = totalField.getText().replace(" DZ", "").trim();

            double subtotal = parseDoubleSafely(subtotalText);
            double discount = parseDoubleSafely(discountText);
            double debt = parseDoubleSafely(debtText);
            double total = parseDoubleSafely(totalText);

            // 2️⃣ Get current date/time
            String date = LocalDate.now().toString();
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // 3️⃣ Insert sale header
            String salesQuery = "INSERT INTO purchases (purchase_date, purchase_time, subtotal, discount, debt, total, customer_name, customer_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement salesStmt = conn.prepareStatement(salesQuery, Statement.RETURN_GENERATED_KEYS)) {
                salesStmt.setDate(1, Date.valueOf(LocalDate.parse(date)));
                salesStmt.setTime(2, Time.valueOf(LocalTime.parse(time)));
                salesStmt.setDouble(3, subtotal);
                salesStmt.setDouble(4, discount);
                salesStmt.setDouble(5, debt);
                salesStmt.setDouble(6, total);
                salesStmt.setString(7, customerName);
                salesStmt.setString(8, customerId);

                int rowsAffected = salesStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No rows inserted into sales table");
                }

                // 4️⃣ Get generated sale_id
                try (ResultSet rs = salesStmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        showFailedAlert("فشل", "تعذر الحصول على رقم عملية البيع");
                    }
                    int purchaseId = rs.getInt(1);

                    // 5️⃣ Insert sale items
                    try (PreparedStatement itemsStmt = conn.prepareStatement(
                            "INSERT INTO purchase_items (purchase_id, product_id, number, product_name, quantity, price, total_price) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        int itemNum = 1;
                        boolean itemsInserted = false;

                        for (SaleItem item : productList) {
                            try {
                                itemsStmt.setInt(1, purchaseId);
                                itemsStmt.setString(2, item.getProductId());
                                itemsStmt.setString(3, String.valueOf(itemNum++));
                                itemsStmt.setString(4, item.getProductName());
                                itemsStmt.setInt(5, parseIntSafely(item.getQuantity()));
                                itemsStmt.setDouble(6, parseDoubleSafely(item.getPrice()));
                                itemsStmt.setDouble(7, parseDoubleSafely(item.getTotalPrice()));
                                itemsStmt.addBatch(); // Add each item to the batch
                                itemsInserted = true;
                            } catch (SQLException e) {
                                showSuccessAlert("نجاح", "تم الحصول على المعلومات بنجاح.");
                            }
                        }

                        if (itemsInserted) {
                            int[] itemRowsAffected = itemsStmt.executeBatch();
                            for (int i = 0; i < itemRowsAffected.length; i++) {
                                if (itemRowsAffected[i] == 0) {
                                    showWarningAlert("تحذير", "لم يتم إدراجه.");
                                }
                            }
                        } else {
                            showFailedAlert("فشل", "لم تتم إضافة أي عناصر إلى الدفعة الخاصة");
                        }
                    }

                    conn.commit();
                    showSuccessAlert("عملية", "المعاملة الملتزمة");
                    return purchaseId;
                }
            }
        } catch (SQLException e) {
            showFailedAlert("فشل", "خطأ في قاعدة البيانات.");
            throw e;
        }
    }

    private void updateDateTime() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    LocalDateTime now = LocalDateTime.now();
                    dateField.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    timeField.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
