/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JIC
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SalesTransactionPanel extends JPanel {

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> productComboBox;
    private JTextField quantityField;
    private JComboBox<String> customerComboBox;
    private JButton addTransactionButton, searchTransactionButton, resetFilterButton;

    public SalesTransactionPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID Transaksi", "Nama Produk", "Jumlah", "Total Biaya", "Nama Pelanggan", "Tanggal"}, 0);
        transactionTable = new JTable(tableModel);
        add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));

        inputPanel.add(new JLabel("Produk:"));
        productComboBox = new JComboBox<>();
        inputPanel.add(productComboBox);

        inputPanel.add(new JLabel("Jumlah:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        inputPanel.add(new JLabel("Pelanggan:"));
        customerComboBox = new JComboBox<>();
        inputPanel.add(customerComboBox);

        addTransactionButton = new JButton("Tambah Transaksi");
        addTransactionButton.addActionListener(e -> addTransaction());
        inputPanel.add(addTransactionButton);

        searchTransactionButton = new JButton("Cari Transaksi");
        searchTransactionButton.addActionListener(e -> searchTransactions());
        inputPanel.add(searchTransactionButton);

        resetFilterButton = new JButton("Reset Filter");
        resetFilterButton.addActionListener(e -> loadTransactions());
        inputPanel.add(resetFilterButton);

        add(inputPanel, BorderLayout.SOUTH);

        loadProducts();
        loadCustomers();
        loadTransactions();
    }

    private void loadProducts() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            productComboBox.removeAllItems();
            while (resultSet.next()) {
                String productId = resultSet.getString("id");
                String productName = resultSet.getString("name");
                productComboBox.addItem(productId + " - " + productName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat produk.");
        }
    }

    private void loadCustomers() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            customerComboBox.removeAllItems();
            while (resultSet.next()) {
                String customerId = resultSet.getString("id");
                String customerName = resultSet.getString("name");
                customerComboBox.addItem(customerId + " - " + customerName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat pelanggan.");
        }
    }

    private void addTransaction() {
        String product = (String) productComboBox.getSelectedItem();
        int productId = product != null ? Integer.parseInt(product.split(" - ")[0]) : 0;
        int quantity = Integer.parseInt(quantityField.getText());
        String customer = (String) customerComboBox.getSelectedItem();
        String customerId = customer != null ? customer.split(" - ")[0] : "";

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT price FROM products WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double price = resultSet.getDouble("price");
                double totalCost = price * quantity;

                query = "INSERT INTO transactions (product_id, quantity, total_cost, customer_id) VALUES (?, ?, ?, ?)";
                statement = connection.prepareStatement(query);
                statement.setInt(1, productId);
                statement.setInt(2, quantity);
                statement.setDouble(3, totalCost);
                statement.setString(4, customerId);
                statement.executeUpdate();

                loadTransactions();
                JOptionPane.showMessageDialog(this, "Transaksi berhasil ditambahkan.");
            } else {
                JOptionPane.showMessageDialog(this, "Produk tidak ditemukan.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan transaksi.");
        }
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT t.id, p.name AS product_name, t.quantity, t.total_cost, c.name AS customer_name, t.transaction_date "
                    + "FROM transactions t "
                    + "JOIN products p ON t.product_id = p.id "
                    + "JOIN customers c ON t.customer_id = c.id";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String productName = resultSet.getString("product_name");
                int quantity = resultSet.getInt("quantity");
                double totalCost = resultSet.getDouble("total_cost");
                String formattedTotalCost = CurrencyUtil.formatRupiah(totalCost);
                String customerName = resultSet.getString("customer_name");
                Timestamp transactionDate = resultSet.getTimestamp("transaction_date");

                tableModel.addRow(new Object[]{id, productName, quantity, formattedTotalCost, customerName, transactionDate});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat transaksi.");
        }
    }

    private void searchTransactions() {
        String customerName = JOptionPane.showInputDialog(this, "Masukkan Nama Pelanggan untuk mencari:");
        if (customerName != null && !customerName.trim().isEmpty()) {
            tableModel.setRowCount(0);
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT t.id, p.name AS product_name, t.quantity, t.total_cost, c.name AS customer_name, t.transaction_date "
                        + "FROM transactions t "
                        + "JOIN products p ON t.product_id = p.id "
                        + "JOIN customers c ON t.customer_id = c.id "
                        + "WHERE c.name LIKE ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, "%" + customerName + "%");
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String productName = resultSet.getString("product_name");
                    int quantity = resultSet.getInt("quantity");
                    double totalCost = resultSet.getDouble("total_cost");
                    String formattedTotalCost = CurrencyUtil.formatRupiah(totalCost);
                    String customerNameResult = resultSet.getString("customer_name");
                    Timestamp transactionDate = resultSet.getTimestamp("transaction_date");

                    tableModel.addRow(new Object[]{id, productName, quantity, formattedTotalCost, customerNameResult, transactionDate});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mencari transaksi.");
            }
        }
    }
}
