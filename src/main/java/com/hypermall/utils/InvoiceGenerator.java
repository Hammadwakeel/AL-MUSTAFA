package com.hypermall.utils;

import com.hypermall.models.CartItem;
import com.hypermall.models.Customer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceGenerator {

    /**
     * Builds the complete text for the receipt including the Salesman, Unit Price, 
     * multiple cart items, and Khata calculations.
     */
    public static String buildInvoiceString(List<CartItem> items, double cash, double cartTotal, 
                                            double oldDebt, double newBalance, double debtCleared, 
                                            double change, Customer c, String salesmanName) {
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        StringBuilder sb = new StringBuilder();
        
        String dLine = "============================================\n";
        String sLine = "--------------------------------------------\n";
        
        sb.append(dLine);
        sb.append("    AL MUSTAFA ELECTRIC WHOLESALE STORE     \n");
        sb.append("          Near HBL Bank, Jehangira          \n");
        sb.append("  Osaka | Tuff | RC | Fans | Solar | China  \n");
        sb.append("               Wires & Cables               \n");
        sb.append("     0311-9396640    |    0343-1216306      \n");
        sb.append(dLine);
        
        sb.append(String.format("Date:     %s\n", dtf.format(LocalDateTime.now())));
        
        // Add the active Salesman
        sb.append(String.format("Salesman: %s\n", salesmanName != null ? salesmanName : "Admin"));
        
        // Handle walk-in customers safely
        String cName = (c != null && c.getName() != null) ? c.getName() : "Walk-in";
        String cPhone = (c != null && c.getPhone() != null) ? c.getPhone() : "N/A";
        
        if (cName.length() > 34) cName = cName.substring(0, 31) + "...";
        if (cPhone.length() > 34) cPhone = cPhone.substring(0, 31) + "...";
        
        sb.append(String.format("Cust:     %-34s\n", cName));
        sb.append(String.format("Ph:       %-34s\n", cPhone));
        
        sb.append(dLine);
        
        // 4 COLUMN LAYOUT INCLUDING UNIT PRICE
        sb.append(String.format("%-18s %4s %8s %10s\n", "Item", "Qty", "Price", "Amount"));
        sb.append(sLine);
        
        for (CartItem item : items) {
            String name = item.getProduct().getName();
            if (name.length() > 16) name = name.substring(0, 16);
            
            sb.append(String.format("%-18s %4d %8s %10s\n", 
                name, 
                item.getQuantity(), 
                String.format("%,.0f", item.getUnitPrice()),   // FIXED: getUnitPrice()
                String.format("%,.0f", item.getSubtotal())     
            ));
        }
        
        sb.append(dLine);
        sb.append(String.format("%-24s %19s\n", "BILL AMOUNT:", String.format("Rs. %,.2f", cartTotal)));
        
        if (oldDebt > 0) {
            sb.append(String.format("%-24s %19s\n", "PREVIOUS KHATA:", String.format("Rs. %,.2f", oldDebt)));
            sb.append(sLine);
            sb.append(String.format("%-24s %19s\n", "TOTAL DUE:", String.format("Rs. %,.2f", (cartTotal + oldDebt))));
        }
        
        sb.append(String.format("%-24s %19s\n", "CASH RECEIVED:", String.format("Rs. %,.2f", cash)));
        sb.append(dLine);
        
        if (debtCleared > 0) {
            sb.append(String.format("%-24s %19s\n", "KHATA CLEARED:", String.format("Rs. %,.2f", debtCleared)));
        }
        
        if (newBalance > 0) {
            sb.append(String.format("%-24s %19s\n", "REMAINING KHATA:", String.format("Rs. %,.2f", newBalance)));
        } else {
            sb.append(String.format("%-24s %19s\n", "KHATA BALANCE:", "Rs. 0.00"));
        }
        
        sb.append(String.format("%-24s %19s\n", "CHANGE RETURN:", String.format("Rs. %,.2f", change)));
        
        sb.append(dLine);
        sb.append("          THANK YOU FOR SHOPPING!           \n");
        sb.append(dLine);
        
        return sb.toString();
    }

    /**
     * Saves the generated text to a physical file in the 'invoices' folder.
     */
    public static boolean saveInvoiceLocally(String invoiceContent, String absolutePath) {
        try {
            File file = new File(absolutePath);
            
            // Ensures the 'invoices' folder is created if it was deleted
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.print(invoiceContent);
            }
            System.out.println("✅ Invoice saved to: " + absolutePath);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error saving invoice: " + e.getMessage());
            return false;
        }
    }
}