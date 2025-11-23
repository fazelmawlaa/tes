package com.restaurant.app;

import java.util.List;
import java.util.Scanner;

import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.service.RestaurantSystem;
import com.restaurant.utils.InputUtil;

public class MainPelayan {

    private static Scanner sc = InputUtil.sc;
    private static RestaurantSystem rs = RestaurantSystem.getInstance();

    public static void run() {
        while (true) {
            // Tampilkan notifikasi real-time sebelum menu
            tampilRealTimeNotifications();

            System.out.println("\n===== MENU PELAYAN =====");
            System.out.println("1. Lihat Pesanan MENUNGGU (Pesanan Baru)");
            System.out.println("2. Terima Pesanan -> DIPROSES (Kirim ke Dapur)");
            System.out.println("3. Lihat Pesanan SIAP DISAJIKAN");
            System.out.println("4. Sajikan Pesanan ke Meja -> DISAJIKAN");
            System.out.println("5. Lihat Detail Pesanan");
            System.out.println("0. Logout");
            System.out.print("Pilih: ");

            int p = sc.nextInt();
            sc.nextLine();

            switch (p) {
                case 1:
                    rs.tampilPesananDenganStatus("MENUNGGU");
                    break;
                case 2:
                    terimaPesanan();
                    break;
                case 3:
                    lihatPesananSiapDisajikan();
                    break;
                case 4:
                    sajikanPesanan();
                    break;
                case 5:
                    lihatDetailPesanan();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Pilihan tidak valid!");
            }
        }
    }

    /**
     * Tampilkan notifikasi real-time untuk Pelayan
     */
    private static void tampilRealTimeNotifications() {
        rs.refreshPesananFromFile(); // Reload dari file untuk update terbaru

        List<String> notifications = rs.getRealTimeNotifications("pelayan");
        if (!notifications.isEmpty()) {
            System.out.println("\n📢 NOTIFIKASI REAL-TIME:");
            for (String notif : notifications) {
                System.out.println("  " + notif);
            }
        }

        // Check pesanan baru (MENUNGGU)
        List<Pesanan> pesananBaru = rs.getPesananByStatusForRole("pelayan", "MENUNGGU");
        if (!pesananBaru.isEmpty()) {
            System.out.println("\n🔔 " + pesananBaru.size() + " pesanan baru menunggu diproses!");
        }

        // Check pesanan siap disajikan
        List<Pesanan> siapDisajikan = rs.getPesananByStatusForRole("pelayan", "SIAP DISAJIKAN");
        if (!siapDisajikan.isEmpty()) {
            System.out.println("🍽️ " + siapDisajikan.size() + " pesanan siap disajikan!");
        }
    }

    /**
     * Pelayan menerima pesanan dari customer dan mengirim ke dapur
     * Status: MENUNGGU -> DIPROSES
     */
    private static void terimaPesanan() {
        System.out.println("\n=== PESANAN MENUNGGU ===");
        rs.tampilPesananDenganStatus("MENUNGGU");

        System.out.print("Masukkan ID pesanan yang akan diterima dan dikirim ke dapur: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean ok = rs.updateStatusPesanan(id, "DIPROSES");
        if (ok) {
            System.out.println("✅ Pesanan #" + id + " diterima dan dikirim ke dapur (Status: DIPROSES).");
            System.out.println("📤 Notifikasi telah dikirim ke Koki!");
        } else {
            System.out.println("❌ Pesanan dengan ID " + id + " tidak ditemukan atau status tidak valid.");
        }
    }

    /**
     * Pelayan melihat pesanan yang sudah siap disajikan (selesai dimasak)
     */
    private static void lihatPesananSiapDisajikan() {
        System.out.println("\n=== PESANAN SIAP DISAJIKAN (Selesai Dimasak) ===");
        rs.tampilPesananDenganStatus("SIAP DISAJIKAN");
    }

    /**
     * Pelayan menyajikan pesanan ke meja customer
     * Status: SIAP DISAJIKAN -> DISAJIKAN
     */
    private static void sajikanPesanan() {
        System.out.println("\n=== PESANAN SIAP DISAJIKAN ===");
        rs.tampilPesananDenganStatus("SIAP DISAJIKAN");

        System.out.print("Masukkan ID pesanan yang akan disajikan ke meja: ");
        int id = sc.nextInt();
        sc.nextLine();

        boolean ok = rs.updateStatusPesanan(id, "DISAJIKAN");
        if (ok) {
            System.out.println("✅ Pesanan #" + id + " sudah disajikan ke meja (Status: DISAJIKAN).");
            System.out.println("📤 Notifikasi telah dikirim ke Customer dan Kasir!");
        } else {
            System.out.println("❌ Pesanan dengan ID " + id + " tidak ditemukan atau status tidak valid.");
        }
    }

    private static void lihatDetailPesanan() {
        System.out.print("Masukkan ID pesanan: ");
        int id = sc.nextInt();
        sc.nextLine();

        com.restaurant.model.pesanan.Pesanan pesanan = rs.getPesananById(id);
        if (pesanan != null) {
            System.out.println("ID: " + pesanan.getId() + " | Meja: " + pesanan.getMeja().getNomor() +
                    " | Status: " + pesanan.getStatus() + " | Total: Rp" + pesanan.getTotal());
            System.out.println(pesanan.renderDetail());
        } else {
            System.out.println("Pesanan tidak ditemukan.");
        }
    }
}
