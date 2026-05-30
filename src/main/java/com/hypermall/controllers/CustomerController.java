package com.hypermall.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.scene.layout.GridPane;
import com.hypermall.database.CustomerDAO; 
import com.hypermall.database.SalesDAO;
import com.hypermall.models.Customer;
import com.hypermall.models.SaleRecord;
import java.io.File;

public class CustomerController {
    @FXML private TextField customerSearchFieldMain;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> custIdCol;
    @FXML private TableColumn<Customer, String> custNameCol, custPhoneCol;
    @FXML private TableColumn<Customer, Double> custBalanceCol;

    @FXML private Label historyHeaderLabel;
    @FXML private TableView<SaleRecord> historyTable;
    @FXML private TableColumn<SaleRecord, String> histDateCol, histItemCol;
    @FXML private TableColumn<SaleRecord, Integer> histQtyCol;
    @FXML private TableColumn<SaleRecord, Double> histTotalCol;

    @FXML
    public void initialize() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        custBalanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        custBalanceCol.setCellFactory(column -> new TableCell<Customer, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else {
                    setText(String.format("Rs.%.2f", item));
                    if (item > 0) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
                    else setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        histDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        histItemCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        histQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        histTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // UPDATED: Bulletproof OS-Level File Opening
        TableColumn<SaleRecord, Void> openRecCol = new TableColumn<>("Receipt");
        openRecCol.setPrefWidth(100);
        openRecCol.setCellFactory(param -> new TableCell<SaleRecord, Void>() {
            private final Button btn = new Button("📄 Open");
            {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    SaleRecord data = getTableView().getItems().get(getIndex());
                    if (data.getInvoicePath() != null && !data.getInvoicePath().trim().isEmpty()) {
                        try {
                            File f = new File(data.getInvoicePath());
                            if (f.exists()) {
                                String os = System.getProperty("os.name").toLowerCase();
                                if (os.contains("win")) {
                                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", f.getAbsolutePath()});
                                } else if (os.contains("mac")) {
                                    Runtime.getRuntime().exec(new String[]{"open", f.getAbsolutePath()});
                                } else { 
                                    // Linux (Ubuntu/Debian) command to open default text editor
                                    Runtime.getRuntime().exec(new String[]{"xdg-open", f.getAbsolutePath()});
                                }
                            } else {
                                new Alert(Alert.AlertType.ERROR, "Receipt file no longer exists at:\n" + f.getAbsolutePath()).show();
                            }
                        } catch (Exception ex) { 
                            ex.printStackTrace(); 
                            new Alert(Alert.AlertType.ERROR, "Could not open file. Check terminal for details.").show();
                        }
                    } else {
                        new Alert(Alert.AlertType.INFORMATION, "No invoice file saved for this older transaction.").show();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        historyTable.getColumns().add(openRecCol);

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadCustomerHistory(newVal);
        });

        setupContextMenu();
        refreshCustomerTable("");
        customerSearchFieldMain.textProperty().addListener((obs, oldVal, newVal) -> refreshCustomerTable(newVal));
    }

    private void loadCustomerHistory(Customer c) {
        historyHeaderLabel.setText("Purchase History for: " + c.getName());
        historyHeaderLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        historyTable.setItems(FXCollections.observableArrayList(SalesDAO.getSalesByCustomer(c.getId())));
    }

    private void refreshCustomerTable(String q) { 
        customerTable.setItems(FXCollections.observableArrayList(CustomerDAO.searchCustomers(q))); 
    }

    @FXML private void handleAddCustomerMain() { showCustomerDialog(null); }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem edit = new MenuItem("Edit / Update Balance"); 
        edit.setOnAction(e -> showCustomerDialog(customerTable.getSelectionModel().getSelectedItem()));
        MenuItem payDebt = new MenuItem("Record Khata Payment");
        payDebt.setOnAction(e -> handleDebtPayment());
        MenuItem del = new MenuItem("Delete Record"); 
        del.setOnAction(e -> {
            Customer c = customerTable.getSelectionModel().getSelectedItem();
            if (c != null) { 
                new Alert(Alert.AlertType.CONFIRMATION, "Delete " + c.getName() + "?").showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) { CustomerDAO.deleteCustomer(c.getId()); refreshCustomerTable(""); }
                });
            }
        });
        menu.getItems().addAll(edit, payDebt, new SeparatorMenuItem(), del); 
        customerTable.setContextMenu(menu);
    }

    private void handleDebtPayment() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getBalance() <= 0) return;
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getBalance()));
        dialog.setTitle("Khata Payment");
        dialog.setHeaderText("Receiving payment from " + selected.getName());
        dialog.setContentText("Enter Amount Received: Rs.");
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double payment = Double.parseDouble(amountStr);
                double newBalance = Math.max(0, selected.getBalance() - payment);
                if (CustomerDAO.updateCustomer(selected.getId(), selected.getName(), selected.getPhone(), newBalance)) {
                    refreshCustomerTable("");
                    loadCustomerHistory(selected);
                }
            } catch (Exception e) { }
        });
    }

    private void showCustomerDialog(Customer c) {
        boolean isEdit = (c != null);
        Dialog<ButtonType> d = new Dialog<>(); 
        d.setTitle(isEdit ? "Edit Record" : "New Customer"); 
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        TextField nameField = new TextField(isEdit ? c.getName() : ""); 
        TextField phoneField = new TextField(isEdit ? c.getPhone() : "");
        TextField balanceField = new TextField(isEdit ? String.valueOf(c.getBalance()) : "0.0");
        g.addRow(0, new Label("Name:"), nameField); 
        g.addRow(1, new Label("Phone:"), phoneField);
        if (isEdit) g.addRow(2, new Label("Balance (Rs.):"), balanceField);
        d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) { 
                try {
                    if (isEdit) CustomerDAO.updateCustomer(c.getId(), nameField.getText(), phoneField.getText(), Double.parseDouble(balanceField.getText()));
                    else CustomerDAO.addCustomer(nameField.getText(), phoneField.getText());
                    refreshCustomerTable(""); 
                } catch (Exception ex) { }
            }
        });
    }
}