package com.example.library.controller.sales;

import com.example.library.model.SaleItem;
import com.example.library.util.DatabaseConnection;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.print.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class salesController {

    @FXML private TableView<SaleItem> salesTable;
    @FXML private TableColumn<SaleItem, String> colNumber;
    @FXML private TableColumn<SaleItem, String> colProductName;
    @FXML private TableColumn<SaleItem, String> colQuantity;
    @FXML private TableColumn<SaleItem, String> colPrice;
    @FXML private TableColumn<SaleItem, String> colTotalPrice;

    @FXML private TextField productSearchField;
    @FXML private TextField subtotalField, discountField, debtField, totalField;
    @FXML private TextField dateField, timeField;
    @FXML private ImageView productImageView;

    @FXML private Button addButton, billButton, printButton, saveButton, clearListButton, refreshButton;
    @FXML private Button billsReportButton, searchButton, helpButton, infoButton, deleteButton, editButton, payButton;

    @FXML private Button barcodeButton;

    @FXML private TextField arabictotalField;

    @FXML private TextField quantityField;

    @FXML private MenuItem taxMenuItem1, taxMenuItem2, discountMenuItem1, discountMenuItem2;

    private final ObservableList<SaleItem> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        salesTable.setItems(productList);

        productSearchField.setOnAction(e -> handleBarcodeScan());

        if (addButton != null) addButton.setOnAction(e -> handleAddButtonAction());
        if (billButton != null) billButton.setOnAction(e -> handleBill());
        if (printButton != null) printButton.setOnAction(e -> handlePrint());
        if (saveButton != null) saveButton.setOnAction(e -> handleSave());
        if (clearListButton != null) clearListButton.setOnAction(e -> handleClearList());
        if (refreshButton != null) refreshButton.setOnAction(e -> handleRefresh());
        if (billsReportButton != null) billsReportButton.setOnAction(e -> handleBillsReport());
        if (searchButton != null) searchButton.setOnAction(e -> handleSearch());
        if (helpButton != null) helpButton.setOnAction(e -> handleHelp());
        if (infoButton != null) infoButton.setOnAction(e -> handleInfo());
        if (deleteButton != null) deleteButton.setOnAction(e -> handleDelete());
        if (editButton != null) editButton.setOnAction(e -> handleEdit());
        if (payButton != null) payButton.setOnAction(e -> handlePay());

        if (taxMenuItem1 != null) taxMenuItem1.setOnAction(e -> handleTaxMenuItem1());
        if (taxMenuItem2 != null) taxMenuItem2.setOnAction(e -> handleTaxMenuItem2());
        if (discountMenuItem1 != null) discountMenuItem1.setOnAction(e -> handleDiscountMenuItem1());
        if (discountMenuItem2 != null) discountMenuItem2.setOnAction(e -> handleDiscountMenuItem2());

        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                quantityField.setText(newSelection.getQuantity());
                productSearchField.setText(newSelection.getBarcode());
            }
        });

        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            SaleItem selectedItem = salesTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && newVal.matches("\\d+")) {
                int newQuantity = Integer.parseInt(newVal);
                double price = Double.parseDouble(selectedItem.getPrice());
                selectedItem.setQuantity(String.valueOf(newQuantity));
                selectedItem.setTotal(String.format("%.2f", price * newQuantity));
                salesTable.refresh(); // reflect the changes in the table
                updateTotals();       // update subtotal and total
            }
        });

        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadProductImage(newSelection.getBarcode());
            }
        });

        arabictotalField.setText("صفر دينار جزائري");

        updateDateTime();
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
                    if (file.exists()) {
                        productImageView.setImage(new Image(file.toURI().toString()));
                    } else {
                        productImageView.setImage(new Image(getClass().getResource("/images/image_not_found.png").toExternalForm()));
                    }
                } else {
                    productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshTable() {
        salesTable.refresh();
    }


    public void updateTotals() {
        double subtotal = 0.0;
        for (SaleItem item : salesTable.getItems()) {
            subtotal += Double.parseDouble(item.getTotalPrice());
        }

        double discount = 0.0;
        double debt = 0.0;

        try {
            if (!discountField.getText().isEmpty())
                discount = Double.parseDouble(discountField.getText());
            if (!debtField.getText().isEmpty())
                debt = Double.parseDouble(debtField.getText());
        } catch (NumberFormatException ignored) {
        }

        double total = subtotal - discount + debt;

        subtotalField.setText(String.format("%.2f DZ", subtotal));
        totalField.setText(String.format("%.2f DZ", total));

        arabictotalField.setText(amountToArabicWords(total));
    }

    // --- تحويل المبلغ الكلي إلى كلمات عربية (يدعم الدينار والسنتيم) ---
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

    // تحويل عدد صحيح (0..999999999...) إلى كلمات عربية مبسطة
    private String integerToArabicWords(long number) {
        if (number == 0) return "صفر";

        String[] units = {"", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
                "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر"};
        String[] tens = {"", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"};
        String[] hundreds = {"", "مائة", "مئتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"};

        StringBuilder words = new StringBuilder();

        // مقامات الألوف
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
            if (thousand == 1) {
                words.append("ألف ");
            } else if (thousand == 2) {
                words.append("ألفان ");
            } else {
                words.append(threeDigitsToWords((int) thousand, units, tens, hundreds))
                        .append(" ألف ");
            }
            number %= 1000L;
        }

        if (number > 0) {
            words.append(threeDigitsToWords((int) number, units, tens, hundreds));
        }

        // تنظيف المسافات الزائدة
        return words.toString().trim().replaceAll("\\s+", " ");
    }

    // تحويل جزئي (0..999) إلى كلمات
    private String threeDigitsToWords(int num, String[] units, String[] tens, String[] hundreds) {
        StringBuilder part = new StringBuilder();
        if (num >= 100) {
            int h = num / 100;
            part.append(hundreds[h]);
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
        if (number == 1) return accusativeSingular; // "ديناراً"
        if (number == 2) return dual; // "ديناران"
        if (number >= 3 && number <= 10) return plural; // "دنانير"
        return singular; // for >10 use singular noun after number phrase (قواعد عربية مبسطة)
    }


    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    private void handleBarcodeScan() {
        String barcode = productSearchField.getText();
        if (barcode == null || barcode.isEmpty()) return;

        SaleItem scannedItem = getProductByBarcode(barcode);
        if (scannedItem != null) {
            // Check if product already exists in the table
            for (SaleItem item : salesTable.getItems()) {
                if (item.getProductId().equals(scannedItem.getProductId())) {
                    // Update quantity and total price
                    int currentQty = Integer.parseInt(item.getQuantity());
                    double unitPrice = Double.parseDouble(item.getPrice());
                    currentQty += 1;
                    item.setQuantity(String.valueOf(currentQty));
                    item.setTotal(String.format("%.2f", currentQty * unitPrice));
                    salesTable.refresh();
                    updateTotals();
                    productSearchField.clear();
                    return;
                }
            }

            // Add as a new item if not already in the list
            salesTable.getItems().add(scannedItem);
            updateTotals();
        } else {
            showAlert("تنبيه", "لم يتم العثور على المنتج!");
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
                String number = String.valueOf(salesTable.getItems().size() + 1);

                // Load image from DB path
                String imagePath = rs.getString("image_path"); // assuming this column exists

                if (imagePath != null && !imagePath.isEmpty()) {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        productImageView.setImage(image);
                    } else {
                        productImageView.setImage(new Image(getClass().getResource("/images/image_not_found.png").toExternalForm()));
                    }
                } else {
                    productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
                }

                return new SaleItem(
                        number,
                        rs.getString("id"),
                        rs.getString("product_name"),
                        rs.getString("barcode"), // <-- Fix here: use actual barcode from DB
                        "1",
                        String.format("%.2f", rs.getDouble("price1")),
                        String.format("%.2f", rs.getDouble("price1"))
                );


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void addProductToTable(String name, String barcode, double price, int quantity, double total) {
        String number = String.valueOf(salesTable.getItems().size() + 1);
        SaleItem newItem = new SaleItem(
                number,
                "TEMP", // Replace with actual product ID if available
                name,
                barcode,
                String.valueOf(quantity),
                String.format("%.2f", price),
                String.format("%.2f", total)
        );
        salesTable.getItems().add(newItem);

        productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));

        updateTotals();
    }

    @FXML
    private void handleBarcodeButtonAction() {
        productSearchField.clear();
        productSearchField.requestFocus();
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/sales/CRUD/addproductSales.fxml"));
            Parent root = loader.load();

            addProductTableController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("إضافة منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/addproduct.png"))));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBill() { }
    private void handlePrint() { }
    private void handleSave() { }
    private void handleClearList() {
        salesTable.getItems().clear();
        updateTotals();
        productSearchField.clear();
        productSearchField.requestFocus();

        quantityField.clear();

        productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));
    }
    private void handleRefresh() {
        // Refresh the table view to reflect any changes
        salesTable.refresh();

        // Recalculate totals to ensure they're up-to-date
        updateTotals();

        // Clear the search field but keep all table data
        productSearchField.clear();

        // Clear the quantity field
        quantityField.clear();

        // Remove selection from table
        salesTable.getSelectionModel().clearSelection();

        // Reset product image to default
        productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));

        // Update the date/time display
        updateDateTime();

        // Set focus back to search field for convenience
        productSearchField.requestFocus();
    }

    private void handleBillsReport() { }
    private void handleSearch() { }
    private void handleHelp() { }
    private void handleInfo() { }
    private void handleDelete() {
        // Get all selected items from the table
        ObservableList<SaleItem> selectedItems = salesTable.getSelectionModel().getSelectedItems();

        // Check if any items are selected
        if (selectedItems.isEmpty()) {
            showAlert("تنبيه", "يرجى اختيار منتج أو أكثر للحذف");
            return;
        }

        // Confirm deletion with user
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("تأكيد الحذف");
        confirmation.setHeaderText(null);
        confirmation.setContentText("هل أنت متأكد من حذف " + selectedItems.size() + " منتج(ات)؟");

        // Show confirmation dialog and wait for response
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Remove all selected items from the table
                productList.removeAll(selectedItems);

                // Update the item numbers after deletion
                for (int i = 0; i < productList.size(); i++) {
                    productList.get(i).setNumber(String.valueOf(i + 1));
                }

                // Refresh the table and update totals
                salesTable.refresh();
                updateTotals();

                // Clear selection and reset fields
                salesTable.getSelectionModel().clearSelection();
                productSearchField.clear();
                quantityField.clear();
                productImageView.setImage(new Image(getClass().getResource("/images/image.png").toExternalForm()));

                showAlert("تم", "تم حذف المنتج(ات) المحدد بنجاح");
            }
        });
    }
    private void handleEdit() {
        SaleItem selectedItem = salesTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("تنبيه", "يرجى اختيار منتج لتعديله!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/interfaces/sales/CRUD/updateproductSales.fxml"));
            Parent root = loader.load();

            updateProductTableController controller = loader.getController();
            controller.setMainController(this);
            controller.setProductData(selectedItem); // 👈 Pass product data to edit

            Stage stage = new Stage();
            stage.setTitle("تعديل منتج");
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/addproduct.png"))));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePay() { }

    private void handleTaxMenuItem1() { }
    private void handleTaxMenuItem2() { }
    private void handleDiscountMenuItem1() { }
    private void handleDiscountMenuItem2() { }

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
