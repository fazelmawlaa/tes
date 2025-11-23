package com.restaurant.model.akun;

public abstract class Akun {
    private String id;
    private String nama;
    private String username;
    private String password;
    private String email;

    public Akun(String id, String nama, String username, String password, String email) {
        this.id = id;
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getRole();

    // Format untuk menyimpan ke file
    public String toFileFormat() {
        return id + ";" + nama + ";" + username + ";" + password + ";" + email + ";" + getRole();
    }
}
