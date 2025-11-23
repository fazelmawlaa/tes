package com.restaurant.model;

import com.restaurant.model.pesanan.DetailPesanan;
import com.restaurant.model.pesanan.Pesanan;
import com.restaurant.model.transaksi.Transaksi;

public class Struk {

    public static void cetak(Transaksi transaksi) {
        Pesanan p = transaksi.getPesanan();

        System.out.println("\n==============================");
        System.out.println("          STRUK PESANAN");
        System.out.println("==============================");

        System.out.println("ID Pesanan : " + p.getId());
        System.out.println("Nomor Meja : " + p.getMeja().getNomor());
        System.out.println("Waktu      : " + transaksi.getWaktuFormatted());
        System.out.println("------------------------------");
        System.out.println("Daftar Pesanan:");

        for (DetailPesanan d : p.getItems()) {
            System.out.println(
                    "- " + d.getMenu().getNama() +
                            " x" + d.getJumlah() +
                            " = Rp" + d.getSubtotal() +
                            (d.getCatatan() != null && !d.getCatatan().isEmpty()
                                    ? " (catatan: " + d.getCatatan() + ")"
                                    : ""));
        }

        System.out.println("------------------------------");
        System.out.println("TOTAL     : Rp" + transaksi.getTotal());
        System.out.println("Pembayaran: " + transaksi.getPembayaran().getJenis());
        System.out.println("==============================");
        System.out.println("        TERIMA KASIH");
        System.out.println("==============================\n");
    }
}