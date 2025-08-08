// File: com.example.library.dao.ProductDAO.java
package com.example.library.model;

import com.example.library.model.Product;
import com.example.library.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {
    public static Product getProductByBarcode(String barcode) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = new Product();
                product.setBarcode(rs.getString("barcode"));
                product.setProductName(rs.getString("product_name"));
                product.setPrice1(rs.getDouble("price1"));
                // Add more fields if needed
                return product;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
