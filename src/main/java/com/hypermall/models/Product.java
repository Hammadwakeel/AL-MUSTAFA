package com.hypermall.models;

public class Product {
    private int id;
    private String name;
    private String sku;
    private double price;
    private double wholesalePrice; // NEW
    private int stock;

    public Product(int id, String name, String sku, double price, double wholesalePrice, int stock) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.wholesalePrice = wholesalePrice;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public double getPrice() { return price; }
    public double getWholesalePrice() { return wholesalePrice; }
    public int getStock() { return stock; }
}