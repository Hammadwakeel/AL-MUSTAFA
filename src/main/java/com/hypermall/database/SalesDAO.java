package com.hypermall.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.hypermall.models.CartItem;
import com.hypermall.models.SaleRecord; 

public class SalesDAO {

    /**
     * Ensures the database table has all the required columns for the 
     * new features (Salesman and Invoice Tracking).
     */
    private static void ensureSchemaUpToDate() {
        try (Connection conn = DatabaseManager.connect(); Statement stmt = conn.createStatement()) {
            // Try adding invoice_path if it doesn't exist
            try { stmt.execute("ALTER TABLE sales ADD COLUMN invoice_path TEXT"); } catch (SQLException ignore) {}
            // Try adding salesman_name if it doesn't exist
            try { stmt.execute("ALTER TABLE sales ADD COLUMN salesman_name TEXT"); } catch (SQLException ignore) {}
        } catch (SQLException e) {
            System.err.println("Schema Update Error: " + e.getMessage());
        }
    }

    /**
     * Calculates revenue totals for Dashboard/Reports.
     * Uses 'localtime' to ensure sales show up correctly in Pakistan Time.
     */
    public static double getSalesSummary(String filter) {
        ensureSchemaUpToDate();
        String timeModifier = "date('now', 'localtime')"; 
        if (filter.equalsIgnoreCase("Weekly")) timeModifier = "date('now', 'localtime', '-7 days')";
        if (filter.equalsIgnoreCase("Monthly")) timeModifier = "date('now', 'localtime', '-1 month')";

        String sql = "SELECT SUM(total_price) FROM sales WHERE date(sale_date, 'localtime') >= " + timeModifier;
        
        try (Connection conn = DatabaseManager.connect(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return 0.0;
    }

    public static double getDailySalesTotal() { return getSalesSummary("Daily"); }

    public static double getCustomerTotalSales(int customerId) {
        String sql = "SELECT SUM(total_price) FROM sales WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    /**
     * Fetches a specific customer's purchase history.
     */
    public static List<SaleRecord> getSalesByCustomer(int customerId) {
        ensureSchemaUpToDate();
        List<SaleRecord> history = new ArrayList<>();
        String sql = "SELECT s.id, s.sale_date, p.name AS product_name, s.salesman_name, s.quantity, s.total_price, s.invoice_path " +
                     "FROM sales s " +
                     "JOIN products p ON s.product_id = p.id " +
                     "WHERE s.customer_id = ? ORDER BY s.sale_date DESC";
                     
        try (Connection conn = DatabaseManager.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new SaleRecord(
                    rs.getInt("id"),
                    rs.getString("product_name"),
                    rs.getString("salesman_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getString("sale_date"),
                    rs.getString("invoice_path"),
                    customerId
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    /**
     * NEW: Fetches sales records for the Reports screen.
     * Uses 'localtime' conversion to fix the "Today's sales not showing" bug.
     */
    public static List<SaleRecord> getSalesByDateRange(String startDate, String endDate) {
        ensureSchemaUpToDate();
        List<SaleRecord> records = new ArrayList<>();
        String sql = "SELECT s.id, p.name AS product_name, s.salesman_name, s.quantity, s.total_price, s.sale_date, s.invoice_path, s.customer_id " +
                     "FROM sales s " +
                     "LEFT JOIN products p ON s.product_id = p.id " +
                     "WHERE DATE(s.sale_date, 'localtime') >= ? AND DATE(s.sale_date, 'localtime') <= ? " +
                     "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseManager.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(new SaleRecord(
                    rs.getInt("id"),
                    rs.getString("product_name") != null ? rs.getString("product_name") : "Unknown",
                    rs.getString("salesman_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("total_price"),
                    rs.getString("sale_date"),
                    rs.getString("invoice_path"),
                    rs.getInt("customer_id")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching sales range: " + e.getMessage());
        }
        return records;
    }

    /**
     * Deletes a sale record and restores the product stock.
     * Also resets customer balance if the sale was linked to a customer.
     * Returns true on success, false on failure.
     */
    public static boolean deleteSale(int saleId, int productId, int quantity, double totalPrice, int customerId, double oldBalance) {
        ensureSchemaUpToDate();
        try (Connection conn = DatabaseManager.connect()) {
            conn.setAutoCommit(false);

            // 1. Restore product stock
            String restoreStock = "UPDATE products SET stock = stock + ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(restoreStock)) {
                ps.setInt(1, quantity);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }

            // 2. Restore customer balance (if linked to customer)
            if (customerId > 0) {
                String restoreCredit = "UPDATE customers SET balance = balance + ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(restoreCredit)) {
                    ps.setDouble(1, totalPrice);
                    ps.setInt(2, customerId);
                    ps.executeUpdate();
                }
            }

            // 3. Delete the sale record
            String deleteSale = "DELETE FROM sales WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSale)) {
                ps.setInt(1, saleId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fallback method for existing calls.
     */
    public static boolean processBulkSale(List<CartItem> cartItems, int customerId, double newBalance, String invoicePath) {
        return processBulkSale(cartItems, customerId, newBalance, invoicePath, "Admin");
    }

    /**
     * CORE TRANSACTION: Saves items, updates stock, updates customer khata, and logs salesman.
     */
    public static boolean processBulkSale(List<CartItem> cartItems, int customerId, double newBalance, String invoicePath, String salesmanName) {
        ensureSchemaUpToDate();
        
        try (Connection conn = DatabaseManager.connect()) {
            conn.setAutoCommit(false); 
            String updateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
            String insertSale = "INSERT INTO sales (product_id, customer_id, salesman_name, quantity, unit_price, total_price, price_type, invoice_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String updateCredit = "UPDATE customers SET balance = ? WHERE id = ?";

            try {
                for (CartItem item : cartItems) {
                    // 1. Update Stock Levels
                    try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
                        ps.setInt(1, item.getQuantity()); 
                        ps.setInt(2, item.getProduct().getId());
                        ps.executeUpdate();
                    }
                    
                    // 2. Log individual item sale
                    try (PreparedStatement ps = conn.prepareStatement(insertSale)) {
                        ps.setInt(1, item.getProduct().getId());
                        if (customerId > 0) ps.setInt(2, customerId); else ps.setNull(2, java.sql.Types.INTEGER);
                        ps.setString(3, salesmanName); 
                        ps.setInt(4, item.getQuantity()); 
                        ps.setDouble(5, item.getUnitPrice());
                        ps.setDouble(6, item.getSubtotal()); 
                        ps.setString(7, item.getPriceType());
                        ps.setString(8, invoicePath); 
                        ps.executeUpdate();
                    }
                }
                
                // 3. Update Customer Balance (Khata)
                if (customerId > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(updateCredit)) {
                        ps.setDouble(1, newBalance); 
                        ps.setInt(2, customerId);
                        ps.executeUpdate();
                    }
                }
                
                conn.commit(); 
                return true;
            } catch (SQLException e) {
                conn.rollback(); 
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }
}