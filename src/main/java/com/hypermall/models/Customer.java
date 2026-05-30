package com.hypermall.models;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private double balance; // Tracks how much money they owe the store

    public Customer(int id, String name, String phone, double balance) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.balance = balance;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getBalance() { return balance; }
    
    @Override
    public String toString() {
        return name + " (" + phone + ") - Owes: $" + balance; 
    }
}