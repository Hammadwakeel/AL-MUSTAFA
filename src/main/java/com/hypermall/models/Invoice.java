package com.hypermall.models;

import java.time.LocalDate;

public class Invoice {
    private int id;
    private double total;
    private LocalDate date;

    public Invoice(int id, double total, String date) {
        this.id = id;
        this.total = total;
        this.date = LocalDate.parse(date);
    }

    public int getId() { return id; }
    public double getTotal() { return total; }
    public LocalDate getDate() { return date; }

    @Override
    public String toString() {
        return String.format("ID: %-5d | Date: %-12s | Total: Rs. %-10.2f", id, date, total);
    }
}