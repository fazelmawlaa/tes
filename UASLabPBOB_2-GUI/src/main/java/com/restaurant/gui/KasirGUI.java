package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.model.akun.Pegawai; // Import Pegawai
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.model.transaksi.*;
import com.restaurant.service.RestaurantSystem;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class KasirGUI extends JFrame {

    private Akun currentAkun;
    private RestaurantSystem sys = RestaurantSystem.getInstance();
    private JPanel listContainer;
    private Timer refreshTimer;

    // Palet Warna
    private final Color BG_COLOR = new Color(241, 243, 245);
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);

    public KasirGUI(Akun akun) {
        this.currentAkun = akun;
        setTitle("Dashboard Kasir - " + akun.getNama());
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Content
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);
        listContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Auto Refresh
        refreshTimer = new Timer(2000, e -> refreshList());
        refreshTimer.start();
        refreshList();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setPreferredSize(new Dimension(900, 70));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Pembayaran & Kasir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

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
                "Keluar dari Kasir?", "Logout", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION){
                if(refreshTimer != null) refreshTimer.stop();
                dispose();
                new LoginGUI().setVisible(true);
            }
        });

        header.add(btnLogout, BorderLayout.EAST);
        return header;
    }

    private void refreshList() {
        listContainer.removeAll();
        sys.refreshPesananFromFile();
        List<Pesanan> all = sys.getDaftarPesanan();

        boolean adaPesanan = false;
        if (all != null) {
            for (Pesanan p : all) {
                // Kasir hanya melihat yang DISAJIKAN (siap bayar)
                if (p.getStatus().equalsIgnoreCase("DISAJIKAN")) {
                    listContainer.add(createCard(p));
                    listContainer.add(Box.createVerticalStrut(15));
                    adaPesanan = true;
                }
            }
        }

        if (!adaPesanan) {
            JLabel empty = new JLabel("Tidak ada tagihan yang harus dibayar.", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 16));
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(Box.createVerticalStrut(50));
            listContainer.add(empty);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createCard(Pesanan p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1), 
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Info Kiri
        JLabel info = new JLabel("<html><b style='font-size:14px'>Meja " + p.getMeja().getNomor() + "</b>" +
                "<br><span style='color:gray'>ID: " + p.getId() + "</span></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(info, BorderLayout.WEST);

        // Info Tengah (Total)
        JLabel total = new JLabel("Total: Rp " + (int)p.getTotal());
        total.setFont(new Font("Segoe UI", Font.BOLD, 18));
        total.setForeground(new Color(30, 41, 59));
        total.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(total, BorderLayout.CENTER);

        // Tombol Kanan
        JButton payBtn = new JButton("Proses Bayar");
        payBtn.setBackground(new Color(34, 197, 94)); // Hijau
        payBtn.setForeground(Color.WHITE);
        payBtn.setOpaque(true);
        payBtn.setBorderPainted(false);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        payBtn.addActionListener(e -> showPaymentDialog(p));
        
        card.add(payBtn, BorderLayout.EAST);
        return card;
    }

    private void showPaymentDialog(Pesanan p) {
        String[] options = {"Cash", "Card", "QRIS"};
        int choice = JOptionPane.showOptionDialog(this, 
                "Total Tagihan: Rp " + (int)p.getTotal() + "\nPilih Metode Pembayaran:", 
                "Pembayaran Meja " + p.getMeja().getNomor(), 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, 
                null, options, options[0]);

        if (choice >= 0) {
            Pembayaran pb = null;
            if (choice == 0) { // Cash
                String input = JOptionPane.showInputDialog(this, "Masukkan jumlah uang:");
                if (input != null && !input.isEmpty()) {
                    try {
                        double uang = Double.parseDouble(input);
                        if (uang < p.getTotal()) {
                            JOptionPane.showMessageDialog(this, "Uang tidak cukup!", "Gagal", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        pb = new CashPayment(uang);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Input harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else return;
            } else if (choice == 1) {
                pb = new CardPayment();
            } else {
                pb = new QRISPayment();
            }

            // Proses Transaksi
            Transaksi t = sys.buatTransaksi(p, pb);
            if (t.konfirmasi()) {
                sys.updateStatusPesanan(p.getId(), "LUNAS");
                
                String msg = "Pembayaran Berhasil!";
                if (pb instanceof CashPayment) {
                    double kembalian = ((CashPayment)pb).getJumlahUang() - p.getTotal();
                    msg += "\nKembalian: Rp " + (int)kembalian;
                }
                JOptionPane.showMessageDialog(this, msg);
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Pembayaran Gagal.", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                "P03",           // ID
                "Alaudin Kasir", // Nama
                "kasir1",        // Username
                "pass",          // Password
                "kasir@usk.ac.id", // Email
                "Kasir"          // Role
            );
            new KasirGUI(dummy).setVisible(true);
        });
    }
}