package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.model.transaksi.*;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KasirGUI extends JFrame {

    private Akun currentAkun;
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    private JPanel listContainer;
    private Timer refreshTimer;

    // --- WARNA & FONT ---
    private final Color BG_COLOR = new Color(248, 250, 252);
    private final Color HEADER_COLOR = new Color(37, 99, 235);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(15, 23, 42);

    public KasirGUI(Akun akun) {
        this.currentAkun = akun;
        setTitle("Dashboard Kasir - " + akun.getNama());
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Content Area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Sub-Header (Judul Kolom)
        contentPanel.add(createTableHeaders(), BorderLayout.NORTH);

        // List Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scroll, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Auto Refresh
        refreshTimer = new Timer(2000, e -> refreshList());
        refreshTimer.start();
        refreshList();
    }

    private void refreshList() {
        listContainer.removeAll();
        sys.refreshPesananFromFile();
        List<Pesanan> all = sys.getDaftarPesanan();

        // 1. Filter: Ambil SIAP DISAJIKAN (output Koki) atau DISAJIKAN
        List<Pesanan> readyToPay = new ArrayList<>();
        if (all != null) {
            for (Pesanan p : all) {
                String s = p.getStatus().toUpperCase();
                // Kasir melihat pesanan yang sudah selesai dimasak
                if (s.equals("SIAP DISAJIKAN") || s.equals("DISAJIKAN")) {
                    readyToPay.add(p);
                }
            }
        }

        if (readyToPay.isEmpty()) {
            JLabel empty = new JLabel("Tidak ada tagihan aktif.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        } else {
            // 2. GROUPING BY MEJA
            Map<Integer, List<Pesanan>> grouped = readyToPay.stream()
                .collect(Collectors.groupingBy(p -> p.getMeja().getNomor()));
            
            // Sort by meja
            List<Integer> sortedKeys = new ArrayList<>(grouped.keySet());
            java.util.Collections.sort(sortedKeys);

            for(Integer meja : sortedKeys) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(BG_COLOR);
                wrapper.add(createGroupCard(meja, grouped.get(meja)), BorderLayout.NORTH);
                
                listContainer.add(wrapper);
                listContainer.add(Box.createVerticalStrut(15));
            }
        }
        
        listContainer.add(Box.createVerticalGlue());
        listContainer.revalidate();
        listContainer.repaint();
    }
    
    // --- KARTU TAGIHAN PER MEJA ---
    private JPanel createGroupCard(int mejaNum, List<Pesanan> orders) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(226, 232, 240), 1), 
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(CARD_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 5, 0, 5);

        // HITUNG TOTAL GABUNGAN
        double totalMeja = 0;
        for(Pesanan p : orders) totalMeja += p.getTotal();

        // --- KOLOM 1: INFO MEJA ---
        gbc.gridx = 0; gbc.weightx = 0.2;
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblMeja = new JLabel("Meja " + mejaNum);
        lblMeja.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMeja.setForeground(TEXT_PRIMARY);
        
        JLabel lblCount = new JLabel(orders.size() + " Pesanan Gabungan");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCount.setForeground(Color.GRAY);
        
        infoPanel.add(lblMeja);
        infoPanel.add(lblCount);
        card.add(infoPanel, gbc);

        // --- KOLOM 2: DETAIL MENU ---
        gbc.gridx = 1; gbc.weightx = 0.5;
        StringBuilder sb = new StringBuilder("<html><body style='font-family: Segoe UI;'>");
        
        for(Pesanan p : orders) {
            for (DetailPesanan dp : p.getItems()) {
                sb.append("<div style='margin-bottom:3px;'>");
                sb.append("• <b>").append(dp.getMenu().getNama()).append("</b>");
                sb.append(" <span style='color:gray'>x").append(dp.getJumlah()).append("</span>");
                sb.append(" <span style='color:#16a34a; font-size:10px;'>(Rp").append((int)dp.getSubtotal()).append(")</span>");
                sb.append("</div>");
            }
        }
        sb.append("</body></html>");
        
        JLabel lblMenu = new JLabel(sb.toString());
        lblMenu.setVerticalAlignment(SwingConstants.TOP);
        card.add(lblMenu, gbc);

        // --- KOLOM 3: TOTAL & BUTTON ---
        gbc.gridx = 2; gbc.weightx = 0.3;
        JPanel actionPanel = new JPanel(new BorderLayout(5, 5));
        actionPanel.setOpaque(false);
        
        JLabel totalLbl = new JLabel("Total: Rp " + (int)totalMeja);
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLbl.setForeground(new Color(22, 163, 74)); // Hijau
        totalLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JButton payBtn = new JButton("Bayar & Struk");
        payBtn.setBackground(new Color(34, 197, 94)); // Hijau
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // --- FIX MAC OS (Agar tombol Hijau terlihat) ---
        payBtn.setOpaque(true);
        payBtn.setBorderPainted(false);
        
        final double finalTotal = totalMeja;
        payBtn.addActionListener(e -> prosesBayar(mejaNum, orders, finalTotal));
        
        actionPanel.add(totalLbl, BorderLayout.NORTH);
        actionPanel.add(payBtn, BorderLayout.SOUTH);
        
        card.add(actionPanel, gbc);

        return card;
    }

    private void prosesBayar(int mejaNum, List<Pesanan> orders, double total) {
        String[] options = {"Cash", "QRIS", "Debit"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Total Tagihan Meja " + mejaNum + ": Rp " + (int)total + "\nPilih Metode:", 
            "Pembayaran", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
            null, options, options[0]);

        if (choice >= 0) {
            String metode = options[choice];
            double bayar = 0;
            
            if (choice == 0) { // Cash logic
                String input = JOptionPane.showInputDialog("Masukkan Uang Tunai:");
                if(input == null || input.isEmpty()) return;
                try {
                    bayar = Double.parseDouble(input);
                    if(bayar < total) {
                        JOptionPane.showMessageDialog(this, "Uang kurang! Pembayaran dibatalkan."); return;
                    }
                } catch(Exception ex) { 
                    JOptionPane.showMessageDialog(this, "Input harus angka!"); return; 
                }
            } else {
                bayar = total; // Non-tunai pas
            }

            Pembayaran pb = new CashPayment(bayar); 
            for(Pesanan p : orders) {
                sys.buatTransaksi(p, pb); 
                sys.updateStatusPesanan(p.getId(), "LUNAS");
            }
            
            tampilkanStruk(mejaNum, orders, total, bayar, metode);
            refreshList();
        }
    }

    private void tampilkanStruk(int meja, List<Pesanan> orders, double total, double bayar, String metode) {
        StringBuilder struk = new StringBuilder();
        struk.append("========== RESTAURANT SYSTEM ==========\n");
        struk.append("Tanggal: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))).append("\n");
        struk.append("Meja   : ").append(meja).append("\n");
        struk.append("---------------------------------------\n");
        struk.append(String.format("%-20s %5s %10s\n", "MENU", "QTY", "SUBTOTAL"));
        struk.append("---------------------------------------\n");
        
        for(Pesanan p : orders) {
            for(DetailPesanan dp : p.getItems()) {
                struk.append(String.format("%-20s x%-4d %10d\n", 
                    limitString(dp.getMenu().getNama(), 20), 
                    dp.getJumlah(), 
                    (int)dp.getSubtotal()));
            }
        }
        
        struk.append("---------------------------------------\n");
        struk.append(String.format("TOTAL     : Rp %d\n", (int)total));
        struk.append(String.format("BAYAR (%s): Rp %d\n", metode, (int)bayar));
        struk.append(String.format("KEMBALI   : Rp %d\n", (int)(bayar - total)));
        struk.append("=======================================\n");
        struk.append("          TERIMA KASIH                 \n");

        JTextArea area = new JTextArea(struk.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setMargin(new Insets(10,10,10,10));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Cetak Struk", JOptionPane.PLAIN_MESSAGE);
    }
    
    private String limitString(String s, int max) {
        if(s.length() > max) return s.substring(0, max-3) + "...";
        return s;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(100, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Kasir - Pembayaran");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69)); // Merah
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // --- FIX MAC OS (Agar tombol Merah terlihat) ---
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        
        btnLogout.addActionListener(e -> {
            if(refreshTimer != null) refreshTimer.stop();
            dispose();
            new LoginGUI().setVisible(true);
        });
        
        JPanel pBtn = new JPanel(new GridBagLayout()); 
        pBtn.setOpaque(false); pBtn.add(btnLogout);
        header.add(pBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createTableHeaders() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(HEADER_COLOR);
        panel.setBorder(new EmptyBorder(10,10,10,10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font f = new Font("Segoe UI", Font.BOLD, 14);
        Color c = Color.WHITE;
        
        gbc.gridx=0; gbc.weightx=0.2;
        JLabel h1 = new JLabel("Info Meja"); h1.setFont(f); h1.setForeground(c);
        panel.add(h1, gbc);
        
        gbc.gridx=1; gbc.weightx=0.5;
        JLabel h2 = new JLabel("Detail Pesanan"); h2.setFont(f); h2.setForeground(c);
        panel.add(h2, gbc);
        
        gbc.gridx=2; gbc.weightx=0.3; 
        JLabel h3 = new JLabel("Total & Aksi"); h3.setFont(f); h3.setForeground(c);
        panel.add(h3, gbc);
        
        return panel;
    }
}