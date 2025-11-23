package com.restaurant.model.menu;

public class Makanan extends MenuItem {
    private String kategori;
    private String tingkatPedas;

    public Makanan(String nama, double harga, String kategori, String tingkatPedas) {
        super(nama, harga);
        this.kategori = kategori;
        this.tingkatPedas = tingkatPedas;
    }

    public String getKategori() {
        return kategori;
    }

    public String getTingkatPedas() {
        return tingkatPedas;
    }

    @Override
    public String getInfo() {
        return nama + " (" + kategori + ", pedas: " + tingkatPedas + ") - Rp" + harga;
    }
}
