package com.restaurant.model.pesanan;

import com.restaurant.model.menu.MenuItem;

public class DetailPesanan {

    private MenuItem menu;
    private int jumlah;
    private String catatan;

    public DetailPesanan(MenuItem menu, int jumlah, String catatan) {
        this.menu = menu;
        this.jumlah = jumlah;
        this.catatan = catatan;
    }

    public MenuItem getMenu() {
        return menu;
    }

    public void setMenu(MenuItem menu) {
        this.menu = menu;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    // ===============================
    // SUBTOTAL ITEM
    // ===============================
    public double getSubtotal() {
        return menu.getHarga() * jumlah;
    }

    @Override
    public String toString() {
        return menu.getNama() +
                " x" + jumlah +
                " (Rp" + getSubtotal() + ")" +
                (catatan != null && !catatan.isEmpty() ? " | Catatan: " + catatan : "");
    }
}
