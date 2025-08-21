package com.example.library.controller.client;


import com.example.library.controller.sales.printFormController;
import com.example.library.model.Debt;
import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.animation.*;
import javafx.application.Platform;
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
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.library.Alert.alert.*;
import static javax.swing.JOptionPane.showInputDialog;

public class salesClientController {

    @FXML private TableView<SaleItem> salesTable;
    @FXML private TableColumn<SaleItem, String> colNumber;
    @FXML private TableColumn<SaleItem, String> colBarcode;
    @FXML private TableColumn<SaleItem, String> colProductName;
    @FXML private TableColumn<SaleItem, String> colQuantity;
    @FXML private TableColumn<SaleItem, String> colPrice;
    @FXML private TableColumn<SaleItem, String> colTotalPrice;

    @FXML private TableView<Debt> debtTableView;

    @FXML private TextField productSearchField;
    @FXML private TextField subtotalField, discountField, debtField, totalField;
    @FXML private TextField dateField, timeField;
    @FXML private ImageView productImageView;

    @FXML private Button addButton, billButton, printButton, saveButton, clearListButton, refreshButton;
    @FXML private Button billsReportButton, searchButton, helpButton, ClientPayButton, deleteButton, editButton, payButton;
    @FXML private Button barcodeButton;

    @FXML private TextField debtField2;
    @FXML private TextField clientIdFeild;
    @FXML private MenuButton taxMenuButton;
    @FXML private Label discountRateLabel;
    @FXML private MenuButton discountMenuButton;
    @FXML private MenuButton clientNameMenu;
    @FXML private TextField ArabicTotalField;
    @FXML private TextField quantityField;

    @FXML private MenuItem taxMenuItem1, taxMenuItem2, discountMenuItem1, discountMenuItem2;

    private Map<String, String> clientMap = new HashMap<>();
    private final ObservableList<SaleItem> productList = FXCollections.observableArrayList();
    private String customerId;
    private double cachedSubtotal = 0.0;

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        updateUIState();
    }

    @FXML
    public void initialize() {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        salesTable.setItems(productList);

        setupEventHandlers();
        initializeUIComponents();
        setupListeners();
        updateDateTime();
        setupTaxMenu();
        setupDiscountMenu();
        setupDebtField();
        loadClients();
        updateUIState();
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
        if (ClientPayButton != null) ClientPayButton.setOnAction(e -> handlePayClientWindow());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete());
        if (editButton != null) editButton.setOnAction(e -> handleEdit());
        if (payButton != null) payButton.setOnAction(e -> handleSavePay());
        if (barcodeButton != null) barcodeButton.setOnAction(e -> handleBarcodeButtonAction());
        if (clientNameMenu != null) clientNameMenu.setText("الرجاء اختيار العميل...");
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
                    if (newQuantity <= 0) {
                        showWarningAlert("تنبيه", "الكمية يجب أن تكون أكبر من صفر!");
                        quantityField.setText(oldVal);
                        return;
                    }
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

        // Disable productSearchField if no customer is selected
        clientNameMenu.textProperty().addListener((obs, old, newVal) -> {
            productSearchField.setDisable(newVal.equals("اختر العميل") || customerId == null);
            updateUIState();
        });
    }

    private void updateUIState() {
        boolean isCustomerSelected = customerId != null && !customerId.trim().isEmpty();
        productSearchField.setDisable(!isCustomerSelected);
        addButton.setDisable(!isCustomerSelected);
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
            showFailedAlert("فشل", "تعذر تحميل الصورة: " + e.getMessage());
        }
    }

    private void setupDebtField() {
        debtField2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches("\\d*(\\.\\d{0,2})?")) {
                debtField2.setText(oldValue != null ? oldValue : "");
            } else {
                try {
                    double value = newValue.isEmpty() ? 0.0 : Double.parseDouble(newValue);
                    debtField.setText(String.format("%.2f DZ", value));
                    updateTotals();
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
        updateTotals();
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

    public void updateTotals() {
        cachedSubtotal = productList.isEmpty() ? 0.0 :
                productList.stream().mapToDouble(item -> parseDoubleOrZero(item.getTotalPrice())).sum();

        double flatDiscount = Math.min(parseDoubleOrZero(discountField.getText()), cachedSubtotal);
        double debt = parseDoubleOrZero(debtField.getText());
        double shipping = parseDoubleOrZero(debtField2.getText());
        double taxRate = parseDoubleOrZero(taxMenuButton.getText().replace("%", ""));
        taxRate = Math.min(taxRate, 100);
        double taxAmount = cachedSubtotal * taxRate / 100;
        double discountRate = parseDoubleOrZero(discountMenuButton.getText().replace("%", ""));
        discountRate = Math.min(discountRate, 100);
        double discountAmount = cachedSubtotal * discountRate / 100;
        double total = cachedSubtotal - flatDiscount - discountAmount + debt + shipping + taxAmount;
        total = Math.max(total, 0);

        discountField.setText(String.format("%.2f DZ", discountAmount));
        subtotalField.setText(String.format("%.2f DZ", cachedSubtotal));
        totalField.setText(String.format("%.2f DZ", total));
        ArabicTotalField.setText(amountToArabicWords(total));
    }

    private double parseDoubleOrZero(String text) {
        try {
            return text.isEmpty() ? 0.0 : Double.parseDouble(text.trim().replace(" DZ", ""));
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
        if (customerId == null || customerId.trim().isEmpty()) {
            showCustomerSelectionDialog();
            return;
        }

        String barcode = productSearchField.getText().trim();
        if (barcode.isEmpty()) {
            productSearchField.requestFocus();
            return;
        }

        if (!barcode.matches("\\d{13}")) {
            showWarningAlert("تنبيه", "رمز الباركود يجب أن يكون 13 رقمًا!");
            productSearchField.clear();
            productSearchField.requestFocus();
            return;
        }

        SaleItem scannedItem = getProductByBarcode(barcode);
        if (scannedItem != null) {
            Platform.runLater(() -> {
                boolean itemUpdated = false;
                for (SaleItem item : productList) {
                    if (item.getBarcode().equals(scannedItem.getBarcode())) {
                        try {
                            int currentQty = Integer.parseInt(item.getQuantity());
                            double unitPrice = Double.parseDouble(item.getPrice());
                            double scannedPrice = Double.parseDouble(scannedItem.getPrice());
                            if (Math.abs(unitPrice - scannedPrice) > 0.01) {
                                showWarningAlert("تحذير", "سعر المنتج في القائمة لا يتطابق مع قاعدة البيانات!");
                                return;
                            }
                            currentQty++;
                            item.setQuantity(String.valueOf(currentQty));
                            item.setTotal(String.format("%.2f", currentQty * unitPrice));
                            itemUpdated = true;
                            quantityField.setText(String.valueOf(currentQty));
                            updateProductStock(barcode, 1);
                            break;
                        } catch (NumberFormatException e) {
                            showFailedAlert("خطأ", "خطأ في تحديث الكمية أو السعر: " + e.getMessage());
                            return;
                        }
                    }
                }

                if (!itemUpdated) {
                    scannedItem.setNumber(String.valueOf(productList.size() + 1));
                    productList.add(scannedItem);
                    quantityField.setText(scannedItem.getQuantity());
                    updateProductStock(barcode, 1);
                }

                salesTable.refresh();
                salesTable.getSelectionModel().select(scannedItem);
                updateTotals();
                loadProductImage(barcode);
                productSearchField.clear();
                productSearchField.requestFocus();
            });
        } else {
            showFailedAlert("فشل", "لم يتم العثور على المنتج برمز الباركود: " + barcode);
            productSearchField.clear();
            productSearchField.requestFocus();
        }
    }

    @FXML
    private void handleAddButtonAction() {
        if (customerId == null || customerId.trim().isEmpty()) {
            showCustomerSelectionDialog();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/addproductSalesClient.fxml"));
            Parent root = loader.load();
            addproductSalesClientController controller = loader.getController();
            controller.setMainController(this);
            controller.setCustomerId(customerId);

            Stage stage = new Stage();
            stage.setTitle("إضافة منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/addproduct.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة إضافة المنتج.");
        }
    }

    private void showCustomerSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("اختيار العميل");
        dialog.setHeaderText("يرجى اختيار العميل قبل إضافة المنتجات");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> customerComboBox = new ComboBox<>();
        customerComboBox.getItems().addAll(clientMap.values());
        customerComboBox.setPromptText("اختر العميل");

        dialogPane.setContent(customerComboBox);

        Platform.runLater(customerComboBox::requestFocus);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String selectedCustomer = customerComboBox.getValue();
                if (selectedCustomer != null) {
                    customerId = clientMap.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(selectedCustomer))
                            .findFirst()
                            .map(Map.Entry::getKey)
                            .orElse(null);
                    if (customerId != null) {
                        clientNameMenu.setText(selectedCustomer);
                        updateUIState();
                        return selectedCustomer;
                    }
                }
                showWarningAlert("تنبيه", "يرجى اختيار عميل!");
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(customerName -> {
            if (customerId != null) {
                showSuccessAlert("نجاح", "تم اختيار العميل: " + customerName);
                productSearchField.requestFocus();
            }
        });
    }

    private SaleItem getProductByBarcode(String barcode) {
        if (barcode == null || !barcode.matches("\\d{13}")) {
            showWarningAlert("تنبيه", "رمز الباركود غير صالح! يجب أن يكون 13 رقمًا.");
            return null;
        }

        String query = "SELECT id, product_name, price1, quantity FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات!");
                return null;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, barcode);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {

                    String productId = rs.getString("id");
                    String productName = rs.getString("product_name");
                    double price = rs.getDouble("price1");
                    String formattedPrice = String.format("%.2f", price);
                    String number = String.valueOf(productList.size() + 1);

                    return new SaleItem(
                            number,
                            productId,
                            productName,
                            barcode,
                            "1",
                            formattedPrice,
                            formattedPrice
                    );
                } else {
                    showFailedAlert("فشل", "لم يتم العثور على المنتج برمز الباركود: " + barcode);
                    return null;
                }
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "خطأ في قاعدة البيانات");
            return null;
        }
    }

    private void updateProductStock(String barcode, int quantityToRemove) {
        String query = "UPDATE products SET quantity = quantity - ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات!");
                return;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, quantityToRemove);
                stmt.setString(2, barcode);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    showFailedAlert("خطأ", "فشل تحديث المخزون للمنتج: " + barcode);
                }
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "خطأ في تحديث المخزون.");
        }
    }

    private void restoreProductStock(String barcode, int quantityToAdd) {
        String query = "UPDATE products SET quantity = quantity + ? WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                showFailedAlert("خطأ", "فشل الاتصال بقاعدة البيانات!");
                return;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, quantityToAdd);
                stmt.setString(2, barcode);
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "خطأ في استعادة المخزون.");
        }
    }

    public void addProductToTable(String name, String barcode, double price, int quantity, double total, boolean isFromDatabase) {
        if (name == null || name.trim().isEmpty()) {
            showWarningAlert("تنبيه", "اسم المنتج غير صالح!");
            return;
        }
        if (barcode == null || barcode.trim().isEmpty()) {
            showWarningAlert("تنبيه", "رمز الباركود غير صالح!");
            return;
        }
        if (price <= 0) {
            showWarningAlert("تنبيه", "السعر يجب أن يكون أكبر من صفر!");
            return;
        }
        if (quantity <= 0) {
            showWarningAlert("تنبيه", "الكمية يجب أن تكون أكبر من صفر!");
            return;
        }
        if (Math.abs(total - price * quantity) > 0.01) {
            showWarningAlert("تنبيه", "المجموع الإجمالي لا يتطابق مع السعر × الكمية!");
            return;
        }

        for (SaleItem item : productList) {
            if (item.getBarcode().equals(barcode)) {
                int newQuantity = Integer.parseInt(item.getQuantity()) + quantity;
                item.setQuantity(String.valueOf(newQuantity));
                item.setTotal(String.format("%.2f", price * newQuantity));
                Platform.runLater(() -> {
                    salesTable.refresh();
                    updateTotals();
                    loadProductImage(barcode);
                });
                showSuccessAlert("نجاح", "تم تحديث كمية المنتج في القائمة!");
                if (isFromDatabase) {
                    updateProductStock(barcode, quantity);
                }
                return;
            }
        }

        String productId = isFromDatabase ? getProductIdFromDatabase(barcode) : barcode;
        if (productId == null) {
            showFailedAlert("فشل", "تعذر الحصول على معرف المنتج من قاعدة البيانات!");
            return;
        }

        String number = String.valueOf(productList.size() + 1);
        SaleItem newItem = new SaleItem(
                number,
                productId,
                name,
                barcode,
                String.valueOf(quantity),
                String.format("%.2f", price),
                String.format("%.2f", total)
        );

        Platform.runLater(() -> {
            productList.add(newItem);
            salesTable.refresh();
            salesTable.getSelectionModel().select(newItem);
            updateTotals();
            loadProductImage(barcode);
            showSuccessAlert("نجاح", "تم إضافة المنتج بنجاح!");
        });

        if (isFromDatabase) {
            updateProductStock(barcode, quantity);
        }
    }

    private String getProductIdFromDatabase(String barcode) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM products WHERE barcode = ?")) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
            return barcode;
        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر الحصول على معرف المنتج.");
            return barcode;
        }
    }

    @FXML
    private void handleBarcodeButtonAction() {
        try {
            productSearchField.clear();
            productSearchField.requestFocus();
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر إفراغ حقل الباركود.");
        }
    }

    @FXML
    private void handlePrintButton() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/printClientForm.fxml"));
            Parent root = loader.load();

            // Get the bill controller
            printFormClientController controller = loader.getController();

            // Prepare data to pass
            ObservableList<SaleItem> items = productList; // Current items from salesTable
            String subtotal = subtotalField.getText();    // Current subtotal
            String discount = discountField.getText();    // Current discount
            String debt = debtField.getText();            // Current debt
            String total = totalField.getText();          // Current total
            String date = dateField.getText();            // Current date (e.g., "2025-08-09")
            String time = timeField.getText();

            String customerName = clientNameMenu.getText();
            String customerId = clientIdFeild.getText();// Current time (e.g., "00:56")

            // Set the data in the bill controller
            controller.setSalesClientData(items, subtotal, discount, debt, total, date, time, customerName, customerId);

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

    @FXML
    private void handleBill() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/billclient.fxml"));
            Parent root = loader.load();

            // Get the bill controller
            billClientController controller = loader.getController();

            // Prepare data to pass
            ObservableList<SaleItem> items = productList; // Current items from salesTable
            String subtotal = subtotalField.getText();
            String discount = discountField.getText();
            String debt = debtField.getText();
            String total = totalField.getText();
            String date = dateField.getText();
            String time = timeField.getText();

            // ✅ Get existing customer name & ID from your current form
            String customerName = clientNameMenu.getText();
            String customerId = clientIdFeild.getText();

            // Pass the data to the bill controller
            controller.setSalesData(items, subtotal, discount, debt, total, date, time, customerName, customerId);

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

    @FXML
    private void handleSave() {
        if (productList.isEmpty()) {
            showWarningAlert("تنبيه", "لا توجد منتجات لحفظها!");
            return;
        }

        // Get customer details from customerNameMenu
        String customerName = clientNameMenu.getText();
        if (customerName == null || customerName.trim().isEmpty() || customerName.equals("اختر العميل")) {
            showWarningAlert("تنبيه", "الرجاء اختيار عميل من القائمة!");
            return;
        }

        // Search for customer in database
        String customerId = null;
        String query = "SELECT customer_id FROM client WHERE customer_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerId = rs.getString("customer_id");
            } else {
                showWarningAlert("تنبيه", "تعذر العثور على العميل في قاعدة البيانات!");
                return;
            }
        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر الاتصال بقاعدة البيانات: " + e.getMessage());
            return;
        }

        if (customerId == null) {
            showWarningAlert("تنبيه", "تعذر العثور على بيانات العميل!");
            return;
        }

        try {
            // Get and format values from UI fields
            String subtotalText = subtotalField.getText().replace(" DZ", "").trim();
            String discountText = discountField.getText().replace(" DZ", "").trim();
            String debtText = debtField.getText().replace(" DZ", "").trim();
            String totalText = totalField.getText().replace(" DZ", "").trim();

            // Parse values safely (for validation, but pass strings to saveClientSale)
            double subtotal = parseDoubleSafely(subtotalText);
            double discount = parseDoubleSafely(discountText);
            double debt = parseDoubleSafely(debtText);
            double total = parseDoubleSafely(totalText);

            // Validate parsed values
            if (subtotal < 0 || discount < 0 || debt < 0 || total < 0) {
                showWarningAlert("تنبيه", "القيم المالية يجب أن تكون موجبة!");
                return;
            }

            // Get current date/time
            String date = LocalDate.now().toString(); // 2025-08-14
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")); // e.g., 00:26:00

            // Show bill preview before saving
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/billClient.fxml"));
            Parent root = loader.load();

            // Get controller and set data with formatted values
            billClientController controller = loader.getController();
            controller.setSalesData(
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
            String finalCustomerId = customerId;
            confirmButton.setOnAction(e -> {
                try {
                    // Call saveClientSale with productList and string parameters
                    billClientController controllerInstance = loader.getController(); // Reuse the loaded controller
                    controllerInstance.saveClientSale(
                            productList,
                            subtotalText, // Use cleaned text from UI
                            discountText,
                            debtText,
                            totalText,
                            date,
                            time,
                            customerName,
                            finalCustomerId
                    );
                    showSuccessAlert("نجاح", "تم حفظ الفاتورة بنجاح!");
                    billStage.close();
                } catch (Exception ex) {
                    showFailedAlert("خطأ", "تعذر حفظ الفاتورة: " + ex.getMessage());
                    ex.printStackTrace(); // Add stack trace for debugging
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

    private void loadClients() {
        clientMap.clear();
        clientNameMenu.getItems().clear();

        String query = "SELECT customer_id, customer_name FROM client ORDER BY customer_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String customerName = rs.getString("customer_name");

                clientMap.put(customerId, customerName);

                MenuItem menuItem = new MenuItem(customerName); // Only display customer name

                menuItem.setOnAction(e -> {
                    clientNameMenu.setText(customerName); // Set only the name in the menu
                    this.customerId = customerId; // Update the controller's customerId
                    clientIdFeild.setText(customerId); // Display customerId in the field
                    updateUIState();
                });

                clientNameMenu.getItems().add(menuItem);
            }

            if (clientNameMenu.getItems().isEmpty()) {
                MenuItem emptyItem = new MenuItem("لا يوجد عملاء");
                emptyItem.setDisable(true);
                clientNameMenu.getItems().add(emptyItem);
            }

        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر تحميل قائمة العملاء: " + e.getMessage());
            MenuItem errorItem = new MenuItem("خطأ في تحميل العملاء");
            errorItem.setDisable(true);
            clientNameMenu.getItems().add(errorItem);
        }
    }

    @FXML
    private void handleClearList() {
        try {
            for (SaleItem item : productList) {
                int quantity = parseIntSafely(item.getQuantity());
                if (quantity > 0) {
                    restoreProductStock(item.getBarcode(), quantity);
                }
            }
            productList.clear();
            updateTotals();
            productSearchField.clear();
            quantityField.clear();
            productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
            productSearchField.requestFocus();
            debtField.clear();
            debtField2.clear();
            discountMenuButton.setText("0%");
            taxMenuButton.setText("0%");
            showSuccessAlert("نجاح", "تم مسح القائمة بنجاح!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر مسح القائمة.");
        }
    }

    @FXML
    private void handleRefresh() {
        try {
            for (SaleItem item : productList) {
                int quantity = parseIntSafely(item.getQuantity());
                if (quantity > 0) {
                    restoreProductStock(item.getBarcode(), quantity);
                }
            }
            productList.clear();
            salesTable.refresh();
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
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر تحديث الصفحة.");
        }
    }

    @FXML
    private void handleBillsReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/CRUD/getRaportsClient.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("كشف الفواتير");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/report-file.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح تقرير الفواتير.");
        }
    }

    @FXML
    private void handleDelete() {
        SaleItem selectedItem = salesTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showWarningAlert("تنبيه", "يرجى تحديد منتج للحذف!");
            return;
        }
        try {
            int quantity = parseIntSafely(selectedItem.getQuantity());
            if (quantity > 0) {
                restoreProductStock(selectedItem.getBarcode(), quantity);
            }
            productList.remove(selectedItem);
            renumberProductList();
            salesTable.refresh();
            updateTotals();
            showSuccessAlert("نجاح", "تم حذف المنتج بنجاح!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر حذف المنتج.");
        }
    }

    @FXML
    private void handleSearch() {
        try {
            // Load the FXML file for the add new product interface
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/interfaces/sales/CRUD/searchproduct.fxml"));
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
            showFailedAlert("فشل", "فتح نافذ البحث.");
        }
    }

    @FXML
    public void refreshTable() {
        try {
            salesTable.refresh();
            showSuccessAlert("نجاح", "تم تحديث القائمة!");
        } catch (Exception e) {
            showFailedAlert("فشل", "تعذر تحديث القائمة!");
        }
    }

    private void renumberProductList() {
        int index = 1;
        for (SaleItem item : productList) {
            item.setNumber(String.valueOf(index++));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/updateproductSalesClient.fxml"));
            Parent root = loader.load();
            updateProductTableClientController controller = loader.getController();
            controller.setMainController(this);
            controller.setProductData(selectedItem);

            Stage stage = new Stage();
            stage.setTitle("تعديل منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/addproduct.png")));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة تعديل المنتج.");
        }
    }

    @FXML
    private void handleSavePay() {
        try {
            // Retrieve UI input
            String customerName = clientNameMenu.getText();
            if (customerName == null || customerName.trim().isEmpty() || customerName.equals("اختر العميل")) {
                showWarningAlert("تنبيه", "الرجاء اختيار اسم العميل!");
                return;
            }

            String customerId = clientIdFeild.getText().trim();
            if (customerId.isEmpty()) {
                showWarningAlert("تنبيه", "الرجاء إدخال رقم العميل!");
                return;
            }

            String totalText = totalField.getText().replace(" DZ", "").trim();
            if (totalText.isEmpty()) {
                showWarningAlert("تنبيه", "الرجاء إدخال المجموع الكلي!");
                return;
            }

            double totalAmount = parseDoubleSafely(totalText);
            if (totalAmount <= 0) {
                showWarningAlert("تنبيه", "المجموع الكلي يجب أن يكون موجبًا!");
                return;
            }

            // Optional: Check if payment is entered
            TextField paymentField = new TextField(); // Replace with actual payment field if exists
            String paymentText = paymentField.getText().replace(" DZ", "").trim();
            double paymentAmount = parseDoubleSafely(paymentText);
            double debtAmount = (paymentAmount > 0) ? Math.max(0, totalAmount - paymentAmount) : totalAmount;

            String status = (debtAmount == 0) ? "غير مدفوع" : (paymentAmount > 0) ? "مدفوع جزئيًا" : "غير مدفوع";
            String notes = (paymentAmount > 0) ? "دفعة: " + paymentAmount + " DZ، باقي: " + debtAmount + " DZ" : "دين جديد";

            // Check if debt already exists
            String checkSQL = "SELECT debt_id, amount FROM client_debts WHERE customer_id = ? ORDER BY created_at DESC LIMIT 1";
            int debtId = -1;
            double existingDebt = 0;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setString(1, customerId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    debtId = rs.getInt("debt_id");
                    existingDebt = rs.getDouble("amount");
                }
            }

            // Database operation
            String sql;
            if (debtId == -1) {
                sql = "INSERT INTO client_debts (customer_id, amount, debt_date, status, notes) VALUES (?, ?, ?, ?, ?)";
            } else {
                debtAmount += existingDebt;
                sql = "UPDATE client_debts SET amount = ?, status = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE debt_id = ?";
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (debtId == -1) {
                    pstmt.setString(1, customerId);
                    pstmt.setDouble(2, debtAmount);
                    pstmt.setDate(3, Date.valueOf(LocalDate.now())); // 2025-08-14, 12:18 PM CET
                    pstmt.setString(4, status);
                    pstmt.setString(5, notes);
                } else {
                    pstmt.setDouble(1, debtAmount);
                    pstmt.setString(2, status);
                    pstmt.setString(3, notes);
                    pstmt.setInt(4, debtId);
                }

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSuccessAlert("نجاح", "تم حفظ الدين بنجاح! الحالة: ");
                    handleSave();
                    refreshDebtTable();
                    handleClearList();
                } else {
                    showFailedAlert("خطأ", "فشل في حفظ الدين.");
                }
            }

        } catch (SQLException e) {
            showFailedAlert("خطأ", "تعذر حفظ الدين.");
        } catch (Exception e) {
            showFailedAlert("خطأ", "خطأ غير متوقع.");
        }
    }

    private void refreshDebtTable() {
        if (debtTableView == null) {
            showWarningAlert("تنبيه", "الجدول فارغ.");
            return;
        }

        ObservableList<Debt> debts = FXCollections.observableArrayList();

        String sql =
                "SELECT c.customer_id, c.customer_name, " +
                        "       SUM(cd.amount) AS total_debt, " +
                        "       MAX(cd.debt_date) AS last_debt_date, " +
                        "       GROUP_CONCAT(cd.notes SEPARATOR ', ') AS all_notes " +
                        "FROM client_debts cd " +
                        "JOIN client c ON cd.customer_id = c.customer_id " +
                        "GROUP BY c.customer_id, c.customer_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                debts.add(new Debt(
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getBigDecimal("total_debt"),
                        rs.getDate("last_debt_date").toLocalDate(),
                        rs.getString("all_notes")
                ));
            }

        } catch (SQLException e) {
            showFailedAlert("فشل", "تعذر تحديث الديون.");
        }

        debtTableView.setItems(debts);
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
            showFailedAlert("فشل", "تعذر فتح نافذة المساعدة.");
        }
    }

    @FXML
    private void handlePayClientWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/client/Form/pay_Client.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("واجهة الديون");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/pay.png")));
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            showFailedAlert("فشل", "تعذر فتح نافذة الفاتورة.");
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

        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) dateField.getScene().getWindow();
                if (stage != null) {
                    stage.iconifiedProperty().addListener((obs, wasIconified, isIconified) -> {
                        if (isIconified) {
                            timeline.pause();
                        } else {
                            timeline.play();
                        }
                    });
                    timeline.play();
                } else {
                    showFailedAlert("خطأ", "تعذر الوصول إلى نافذة التطبيق.");
                }
            } catch (Exception e) {
                showFailedAlert("خطأ", "خطأ في إعداد تحديث الوقت.");
            }
        });
    }
}