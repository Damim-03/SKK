module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires usb4java;
    requires mysql.connector.j;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires eu.hansolo.toolbox;

    opens com.example to javafx.fxml;
    opens com.example.library.controller.home to javafx.fxml;
    opens com.example.library.controller.inventory to javafx.fxml;
    opens com.example.library.controller.sales to javafx.fxml;
    opens com.example.library.model to javafx.base;

    exports com.example;
    exports com.example.library.controller.inventory;
    exports com.example.library.controller.home;
    exports com.example.library.controller.sales;
}