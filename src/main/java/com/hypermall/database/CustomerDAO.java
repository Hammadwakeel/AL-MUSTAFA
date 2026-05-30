package com.hypermall.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hypermall.models.Customer;

public class CustomerDAO {

    public static boolean addCustomer(String name, String phone) {
        String sql = "INSERT INTO customers(name, phone, balance) VALUES(?,?, 0.0)";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setString(2, phone);
            pstmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public static List<Customer> searchCustomers(String query) {
        List<Customer> results = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%"); pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getDouble("balance")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }

    public static boolean updateCustomer(int id, String name, String phone, double balance) {
        String sql = "UPDATE customers SET name = ?, phone = ?, balance = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name); pstmt.setString(2, phone); pstmt.setDouble(3, balance); pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id); return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}