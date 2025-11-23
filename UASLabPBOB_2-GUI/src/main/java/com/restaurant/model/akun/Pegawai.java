package com.restaurant.model.akun;

public class Pegawai extends Akun {

    private String tipePegawai; // kasir / pelayan / koki

    public Pegawai(String id, String nama, String username, String password, String email, String tipePegawai) {
        super(id, nama, username, password, email);
        this.tipePegawai = tipePegawai;
    }

    @Override
    public String getRole() {
        return tipePegawai;
    }
}


