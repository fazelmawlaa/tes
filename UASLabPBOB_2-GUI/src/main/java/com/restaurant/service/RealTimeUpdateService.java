package com.restaurant.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.restaurant.model.pesanan.Pesanan;

/**
 * Service untuk real-time update pesanan antar role
 * Menggunakan polling mechanism untuk detect perubahan
 */
public class RealTimeUpdateService {

    private static final RealTimeUpdateService instance = new RealTimeUpdateService();

    private Map<String, Set<Integer>> lastPesananIds = new ConcurrentHashMap<>();

    private RealTimeUpdateService() {
    }

    public static RealTimeUpdateService getInstance() {
        return instance;
    }

    /**
     * Check apakah pesanan relevan untuk role tertentu
     */
    private boolean isPesananRelevantForRole(Pesanan pesanan, String role) {
        String status = pesanan.getStatus();

        switch (role.toLowerCase()) {
            case "pelayan":
                // Pelayan tertarik pada: MENUNGGU, SIAP DISAJIKAN, LUNAS
                return status.equals("MENUNGGU") ||
                        status.equals("SIAP DISAJIKAN") ||
                        status.equals("LUNAS");
            case "koki":
                // Koki tertarik pada: DIPROSES, SEDANG DIMASAK, SIAP DISAJIKAN
                return status.equals("DIPROSES") ||
                        status.equals("SEDANG DIMASAK") ||
                        status.equals("SIAP DISAJIKAN");
            case "kasir":
                // Kasir tertarik pada: DISAJIKAN, LUNAS
                return status.equals("DISAJIKAN") ||
                        status.equals("LUNAS");
            case "customer":
                // Customer tertarik pada semua status pesanannya
                return true; // Customer akan filter berdasarkan meja di level aplikasi
            default:
                return false;
        }
    }

    /**
     * Mendapatkan pesanan dengan status tertentu yang baru ditambahkan atau berubah
     */
    public List<Pesanan> getPesananByStatusForRole(String role, String status) {
        List<Pesanan> result = new ArrayList<>();
        List<Pesanan> allPesanan = FileStorageService.loadPesanan();

        for (Pesanan p : allPesanan) {
            if (p.getStatus().equals(status) && isPesananRelevantForRole(p, role)) {
                result.add(p);
            }
        }

        return result;
    }

    /**
     * Reset tracking untuk role tertentu (untuk refresh lengkap)
     */
    public void resetTrackingForRole(String role) {
        String key = role + "_updates";
        lastPesananIds.remove(key);
    }

    /**
     * Mendapatkan notifikasi untuk role tertentu berdasarkan list pesanan
     */
    public List<String> getNotificationsForRole(String role, List<Pesanan> allPesanan) {
        List<String> notifications = new ArrayList<>();

        for (Pesanan p : allPesanan) {
            if (isPesananRelevantForRole(p, role)) {
                String notif = formatNotification(p, role);
                if (notif != null && !notif.isEmpty()) {
                    notifications.add(notif);
                }
            }
        }

        return notifications;
    }

    /**
     * Format notifikasi untuk pesanan update
     */
    public String formatNotification(Pesanan pesanan, String role) {
        String status = pesanan.getStatus();
        StringBuilder sb = new StringBuilder();

        switch (status) {
            case "MENUNGGU":
                if (role.equals("pelayan")) {
                    sb.append("🔔 Pesanan baru #").append(pesanan.getId())
                            .append(" dari Meja ").append(pesanan.getMeja().getNomor());
                }
                break;
            case "DIPROSES":
                if (role.equals("koki")) {
                    sb.append("🆕 Pesanan #").append(pesanan.getId())
                            .append(" masuk dapur, Meja ").append(pesanan.getMeja().getNomor());
                }
                break;
            case "SEDANG DIMASAK":
                if (role.equals("koki")) {
                    sb.append("👨‍🍳 Pesanan #").append(pesanan.getId())
                            .append(" sedang dimasak");
                }
                break;
            case "SIAP DISAJIKAN":
                if (role.equals("pelayan")) {
                    sb.append("🍽️ Pesanan #").append(pesanan.getId())
                            .append(" siap disajikan ke Meja ").append(pesanan.getMeja().getNomor());
                }
                if (role.equals("customer")) {
                    sb.append("✅ Pesanan #").append(pesanan.getId())
                            .append(" selesai dimasak, menunggu disajikan");
                }
                break;
            case "DISAJIKAN":
                if (role.equals("kasir")) {
                    sb.append("💰 Pesanan #").append(pesanan.getId())
                            .append(" sudah disajikan, Meja ").append(pesanan.getMeja().getNomor())
                            .append(" siap bayar (Rp").append((int) pesanan.getTotal()).append(")");
                }
                if (role.equals("customer")) {
                    sb.append("✅ Pesanan #").append(pesanan.getId())
                            .append(" sudah disajikan, selamat menikmati!");
                }
                break;
            case "LUNAS":
                if (role.equals("pelayan")) {
                    sb.append("✅ Pesanan #").append(pesanan.getId())
                            .append(" telah dibayar, Meja ").append(pesanan.getMeja().getNomor())
                            .append(" kosong");
                }
                if (role.equals("customer")) {
                    sb.append("✅ Pesanan #").append(pesanan.getId())
                            .append(" telah lunas. Terima kasih!");
                }
                break;
        }

        return sb.length() > 0 ? sb.toString() : null;
    }
}
