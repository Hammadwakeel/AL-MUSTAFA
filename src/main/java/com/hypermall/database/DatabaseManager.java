package com.hypermall.database;

import java.sql.*;
import java.io.File;

public class DatabaseManager {
    // We point to a folder named 'db' sitting next to the .exe
    private static final String DB_URL = "jdbc:sqlite:db/inventory.db";

    public static Connection connect() throws SQLException {
        // Create the 'db' folder if it doesn't exist so SQLite doesn't crash
        File dbFolder = new File("db");
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        // 1. Create the new salesmen table
        String salesmenTable = "CREATE TABLE IF NOT EXISTS salesmen (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL);";

        // 2. Customers table
        String customerTable = "CREATE TABLE IF NOT EXISTS customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "phone TEXT, " +
                "balance REAL DEFAULT 0.0);";

        // 3. Products table
        String productTable = "CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "sku TEXT UNIQUE, " +
                "price REAL, " +
                "wholesale_price REAL, " +
                "stock INTEGER);";

        // 4. Sales table (Now including salesman_name)
        String salesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "customer_id INTEGER, " +
                "salesman_name TEXT, " + // <-- NEW COLUMN
                "quantity INTEGER, " +
                "unit_price REAL, " +
                "total_price REAL, " +
                "price_type TEXT, " +
                "sale_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "invoice_path TEXT, " + 
                "FOREIGN KEY(product_id) REFERENCES products(id), " +
                "FOREIGN KEY(customer_id) REFERENCES customers(id));";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            // Execute all table creation queries
            stmt.execute(salesmenTable);
            stmt.execute(customerTable);
            stmt.execute(productTable);
            stmt.execute(salesTable);
            
            // Insert a default "Admin" salesman so the dropdown is never empty
            stmt.execute("INSERT OR IGNORE INTO salesmen (name) VALUES ('Admin')");
            
            System.out.println("✅ Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Database init error: " + e.getMessage());
        }
    }
}