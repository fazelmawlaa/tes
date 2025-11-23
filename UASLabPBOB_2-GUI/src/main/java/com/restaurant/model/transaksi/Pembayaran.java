package com.restaurant.model.transaksi;

public interface Pembayaran {

    String getJenis();

    // proses pembayaran
    boolean prosesPembayaran(double total);
}
