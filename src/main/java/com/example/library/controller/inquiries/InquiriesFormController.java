package com.example.library.controller.inquiries;

import com.example.library.util.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;

import static com.example.library.Alert.alert.showFailedAlert;

public class InquiriesFormController {

    // 🟢 Labels (المستطيلات العلوية)
    @FXML private Label lblDailySalesCount;   // عدد المبيعات اليومية
    @FXML private Label lblDailyProfit;       // صافي اجمالي الربح اليومي
    @FXML private Label lblInvoicesCount;     // عدد الفواتير المخزنة

    // 🟢 Charts في تبويب المبيعات
    @FXML private PieChart pieBestSellers;        // أفضل المنتجات مبيعاً (Pie)
    @FXML private AreaChart<String, Number> dailyAreaChart;  // المبيعات اليومية
    @FXML private LineChart<String, Number> monthlyLineChart; // المبيعات الشهرية

    // 🟢 Charts في تبويب المخزون
    @FXML private PieChart pieValidVsExpired;
    @FXML private PieChart pieBestSellersStock;
    @FXML private PieChart pieStockLevels;
    @FXML private PieChart pieLowStock;
    @FXML private PieChart pieStockValue;

    @FXML private Label lblDailyPurchasesCount;     // عدد المشتريات اليومية
    @FXML private Label lblDailyPurchaseCost;       // اجمالي تكلفة المشتريات اليومية
    @FXML private Label lblPurchaseInvoicesCount;   // عدد فواتير المشتريات

    @FXML private PieChart pieTopPurchasedProducts;          // أكثر المنتجات شراءً
    @FXML private AreaChart<String, Number> dailyPurchaseAreaChart; // المشتريات اليومية
    @FXML private LineChart<String, Number> monthlyPurchaseLineChart; // المشتريات الشهرية

    // Labels
    @FXML private Label lblRegularClients;   // عدد العملاء النظاميين
    @FXML private Label lblDebtorClients;    // عدد العملاء المديونين
    @FXML private Label lblTotalDebt;        // صافي إجمالي الديون
    @FXML private Label lblInvoicesCount2;   // عدد الفواتير المخزنة

    // Charts
    @FXML private PieChart pieClientsStatus; // نسبة العملاء الدافعين وغير الدافعين
    @FXML private AreaChart<String, Number> areaDailySales; // مبيعات يومية
    @FXML private LineChart<String, Number> lineMonthlySales; // مبيعات شهرية

    @FXML
    public void initialize() {
        // 🟢 المبيعات
        loadSalesSummary();
        loadBestSellers();
        loadDailySalesChart();
        loadMonthlySalesChart();

        // 🟢 المشتريات
        loadPurchasesSummary();
        loadTopPurchasedProducts();
        loadDailyPurchasesChart();
        loadMonthlyPurchasesChart();

        // 🟢 المخزون
        loadStockCharts();

        // 🟢 العملاء
        loadClientsStats();
        loadClientsCharts();
    }

    // ============================================================
    // 🟢 المبيعات
    // ============================================================

    private void loadSalesSummary() {
        String dailySales = "SELECT COUNT(*) FROM sales WHERE sale_date = CURDATE()";
        String dailyProfit = "SELECT IFNULL(SUM(total),0) FROM sales WHERE sale_date = CURDATE()";
        String invoicesCount = "SELECT COUNT(*) FROM sales";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(dailySales);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblDailySalesCount.setText(rs.getInt(1) + " عملية");
            }

            try (PreparedStatement ps = conn.prepareStatement(dailyProfit);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblDailyProfit.setText(String.format("%,.2f DZ", rs.getDouble(1)));
            }

            try (PreparedStatement ps = conn.prepareStatement(invoicesCount);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblInvoicesCount.setText(rs.getInt(1) + " فاتورة");
            }

        } catch (Exception e) {
            lblDailySalesCount.setText("خطأ في التحميل");
            lblDailyProfit.setText("خطأ في التحميل");
            lblInvoicesCount.setText("خطأ في التحميل");
        }
    }

    private void loadBestSellers() {
        String query =
                "SELECT si.product_name, SUM(si.quantity) AS total_qty " +
                        "FROM sale_items si " +
                        "JOIN sales s ON si.sale_id = s.sale_id " +
                        "GROUP BY si.product_name " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT 5";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("total_qty");
                pieData.add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
            }
            pieBestSellers.setData(pieData);
            pieBestSellers.setTitle("أفضل المنتجات مبيعاً");

        } catch (Exception e) {
            pieBestSellers.setTitle("أفضل المنتجات مبيعاً - لا توجد بيانات");
        }
    }

    private void loadDailySalesChart() {
        String query =
                "SELECT sale_date, SUM(total) as daily_total " +
                        "FROM sales " +
                        "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "GROUP BY sale_date " +
                        "ORDER BY sale_date";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("المبيعات اليومية");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                String formattedDate = date.getDayOfMonth() + " " + getArabicMonthName(date.getMonthValue());
                series.getData().add(new XYChart.Data<>(
                        formattedDate,
                        rs.getDouble("daily_total")
                ));
            }
            dailyAreaChart.getData().clear();
            dailyAreaChart.getData().add(series);
            dailyAreaChart.setTitle("المبيعات اليومية خلال آخر 7 أيام");

        } catch (Exception e) {
            dailyAreaChart.setTitle("المبيعات اليومية - لا توجد بيانات");
        }
    }

    private void loadMonthlySalesChart() {
        String query =
                "SELECT DATE_FORMAT(sale_date,'%Y-%m') AS month, SUM(total) as monthly_total " +
                        "FROM sales " +
                        "GROUP BY month " +
                        "ORDER BY month";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("المبيعات الشهرية");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String monthYear = rs.getString("month");
                String[] parts = monthYear.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                String monthName = getArabicMonthName(month) + " " + year;

                series.getData().add(new XYChart.Data<>(
                        monthName,
                        rs.getDouble("monthly_total")
                ));
            }
            monthlyLineChart.getData().clear();
            monthlyLineChart.getData().add(series);
            monthlyLineChart.setTitle("المبيعات الشهرية");

        } catch (Exception e) {
            monthlyLineChart.setTitle("المبيعات الشهرية - لا توجد بيانات");
        }
    }

    // ============================================================
    // 🟢 المخزون
    // ============================================================

    private void loadStockCharts() {
        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1️⃣ صالحة مقابل تالفة
            String validExpiredQuery =
                    "SELECT status, COUNT(*) AS count FROM product_status GROUP BY status";

            ObservableList<PieChart.Data> validExpiredData = FXCollections.observableArrayList();
            try (PreparedStatement ps = conn.prepareStatement(validExpiredQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    validExpiredData.add(new PieChart.Data(status + " (" + count + ")", count));
                }
            }
            applyDataOrEmpty(pieValidVsExpired, validExpiredData, "المنتجات الصالحة مقابل التالفة");

            // 2️⃣ أفضل المنتجات مبيعًا
            String bestSellerQuery =
                    "SELECT si.product_name, SUM(si.quantity) AS total_qty " +
                            "FROM sale_items si " +
                            "JOIN sales s ON si.sale_id = s.sale_id " +
                            "GROUP BY si.product_name " +
                            "ORDER BY total_qty DESC LIMIT 5";

            ObservableList<PieChart.Data> bestSellersStock = FXCollections.observableArrayList();
            try (PreparedStatement ps = conn.prepareStatement(bestSellerQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("total_qty");
                    bestSellersStock.add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
                }
            }
            applyDataOrEmpty(pieBestSellersStock, bestSellersStock, "أفضل 5 منتجات مبيعًا");

            // 3️⃣ المنتجات منخفضة الكمية
            String lowStockQuery =
                    "SELECT product_name, quantity FROM products WHERE quantity <= 10 ORDER BY quantity ASC LIMIT 5";

            ObservableList<PieChart.Data> lowStockData = FXCollections.observableArrayList();
            try (PreparedStatement ps = conn.prepareStatement(lowStockQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");
                    lowStockData.add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
                }
            }
            applyDataOrEmpty(pieLowStock, lowStockData, "المنتجات منخفضة الكمية");

            // 4️⃣ القيمة المالية للمخزون
            String stockValueQuery = "SELECT SUM(quantity * price1) AS total_value FROM products";
            double totalValue = 0;
            try (PreparedStatement ps = conn.prepareStatement(stockValueQuery);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalValue = rs.getDouble("total_value");
            }
            ObservableList<PieChart.Data> stockValueData = FXCollections.observableArrayList();
            if (totalValue > 0) {
                stockValueData.add(new PieChart.Data("القيمة المالية", totalValue));
            }
            applyDataOrEmpty(pieStockValue, stockValueData, "القيمة المالية الكلية للمخزون");

            // 5️⃣ مستويات المخزون
            String stockLevelQuery =
                    "SELECT " +
                            "SUM(CASE WHEN quantity > 100 THEN 1 ELSE 0 END) AS full_stock, " +
                            "SUM(CASE WHEN quantity BETWEEN 20 AND 100 THEN 1 ELSE 0 END) AS medium_stock, " +
                            "SUM(CASE WHEN quantity < 20 THEN 1 ELSE 0 END) AS low_stock " +
                            "FROM products";

            ObservableList<PieChart.Data> stockLevels = FXCollections.observableArrayList();
            try (PreparedStatement ps = conn.prepareStatement(stockLevelQuery);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int full = rs.getInt("full_stock");
                    int medium = rs.getInt("medium_stock");
                    int low = rs.getInt("low_stock");
                    stockLevels.add(new PieChart.Data("ممتلئ (" + full + ")", full));
                    stockLevels.add(new PieChart.Data("متوسط (" + medium + ")", medium));
                    stockLevels.add(new PieChart.Data("قليل (" + low + ")", low));
                }
            }
            applyDataOrEmpty(pieStockLevels, stockLevels, "توزيع مستويات المخزون");

        } catch (Exception e) {
            showFailedAlert("خطأ" , "تغذر تحميل الاستعلام.");
        }
    }

    private void applyDataOrEmpty(PieChart chart, ObservableList<PieChart.Data> data, String title) {
        chart.setTitle(title);
        chart.setPrefSize(300, 300);
        chart.setClockwise(true);
        chart.setStartAngle(90);
        chart.setPadding(new Insets(15, 10, 10, 10));

        boolean hasData = data.stream().anyMatch(d -> d.getPieValue() > 0);

        if (!hasData) {
            PieChart.Data emptyData = new PieChart.Data("لا توجد بيانات", 1);
            chart.setData(FXCollections.observableArrayList(emptyData));

            Platform.runLater(() -> {
                if (emptyData.getNode() != null) {
                    emptyData.getNode().setStyle("-fx-pie-color: lightgray;");
                }
            });
        } else {
            chart.setData(data);
            Platform.runLater(() -> data.forEach(this::addTooltip));
        }
    }

    private void addTooltip(PieChart.Data data) {
        Tooltip tooltip = new Tooltip(data.getName() + ": " + (int) data.getPieValue());
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Platform.runLater(() -> {
            if (data.getNode() != null) {
                Tooltip.install(data.getNode(), tooltip);
            }
        });
    }

    // ============================================================
    // 🟢 المشتريات
    // ============================================================

    private void loadPurchasesSummary() {
        String dailyPurchases = "SELECT COUNT(*) FROM purchases WHERE purchase_date = CURDATE()";
        String dailyTotalCost = "SELECT IFNULL(SUM(total),0) FROM purchases WHERE purchase_date = CURDATE()";
        String invoicesCount = "SELECT COUNT(*) FROM purchases";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(dailyPurchases);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblDailyPurchasesCount.setText(rs.getInt(1) + " عملية");
            }

            try (PreparedStatement ps = conn.prepareStatement(dailyTotalCost);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblDailyPurchaseCost.setText(String.format("%,.2f DZ", rs.getDouble(1)));
            }

            try (PreparedStatement ps = conn.prepareStatement(invoicesCount);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblPurchaseInvoicesCount.setText(rs.getInt(1) + " فاتورة");
            }

        } catch (Exception e) {
            lblDailyPurchasesCount.setText("خطأ في التحميل");
            lblDailyPurchaseCost.setText("خطأ في التحميل");
            lblPurchaseInvoicesCount.setText("خطأ في التحميل");
        }
    }

    private void loadTopPurchasedProducts() {
        String query =
                "SELECT pi.product_name, SUM(pi.quantity) AS total_qty " +
                        "FROM purchase_items pi " +
                        "JOIN purchases p ON pi.purchase_id = p.purchase_id " +
                        "GROUP BY pi.product_name " +
                        "ORDER BY total_qty DESC " +
                        "LIMIT 5";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("total_qty");
                pieData.add(new PieChart.Data(productName + " (" + quantity + ")", quantity));
            }
            pieTopPurchasedProducts.setData(pieData);
            pieTopPurchasedProducts.setTitle("أكثر المنتجات شراءً");

        } catch (Exception e) {
            pieTopPurchasedProducts.setTitle("أكثر المنتجات شراءً - لا توجد بيانات");
        }
    }

    private void loadDailyPurchasesChart() {
        String query =
                "SELECT purchase_date, SUM(total) as daily_total " +
                        "FROM purchases " +
                        "WHERE purchase_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "GROUP BY purchase_date " +
                        "ORDER BY purchase_date";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("المشتريات اليومية");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("purchase_date").toLocalDate();
                String formattedDate = date.getDayOfMonth() + " " + getArabicMonthName(date.getMonthValue());
                series.getData().add(new XYChart.Data<>(
                        formattedDate,
                        rs.getDouble("daily_total")
                ));
            }
            dailyPurchaseAreaChart.getData().clear();
            dailyPurchaseAreaChart.getData().add(series);
            dailyPurchaseAreaChart.setTitle("المشتريات اليومية خلال آخر 7 أيام");

        } catch (Exception e) {
            dailyPurchaseAreaChart.setTitle("المشتريات اليومية - لا توجد بيانات");
        }
    }

    private void loadMonthlyPurchasesChart() {
        String query =
                "SELECT DATE_FORMAT(purchase_date,'%Y-%m') AS month, SUM(total) as monthly_total " +
                        "FROM purchases " +
                        "GROUP BY month " +
                        "ORDER BY month";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("المشتريات الشهرية");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String monthYear = rs.getString("month");
                String[] parts = monthYear.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                String monthName = getArabicMonthName(month) + " " + year;

                series.getData().add(new XYChart.Data<>(
                        monthName,
                        rs.getDouble("monthly_total")
                ));
            }
            monthlyPurchaseLineChart.getData().clear();
            monthlyPurchaseLineChart.getData().add(series);
            monthlyPurchaseLineChart.setTitle("المشتريات الشهرية");

        } catch (Exception e) {
            monthlyPurchaseLineChart.setTitle("المشتريات الشهرية - لا توجد بيانات");
        }
    }

    // ============================================================
    // 🟢 العملاء
    // ============================================================

    private void loadClientsStats() {
        String sqlRegularClients = "SELECT COUNT(*) AS cnt FROM client";
        String sqlDebtorClients = "SELECT COUNT(DISTINCT customer_id) AS cnt FROM client_sales WHERE total > 0";
        String sqlTotalDebt = "SELECT SUM(total) AS total_debt FROM client_sales";
        String sqlInvoicesCount = "SELECT COUNT(*) AS cnt FROM client_sales";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // عدد العملاء النظاميين
            try (PreparedStatement ps = conn.prepareStatement(sqlRegularClients);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblRegularClients.setText(rs.getInt("cnt") + " عميل");
                }
            }

            // عدد العملاء المديونيين
            try (PreparedStatement ps = conn.prepareStatement(sqlDebtorClients);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblDebtorClients.setText(rs.getInt("cnt") + " عميل");
                }
            }

            // إجمالي الديون
            try (PreparedStatement ps = conn.prepareStatement(sqlTotalDebt);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblTotalDebt.setText(String.format("%,.2f DZ", rs.getDouble("total_debt")));
                }
            }

            // عدد الفواتير
            try (PreparedStatement ps = conn.prepareStatement(sqlInvoicesCount);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lblInvoicesCount2.setText(rs.getInt("cnt") + " فاتورة");
                }
            }
        } catch (Exception e) {
            lblRegularClients.setText("خطأ في التحميل");
            lblDebtorClients.setText("خطأ في التحميل");
            lblTotalDebt.setText("خطأ في التحميل");
            lblInvoicesCount2.setText("خطأ في التحميل");
        }
    }

    private void loadClientsCharts() {
        loadClientsStatusChart();
        loadDailyClientSalesChart();
        loadMonthlyClientSalesChart();
    }

    private void loadClientsStatusChart() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        String sql = "SELECT " +
                "SUM(CASE WHEN status = 'مدفوع' THEN 1 ELSE 0 END) AS paid, " +
                "SUM(CASE WHEN status != 'مدفوع' THEN 1 ELSE 0 END) AS unpaid " +
                "FROM client_debts";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int paid = rs.getInt("paid");
                int unpaid = rs.getInt("unpaid");
                data.add(new PieChart.Data("دافعون (" + paid + ")", paid));
                data.add(new PieChart.Data("مدينون (" + unpaid + ")", unpaid));
            }
            pieClientsStatus.setData(data);
            pieClientsStatus.setTitle("حالة عملاء الديون");

        } catch (Exception e) {
            pieClientsStatus.setTitle("حالة عملاء الديون - لا توجد بيانات");
        }
    }

    private void loadDailyClientSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("مبيعات العملاء اليومية");

        String sql = "SELECT sale_date, SUM(total) AS daily_total " +
                "FROM client_sales " +
                "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY sale_date " +
                "ORDER BY sale_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                double total = rs.getDouble("daily_total");
                String formattedDate = date.getDayOfMonth() + " " + getArabicMonthName(date.getMonthValue());
                series.getData().add(new XYChart.Data<>(formattedDate, total));
            }

            areaDailySales.getData().clear();
            areaDailySales.getData().add(series);
            areaDailySales.setTitle("مبيعات العملاء اليومية خلال آخر 7 أيام");

        } catch (Exception e) {
            areaDailySales.setTitle("مبيعات العملاء اليومية - لا توجد بيانات");
        }
    }

    private void loadMonthlyClientSalesChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("مبيعات العملاء الشهرية");

        String sql = "SELECT DATE_FORMAT(sale_date,'%Y-%m') AS month, SUM(total) AS monthly_total " +
                "FROM client_sales " +
                "GROUP BY month " +
                "ORDER BY month";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String monthYear = rs.getString("month");
                String[] parts = monthYear.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                String monthName = getArabicMonthName(month) + " " + year;

                series.getData().add(new XYChart.Data<>(monthName, rs.getDouble("monthly_total")));
            }

            lineMonthlySales.getData().clear();
            lineMonthlySales.getData().add(series);
            lineMonthlySales.setTitle("مبيعات العملاء الشهرية");

        } catch (Exception e) {
            lineMonthlySales.setTitle("مبيعات العملاء الشهرية - لا توجد بيانات");
        }
    }

    // 🔹 دالة مساعدة للحصول على اسم الشهر بالعربية
    private String getArabicMonthName(int month) {
        String[] arabicMonths = {
                "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
                "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        };
        return (month >= 1 && month <= 12) ? arabicMonths[month - 1] : "غير معروف";
    }
}