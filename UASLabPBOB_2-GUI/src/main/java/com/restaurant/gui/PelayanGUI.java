package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.akun.Pegawai; // Import Pegawai
import com.restaurant.model.menu.MenuItem;
import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*; 
import java.awt.event.*;
import java.util.List;

public class PelayanGUI extends JFrame {

    private Akun currentAkun;
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    
    // Components
    private JTextField tfMeja;
    private JComboBox<String> cbMenu;
    private JSpinner spinJumlah;
    private JTextField tfCatatan;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    
    // Temporary cart sebelum dikirim ke database
    private java.util.List<DetailPesanan> tempItems = new java.util.ArrayList<>();

    public PelayanGUI(Akun akun) {
        this.currentAkun = akun;
        setTitle("Dashboard Pelayan - " + akun.getUsername());
        setSize(600, 650); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        setContentPane(mainPanel);

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Content Container
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // --- INPUT PANEL ---
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx=0; gbc.gridy=0; inputPanel.add(new JLabel("No Meja:"), gbc);
        tfMeja = new JTextField();
        gbc.gridx=1; gbc.gridy=0; gbc.weightx=1.0; inputPanel.add(tfMeja, gbc);

        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0; inputPanel.add(new JLabel("Pilih Menu:"), gbc);
        cbMenu = new JComboBox<>();
        loadMenuToCombo(); 
        gbc.gridx=1; gbc.gridy=1; gbc.weightx=1.0; inputPanel.add(cbMenu, gbc);

        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; inputPanel.add(new JLabel("Jumlah:"), gbc);
        spinJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        gbc.gridx=1; gbc.gridy=2; gbc.weightx=1.0; inputPanel.add(spinJumlah, gbc);
        
        gbc.gridx=0; gbc.gridy=3; gbc.weightx=0; inputPanel.add(new JLabel("Catatan:"), gbc);
        tfCatatan = new JTextField();
        gbc.gridx=1; gbc.gridy=3; gbc.weightx=1.0; inputPanel.add(tfCatatan, gbc);

        JButton btnAdd = new JButton("Tambah Item");
        btnAdd.addActionListener(e -> tambahKeKeranjang());
        gbc.gridx=1; gbc.gridy=4; inputPanel.add(btnAdd, gbc);

        contentPanel.add(inputPanel, BorderLayout.NORTH);

        // --- LIST KERANJANG ---
        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        contentPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);

        // --- TOMBOL KIRIM ---
        JButton btnKirim = new JButton("Kirim Pesanan ke Dapur");
        btnKirim.setBackground(new Color(59, 130, 246));
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setOpaque(true);
        btnKirim.setBorderPainted(false);
        btnKirim.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnKirim.setPreferredSize(new Dimension(100, 50));
        
        btnKirim.addActionListener(e -> kirimPesanan());
        contentPanel.add(btnKirim, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(59, 130, 246));
        header.setPreferredSize(new Dimension(100, 50));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("Dashboard Pelayan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Keluar dari akun pelayan?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new LoginGUI().setVisible(true);
            }
        });

        header.add(btnLogout, BorderLayout.EAST);
        return header;
    }

    private void loadMenuToCombo() {
        List<MenuItem> menuList = sys.getMenuList();
        for (MenuItem m : menuList) {
            cbMenu.addItem(m.getNama());
        }
    }

    private void tambahKeKeranjang() {
        String namaMenu = (String) cbMenu.getSelectedItem();
        int qty = (int) spinJumlah.getValue();
        String cat = tfCatatan.getText();

        MenuItem itemDipilih = null;
        for(MenuItem m : sys.getMenuList()) {
            if(m.getNama().equals(namaMenu)) {
                itemDipilih = m;
                break;
            }
        }

        if(itemDipilih != null) {
            DetailPesanan dp = new DetailPesanan(itemDipilih, qty, cat);
            tempItems.add(dp);
            cartModel.addElement(namaMenu + " x" + qty + " (" + cat + ")");
            
            tfCatatan.setText("");
            spinJumlah.setValue(1);
        }
    }

    private void kirimPesanan() {
        String mejaStr = tfMeja.getText();
        if(mejaStr.isEmpty() || tempItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi nomor meja dan minimal 1 item!");
            return;
        }

        try {
            int noMeja = Integer.parseInt(mejaStr);
            Pesanan p = sys.buatPesananKosong(noMeja);
            for(DetailPesanan dp : tempItems) {
                p.tambahItem(dp);
            }
            sys.saveData();

            JOptionPane.showMessageDialog(this, "Pesanan Berhasil Dikirim! ID: " + p.getId());
            
            tempItems.clear();
            cartModel.clear();
            tfMeja.setText("");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nomor meja harus angka!");
        }
    }

    // ==========================================================
    // PERBAIKAN UTAMA DI SINI (MAIN METHOD)
    // ==========================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            // Menggunakan Pegawai, BUKAN Akun
            Pegawai dummy = new Pegawai(
                "P01",           // ID
                "Budi Pelayan",  // Nama
                "pelayan1",      // Username
                "pass",          // Password
                "budi@usk.ac.id",// Email
                "Pelayan"        // Role
            );
            new PelayanGUI(dummy).setVisible(true);
        });
    }
}