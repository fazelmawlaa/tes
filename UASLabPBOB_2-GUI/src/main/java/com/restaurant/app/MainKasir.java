package com.restaurant.app;

import java.util.List;
import java.util.Scanner;

import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.model.transaksi.*;
import com.restaurant.model.Struk;
import com.restaurant.service.RestaurantSystem;
import com.restaurant.utils.InputUtil;

public class MainKasir {

    private static Scanner sc = InputUtil.sc;
    private static RestaurantSystem rs = RestaurantSystem.getInstance();

    public static void run() {
        while (true) {
            // Tampilkan notifikasi real-time sebelum menu
            tampilRealTimeNotifications();

            System.out.println("\n===== MENU KASIR =====");
            System.out.println("1. Lihat Pesanan Siap Bayar (DISAJIKAN)");
            System.out.println("2. Proses Pembayaran");
            System.out.println("0. Logout");
            System.out.print("Pilih: ");

            int p = sc.nextInt();
            sc.nextLine();

            switch (p) {
                case 1:
                    lihatSiapBayar();
                    break;
                case 2:
                    prosesPembayaran();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Pilihan tidak valid!");
            }
        }
    }

    private static void lihatSiapBayar() {
        System.out.println("\n=== PESANAN DISAJIKAN (Siap untuk Pembayaran) ===");
        rs.tampilPesananDenganStatus("DISAJIKAN");
    }

    /**
     * Kasir memproses pembayaran pesanan yang sudah disajikan
     * Status: DISAJIKAN -> LUNAS
     */
    private static void prosesPembayaran() {
        System.out.println("\n=== PILIH PESANAN UNTUK DIBAYAR ===");
        System.out.println("(Hanya pesanan yang sudah DISAJIKAN bisa dibayar)");
        rs.tampilPesananDenganStatus("DISAJIKAN");

        System.out.print("Masukkan ID pesanan: ");
        int id = sc.nextInt();
        sc.nextLine();

        Pesanan p = rs.getPesananById(id);
        if (p == null) {
            System.out.println("❌ ID tidak ditemukan.");
            return;
        }

        // Validasi: hanya pesanan yang sudah disajikan yang bisa dibayar
        if (!p.getStatus().equals("DISAJIKAN")) {
            System.out.println("❌ Pesanan belum disajikan. Status saat ini: " + p.getStatus());
            System.out.println("   Hanya pesanan dengan status DISAJIKAN yang bisa dibayar.");
            return;
        }

        System.out.println("ID: " + p.getId() + " | Meja: " + p.getMeja().getNomor() + " | Total: Rp" + p.getTotal());
        System.out.println(p.renderDetail());
        System.out.println("\nTotal bayar: Rp" + p.getTotal());
        System.out.println("Metode Pembayaran:");
        System.out.println("1. Cash (Tunai)");
        System.out.println("2. Card (Kartu)");
        System.out.println("3. QRIS");

        System.out.print("Pilih metode: ");
        int m = sc.nextInt();
        sc.nextLine();

        Pembayaran pb;

        switch (m) {
            case 1:
                System.out.print("Masukkan uang: ");
                double uang = sc.nextDouble();
                sc.nextLine();
                pb = new CashPayment(uang);
                break;
            case 2:
                pb = new CardPayment();
                break;
            case 3:
                pb = new QRISPayment();
                break;
            default:
                System.out.println("Metode tidak valid.");
                return;
        }

        Transaksi t = rs.buatTransaksi(p, pb);

        if (t.konfirmasi()) {
            Struk.cetak(t);
            rs.updateStatusPesanan(id, "LUNAS");
            rs.saveData();
            System.out.println("✅ Pembayaran berhasil! Pesanan #" + id + " status menjadi LUNAS.");
            System.out.println("📤 Notifikasi telah dikirim ke Pelayan dan Customer!");
        } else {
            System.out.println("Pembayaran gagal.");
        }
    }

    /**
     * Tampilkan notifikasi real-time untuk Kasir
     */
    private static void tampilRealTimeNotifications() {
        rs.refreshPesananFromFile(); // Reload dari file untuk update terbaru

        List<String> notifications = rs.getRealTimeNotifications("kasir");
        if (!notifications.isEmpty()) {
            System.out.println("\n📢 NOTIFIKASI REAL-TIME:");
            for (String notif : notifications) {
                System.out.println("  " + notif);
            }
        }

        // Check pesanan siap bayar
        List<Pesanan> siapBayar = rs.getPesananByStatusForRole("kasir", "DISAJIKAN");
        if (!siapBayar.isEmpty()) {
            System.out.println("\n💰 " + siapBayar.size() + " pesanan siap untuk pembayaran!");
        }
    }
}
