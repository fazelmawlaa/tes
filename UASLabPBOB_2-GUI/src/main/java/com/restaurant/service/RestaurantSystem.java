package com.restaurant.service;

import java.util.ArrayList;
import java.util.List;

import com.restaurant.model.menu.*;
import com.restaurant.model.pesanan.*;
import com.restaurant.model.transaksi.*;

public class RestaurantSystem {

    private static final RestaurantSystem instance = new RestaurantSystem();

    public static RestaurantSystem getInstance() {
        return instance;
    }

    private RestaurantSystem() {
        initMeja();
        menu = FileStorageService.loadMenu();
        daftarPesanan = FileStorageService.loadPesanan();
        idCounter = FileStorageService.loadLastId();
    }

    private List<MenuItem> menu = new ArrayList<>();
    private List<Pesanan> daftarPesanan = new ArrayList<>();
    private List<Meja> mejaList = new ArrayList<>();
    private int idCounter = 1;

    private void initMeja() {
        for (int i = 1; i <= 30; i++) {
            mejaList.add(new Meja(i));
        }
    }

    // ==========================================
    // 2. PERBAIKAN: GETTER DAFTAR PESANAN
    // ==========================================
    public List<Pesanan> getDaftarPesanan() {
        return daftarPesanan;
    }
    // ==========================================

    public List<MenuItem> getMenuList() { return menu; }

    public MenuItem getMenu(int index) {
        if (index < 0 || index >= menu.size()) return null;
        return menu.get(index);
    }

    public void tampilMenu() {
        int i = 1;
        for (MenuItem m : menu) {
            System.out.println(i + ". " + m.toString());
            i++;
        }
    }

    public List<Meja> getMejaKosong() {
        List<Meja> kosong = new ArrayList<>();
        for (Meja m : mejaList) {
            boolean dipakai = false;
            for (Pesanan p : daftarPesanan) {
                if (p.getMeja().getNomor() == m.getNomor() && !p.getStatus().equals("LUNAS")) {
                    dipakai = true;
                    break;
                }
            }
            if (!dipakai) kosong.add(m);
        }
        return kosong;
    }

    public Pesanan buatPesananKosong(int noMeja) {
        refreshPesananFromFile();
        Pesanan p = new Pesanan(idCounter++, new Meja(noMeja));
        p.setStatus("MENUNGGU");
        daftarPesanan.add(p);
        saveData();
        return p;
    }

    public List<Pesanan> getPesananByStatusForRole(String role, String status) {
        refreshPesananFromFile();
        return RealTimeUpdateService.getInstance().getPesananByStatusForRole(role, status);
    }

    public List<Pesanan> getPesananByMeja(int meja) {
        List<Pesanan> hasil = new ArrayList<>();
        for (Pesanan p : daftarPesanan) {
            if (p.getMeja().getNomor() == meja) hasil.add(p);
        }
        return hasil;
    }

    public Pesanan getPesananById(int id) {
        for (Pesanan p : daftarPesanan) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public boolean updateStatusPesanan(int id, String statusBaru) {
        refreshPesananFromFile();
        Pesanan p = getPesananById(id);
        if (p == null) return false;
        p.setStatus(statusBaru);
        saveData();
        return true;
    }

    public void refreshPesananFromFile() {
        daftarPesanan = FileStorageService.loadPesanan();
    }

    public List<String> getRealTimeNotifications(String role) {
        refreshPesananFromFile();
        return RealTimeUpdateService.getInstance().getNotificationsForRole(role, daftarPesanan);
    }

    public void tampilPesananDenganStatus(String status) {
        boolean found = false;
        for (Pesanan p : daftarPesanan) {
            if (p.getStatus().equalsIgnoreCase(status)) {
                System.out.println("ID:" + p.getId() + " | Meja:" + p.getMeja().getNomor() + " | Total: Rp" + p.getTotal());
                System.out.println(p.renderDetail());
                found = true;
            }
        }
        if (!found) System.out.println("Tidak ada pesanan dengan status: " + status);
    }

    public Transaksi buatTransaksi(Pesanan p, Pembayaran pb) {
        Transaksi t = new Transaksi(p, pb);
        FileStorageService.saveTransaksi(t);
        return t;
    }

    public void saveData() {
        FileStorageService.savePesanan(daftarPesanan, idCounter);
    }
}