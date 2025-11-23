package com.restaurant.model.transaksi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.restaurant.model.pesanan.Pesanan;

public class Transaksi {

    private Pesanan pesanan;
    private Pembayaran pembayaran;
    private double total;
    private LocalDateTime waktu;

    public Transaksi(Pesanan pesanan, Pembayaran pembayaran) {
        this.pesanan = pesanan;
        this.pembayaran = pembayaran;
        this.total = pesanan.getTotal();
        this.waktu = LocalDateTime.now();
    }

    // ==========================================
    // GETTER
    // ==========================================
    public Pesanan getPesanan() {
        return pesanan;
    }

    public Pembayaran getPembayaran() {
        return pembayaran;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public String getWaktuFormatted() {
        return waktu.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ==========================================
    // KONFIRMASI (dipanggil oleh customer/kasir)
    // ==========================================
    public boolean konfirmasi() {
        return pembayaran.prosesPembayaran(total);
    }

    // ==========================================
    // UNTUK STRUK (ringkas)
    // ==========================================
    @Override
    public String toString() {
        return "Transaksi Pesanan #" + pesanan.getId() +
                " | Total: Rp" + total +
                " | Metode: " + pembayaran.getJenis() +
                " | Waktu: " + getWaktuFormatted();
    }
}
