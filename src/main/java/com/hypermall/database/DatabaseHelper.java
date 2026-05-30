// package com.hypermall.database;

// import java.sql.*;
// import java.util.ArrayList;
// import java.util.List;
// import com.hypermall.models.Product;
// import com.hypermall.models.CartItem;
// import com.hypermall.models.Customer;

// public class DatabaseHelper {
//     private static final String URL = "jdbc:sqlite:hypermall_inventory.db";

//     public static Connection connect() throws SQLException {
//         try { Class.forName("org.sqlite.JDBC"); } 
//         catch (ClassNotFoundException e) { e.printStackTrace(); }
//         return DriverManager.getConnection(URL);
//     }

//     public static void initialize() {
//         String productTable = "CREATE TABLE IF NOT EXISTS products (" +
//                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                      "name TEXT NOT NULL, " +
//                      "sku TEXT UNIQUE NOT NULL, " +
//                      "price REAL NOT NULL, " +
//                      "wholesale_price REAL DEFAULT 0.0, " + 
//                      "stock INTEGER NOT NULL);";

//         // UPDATED: Added 'balance' for credit tracking
//         String customerTable = "CREATE TABLE IF NOT EXISTS customers (" +
//                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                      "name TEXT NOT NULL, " +
//                      "phone TEXT UNIQUE NOT NULL, " +
//                      "balance REAL DEFAULT 0.0);";

//         String salesTable = "CREATE TABLE IF NOT EXISTS sales (" +
//                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                      "product_id INTEGER, " +
//                      "customer_id INTEGER, " + 
//                      "quantity INTEGER, " +
//                      "unit_price REAL, " +     
//                      "total_price REAL, " +
//                      "price_type TEXT, " +     
//                      "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

//         try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
//             stmt.execute(productTable);
//             stmt.execute(customerTable);
//             stmt.execute(salesTable);
//             System.out.println("Database tables initialized successfully.");
//         } catch (SQLException e) { e.printStackTrace(); }
//     }

//     public static double getSalesSummary(String filter) {
//         String timeModifier = "date('now')"; 
//         if (filter.equalsIgnoreCase("Weekly")) timeModifier = "date('now', '-7 days')";
//         if (filter.equalsIgnoreCase("Monthly")) timeModifier = "date('now', '-1 month')";

//         String sql = "SELECT SUM(total_price) FROM sales WHERE date(sale_date) >= " + timeModifier;
//         try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
//             if (rs.next()) return rs.getDouble(1);
//         } catch (SQLException e) { e.printStackTrace(); }
//         return 0.0;
//     }

//     public static double getDailySalesTotal() { return getSalesSummary("Daily"); }

//     // --- NEW: Get Customer's Historical Sales Total ---
//     public static double getCustomerTotalSales(int customerId) {
//         String sql = "SELECT SUM(total_price) FROM sales WHERE customer_id = ?";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, customerId);
//             ResultSet rs = pstmt.executeQuery();
//             if (rs.next()) return rs.getDouble(1);
//         } catch (SQLException e) { e.printStackTrace(); }
//         return 0.0;
//     }

//     // --- UPDATED: Customer Management ---
//     public static boolean addCustomer(String name, String phone) {
//         String sql = "INSERT INTO customers(name, phone, balance) VALUES(?,?, 0.0)";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, name); pstmt.setString(2, phone);
//             pstmt.executeUpdate(); return true;
//         } catch (SQLException e) { return false; }
//     }

//     public static List<Customer> searchCustomers(String query) {
//         List<Customer> results = new ArrayList<>();
//         String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ?";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, "%" + query + "%"); pstmt.setString(2, "%" + query + "%");
//             ResultSet rs = pstmt.executeQuery();
//             while (rs.next()) {
//                 results.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getDouble("balance")));
//             }
//         } catch (SQLException e) { e.printStackTrace(); }
//         return results;
//     }

//     // --- UPDATED: Process Sales with Credit Calculation ---
//     public static boolean processBulkSale(List<CartItem> cartItems, int customerId, double amountPaid, double finalTotal) {
//         String updateStock = "UPDATE products SET stock = stock - ? WHERE id = ?";
//         String insertSale = "INSERT INTO sales (product_id, customer_id, quantity, unit_price, total_price, price_type) VALUES (?, ?, ?, ?, ?, ?)";
//         String updateCredit = "UPDATE customers SET balance = balance + ? WHERE id = ?";

//         try (Connection conn = connect()) {
//             conn.setAutoCommit(false); 
//             try {
//                 // 1. Log Sales and Deduct Stock
//                 for (CartItem item : cartItems) {
//                     try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
//                         ps.setInt(1, item.getQuantity());
//                         ps.setInt(2, item.getProduct().getId());
//                         ps.executeUpdate();
//                     }
//                     try (PreparedStatement ps = conn.prepareStatement(insertSale)) {
//                         ps.setInt(1, item.getProduct().getId());
//                         if (customerId > 0) ps.setInt(2, customerId); else ps.setNull(2, java.sql.Types.INTEGER);
//                         ps.setInt(3, item.getQuantity());
//                         ps.setDouble(4, item.getUnitPrice());
//                         ps.setDouble(5, item.getSubtotal());
//                         ps.setString(6, item.getPriceType());
//                         ps.executeUpdate();
//                     }
//                 }
                
//                 // 2. Add to Customer Credit Ledger if they underpaid
//                 if (customerId > 0 && amountPaid < finalTotal) {
//                     double creditOwed = finalTotal - amountPaid;
//                     try (PreparedStatement ps = conn.prepareStatement(updateCredit)) {
//                         ps.setDouble(1, creditOwed);
//                         ps.setInt(2, customerId);
//                         ps.executeUpdate();
//                     }
//                 }

//                 conn.commit();
//                 return true;
//             } catch (SQLException e) {
//                 conn.rollback();
//                 return false;
//             }
//         } catch (SQLException e) { return false; }
//     }

//     public static boolean addProduct(String name, String sku, double retailPrice, double wholesalePrice, int stock) {
//         String sql = "INSERT INTO products(name, sku, price, wholesale_price, stock) VALUES(?,?,?,?,?)";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, name); pstmt.setString(2, sku);
//             pstmt.setDouble(3, retailPrice); pstmt.setDouble(4, wholesalePrice); pstmt.setInt(5, stock);
//             pstmt.executeUpdate(); return true;
//         } catch (SQLException e) { return false; }
//     }

//     public static List<Product> searchProducts(String query) {
//         List<Product> results = new ArrayList<>();
//         String sql = "SELECT * FROM products WHERE name LIKE ? OR sku LIKE ?";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, "%" + query + "%"); pstmt.setString(2, "%" + query + "%");
//             ResultSet rs = pstmt.executeQuery();
//             while (rs.next()) {
//                 results.add(new Product(rs.getInt("id"), rs.getString("name"), 
//                             rs.getString("sku"), rs.getDouble("price"), rs.getDouble("wholesale_price"), rs.getInt("stock")));
//             }
//         } catch (SQLException e) { e.printStackTrace(); }
//         return results;
//     }

//     public static boolean updateProduct(int id, String name, double price, double wholesalePrice, int stock) {
//         String sql = "UPDATE products SET name = ?, price = ?, wholesale_price = ?, stock = ? WHERE id = ?";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, name); pstmt.setDouble(2, price); pstmt.setDouble(3, wholesalePrice);
//             pstmt.setInt(4, stock); pstmt.setInt(5, id);
//             return pstmt.executeUpdate() > 0;
//         } catch (SQLException e) { return false; }
//     }

//     public static boolean deleteProduct(int id) {
//         String sql = "DELETE FROM products WHERE id = ?";
//         try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, id); return pstmt.executeUpdate() > 0;
//         } catch (SQLException e) { return false; }
//     }
// }