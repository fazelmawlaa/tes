package com.restaurant.gui;

import com.restaurant.model.akun.Akun;
import com.restaurant.service.AuthService;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.*;

public class LoginGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardContainer;
    private AuthService auth = AuthService.getInstance(); // Pastikan AuthService punya method getInstance()

    // Palet Warna
    private final Color BG_COLOR = new Color(241, 243, 245);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private final Color BLUE_BUTTON = new Color(59, 130, 246);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    
    // Fonts
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    private final Font INPUT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    public LoginGUI() {
        setTitle("Restaurant System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);

        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(BG_COLOR);
        setContentPane(mainContainer);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(CARD_COLOR);
        cardContainer.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1), 
            new EmptyBorder(40, 50, 40, 50)
        ));

        cardContainer.add(createLoginPanel(), "LOGIN");
        cardContainer.add(createRegisterPanel(), "REGISTER");

        mainContainer.add(cardContainer);
        cardLayout.show(cardContainer, "LOGIN");
    }

    // Panel Login
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = createGbc();

        addTitle(panel, "Restaurant System", "Login", gbc);

        addLabel(panel, "Username", gbc);
        JTextField tfUser = createTextField();
        panel.add(tfUser, gbc);
        addSpacer(gbc, 15);

        addLabel(panel, "Password", gbc);
        JPasswordField pfPass = createPasswordField();
        panel.add(pfPass, gbc);
        addSpacer(gbc, 15);

        addLabel(panel, "Login Sebagai", gbc);
        String[] loginRoles = {"Kasir", "Koki", "Pelayan", "Customer"};
        JComboBox<String> cbRole = createComboBox(loginRoles);
        panel.add(cbRole, gbc);
        addSpacer(gbc, 25);

        JButton btnLogin = createBlueButton("Login");
        btnLogin.addActionListener(e -> {
            String user = tfUser.getText().trim();
            String pass = new String(pfPass.getPassword());
            String role = (String) cbRole.getSelectedItem();
            
            // PERBAIKAN: Menggunakan auth.login(), lalu cek Role manual
            Akun a = auth.login(user, pass);
            
            if (a != null) {
                // Cek apakah Role yang dipilih sesuai dengan Role di akun
                if (a.getRole().equalsIgnoreCase(role)) {
                    JOptionPane.showMessageDialog(this, "Login Berhasil sebagai " + role);
                    dispose();
                    
                    // Routing ke GUI masing-masing
                    SwingUtilities.invokeLater(() -> {
                        if (role.equalsIgnoreCase("Kasir")) {
                            new KasirGUI(a).setVisible(true); 
                        } else if (role.equalsIgnoreCase("Koki")) {
                            new KokiGUI(a).setVisible(true); 
                        } else if (role.equalsIgnoreCase("Pelayan")) {
                            new PelayanGUI(a).setVisible(true); 
                        } else {
                            JOptionPane.showMessageDialog(null, "Menu Customer belum tersedia.");
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Login Gagal! Role salah (Akun ini terdaftar sebagai " + a.getRole() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Login Gagal! Username atau Password salah.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnLogin, gbc);

        addFooterLink(panel, "Belum punya akun? ", "Daftar di sini", gbc, () -> cardLayout.show(cardContainer, "REGISTER"));

        return panel;
    }

    // PANEL REGISTER
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = createGbc();

        addTitle(panel, "Daftar Akun Baru", "Isi data pegawai atau customer", gbc);

        addLabel(panel, "Username", gbc);
        JTextField tfUser = createTextField();
        panel.add(tfUser, gbc);
        addSpacer(gbc, 15);

        addLabel(panel, "Password", gbc);
        JPasswordField pfPass = createPasswordField();
        panel.add(pfPass, gbc);
        addSpacer(gbc, 15);

        // --- Tipe Akun ---
        addLabel(panel, "Tipe Akun", gbc);
        String[] tipeAkun = {"Pegawai", "Customer"};
        JComboBox<String> cbTipe = createComboBox(tipeAkun);
        panel.add(cbTipe, gbc);
        addSpacer(gbc, 15);

        // --- Role Pegawai ---
        JLabel lblRole = new JLabel("Role Pegawai");
        lblRole.setFont(LABEL_FONT);
        lblRole.setForeground(TEXT_PRIMARY);
        
        String[] roles = {"Kasir", "Koki", "Pelayan"};
        JComboBox<String> cbRole = createComboBox(roles);

        JPanel rolePanel = new JPanel(new BorderLayout());
        rolePanel.setBackground(CARD_COLOR);
        rolePanel.add(lblRole, BorderLayout.NORTH);
        rolePanel.add(cbRole, BorderLayout.CENTER);
        rolePanel.setBorder(new EmptyBorder(0,0,15,0)); 
        
        panel.add(rolePanel, gbc);

        // Logic Hide/Show Role
        cbTipe.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean isPegawai = "Pegawai".equals(cbTipe.getSelectedItem());
                rolePanel.setVisible(isPegawai);
                panel.revalidate(); 
            }
        });

        JButton btnDaftar = createBlueButton("Daftar");
        btnDaftar.addActionListener(e -> {
            String user = tfUser.getText().trim();
            String pass = new String(pfPass.getPassword());
            String tipe = (String) cbTipe.getSelectedItem();
            
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Harap isi semua kolom.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = false;

            // PERBAIKAN: Memilah registerCustomer atau registerPegawai
            if ("Customer".equals(tipe)) {
                // Untuk customer, nama disamakan dengan username, email dikosongkan
                success = auth.registerCustomer(user, user, pass, ""); 
            } else {
                // Untuk pegawai
                String finalRole = (String) cbRole.getSelectedItem();
                // Generate Email Dummy agar lolos validasi AuthService (harus @usk.ac.id)
                String dummyEmail = user.replaceAll("\\s+", "").toLowerCase() + "@usk.ac.id";
                
                success = auth.registerPegawai(user, user, pass, dummyEmail, finalRole);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Registrasi Berhasil! Silakan Login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                tfUser.setText("");
                pfPass.setText("");
                cardLayout.show(cardContainer, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Registrasi Gagal (Username sudah ada atau Email Pegawai tidak valid).", "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnDaftar, gbc);

        addFooterLink(panel, "Sudah punya akun? ", "Login di sini", gbc, () -> cardLayout.show(cardContainer, "LOGIN"));

        return panel;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        return gbc;
    }
    private void addSpacer(GridBagConstraints gbc, int h) { gbc.insets = new Insets(0, 0, h, 0); }
    private void addTitle(JPanel p, String t, String s, GridBagConstraints gbc) {
        JLabel lt = new JLabel(t, SwingConstants.CENTER); lt.setFont(TITLE_FONT); lt.setForeground(TEXT_PRIMARY);
        JLabel ls = new JLabel(s, SwingConstants.CENTER); ls.setFont(new Font("SansSerif", Font.PLAIN, 14)); ls.setForeground(TEXT_SECONDARY);
        gbc.insets = new Insets(0,0,5,0); p.add(lt, gbc);
        gbc.insets = new Insets(0,0,30,0); p.add(ls, gbc);
        gbc.insets = new Insets(0,0,0,0);
    }
    private void addLabel(JPanel p, String t, GridBagConstraints gbc) {
        JLabel l = new JLabel(t); l.setFont(LABEL_FONT); l.setForeground(TEXT_PRIMARY);
        gbc.insets = new Insets(0,0,8,0); p.add(l, gbc); gbc.insets = new Insets(0,0,0,0);
    }
    private JTextField createTextField() { JTextField t = new JTextField(20); styleInput(t); return t; }
    private JPasswordField createPasswordField() { JPasswordField p = new JPasswordField(20); styleInput(p); return p; }
    private JComboBox<String> createComboBox(String[] i) {
        JComboBox<String> c = new JComboBox<>(i); c.setFont(INPUT_FONT); c.setBackground(Color.WHITE);
        c.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(5,5,5,5))); return c;
    }
    private void styleInput(JTextField t) {
        t.setFont(INPUT_FONT); t.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(10,12,10,12)));
    }
    private JButton createBlueButton(String t) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isArmed()?BLUE_BUTTON.darker():BLUE_BUTTON); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif",Font.BOLD,15)); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(100,45)); return b;
    }
    private void addFooterLink(JPanel p, String pr, String lt, GridBagConstraints gbc, Runnable act) {
        JLabel l = new JLabel("<html>"+pr+"<span style='color:#3b82f6;'><u>"+lt+"</u></span></html>", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif",Font.PLAIN,14)); l.setForeground(TEXT_SECONDARY); l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){act.run();}});
        gbc.insets = new Insets(20,0,0,0); p.add(l, gbc);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}