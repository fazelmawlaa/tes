package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

public class KokiGUI extends JFrame {

    private Akun currentAkun;
    private JPanel listContainer;
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    private Timer refreshTimer;

    // --- WARNA ---
    private final Color THEME_BLUE = new Color(59, 130, 246); 
    private final Color BG_COLOR = new Color(248, 250, 252);
    
    // Status Colors
    private final Color COLOR_DIPROSES = new Color(234, 179, 8); // Kuning
    private final Color COLOR_DIMASAK = new Color(59, 130, 246); // Biru
    private final Color COLOR_SIAP = new Color(34, 197, 94);     // Hijau

    public KokiGUI(Akun akun) {
        this.currentAkun = akun;
        setTitle("Dapur Restaurant - Chef " + akun.getNama());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(createTableHeaders(), BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        refreshTimer = new Timer(3000, e -> refreshList());
        refreshTimer.start();
        refreshList();
    }

    private void refreshList() {
        listContainer.removeAll();
        sys.refreshPesananFromFile();
        List<Pesanan> allOrders = sys.getDaftarPesanan();

        // 1. FILTER: Hanya ambil status aktif dapur
        List<String> visibleStatuses = Arrays.asList(
            "MENUNGGU", "DIPROSES", "SEDANG DIMASAK", "SIAP DISAJIKAN"
        );
        
        List<Pesanan> filtered = new ArrayList<>();
        if(allOrders != null) {
            for(Pesanan p : allOrders) {
                if(visibleStatuses.contains(p.getStatus().toUpperCase())) {
                    filtered.add(p);
                }
            }
        }

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("Tidak ada pesanan aktif.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(30));
            listContainer.add(empty);
        } else {
            // 2. GROUPING: Satukan berdasarkan Nomor Meja
            Map<Integer, List<Pesanan>> groupedByTable = filtered.stream()
                .collect(Collectors.groupingBy(p -> p.getMeja().getNomor()));

            // Sort by nomor meja
            List<Integer> sortedTables = new ArrayList<>(groupedByTable.keySet());
            java.util.Collections.sort(sortedTables);

            for (Integer mejaNum : sortedTables) {
                List<Pesanan> pesananMeja = groupedByTable.get(mejaNum);
                
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(BG_COLOR);
                wrapper.add(createGroupCard(mejaNum, pesananMeja), BorderLayout.NORTH);
                
                listContainer.add(wrapper);
                listContainer.add(Box.createVerticalStrut(15));
            }
        }
        
        listContainer.add(Box.createVerticalGlue());
        listContainer.revalidate();
        listContainer.repaint();
    }

    // --- KARTU GROUP (SATU MEJA BANYAK ITEM) ---
    private JPanel createGroupCard(int mejaNum, List<Pesanan> orders) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 5, 0, 5);

        // --- 1. INFO MEJA ---
        gbc.gridx = 0; gbc.weightx = 0.25;
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblMeja = new JLabel("Meja " + mejaNum);
        lblMeja.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblMeja.setForeground(new Color(15, 23, 42));
        
        String ids = orders.stream().map(p -> "#" + p.getId()).collect(Collectors.joining(", "));
        JLabel lblId = new JLabel("<html><div style='width:100px;'>" + ids + "</div></html>");
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblId.setForeground(Color.GRAY);
        
        infoPanel.add(lblMeja);
        infoPanel.add(lblId);
        card.add(infoPanel, gbc);

        // --- 2. DETAIL MENU (GABUNGAN SEMUA PESANAN) ---
        gbc.gridx = 1; gbc.weightx = 0.50;
        
        StringBuilder sb = new StringBuilder("<html><body style='font-family: Segoe UI;'>");
        for(Pesanan p : orders) {
            for (DetailPesanan dp : p.getItems()) {
                sb.append("<div style='margin-bottom: 4px;'>");
                sb.append("<b>").append(dp.getMenu().getNama()).append("</b>");
                sb.append(" <span style='color:gray'>x").append(dp.getJumlah()).append("</span>");
                
                if (dp.getCatatan() != null && !dp.getCatatan().isEmpty()) {
                    sb.append("<br><span style='color:rgb(220,38,38); font-size:11px;'><i>*")
                      .append(dp.getCatatan()).append("</i></span>");
                }
                sb.append("</div>");
            }
        }
        sb.append("</body></html>");
        
        JLabel lblMenu = new JLabel(sb.toString());
        lblMenu.setVerticalAlignment(SwingConstants.TOP);
        card.add(lblMenu, gbc);

        // --- 3. STATUS DROPDOWN (BATCH UPDATE) ---
        gbc.gridx = 2; gbc.weightx = 0.25;
        
        String[] options = {"DIPROSES", "SEDANG DIMASAK", "SIAP DISAJIKAN"};
        JComboBox<String> cbStatus = new JComboBox<>(options);
        cbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbStatus.setBackground(Color.WHITE);
        
        String currentStatus = orders.get(0).getStatus().toUpperCase();
        if(currentStatus.equals("MENUNGGU")) cbStatus.setSelectedItem("DIPROSES");
        else if(Arrays.asList(options).contains(currentStatus)) cbStatus.setSelectedItem(currentStatus);
        
        styleComboBox(cbStatus, (String)cbStatus.getSelectedItem());

        cbStatus.addActionListener(e -> {
            String selected = (String) cbStatus.getSelectedItem();
            for(Pesanan p : orders) {
                sys.updateStatusPesanan(p.getId(), selected);
            }
            styleComboBox(cbStatus, selected);
        });
        
        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrapper.setOpaque(false);
        statusWrapper.add(cbStatus);
        
        card.add(statusWrapper, gbc);

        return card;
    }

    private void styleComboBox(JComboBox<String> box, String status) {
        if(status.contains("DIPROSES")) box.setForeground(COLOR_DIPROSES);
        else if(status.contains("DIMASAK")) box.setForeground(COLOR_DIMASAK);
        else box.setForeground(COLOR_SIAP);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(THEME_BLUE);
        header.setPreferredSize(new Dimension(100, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel title = new JLabel("Dapur Restaurant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69)); // Merah
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setPreferredSize(new Dimension(100, 35));
        
        // --- FIX MAC OS (Agar tombol Merah terlihat) ---
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        
        btnLogout.addActionListener(e -> {
            if (refreshTimer != null) refreshTimer.stop();
            dispose();
            new LoginGUI().setVisible(true);
        });

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        btnPanel.add(btnLogout);
        header.add(btnPanel, BorderLayout.EAST);

        return header;
    }
    
    private JPanel createTableHeaders() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(THEME_BLUE);
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.insets = new Insets(0, 5, 0, 5);
        
        Font font = new Font("Segoe UI", Font.BOLD, 14);
        Color color = Color.WHITE;

        gbc.gridx = 0; gbc.weightx = 0.25;
        JLabel h1 = new JLabel("Info Meja"); h1.setFont(font); h1.setForeground(color);
        panel.add(h1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.50;
        JLabel h2 = new JLabel("Detail Menu"); h2.setFont(font); h2.setForeground(color);
        panel.add(h2, gbc);

        gbc.gridx = 2; gbc.weightx = 0.25;
        JLabel h3 = new JLabel("Status Pesanan"); h3.setFont(font); h3.setForeground(color);
        panel.add(h3, gbc);

        return panel;
    }
}