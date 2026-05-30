package com.hypermall.models;
import javafx.beans.property.*;

public class CartItem {
    private final Product product;
    private final IntegerProperty quantity;
    private double unitPrice; 
    private String priceType; 

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = product.getPrice(); // Defaults to retail
        this.priceType = "Retail";
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int q) { this.quantity.set(q); }
    public IntegerProperty quantityProperty() { return quantity; }
    
    public double getUnitPrice() { return unitPrice; }
    
    // --- NEW: Price Tier Toggles ---
    public void applyRetailPrice() {
        this.unitPrice = product.getPrice();
        this.priceType = "Retail";
    }
    
    public void applyWholesalePrice() {
        this.unitPrice = product.getWholesalePrice();
        this.priceType = "Wholesale";
    }
    
    public void applyCustomPrice(double customPrice) {
        this.unitPrice = customPrice;
        this.priceType = "Custom";
    }
    
    public String getPriceType() { return priceType; }
    
    public double getSubtotal() { return unitPrice * getQuantity(); }
}