package com.hypermall.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.scene.layout.GridPane;
import com.hypermall.database.ProductDAO; 
import com.hypermall.database.SalesDAO;   
import com.hypermall.models.Product;

public class InventoryController {
    @FXML private TextField searchField;
    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, Integer> idCol, stockCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, Double> priceCol, wholesalePriceCol;
    
    @FXML private Label dailyTotalLabel;
    @FXML private Label totalStockValueLabel; // NEW LABEL

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        wholesalePriceCol.setCellValueFactory(new PropertyValueFactory<>("wholesalePrice"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        setupContextMenu();
        refreshTable("");
        
        dailyTotalLabel.setText(String.format("Today's Total Revenue: Rs.%,.2f", SalesDAO.getDailySalesTotal()));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTable(newVal));
    }

    // ==========================================
    // NEW: Updates the stock value on the screen
    // ==========================================
    private void updateTotalStockValue() {
        double totalValue = ProductDAO.getTotalInventoryValue();
        totalStockValueLabel.setText(String.format("Total Stock Value: Rs.%,.2f", totalValue));
    }

    @FXML private void handleSearch() { refreshTable(searchField.getText()); }
    @FXML private void handleAddProduct() { showProductDialog(null); }

    private void showProductDialog(Product p) {
        boolean isEdit = (p != null);
        Dialog<ButtonType> d = new Dialog<>(); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane(); 
        TextField name = new TextField(isEdit ? p.getName() : "");
        TextField sku = new TextField(isEdit ? p.getSku() : ""); sku.setDisable(isEdit);
        TextField rPrice = new TextField(isEdit ? String.valueOf(p.getPrice()) : "");
        TextField wPrice = new TextField(isEdit ? String.valueOf(p.getWholesalePrice()) : "");
        TextField stock = new TextField(isEdit ? String.valueOf(p.getStock()) : "");
        g.addRow(0, new Label("Name:"), name); g.addRow(1, new Label("SKU:"), sku);
        g.addRow(2, new Label("Retail:"), rPrice); g.addRow(3, new Label("Wholesale:"), wPrice);
        g.addRow(4, new Label("Stock:"), stock); d.getDialogPane().setContent(g);

        d.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    if (isEdit) ProductDAO.updateProduct(p.getId(), name.getText(), Double.parseDouble(rPrice.getText()), Double.parseDouble(wPrice.getText()), Integer.parseInt(stock.getText()));
                    else ProductDAO.addProduct(name.getText(), sku.getText(), Double.parseDouble(rPrice.getText()), Double.parseDouble(wPrice.getText()), Integer.parseInt(stock.getText()));
                    refreshTable("");
                } catch (Exception e) {}
            }
        });
    }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit Details"); edit.setOnAction(e -> showProductDialog(inventoryTable.getSelectionModel().getSelectedItem()));
        MenuItem del = new MenuItem("Remove"); del.setOnAction(e -> {
            Product p = inventoryTable.getSelectionModel().getSelectedItem();
            if (p != null) { 
                ProductDAO.deleteProduct(p.getId()); 
                refreshTable(""); 
            }
        });
        menu.getItems().addAll(edit, del); inventoryTable.setContextMenu(menu);
    }

    private void refreshTable(String q) { 
        inventoryTable.setItems(FXCollections.observableArrayList(ProductDAO.searchProducts(q))); 
        updateTotalStockValue(); // Ensures the total value updates whenever the table changes
    }
}