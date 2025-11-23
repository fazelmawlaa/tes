package com.restaurant.model.pesanan;

import java.util.ArrayList;
import java.util.List;

public class Pesanan {

    private int id;
    private Meja meja;
    // Status alur dunia nyata: MENUNGGU -> DIPROSES -> SEDANG DIMASAK -> SIAP DISAJIKAN -> DISAJIKAN -> LUNAS
    private String status; // MENUNGGU, DIPROSES, SEDANG DIMASAK, SIAP DISAJIKAN, DISAJIKAN, LUNAS
    private List<DetailPesanan> items;

    public Pesanan(int id, Meja meja) {
        this.id = id;
        this.meja = meja;
        this.status = "MENUNGGU";
        this.items = new ArrayList<>();
    }

    // ======================================
    // GETTER & SETTER
    // ======================================
    public int getId() {
        return id;
    }

    public Meja getMeja() {
        return meja;
    }

    public void setMeja(Meja meja) {
        this.meja = meja;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DetailPesanan> getItems() {
        return items;
    }

    // ======================================
    // TAMBAH ITEM
    // ======================================
    public void tambahItem(DetailPesanan dp) {
        items.add(dp);
    }

    // ======================================
    // HITUNG TOTAL
    // ======================================
    public double getTotal() {
        double total = 0;
        for (DetailPesanan d : items) {
            total += d.getSubtotal();
        }
        return total;
    }

    // ======================================
    // RENDER DETAIL PESANAN (untuk layar & struk)
    // ======================================
    public String renderDetail() {
        StringBuilder sb = new StringBuilder();
        for (DetailPesanan d : items) {
            sb.append("- ").append(d.toString()).append("\n");
        }
        return sb.toString();
    }

    // ======================================
    // TO STRING (UNTUK LIST PELAYAN/KOKI/KASIR)
    // ======================================
    @Override
    public String toString() {
        return "Pesanan #" + id +
                " | Meja " + meja.getNomor() +
                " | Status: " + status +
                " | Total: Rp" + getTotal();
    }
}
