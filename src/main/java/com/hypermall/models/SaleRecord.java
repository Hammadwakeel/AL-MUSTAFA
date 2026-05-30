package com.hypermall.models;

import javafx.beans.property.*;

public class SaleRecord {
    private final IntegerProperty id;
    private final StringProperty productName;
    private final StringProperty salesman;
    private final IntegerProperty quantity;
    private final DoubleProperty totalPrice;
    private final StringProperty date;
    private final StringProperty invoicePath;

    public SaleRecord(int id, String productName, String salesman, int quantity, double totalPrice, String date, String invoicePath) {
        this.id = new SimpleIntegerProperty(id);
        this.productName = new SimpleStringProperty(productName);
        this.salesman = new SimpleStringProperty(salesman != null ? salesman : "Admin");
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(totalPrice);
        this.date = new SimpleStringProperty(date);
        this.invoicePath = new SimpleStringProperty(invoicePath);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getProductName() { return productName.get(); }
    public String getSalesman() { return salesman.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotalPrice() { return totalPrice.get(); }
    public String getDate() { return date.get(); }
    public String getInvoicePath() { return invoicePath.get(); }

    // Aliases specifically for the new ReportsController mapping
    public double getTotalAmount() { return totalPrice.get(); }
    public String getSaleDate() { return date.get(); }

    // Property Getters (For JavaFX Tables)
    public IntegerProperty idProperty() { return id; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty salesmanProperty() { return salesman; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public StringProperty dateProperty() { return date; }
    public StringProperty invoicePathProperty() { return invoicePath; }
}