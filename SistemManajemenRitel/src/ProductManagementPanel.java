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

public class ProductManagementPanel extends JPanel {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, priceField, quantityField;
    private JButton addButton, updateButton, deleteButton;

    public ProductManagementPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Harga", "Jumlah"}, 0);
        productTable = new JTable(tableModel);
        add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Nama:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Harga:"));
        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("Jumlah:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        addButton = new JButton("Tambah Produk");
        addButton.addActionListener(e -> addProduct());
        inputPanel.add(addButton);

        updateButton = new JButton("Perbarui Produk");
        updateButton.addActionListener(e -> updateProduct());
        inputPanel.add(updateButton);

        deleteButton = new JButton("Hapus Produk");
        deleteButton.addActionListener(e -> deleteProduct());
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.SOUTH);

        productTable.getSelectionModel().addListSelectionListener(e -> fillFieldsFromSelectedRow());

        loadProducts();
    }

    private void addProduct() {
        String name = nameField.getText();
        double price = CurrencyUtil.parseRupiah(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO products (name, price, quantity) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, quantity);
            statement.executeUpdate();

            loadProducts();
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan produk.");
        }
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan diperbarui.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        double price = CurrencyUtil.parseRupiah(priceField.getText());
        int quantity = Integer.parseInt(quantityField.getText());

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE products SET name = ?, price = ?, quantity = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setInt(3, quantity);
            statement.setInt(4, id);
            statement.executeUpdate();

            loadProducts();
            JOptionPane.showMessageDialog(this, "Produk berhasil diperbarui.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memperbarui produk.");
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan dihapus.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM products WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, id);
                statement.executeUpdate();

                loadProducts();
                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus produk.");
            }
        }
    }

    private void loadProducts() {
        tableModel.setRowCount(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                String formattedPrice = CurrencyUtil.formatRupiah(price);

                tableModel.addRow(new Object[]{id, name, formattedPrice, quantity});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat produk.");
        }
    }

    private void fillFieldsFromSelectedRow() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            String formattedPrice = (String) tableModel.getValueAt(selectedRow, 2);
            double parsedPrice = CurrencyUtil.parseRupiah(formattedPrice);
            priceField.setText(CurrencyUtil.formatRupiah(parsedPrice));
            quantityField.setText(String.valueOf(tableModel.getValueAt(selectedRow, 3)));
        }
    }
}
