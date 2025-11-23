package com.restaurant.model.menu;

public abstract class MenuItem {
    protected String nama;
    protected double harga;

    public MenuItem(String nama, double harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public String getNama() {
        return nama;
    }

    public double getHarga() {
        return harga;
    }

    public abstract String getInfo();

    @Override
    public String toString() {
        return getInfo();
    }
}
