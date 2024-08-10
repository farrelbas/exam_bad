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

public class CustomerManagementPanel extends JPanel {

    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, emailField, phoneField;
    private JButton addButton, updateButton, deleteButton;

    public CustomerManagementPanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Email", "Telepon"}, 0);
        customerTable = new JTable(tableModel);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Nama:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        inputPanel.add(new JLabel("Telepon:"));
        phoneField = new JTextField();
        inputPanel.add(phoneField);

        addButton = new JButton("Tambah Pelanggan");
        addButton.addActionListener(e -> addCustomer());
        inputPanel.add(addButton);

        updateButton = new JButton("Perbarui Pelanggan");
        updateButton.addActionListener(e -> updateCustomer());
        inputPanel.add(updateButton);

        deleteButton = new JButton("Hapus Pelanggan");
        deleteButton.addActionListener(e -> deleteCustomer());
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.SOUTH);

        customerTable.getSelectionModel().addListSelectionListener(e -> fillFieldsFromSelectedRow());

        loadCustomers();
    }

    private void addCustomer() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO customers (name, email, phone) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.executeUpdate();

            loadCustomers();
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil ditambahkan.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambahkan pelanggan.");
        }
    }

    private void updateCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pelanggan yang akan diperbarui.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setInt(4, id);
            statement.executeUpdate();

            loadCustomers();
            JOptionPane.showMessageDialog(this, "Pelanggan berhasil diperbarui.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memperbarui pelanggan.");
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pelanggan yang akan dihapus.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus pelanggan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM customers WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, id);
                statement.executeUpdate();

                loadCustomers();
                JOptionPane.showMessageDialog(this, "Pelanggan berhasil dihapus.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menghapus pelanggan.");
            }
        }
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM customers";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String phone = resultSet.getString("phone");

                tableModel.addRow(new Object[]{id, name, email, phone});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat pelanggan.");
        }
    }

    private void fillFieldsFromSelectedRow() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow != -1) {
            nameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            emailField.setText((String) tableModel.getValueAt(selectedRow, 2));
            phoneField.setText((String) tableModel.getValueAt(selectedRow, 3));
        }
    }
}
