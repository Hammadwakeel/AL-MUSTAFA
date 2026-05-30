package com.hypermall.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hypermall.models.Product;

public class ProductDAO {

    public static boolean addProduct(String name, String sku, double retailPrice, double wholesalePrice, int stock) {
        String sql = "INSERT INTO products(name, sku, price, wholesale_price, stock) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setString(2, sku);
            pstmt.setDouble(3, retailPrice); pstmt.setDouble(4, wholesalePrice); pstmt.setInt(5, stock);
            pstmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public static List<Product> searchProducts(String query) {
        List<Product> results = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR sku LIKE ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%"); pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new Product(rs.getInt("id"), rs.getString("name"), 
                            rs.getString("sku"), rs.getDouble("price"), rs.getDouble("wholesale_price"), rs.getInt("stock")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    public static boolean updateProduct(int id, String name, double price, double wholesalePrice, int stock) {
        String sql = "UPDATE products SET name = ?, price = ?, wholesale_price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setDouble(2, price); pstmt.setDouble(3, wholesalePrice);
            pstmt.setInt(4, stock); pstmt.setInt(5, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ==========================================
    // NEW: Calculate Total Inventory Value
    // ==========================================
    public static double getTotalInventoryValue() {
        String sql = "SELECT SUM(price * stock) AS total_value FROM products";
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total_value");
            }
        } catch (SQLException e) {
            System.out.println("Error calculating inventory value: " + e.getMessage());
        }
        return 0.0;
    }
}