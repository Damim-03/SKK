package com.example.library.model;

import com.example.library.util.DatabaseConnection;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryLoader {

    public static void loadCategoriesIntoMenuButton(MenuButton categoryMenuButton) {
        String query = "SELECT name_of_category FROM category";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            categoryMenuButton.getItems().clear();

            while (rs.next()) {
                String categoryName = rs.getString("name_of_category");
                MenuItem item = new MenuItem(categoryName);
                item.setOnAction(e -> categoryMenuButton.setText(categoryName));
                categoryMenuButton.getItems().add(item);
            }

            categoryMenuButton.setText("Select Category");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
