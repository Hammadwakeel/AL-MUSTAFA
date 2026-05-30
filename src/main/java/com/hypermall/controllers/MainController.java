// package com.hypermall.controllers;

// import javafx.fxml.FXML;
// import javafx.scene.control.*;
// import javafx.scene.control.cell.TextFieldTableCell; 
// import javafx.scene.control.cell.ComboBoxTableCell; 
// import javafx.util.converter.IntegerStringConverter; 
// import javafx.util.converter.DefaultStringConverter; 
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.scene.layout.GridPane;
// import javafx.scene.layout.VBox;
// import javafx.scene.layout.HBox;
// import javafx.geometry.Insets;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.beans.property.SimpleDoubleProperty;
// import com.hypermall.database.DatabaseHelper;
// import com.hypermall.models.Product;
// import com.hypermall.models.CartItem;
// import com.hypermall.models.Customer;
// import java.io.PrintWriter;
// import java.io.File;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// public class MainController {
    
//     // --- Layout Containers ---
//     @FXML private VBox inventoryView;
//     @FXML private VBox posView;
//     @FXML private VBox customerView; // NEW

//     // --- Inventory View Elements ---
//     @FXML private TextField searchField;
//     @FXML private TableView<Product> inventoryTable;
//     @FXML private TableColumn<Product, Integer> idCol;
//     @FXML private TableColumn<Product, String> nameCol;
//     @FXML private TableColumn<Product, Double> priceCol;
//     @FXML private TableColumn<Product, Double> wholesalePriceCol;
//     @FXML private TableColumn<Product, Integer> stockCol;
//     @FXML private Label dailyTotalLabel;

//     // --- POS View Elements ---
//     @FXML private TextField posSearchField;
//     @FXML private TableView<Product> searchResultTable;
//     @FXML private TableColumn<Product, String> resNameCol;
//     @FXML private TableColumn<Product, String> resSkuCol;
//     @FXML private TableColumn<Product, Double> resPriceCol;
//     @FXML private TableColumn<Product, Integer> resStockCol;

//     @FXML private TableView<CartItem> cartTable;
//     @FXML private TableColumn<CartItem, String> cNameCol;
//     @FXML private TableColumn<CartItem, Integer> cQtyCol;
//     @FXML private TableColumn<CartItem, String> cPriceTypeCol; 
//     @FXML private TableColumn<CartItem, Double> cPriceCol;
    
//     @FXML private TextField customerSearchField;
//     @FXML private Label customerInfoLabel;
//     private Customer currentCustomer = null; 

//     @FXML private Label posTotalLabel;
//     @FXML private TextField posCashField;
//     @FXML private Label posChangeLabel;

//     // --- NEW: Customer View Elements ---
//     @FXML private TextField customerSearchFieldMain;
//     @FXML private TableView<Customer> customerTable;
//     @FXML private TableColumn<Customer, Integer> custIdCol;
//     @FXML private TableColumn<Customer, String> custNameCol;
//     @FXML private TableColumn<Customer, String> custPhoneCol;
//     @FXML private TableColumn<Customer, Double> custBalanceCol;

//     private ObservableList<Product> productList = FXCollections.observableArrayList();
//     private ObservableList<CartItem> currentCart = FXCollections.observableArrayList();

//     @FXML
//     public void initialize() {
//         // --- Setup Inventory Table ---
//         idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
//         nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
//         priceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));
//         wholesalePriceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("wholesalePrice"));
//         stockCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock"));

//         stockCol.setCellFactory(column -> new TableCell<Product, Integer>() {
//             @Override
//             protected void updateItem(Integer item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty || item == null) { setText(null); setStyle(""); } 
//                 else {
//                     setText(item.toString());
//                     if (item < 10) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
//                     else setStyle("-fx-text-fill: #27ae60; -fx-font-weight: normal;");
//                 }
//             }
//         });

//         // --- Setup Customer/Khata Table ---
//         custIdCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
//         custNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
//         custPhoneCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("phone"));
//         custBalanceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("balance"));
        
//         // Highlight in RED if they owe money
//         custBalanceCol.setCellFactory(column -> new TableCell<Customer, Double>() {
//             @Override
//             protected void updateItem(Double item, boolean empty) {
//                 super.updateItem(item, empty);
//                 if (empty || item == null) { setText(null); setStyle(""); } 
//                 else {
//                     setText(String.format("$%.2f", item));
//                     if (item > 0) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
//                     else setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
//                 }
//             }
//         });

//         // --- Setup POS Search Table ---
//         resNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
//         resSkuCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("sku"));
//         resPriceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("price"));
//         resStockCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stock"));

//         // --- Setup POS Cart Table ---
//         cartTable.setItems(currentCart);
//         cNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
//         cQtyCol.setCellValueFactory(d -> d.getValue().quantityProperty().asObject());
//         cQtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
//         ObservableList<String> priceTiers = FXCollections.observableArrayList("Retail", "Wholesale", "Custom");
//         cPriceTypeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriceType()));
//         cPriceTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), priceTiers));
        
//         cPriceTypeCol.setOnEditCommit(event -> {
//             CartItem item = event.getRowValue();
//             String selection = event.getNewValue();
            
//             if (selection.equals("Retail")) item.applyRetailPrice();
//             else if (selection.equals("Wholesale")) item.applyWholesalePrice();
//             else if (selection.equals("Custom")) {
//                 TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getUnitPrice()));
//                 dialog.setTitle("Custom Price Override");
//                 dialog.setHeaderText("Set a custom price for " + item.getProduct().getName());
//                 dialog.setContentText("Enter Price: $");
//                 Optional<String> result = dialog.showAndWait();
//                 if (result.isPresent()) {
//                     try { item.applyCustomPrice(Double.parseDouble(result.get())); } 
//                     catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Invalid Number", "Please enter a valid monetary amount."); }
//                 }
//             }
//             updatePosTotals();
//         });

//         cPriceCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()).asObject());

//         setupContextMenu();
//         setupCartContextMenu(); 
        
//         refreshTable("");
//         refreshCustomerTable("");
//         updateSummary();

//         // --- Listeners ---
//         searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTable(newVal));
//         customerSearchFieldMain.textProperty().addListener((obs, oldVal, newVal) -> refreshCustomerTable(newVal));
        
//         posSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
//             String q = newVal.trim();
//             ObservableList<Product> results = FXCollections.observableArrayList(DatabaseHelper.searchProducts(q));
//             searchResultTable.setItems(results);
//             if (!results.isEmpty()) searchResultTable.getSelectionModel().selectFirst();
//         });

//         posSearchField.setOnAction(e -> addToCart());
//         searchResultTable.setOnMouseClicked(event -> { if (event.getClickCount() == 2) addToCart(); });
//         customerSearchField.setOnAction(e -> handleCustomerSearch()); 

//         cQtyCol.setOnEditCommit(event -> {
//             CartItem editedItem = event.getRowValue();
//             int maxStock = editedItem.getProduct().getStock();
//             int requestedQty = event.getNewValue() != null ? event.getNewValue() : 1;

//             if (requestedQty > maxStock) {
//                 showAlert(Alert.AlertType.WARNING, "Stock Limit Reached", "Only " + maxStock + " available in stock.");
//                 cartTable.refresh(); 
//             } else if (requestedQty > 0) {
//                 editedItem.setQuantity(requestedQty);
//             } else {
//                 editedItem.setQuantity(1); 
//             }
//             updatePosTotals();
//         });

//         posCashField.textProperty().addListener((obs, old, newVal) -> updatePosTotals());

//         switchToPOS();
//     }

//     // --- NAVIGATION METHODS ---
//     @FXML private void switchToInventory() {
//         posView.setVisible(false); customerView.setVisible(false);
//         inventoryView.setVisible(true); refreshTable("");
//     }

//     @FXML private void switchToPOS() {
//         inventoryView.setVisible(false); customerView.setVisible(false);
//         posView.setVisible(true); javafx.application.Platform.runLater(() -> posSearchField.requestFocus());
//     }

//     @FXML private void switchToCustomers() {
//         inventoryView.setVisible(false); posView.setVisible(false);
//         customerView.setVisible(true); refreshCustomerTable("");
//     }

//     // --- CUSTOMER KHATA LOGIC ---
//     @FXML
//     private void handleCustomerSearch() {
//         String query = customerSearchField.getText().trim();
//         if (query.isEmpty()) {
//             showAddCustomerDialog("");
//             return;
//         }

//         List<Customer> found = DatabaseHelper.searchCustomers(query);
//         if (found.isEmpty()) {
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Customer not found. Register new customer?", ButtonType.YES, ButtonType.NO);
//             alert.showAndWait().ifPresent(res -> {
//                 if (res == ButtonType.YES) showAddCustomerDialog(query);
//             });
//         } else if (found.size() == 1) {
//             setCustomer(found.get(0));
//         } else {
//             setCustomer(found.get(0)); 
//             showAlert(Alert.AlertType.INFORMATION, "Multiple matches", "Selected: " + found.get(0).getName());
//         }
//     }

//     @FXML
//     private void handleAddCustomerMain() {
//         showAddCustomerDialog("");
//     }

//     private void showAddCustomerDialog(String presetText) {
//         Dialog<ButtonType> dialog = new Dialog<>();
//         dialog.setTitle("Register New Customer");
//         ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
//         dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        
//         GridPane grid = new GridPane();
//         grid.setHgap(10); grid.setVgap(10);
        
//         TextField nameField = new TextField();
//         TextField phoneField = new TextField(presetText);
        
//         grid.addRow(0, new Label("Name:"), nameField);
//         grid.addRow(1, new Label("Phone:"), phoneField);
//         dialog.getDialogPane().setContent(grid);
        
//         dialog.showAndWait().ifPresent(res -> {
//             if (res == saveButton) {
//                 if (DatabaseHelper.addCustomer(nameField.getText(), phoneField.getText())) {
//                     List<Customer> newCust = DatabaseHelper.searchCustomers(phoneField.getText());
//                     if (!newCust.isEmpty()) {
//                         setCustomer(newCust.get(0));
//                         refreshCustomerTable(""); // Update the Khata dashboard behind the scenes
//                     }
//                 } else {
//                     showAlert(Alert.AlertType.ERROR, "Error", "Could not save customer (phone might already exist).");
//                 }
//             }
//         });
//     }

//     private void setCustomer(Customer c) {
//         this.currentCustomer = c;
//         double totalBought = DatabaseHelper.getCustomerTotalSales(c.getId());
//         customerInfoLabel.setText(String.format("👤 %s | Owes you: $%.2f | Total Historic Purchases: $%.2f", 
//                                                 c.getName(), c.getBalance(), totalBought));
//         customerInfoLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
//         updatePosTotals(); 
//     }

//     @FXML private void clearCustomer() {
//         this.currentCustomer = null;
//         customerSearchField.clear();
//         customerInfoLabel.setText("Walk-in Customer (No Credit Allowed)");
//         customerInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
//         updatePosTotals();
//     }

//     private void refreshCustomerTable(String query) {
//         customerTable.setItems(FXCollections.observableArrayList(DatabaseHelper.searchCustomers(query)));
//     }

//     // --- POS LOGIC ---
//     @FXML private void performPosSearch() {
//         searchResultTable.setItems(FXCollections.observableArrayList(DatabaseHelper.searchProducts(posSearchField.getText().trim())));
//     }

//     @FXML private void addToCart() {
//         Product selected = searchResultTable.getSelectionModel().getSelectedItem();
//         if (selected != null) {
//             boolean exists = false;
//             for (CartItem item : currentCart) {
//                 if (item.getProduct().getId() == selected.getId()) {
//                     if (item.getQuantity() + 1 > selected.getStock()) {
//                         showAlert(Alert.AlertType.WARNING, "Stock Limit Reached", "Cannot add more.");
//                         return;
//                     }
//                     item.setQuantity(item.getQuantity() + 1); 
//                     exists = true; break;
//                 }
//             }
//             if (!exists) {
//                 if (1 > selected.getStock()) {
//                     showAlert(Alert.AlertType.WARNING, "Out of Stock", "This item is out of stock.");
//                     return;
//                 }
//                 currentCart.add(new CartItem(selected, 1));
//             }
//             updatePosTotals(); posSearchField.clear(); posSearchField.requestFocus(); 
//         }
//     }

//     @FXML private void removeCartItem() {
//         CartItem selected = cartTable.getSelectionModel().getSelectedItem();
//         if (selected != null) {
//             currentCart.remove(selected); updatePosTotals(); posSearchField.requestFocus();
//         }
//     }

//     private void updatePosTotals() {
//         double total = currentCart.stream().mapToDouble(CartItem::getSubtotal).sum();
//         posTotalLabel.setText(String.format("Grand Total: $%.2f", total));
//         try {
//             double cash = Double.parseDouble(posCashField.getText());
//             double diff = cash - total;
            
//             if (diff < 0) {
//                 if (currentCustomer != null) {
//                     posChangeLabel.setText(String.format("Adding $%.2f to Khata/Credit", Math.abs(diff)));
//                     posChangeLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 16px;"); 
//                 } else {
//                     posChangeLabel.setText(String.format("Missing: $%.2f (Requires Khata Registration)", Math.abs(diff)));
//                     posChangeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;"); 
//                 }
//             } else {
//                 posChangeLabel.setText(String.format("Change Return: $%.2f", diff));
//                 posChangeLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;"); 
//             }
//         } catch (Exception ex) { 
//             posChangeLabel.setText("Change: $0.00"); 
//             posChangeLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16px;");
//         }
//         cartTable.refresh(); 
//     }

//     // --- UPDATED: AUTOMATIC KHATA PROMPT ---
//     @FXML
//     private void submitSale() {
//         if (currentCart.isEmpty()) {
//             showAlert(Alert.AlertType.WARNING, "Empty Cart", "Add products to the cart before submitting.");
//             return;
//         }

//         double finalTotal = currentCart.stream().mapToDouble(CartItem::getSubtotal).sum();
//         double cash = 0.0;
//         try {
//             cash = Double.parseDouble(posCashField.getText());
//         } catch (NumberFormatException ex) {
//             if (currentCustomer == null) cash = finalTotal;
//             else {
//                 showAlert(Alert.AlertType.ERROR, "Input Required", "Enter amount paid to calculate credit accurately.");
//                 return;
//             }
//         }

//         // --- AUTOMATIC KHATA REGISTRATION INTERCEPTOR ---
//         if (cash < finalTotal && currentCustomer == null) {
//             Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
//                 String.format("Customer is short by $%.2f.\nDo you want to register them to the Khata System to track this credit?", (finalTotal - cash)),
//                 ButtonType.YES, ButtonType.NO);
            
//             Optional<ButtonType> result = alert.showAndWait();
//             if (result.isPresent() && result.get() == ButtonType.YES) {
//                 showAddCustomerDialog(""); // Pop the registration window
                
//                 // If they successfully registered the customer, currentCustomer is no longer null!
//                 if (currentCustomer != null) {
//                     finalizeSaleTransaction(cash, finalTotal); // Automatically finish the sale!
//                 }
//             }
//             return; // Stops here if they click NO or cancel the registration
//         }

//         // Normal execution for walk-ins who pay in full, or pre-linked customers
//         finalizeSaleTransaction(cash, finalTotal);
//     }

//     // Helper method so we don't repeat the database logic
//     private void finalizeSaleTransaction(double cash, double finalTotal) {
//         int customerId = currentCustomer != null ? currentCustomer.getId() : -1;
//         if (DatabaseHelper.processBulkSale(currentCart, customerId, cash, finalTotal)) {
//             generateInvoice(new ArrayList<>(currentCart), cash, finalTotal, currentCustomer);
//             currentCart.clear();
//             posCashField.clear();
//             clearCustomer(); 
//             updatePosTotals();
//             updateSummary();
//             refreshCustomerTable(""); // Ensure Khata dashboard is fresh
//             showAlert(Alert.AlertType.INFORMATION, "Sale Successful", "Sale completed and invoice generated.");
//             posSearchField.requestFocus();
//         } else {
//             showAlert(Alert.AlertType.ERROR, "Sale Failed", "Check stock levels. A product might be out of stock.");
//         }
//     }

//     // --- INVENTORY LOGIC ---
//     private void updateSummary() {
//         if (dailyTotalLabel != null) {
//             dailyTotalLabel.setText(String.format("Today's Total Revenue: $%.2f", DatabaseHelper.getDailySalesTotal()));
//         }
//     }

//     private void generateInvoice(List<CartItem> items, double cashReceived, double totalAmount, Customer c) {
//         String fileName = "Invoice_" + System.currentTimeMillis() + ".txt";
//         try (PrintWriter writer = new PrintWriter(new File(fileName))) {
//             DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//             writer.println("=================================");
//             writer.println("       HYPERMALL RETAIL          ");
//             writer.println("=================================");
//             writer.println("Date: " + dtf.format(LocalDateTime.now()));
//             if (c != null) {
//                 writer.println("Customer: " + c.getName());
//                 writer.println("Phone:    " + c.getPhone());
//             } else { writer.println("Customer: Walk-in"); }
//             writer.println("---------------------------------");
//             writer.printf("%-15s %-5s %-10s %-8s\n", "Item", "Qty", "Price", "Tier");
            
//             for (CartItem item : items) {
//                 writer.printf("%-15s %-5d $%-10.2f %-8s\n", 
//                     item.getProduct().getName(), item.getQuantity(), item.getSubtotal(), item.getPriceType());
//             }
            
//             writer.println("---------------------------------");
//             writer.printf("GRAND TOTAL:    $%.2f\n", totalAmount);
//             writer.printf("CASH RECEIVED:  $%.2f\n", cashReceived);
            
//             if (cashReceived < totalAmount && c != null) writer.printf("ADDED TO KHATA: $%.2f\n", (totalAmount - cashReceived));
//             else writer.printf("CHANGE RETURN:  $%.2f\n", (cashReceived - totalAmount));
            
//             writer.println("=================================");
//             writer.println("   THANK YOU FOR SHOPPING!       ");
//         } catch (Exception e) { e.printStackTrace(); }
//     }

//     @FXML private void handleSearch() { refreshTable(searchField.getText()); }
//     @FXML private void handleAddProduct() { showProductDialog(null); }
//     private void handleEditProduct() {
//         Product selected = inventoryTable.getSelectionModel().getSelectedItem();
//         if (selected != null) showProductDialog(selected);
//     }
//     private void handleDeleteProduct() {
//         Product selected = inventoryTable.getSelectionModel().getSelectedItem();
//         if (selected != null) {
//             new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getName() + "?", ButtonType.YES, ButtonType.NO).showAndWait().ifPresent(r -> {
//                 if (r == ButtonType.YES) { DatabaseHelper.deleteProduct(selected.getId()); refreshTable(""); updateSummary(); }
//             });
//         }
//     }

//     private void setupContextMenu() {
//         ContextMenu contextMenu = new ContextMenu();
//         MenuItem editItem = new MenuItem("Edit Details"); editItem.setOnAction(e -> handleEditProduct());
//         MenuItem deleteItem = new MenuItem("Remove Product"); deleteItem.setOnAction(e -> handleDeleteProduct());
//         contextMenu.getItems().addAll(editItem, deleteItem); inventoryTable.setContextMenu(contextMenu);
//     }
//     private void setupCartContextMenu() {
//         ContextMenu cartMenu = new ContextMenu();
//         MenuItem removeItem = new MenuItem("Remove Item"); removeItem.setOnAction(e -> removeCartItem());
//         cartMenu.getItems().add(removeItem); cartTable.setContextMenu(cartMenu);
//     }

//     private void showProductDialog(Product product) {
//         boolean isEdit = (product != null);
//         Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(isEdit ? "Update Product" : "New Product");
//         ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
//         dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        
//         GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        
//         TextField name = new TextField(isEdit ? product.getName() : "");
//         TextField sku = new TextField(isEdit ? product.getSku() : ""); sku.setDisable(isEdit);
//         TextField retailPrice = new TextField(isEdit ? String.valueOf(product.getPrice()) : "");
//         TextField wholesalePrice = new TextField(isEdit ? String.valueOf(product.getWholesalePrice()) : "");
//         TextField stock = new TextField(isEdit ? String.valueOf(product.getStock()) : "");
        
//         grid.addRow(0, new Label("Name:"), name); grid.addRow(1, new Label("Code (SKU):"), sku);
//         grid.addRow(2, new Label("Retail Price:"), retailPrice); grid.addRow(3, new Label("Wholesale Price:"), wholesalePrice);
//         grid.addRow(4, new Label("Quantity (Stock):"), stock); dialog.getDialogPane().setContent(grid);
        
//         dialog.showAndWait().ifPresent(res -> {
//             if (res == saveButton) {
//                 try {
//                     if (isEdit) DatabaseHelper.updateProduct(product.getId(), name.getText(), Double.parseDouble(retailPrice.getText()), Double.parseDouble(wholesalePrice.getText()), Integer.parseInt(stock.getText()));
//                     else DatabaseHelper.addProduct(name.getText(), sku.getText(), Double.parseDouble(retailPrice.getText()), Double.parseDouble(wholesalePrice.getText()), Integer.parseInt(stock.getText()));
//                     refreshTable("");
//                 } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Input Error", "Please ensure prices and quantity are valid numbers."); }
//             }
//         });
//     }

//     private void refreshTable(String query) { inventoryTable.setItems(FXCollections.observableArrayList(DatabaseHelper.searchProducts(query))); }
//     private void showAlert(Alert.AlertType type, String title, String msg) { Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
// }