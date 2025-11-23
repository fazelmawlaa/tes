package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

public class KokiGUI extends JFrame {

    private Akun currentAkun;
    private JPanel listContainer;
    
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    private Timer refreshTimer;

    // --- PALET WARNA ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_COLOR = new Color(59, 130, 246);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    // Warna Status
    private final Color STATUS_NEW = new Color(234, 179, 8);       // Kuning
    private final Color STATUS_COOKING = new Color(37, 99, 235);   // Biru
    private final Color STATUS_DONE = new Color(22, 163, 74);      // Hijau

    public KokiGUI(Akun akun) {
        this.currentAkun = akun;
        
        setTitle("Dashboard Koki - Restaurant System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // --- HEADER ---
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // --- CONTENT ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);
        listContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Auto-refresh setiap 2 detik
        refreshTimer = new Timer(2000, e -> refreshList());
        refreshTimer.start();
        
        refreshList(); 
    }

    private void refreshList() {
        listContainer.removeAll();

        // 1. Header Kolom
        listContainer.add(createColumnHeader());
        listContainer.add(Box.createVerticalStrut(15)); 

        // 2. Load Data
        sys.refreshPesananFromFile(); // Ambil data terbaru dari file
        List<Pesanan> allOrders = sys.getDaftarPesanan(); 
        boolean adaPesanan = false;
        
        // Daftar status yang WAJIB muncul di layar Koki
        List<String> visibleStatuses = Arrays.asList(
            "MENUNGGU",         // Jika belum diproses pelayan (opsional)
            "DIPROSES",         // Baru masuk dapur
            "SEDANG DIMASAK",   // Sedang dimasak
            "SIAP DISAJIKAN",   // Selesai masak (menunggu pelayan)
            "SELESAI DIMASAK"   // Alternatif nama status
        );

        if (allOrders != null) {
            for (Pesanan p : allOrders) {
                String status = p.getStatus().toUpperCase();
                
                // PERBAIKAN LOGIKA FILTER:
                // Cek apakah status pesanan ada di daftar visibleStatuses
                if (visibleStatuses.contains(status)) {
                    JPanel row = createOrderCard(p);
                    listContainer.add(row);
                    listContainer.add(Box.createVerticalStrut(15)); 
                    adaPesanan = true;
                }
            }
        }

        // 3. State Kosong
        if (!adaPesanan) {
            JLabel empty = new JLabel("Tidak ada pesanan aktif di dapur.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 16));
            empty.setForeground(TEXT_SECONDARY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    // --- PANEL HEADER KOLOM ---
    private JPanel createColumnHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(HEADER_COLOR);
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.weightx = 0.2;
        panel.add(createHeaderText("Info Meja"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        panel.add(createHeaderText("Detail Menu"), gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        panel.add(createHeaderText("Status Dapur"), gbc);

        return panel;
    }

    // --- PANEL KARTU PESANAN (ROW) ---
    private JPanel createOrderCard(Pesanan p) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1), 
            new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 0, 5);

        // --- KOLOM 1: INFO MEJA ---
        gbc.gridx = 0; gbc.weightx = 0.2;
        JPanel infoBox = new JPanel(new GridLayout(2, 1, 0, 5));
        infoBox.setOpaque(false);
        
        JLabel lblMeja = new JLabel("Meja " + p.getMeja().getNomor());
        lblMeja.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMeja.setForeground(TEXT_PRIMARY);
        
        JLabel lblId = new JLabel("#ORD-" + p.getId());
        lblId.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblId.setForeground(TEXT_SECONDARY);
        
        infoBox.add(lblMeja);
        infoBox.add(lblId);
        panel.add(infoBox, gbc);

        // --- KOLOM 2: DETAIL MENU ---
        gbc.gridx = 1; gbc.weightx = 0.6;
        
        StringBuilder sb = new StringBuilder("<html><body style='font-family: Segoe UI; margin: 0;'>");
        for(DetailPesanan dp : p.getItems()) {
            sb.append("<div style='margin-bottom: 4px;'>");
            sb.append("<b>").append(dp.getMenu().getNama()).append("</b>");
            sb.append(" <span style='color: #64748b;'>x").append(dp.getJumlah()).append("</span>");
            
            if(dp.getCatatan() != null && !dp.getCatatan().isEmpty()) {
                sb.append("<br><i style='font-size: 10px; color: #ef4444;'>*").append(dp.getCatatan()).append("</i>");
            }
            sb.append("</div>");
        }
        sb.append("</body></html>");
        
        JLabel lblItems = new JLabel(sb.toString());
        lblItems.setVerticalAlignment(SwingConstants.TOP); 
        panel.add(lblItems, gbc);

        // --- KOLOM 3: DROPDOWN STATUS ---
        gbc.gridx = 2; gbc.weightx = 0.2;
        
        String[] options = {"DIPROSES", "SEDANG DIMASAK", "SIAP DISAJIKAN"};
        JComboBox<String> cbStatus = new JComboBox<>(options);
        cbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbStatus.setFocusable(false);
        
        // Set nilai awal dropdown dengan aman
        String currentStatus = p.getStatus().toUpperCase();
        
        if (currentStatus.contains("DIMASAK")) {
            cbStatus.setSelectedItem("SEDANG DIMASAK");
        } else if (currentStatus.contains("SIAP") || currentStatus.contains("SELESAI")) {
            cbStatus.setSelectedItem("SIAP DISAJIKAN");
        } else {
            cbStatus.setSelectedItem("DIPROSES");
        }

        updateStatusColor(cbStatus, (String)cbStatus.getSelectedItem());

        cbStatus.addActionListener(e -> {
            String selected = (String) cbStatus.getSelectedItem();
            
            if ("SIAP DISAJIKAN".equals(selected)) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Pesanan Meja " + p.getMeja().getNomor() + " selesai dimasak?", 
                    "Konfirmasi", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    refreshList(); // Reset jika batal
                    return; 
                }
            }

            // Update Backend & UI
            boolean success = sys.updateStatusPesanan(p.getId(), selected);
            if(success) {
                p.setStatus(selected); 
                updateStatusColor(cbStatus, selected);
                // Tidak perlu refreshList() disini agar item tidak "lompat" atau hilang tiba-tiba
                // Item hanya akan hilang/berubah posisi saat refresh otomatis (timer) berjalan
            }
        });

        panel.add(cbStatus, gbc);

        return panel;
    }

    private void updateStatusColor(JComboBox<String> box, String status) {
        if (status == null) return;
        
        if (status.contains("DIPROSES")) {
            box.setForeground(STATUS_NEW);
        } else if (status.contains("DIMASAK")) {
            box.setForeground(STATUS_COOKING);
        } else if (status.contains("SIAP") || status.contains("SELESAI")) {
            box.setForeground(STATUS_DONE);
        } else {
            box.setForeground(Color.BLACK);
        }
    }

    private JLabel createHeaderText(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(900, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30)); 

        JLabel lblTitle = new JLabel("Dapur Restaurant");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69)); 
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(110, 35));
        
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Yakin ingin logout?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (refreshTimer != null) refreshTimer.stop(); 
                this.dispose(); 
                new LoginGUI().setVisible(true); 
            }
        });

        JPanel btnWrap = new JPanel(new GridBagLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(btnLogout);
        
        header.add(btnWrap, BorderLayout.EAST);
        return header;
    }
}