package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.menu.MenuItem;
import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*; 
import java.util.List;

public class PelayanGUI extends JFrame {

    private Akun currentAkun;
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    
    private JTextField tfMeja;
    private JComboBox<String> cbMenu;
    private JSpinner spinJumlah;
    private JTextField tfCatatan;
    private DefaultListModel<String> cartModel;
    
    private java.util.List<DetailPesanan> tempItems = new java.util.ArrayList<>();

    public PelayanGUI(Akun akun) {
        this.currentAkun = akun;
        setTitle("Dashboard Pelayan - " + akun.getNama());
        setSize(600, 700); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 250, 252));
        setContentPane(mainPanel);

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(59, 130, 246));
        header.setPreferredSize(new Dimension(100, 60));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));
        
        JLabel lblTitle = new JLabel("Input Pesanan Baru");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69)); // Merah
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        // PENTING UNTUK MAC: Agar warna background tombol muncul
        btnLogout.setOpaque(true); 
        
        btnLogout.addActionListener(e -> { dispose(); new LoginGUI().setVisible(true); });
        
        JPanel btnP = new JPanel(new GridBagLayout()); 
        btnP.setOpaque(false); 
        btnP.add(btnLogout);
        header.add(btnP, BorderLayout.EAST);
        
        mainPanel.add(header, BorderLayout.NORTH);

        // --- Content Area ---
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(new Color(248, 250, 252));

        // --- Form Input Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(15,15,15,15)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Row 0: Meja
        gbc.gridx=0; gbc.gridy=0; formPanel.add(new JLabel("Nomor Meja:"), gbc);
        tfMeja = new JTextField(); 
        gbc.gridx=1; gbc.gridy=0; gbc.weightx=1.0; formPanel.add(tfMeja, gbc);

        // Row 1: Menu
        gbc.gridx=0; gbc.gridy=1; gbc.weightx=0; formPanel.add(new JLabel("Menu:"), gbc);
        cbMenu = new JComboBox<>();
        if(sys.getMenuList() != null) {
             for (MenuItem m : sys.getMenuList()) cbMenu.addItem(m.getNama());
        }
        gbc.gridx=1; gbc.gridy=1; gbc.weightx=1.0; formPanel.add(cbMenu, gbc);

        // Row 2: Jumlah
        gbc.gridx=0; gbc.gridy=2; gbc.weightx=0; formPanel.add(new JLabel("Jumlah:"), gbc);
        spinJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        gbc.gridx=1; gbc.gridy=2; gbc.weightx=1.0; formPanel.add(spinJumlah, gbc);
        
        // Row 3: Catatan
        gbc.gridx=0; gbc.gridy=3; gbc.weightx=0; formPanel.add(new JLabel("Catatan:"), gbc);
        tfCatatan = new JTextField();
        gbc.gridx=1; gbc.gridy=3; gbc.weightx=1.0; formPanel.add(tfCatatan, gbc);

        // Row 4: Tombol Tambah Item
        JButton btnAdd = new JButton("Tambah Item");
        btnAdd.setBackground(new Color(59, 130, 246)); // Biru
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setFocusPainted(false);
        
        // FIX UNTUK MAC OS AGAR TOMBOL TIDAK INVISIBLE
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);
        
        btnAdd.addActionListener(e -> tambahKeKeranjang());
        gbc.gridx=1; gbc.gridy=4; formPanel.add(btnAdd, gbc);

        content.add(formPanel, BorderLayout.NORTH);

        // --- List Keranjang ---
        cartModel = new DefaultListModel<>();
        JList<String> cartList = new JList<>(cartModel);
        cartList.setBorder(new TitledBorder("Keranjang Sementara"));
        content.add(new JScrollPane(cartList), BorderLayout.CENTER);

        // --- Tombol Kirim ---
        JButton btnKirim = new JButton("KIRIM PESANAN KE DAPUR");
        btnKirim.setBackground(new Color(34, 197, 94)); // Hijau
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnKirim.setPreferredSize(new Dimension(100, 50));
        
        // FIX UNTUK MAC OS AGAR TOMBOL TIDAK INVISIBLE
        btnKirim.setOpaque(true);
        btnKirim.setBorderPainted(false);
        
        btnKirim.addActionListener(e -> kirimPesanan());
        
        content.add(btnKirim, BorderLayout.SOUTH);

        mainPanel.add(content, BorderLayout.CENTER);
    }

    private void tambahKeKeranjang() {
        String nama = (String) cbMenu.getSelectedItem();
        if (nama == null) return;

        int qty = (int) spinJumlah.getValue();
        String cat = tfCatatan.getText();
        
        // Cari menu object
        MenuItem item = null;
        for(MenuItem m : sys.getMenuList()) if(m.getNama().equals(nama)) item = m;
        
        if(item != null) {
            tempItems.add(new DetailPesanan(item, qty, cat));
            cartModel.addElement(nama + " x" + qty + (cat.isEmpty() ? "" : " ("+cat+")"));
            spinJumlah.setValue(1); tfCatatan.setText("");
        }
    }

    private void kirimPesanan() {
        if(tfMeja.getText().isEmpty() || tempItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data belum lengkap!"); return;
        }
        try {
            int meja = Integer.parseInt(tfMeja.getText());
            Pesanan p = sys.buatPesananKosong(meja);
            for(DetailPesanan dp : tempItems) p.tambahItem(dp);
            
            p.setStatus("DIPROSES"); // Langsung ke Koki
            sys.saveData();
            
            JOptionPane.showMessageDialog(this, "Terkirim ke Dapur!");
            tempItems.clear(); cartModel.clear(); tfMeja.setText("");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Meja harus angka!");
        }
    }
}