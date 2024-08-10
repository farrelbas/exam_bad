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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Sistem Manajemen Ritel");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Produk", new ProductManagementPanel());
        tabbedPane.addTab("Transaksi", new SalesTransactionPanel());
        tabbedPane.addTab("Pelanggan", new CustomerManagementPanel());

        JPanel logoutPanel = new JPanel();
        tabbedPane.addTab("Logout", logoutPanel);

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tabbedPane.getSelectedIndex() == tabbedPane.getTabCount() - 1) {
                    int response = JOptionPane.showConfirmDialog(
                            MainFrame.this,
                            "Apakah Anda yakin ingin logout?",
                            "Konfirmasi Logout",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        MainFrame.this.dispose();
                        new LoginForm().setVisible(true);
                    } else {
                        tabbedPane.setSelectedIndex(0);
                    }
                }
            }
        });

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
