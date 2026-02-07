package com.example.budgetmanager.models;


public class User {
    private  double balance;
    private int id;
    private String name;
    private String email;
    private String password;

    /**
     * Constructor for NEW users (before database insertion)
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor for EXISTING users (loaded from database)
     * Database provides the auto-generated ID
     */
    public User(int id, String name, String email, String password, double balance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
