package com.example.budgetmanager.dto.requests;

public class UpdateUserInfoRequest {
    //id name email
    private int id;
    private String name;
    private String email;

    public UpdateUserInfoRequest(int id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }
    // getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
