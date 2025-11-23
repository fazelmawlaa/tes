package com.restaurant.app;

import java.util.Scanner;

import com.restaurant.utils.InputUtil;
import com.restaurant.service.AuthService;
import com.restaurant.model.akun.Akun;

public class Main {

    private static AuthService authService = new AuthService();
    private static Scanner sc = InputUtil.sc;

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===== SISTEM RESTORAN =====");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Keluar");
            System.out.print("Pilih: ");

            if (!sc.hasNextInt()) {
                System.out.println("Input tidak valid!");
                sc.nextLine();
                continue;
            }

            int pilih = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (pilih) {
                case 1:
                    login();
                    break;

                case 2:
                    register();
                    break;

                case 0:
                    System.out.println("Terima kasih! Program selesai.");
                    return;

                default:
                    System.out.println("Pilihan tidak valid!");
            }
        }
    }

    // ----------------------------- LOGIN -----------------------------
    private static void login() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        Akun akun = authService.login(username, password);

        if (akun == null) {
            System.out.println("Login gagal! Username atau password salah.");
            return;
        }

        System.out.println("Login berhasil! Selamat datang, " + akun.getNama() + ".");

        // Routing berdasarkan role
        String role = akun.getRole().toLowerCase();

        switch (role) {
            case "customer":
                MainCustomer.run();
                break;
            case "pelayan":
                MainPelayan.run();
                break;
            case "koki":
                MainKoki.run();
                break;
            case "kasir":
                MainKasir.run();
                break;
            default:
                System.out.println("Role tidak dikenali: " + role);
        }
    }

    // ----------------------------- REGISTER -----------------------------
    private static void register() {
        System.out.println("\n===== REGISTER =====");
        System.out.print("Nama: ");
        String nama = sc.nextLine();

        System.out.print("Username: ");
        String username = sc.nextLine();

        System.out.print("Password: ");
        String password = sc.nextLine();

        System.out.print("Email: ");
        String email = sc.nextLine();

        System.out.println("Role:");
        System.out.println("1. Customer");
        System.out.println("2. Pelayan");
        System.out.println("3. Koki");
        System.out.println("4. Kasir");
        System.out.print("Pilih role: ");

        if (!sc.hasNextInt()) {
            System.out.println("Input tidak valid!");
            sc.nextLine();
            return;
        }

        int roleChoice = sc.nextInt();
        sc.nextLine(); // clear buffer

        String role = "";
        boolean isPegawai = false;

        switch (roleChoice) {
            case 1:
                role = "customer";
                break;
            case 2:
                role = "pelayan";
                isPegawai = true;
                break;
            case 3:
                role = "koki";
                isPegawai = true;
                break;
            case 4:
                role = "kasir";
                isPegawai = true;
                break;
            default:
                System.out.println("Pilihan role tidak valid!");
                return;
        }

        boolean success = false;

        if (isPegawai) {
            // Validasi email domain untuk pegawai
            if (!authService.isValidEmailDomain(email, "usk.ac.id")) {
                System.out.println(
                        "Anda tidak memiliki akses untuk mendaftar sebagai karyawan. Hanya email dengan domain usk.ac.id yang dapat mendaftar sebagai karyawan.");
                return;
            }
            success = authService.registerPegawai(nama, username, password, email, role);
        } else {
            success = authService.registerCustomer(nama, username, password, email);
        }

        if (success) {
            System.out.println("Registrasi berhasil! Silakan login.");
        } else {
            System.out.println("Registrasi gagal! Username sudah digunakan.");
        }
    }
}
