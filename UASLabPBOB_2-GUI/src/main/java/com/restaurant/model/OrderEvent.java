package com.restaurant.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model untuk tracking event perubahan status pesanan
 * Mencatat siapa, kapan, dan status perubahan apa
 */
public class OrderEvent {
    private int pesananId;
    private String statusLama;
    private String statusBaru;
    private String roleUser; // role user yang melakukan perubahan
    private String username; // username user yang melakukan perubahan
    private LocalDateTime timestamp;

    public OrderEvent(int pesananId, String statusLama, String statusBaru, String roleUser, String username) {
        this.pesananId = pesananId;
        this.statusLama = statusLama;
        this.statusBaru = statusBaru;
        this.roleUser = roleUser;
        this.username = username;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getPesananId() {
        return pesananId;
    }

    public String getStatusLama() {
        return statusLama;
    }

    public String getStatusBaru() {
        return statusBaru;
    }

    public String getRoleUser() {
        return roleUser;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Mendapatkan notifikasi message untuk role yang relevan
     */
    public String getNotificationMessage() {
        String waktu = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        StringBuilder sb = new StringBuilder();

        switch (statusBaru) {
            case "SEDANG DIMASAK":
                sb.append("🔔 [").append(waktu).append("] Pesanan #").append(pesananId)
                        .append(" sedang dimasak (oleh: ").append(username).append(")");
                break;
            case "SELESAI DIMASAK":
                sb.append("🔔 [").append(waktu).append("] Pesanan #").append(pesananId)
                        .append(" selesai dimasak, siap untuk pembayaran");
                break;
            case "SELESAI":
                sb.append("✅ [").append(waktu).append("] Pesanan #").append(pesananId)
                        .append(" telah selesai dan dibayar");
                break;
            default:
                sb.append("ℹ️ [").append(waktu).append("] Pesanan #").append(pesananId)
                        .append(" status berubah: ").append(statusLama).append(" -> ").append(statusBaru);
        }

        return sb.toString();
    }

    /**
     * Mendapatkan notifikasi untuk role tertentu
     * Alur dunia nyata: MENUNGGU -> DIPROSES -> SEDANG DIMASAK -> SIAP DISAJIKAN ->
     * DISAJIKAN -> LUNAS
     */
    public String getNotificationForRole(String targetRole) {
        String waktu = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        switch (targetRole.toLowerCase()) {
            case "koki":
                // Koki mendapat notifikasi ketika pelayan mengirim pesanan ke dapur (DIPROSES)
                if (statusBaru.equals("DIPROSES")) {
                    return "🆕 [新订单] Pesanan #" + pesananId + " masuk ke dapur, siap dimasak [" + waktu + "]";
                }
                break;
            case "kasir":
                // Kasir mendapat notifikasi ketika pesanan sudah disajikan (bisa dibayar)
                if (statusBaru.equals("DISAJIKAN")) {
                    return "💰 [Siap Bayar] Pesanan #" + pesananId + " sudah disajikan, siap untuk pembayaran [" + waktu
                            + "]";
                }
                break;
            case "pelayan":
                // Pelayan mendapat notifikasi untuk pesanan baru, makanan siap disajikan, dan
                // pembayaran selesai
                if (statusBaru.equals("MENUNGGU") && statusLama == null) {
                    return "🔔 [Pesanan Baru] Pesanan #" + pesananId + " menunggu diterima [" + waktu + "]";
                } else if (statusBaru.equals("SIAP DISAJIKAN")) {
                    return "🍽️ [Siap Disajikan] Pesanan #" + pesananId + " selesai dimasak, siap diantar ke meja ["
                            + waktu + "]";
                } else if (statusBaru.equals("LUNAS")) {
                    return "✅ Pesanan #" + pesananId + " telah dibayar, meja kosong [" + waktu + "]";
                }
                break;
            case "customer":
                // Customer mendapat update status dari awal sampai akhir
                if (statusBaru.equals("DIPROSES")) {
                    return "✅ Pesanan #" + pesananId + " diterima dan sedang dipersiapkan [" + waktu + "]";
                } else if (statusBaru.equals("SEDANG DIMASAK")) {
                    return "👨‍🍳 Pesanan #" + pesananId + " sedang dimasak [" + waktu + "]";
                } else if (statusBaru.equals("SIAP DISAJIKAN")) {
                    return "🍽️ Pesanan #" + pesananId + " selesai dimasak, menunggu disajikan [" + waktu + "]";
                } else if (statusBaru.equals("DISAJIKAN")) {
                    return "✅ Pesanan #" + pesananId + " sudah disajikan, selamat menikmati! [" + waktu + "]";
                } else if (statusBaru.equals("LUNAS")) {
                    return "✅ Pesanan #" + pesananId + " telah lunas dibayar. Terima kasih! [" + waktu + "]";
                }
                break;
        }

        return null; // Tidak ada notifikasi untuk role ini
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] Pesanan #%d: %s -> %s (oleh: %s)",
                timestamp.format(formatter), pesananId, statusLama, statusBaru, username);
    }
}
