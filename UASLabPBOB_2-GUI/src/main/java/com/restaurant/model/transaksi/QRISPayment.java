package com.restaurant.model.transaksi;

public class QRISPayment implements Pembayaran {

    @Override
    public String getJenis() {
        return "QRIS";
    }

    @Override
    public boolean prosesPembayaran(double total) {
        System.out.println("QRIS berhasil diproses.");
        return true;
    }
}
