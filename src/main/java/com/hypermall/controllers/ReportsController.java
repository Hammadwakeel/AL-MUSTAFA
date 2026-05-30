package com.hypermall.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import com.hypermall.database.SalesDAO;
import com.hypermall.database.ProductDAO;
import com.hypermall.models.SaleRecord;

import java.time.LocalDate;
import java.util.List;

public class ReportsController {

    @FXML
    private Label totalStockValueLabel;
    @FXML
    private Label rangeRevenueLabel;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<SaleRecord> salesTable;
    @FXML
    private TableColumn<SaleRecord, Integer> idCol, qtyCol;
    @FXML
    private TableColumn<SaleRecord, String> dateCol, productCol, salesmanCol;
    @FXML
    private TableColumn<SaleRecord, Double> amountCol;

    @FXML
    public void initialize() {
        // Link table columns to the SaleRecord model
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        salesmanCol.setCellValueFactory(new PropertyValueFactory<>("salesman"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Load the Total Inventory Value from ProductDAO
        double totalStockValue = ProductDAO.getTotalInventoryValue();
        totalStockValueLabel.setText(String.format("Rs. %,.2f", totalStockValue));

        // Default to showing Today's sales when page opens
        filterDaily();
    }

    // --- DATE FILTER BUTTONS ---
    @FXML
    private void filterDaily() {
        loadData(LocalDate.now(), LocalDate.now());
    }

    @FXML
    private void filterWeekly() {
        loadData(LocalDate.now().minusDays(7), LocalDate.now());
    }

    @FXML
    private void filterMonthly() {
        loadData(LocalDate.now().withDayOfMonth(1), LocalDate.now());
    }

    @FXML
    private void filterYearly() {
        loadData(LocalDate.now().withDayOfYear(1), LocalDate.now());
    }

    @FXML
    private void filterCustom() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            loadData(startDatePicker.getValue(), endDatePicker.getValue());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select both a Start Date and an End Date.");
            alert.showAndWait();
        }
    }

    // --- FETCH AND UPDATE UI ---
    private void loadData(LocalDate start, LocalDate end) {
        // SQLite expects YYYY-MM-DD
        String startStr = start.toString();
        String endStr = end.toString();

        List<SaleRecord> records = SalesDAO.getSalesByDateRange(startStr, endStr);
        salesTable.setItems(FXCollections.observableArrayList(records));

        // Calculate total revenue for this specific table view
        double totalRangeRevenue = records.stream().mapToDouble(SaleRecord::getTotalAmount).sum();
        rangeRevenueLabel.setText(String.format("Rs. %,.2f", totalRangeRevenue));
    }
}