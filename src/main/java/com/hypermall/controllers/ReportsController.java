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
import java.util.Optional;

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

        // Enable row selection for delete action
        salesTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

    @FXML
    private void deleteSelectedSale() {
        SaleRecord selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a sale record to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Sale Record #" + selected.getId() + "?");
        confirm.setContentText("This will:\n- Restore " + selected.getQuantity() + " units to stock\n- Remove Rs. " + String.format("%,.2f", selected.getTotalPrice()) + " from customer balance\n\nThis action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Get product ID from the product name
            int productId = ProductDAO.getProductIdByName(selected.getProductName());
            int customerId = selected.getCustomerId();
            double totalPrice = selected.getTotalPrice();
            int quantity = selected.getQuantity();

            boolean success = SalesDAO.deleteSale(selected.getId(), productId, quantity, totalPrice, customerId, 0);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sale record deleted successfully.");
                // Refresh the table
                LocalDate today = LocalDate.now();
                loadData(today, today);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale record.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}