package com.hypermall.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.util.converter.*;
import javafx.collections.*;
import javafx.scene.layout.GridPane;
import javafx.beans.property.*;
import javafx.print.PrinterJob;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import com.hypermall.database.ProductDAO;
import com.hypermall.database.CustomerDAO;
import com.hypermall.database.SalesDAO;
import com.hypermall.database.DatabaseManager; 
import com.hypermall.models.*;
import java.io.*;
import java.sql.*; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PosController {
    
    @FXML private ComboBox<String> salesmanComboBox;
    @FXML private TextField posSearchField;
    @FXML private TableView<Product> searchResultTable;
    @FXML private TableColumn<Product, String> resNameCol, resSkuCol;
    @FXML private TableColumn<Product, Double> resPriceCol;
    @FXML private TableColumn<Product, Integer> resStockCol;

    @FXML private TextField customerSearchField;
    @FXML private TableView<Customer> customerSearchResultTable;
    @FXML private TableColumn<Customer, String> custSearchNameCol, custSearchPhoneCol;
    @FXML private TableColumn<Customer, Double> custSearchBalanceCol;
    @FXML private Label customerInfoLabel;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cNameCol, cPriceTypeCol;
    @FXML private TableColumn<CartItem, Integer> cQtyCol;
    @FXML private TableColumn<CartItem, Double> cPriceCol;
    @FXML private TextField posCashField;
    @FXML private Label posTotalLabel, posChangeLabel;
    
    private Customer currentCustomer = null; 
    private ObservableList<CartItem> currentCart = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        resNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        resSkuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
        resPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        resStockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        custSearchNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        custSearchPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        custSearchBalanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        cartTable.setItems(currentCart);
        cNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        cQtyCol.setCellValueFactory(d -> d.getValue().quantityProperty().asObject());
        cQtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        
        ObservableList<String> priceTiers = FXCollections.observableArrayList("Retail", "Wholesale", "Custom");
        
        // FIXED: Removed the invalid "Default" line that caused the crash
        cPriceTypeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPriceType()));
        cPriceTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), priceTiers));
        
        cPriceTypeCol.setOnEditCommit(event -> {
            CartItem item = event.getRowValue();
            String sel = event.getNewValue();
            if (sel.equals("Retail")) item.applyRetailPrice();
            else if (sel.equals("Wholesale")) item.applyWholesalePrice();
            else if (sel.equals("Custom")) {
                TextInputDialog d = new TextInputDialog(String.valueOf(item.getUnitPrice()));
                d.setHeaderText("Custom Price Override");
                d.showAndWait().ifPresent(res -> {
                    try { item.applyCustomPrice(Double.parseDouble(res)); } catch(Exception e){}
                });
            }
            updatePosTotals();
        });
        
        cPriceCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getSubtotal()).asObject());

        posSearchField.textProperty().addListener((obs, old, newVal) -> performPosSearch());
        posSearchField.setOnAction(e -> addToCart());
        searchResultTable.setOnMouseClicked(event -> { if (event.getClickCount() == 2) addToCart(); });
        
        customerSearchField.textProperty().addListener((obs, old, newVal) -> performCustomerSearch());
        customerSearchResultTable.setOnMouseClicked(event -> { if (event.getClickCount() == 2) linkSelectedCustomer(); });
        
        posCashField.textProperty().addListener((obs, old, newVal) -> updatePosTotals());
        
        cQtyCol.setOnEditCommit(event -> {
            CartItem item = event.getRowValue();
            int req = event.getNewValue() != null ? event.getNewValue() : 1;
            if (req > item.getProduct().getStock()) {
                showAlert(Alert.AlertType.WARNING, "Stock Limit", "Not enough stock."); cartTable.refresh(); 
            } else if (req > 0) item.setQuantity(req);
            else item.setQuantity(1); 
            updatePosTotals();
        });

        javafx.application.Platform.runLater(posSearchField::requestFocus);
        posTotalLabel.setText("Bill Total: Rs.0.00");
        posChangeLabel.setText("Enter cash...");
        clearCustomer();
        performPosSearch();
        performCustomerSearch();
        
        loadSalesmen();
    }

    private void loadSalesmen() {
        if (salesmanComboBox != null) {
            salesmanComboBox.getItems().clear();
            String sql = "SELECT name FROM salesmen";
            try (Connection conn = DatabaseManager.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    salesmanComboBox.getItems().add(rs.getString("name"));
                }
                if (!salesmanComboBox.getItems().isEmpty()) {
                    salesmanComboBox.getSelectionModel().selectFirst();
                }
            } catch (SQLException e) {
                System.out.println("Error loading salesmen: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddSalesman() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Salesman");
        dialog.setHeaderText("Add a new Salesman to the system");
        dialog.setContentText("Salesman Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String sql = "INSERT INTO salesmen (name) VALUES (?)";
                try (Connection conn = DatabaseManager.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                     
                    pstmt.setString(1, name.trim());
                    pstmt.executeUpdate();
                    
                    loadSalesmen(); 
                    salesmanComboBox.getSelectionModel().select(name.trim());
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not add salesman. They might already exist.");
                }
            }
        });
    }

    @FXML private void performPosSearch() { searchResultTable.setItems(FXCollections.observableArrayList(ProductDAO.searchProducts(posSearchField.getText().trim()))); }
    @FXML private void performCustomerSearch() { customerSearchResultTable.setItems(FXCollections.observableArrayList(CustomerDAO.searchCustomers(customerSearchField.getText().trim()))); }

    @FXML private void addToCart() {
        Product p = searchResultTable.getSelectionModel().getSelectedItem();
        if (p != null) {
            for (CartItem item : currentCart) {
                if (item.getProduct().getId() == p.getId()) {
                    if (item.getQuantity() + 1 > p.getStock()) return;
                    item.setQuantity(item.getQuantity() + 1); updatePosTotals(); posSearchField.clear(); return;
                }
            }
            if (1 > p.getStock()) return;
            currentCart.add(new CartItem(p, 1)); updatePosTotals(); posSearchField.clear();
        }
    }

    @FXML private void removeCartItem() {
        CartItem item = cartTable.getSelectionModel().getSelectedItem();
        if (item != null) { currentCart.remove(item); updatePosTotals(); }
    }

    private void updatePosTotals() {
        double cartTotal = currentCart.stream().mapToDouble(CartItem::getSubtotal).sum();
        posTotalLabel.setText(String.format("Bill Total: Rs.%.2f", cartTotal)); 
        
        try {
            double cash = Double.parseDouble(posCashField.getText());
            double oldDebt = currentCustomer != null ? currentCustomer.getBalance() : 0.0;
            
            if (cash < cartTotal) {
                posChangeLabel.setText(String.format("Adding Rs.%.2f to Khata", (cartTotal - cash))); 
                posChangeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
            } else {
                double extraCash = cash - cartTotal;
                if (oldDebt > 0) {
                    double debtCleared = Math.min(extraCash, oldDebt);
                    double change = extraCash - debtCleared;
                    posChangeLabel.setText(String.format("Khata Cleared: Rs.%.2f | Change: Rs.%.2f", debtCleared, change));
                    posChangeLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
                } else {
                    posChangeLabel.setText(String.format("Change: Rs.%.2f", extraCash));
                    posChangeLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;"); 
                }
            }
        } catch (Exception ex) { 
            posChangeLabel.setText("Enter cash amount..."); 
            posChangeLabel.setStyle("-fx-text-fill: #7f8c8d;");
        }
        cartTable.refresh(); 
    }

    @FXML private void linkSelectedCustomer() {
        Customer c = customerSearchResultTable.getSelectionModel().getSelectedItem();
        if (c != null) setCustomer(c);
    }

    private void setCustomer(Customer c) {
        currentCustomer = c;
        customerInfoLabel.setText(String.format("✔ Linked: %s | Owes: Rs.%.2f", c.getName(), c.getBalance())); 
        customerInfoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        updatePosTotals(); 
    }

    @FXML private void clearCustomer() { 
        currentCustomer = null; 
        customerSearchField.clear(); 
        customerInfoLabel.setText("No Customer Linked (Sale Blocked)"); 
        customerInfoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        updatePosTotals();
    }

    @FXML private void showAddCustomerDialogEmpty() {
        showAddCustomerDialog(customerSearchField.getText().trim());
    }

    private void showAddCustomerDialog(String preset) {
        Dialog<ButtonType> d = new Dialog<>(); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane(); TextField n = new TextField(), p = new TextField(preset);
        g.addRow(0, new Label("Name:"), n); g.addRow(1, new Label("Phone:"), p); d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && CustomerDAO.addCustomer(n.getText(), p.getText())) {
                setCustomer(CustomerDAO.searchCustomers(p.getText()).get(0));
                performCustomerSearch();
            }
        });
    }

    @FXML private void submitSale() {
        if (currentCart.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Empty Cart", "Please add items."); return; }
        if (currentCustomer == null) { showAlert(Alert.AlertType.ERROR, "Customer Required", "Please link a customer."); return; }

        double cartTotal = currentCart.stream().mapToDouble(CartItem::getSubtotal).sum();
        double cash = 0.0;
        try { cash = Double.parseDouble(posCashField.getText()); } 
        catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Input Required", "Enter cash received."); return; }

        double oldDebt = currentCustomer.getBalance();
        double newDebtAdded = 0;
        double debtCleared = 0;
        double changeToReturn = 0;

        if (cash < cartTotal) {
            newDebtAdded = cartTotal - cash;
        } else {
            double extraCash = cash - cartTotal;
            if (oldDebt > 0) {
                debtCleared = Math.min(extraCash, oldDebt);
                changeToReturn = extraCash - debtCleared;
            } else {
                changeToReturn = extraCash;
            }
        }

        double newBalance = oldDebt + newDebtAdded - debtCleared;
        
        File directory = new File("invoices");
        if (!directory.exists()) directory.mkdir();
        String absoluteFileName = new File("invoices/Invoice_" + System.currentTimeMillis() + ".txt").getAbsolutePath();

        String activeSalesman = salesmanComboBox.getValue() != null ? salesmanComboBox.getValue() : "Admin";

        // VERIFIED: Passing activeSalesman correctly to save to DB
        if (SalesDAO.processBulkSale(currentCart, currentCustomer.getId(), newBalance, absoluteFileName, activeSalesman)) {
            String invoiceContent = buildInvoiceString(new ArrayList<>(currentCart), cash, cartTotal, oldDebt, newBalance, debtCleared, changeToReturn, currentCustomer, activeSalesman);
            
            try (PrintWriter writer = new PrintWriter(new File(absoluteFileName))) { 
                writer.print(invoiceContent); 
            } catch (Exception e) {}
            
            showInvoicePreview(invoiceContent);

            currentCart.clear(); posCashField.clear(); clearCustomer(); updatePosTotals(); performCustomerSearch();
        }
    }

    private String buildInvoiceString(List<CartItem> items, double cash, double cartTotal, double oldDebt, double newBalance, double debtCleared, double change, Customer c, String salesmanName) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        StringBuilder sb = new StringBuilder();
        
        String dLine = "============================================\n";
        String sLine = "--------------------------------------------\n";
        
        sb.append(dLine);
        sb.append("    AL MUSTAFA ELECTRIC WHOLESALE STORE     \n");
        sb.append("          Near HBL Bank, Jehangira          \n");
        sb.append("  Osaka | Tuff | RC | Fans | Solar | China board  \n");
        sb.append("               Wires & Cables               \n");
        sb.append("     0311-9396640    |    0343-1216306      \n");
        sb.append(dLine);
        
        sb.append(String.format("Date:     %s\n", dtf.format(LocalDateTime.now())));
        sb.append(String.format("Salesman: %s\n", salesmanName)); 
        
        String cName = c.getName().length() > 38 ? c.getName().substring(0, 35) + "..." : c.getName();
        String cPhone = c.getPhone().length() > 38 ? c.getPhone().substring(0, 35) + "..." : c.getPhone();
        sb.append(String.format("Customer: %s\n", cName));
        sb.append(String.format("Ph:       %-34s\n", cPhone));
        
        sb.append(dLine);
        
        sb.append(String.format("%-18s %4s %8s %10s\n", "Item", "Qty", "Price", "Amount"));
        sb.append(sLine);
        
        for (CartItem item : items) {
            String name = item.getProduct().getName();
            if (name.length() > 16) name = name.substring(0, 16); 
            
            sb.append(String.format("%-18s %4d %8s %10s\n", 
                name, 
                item.getQuantity(), 
                String.format("%,.0f", item.getUnitPrice()),
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

    private void showInvoicePreview(String content) {
        Alert preview = new Alert(Alert.AlertType.CONFIRMATION);
        preview.setTitle("Sale Successful");
        preview.setHeaderText("Receipt Preview");
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false); 
        textArea.setPrefHeight(500); 
        textArea.setPrefWidth(450);
        
        textArea.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px; -fx-font-weight: bold;"); 
        
        preview.getDialogPane().setContent(textArea);
        ButtonType printBtn = new ButtonType("Print (Ctrl+P)");
        preview.getButtonTypes().setAll(printBtn, ButtonType.CLOSE);
        preview.showAndWait().ifPresent(type -> { if (type == printBtn) printInvoice(content); });
    }

    private void printInvoice(String content) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            Text text = new Text(content); 
            text.setFont(Font.font("monospace", 10)); 
            if (job.printPage(text)) job.endJob();
        } else showAlert(Alert.AlertType.ERROR, "Printer Error", "No printer found.");
    }

    private void showAlert(Alert.AlertType t, String title, String msg) { Alert a = new Alert(t, msg); a.setHeaderText(title); a.showAndWait(); }
}