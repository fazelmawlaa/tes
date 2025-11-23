package com.restaurant.model.akun;

public class Customer extends Akun {

    public Customer(String id, String nama, String username, String password, String email) {
        super(id, nama, username, password, email);
    }

    @Override
    public String getRole() {
        return "customer";
    }
}
