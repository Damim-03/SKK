module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires usb4java;
    requires mysql.connector.j;

    opens com.example to javafx.fxml;
    opens com.example.library.controller.home to javafx.fxml;
    opens com.example.library.controller.inventory to javafx.fxml;
    opens com.example.library.controller.sales to javafx.fxml;

    exports com.example;
    exports com.example.library.controller.inventory;
    exports com.example.library.controller.home;
    exports com.example.library.controller.sales;
}