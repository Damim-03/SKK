package com.example.library.model;

import com.example.library.util.DatabaseConnection;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnitLoader {

    public static void loadUnitsIntoMenuButton(MenuButton unitMenuButton) {
        String query = "SELECT unit_name FROM unit";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            unitMenuButton.getItems().clear();

            while (rs.next()) {
                String unitName = rs.getString("unit_name");
                MenuItem item = new MenuItem(unitName);
                item.setOnAction(e -> unitMenuButton.setText(unitName));
                unitMenuButton.getItems().add(item);
            }

            unitMenuButton.setText("Select Unit");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
