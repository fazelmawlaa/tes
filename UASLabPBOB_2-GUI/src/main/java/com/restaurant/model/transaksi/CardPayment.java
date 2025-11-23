package com.restaurant.model.transaksi;

public class CardPayment implements Pembayaran {

    @Override
    public String getJenis() {
        return "Kartu Debit/Kredit";
    }

    @Override
    public boolean prosesPembayaran(double total) {
        System.out.println("Pembayaran kartu berhasil diproses.");
        return true;
    }
}
