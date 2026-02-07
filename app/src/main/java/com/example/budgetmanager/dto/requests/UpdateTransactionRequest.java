package com.example.budgetmanager.dto.requests;
/**
 * CREATE TRANSACTION REQUEST DTO - DEVELOPER GUIDE
 * PURPOSE: Encapsulate data for transaction creation requests
 *
 * IMMUTABLE: Fields are final, only getters provided
 */
public class UpdateTransactionRequest {
    // to be updated fields
    private final double amount;
    private final String note;
    private final String date;
    private final String type;


    // reference to the transaction to be updated
    private final int id; 

    // constructor
    public UpdateTransactionRequest(double amount, String note, 
                                   String date, int id, String type) {
        this.amount = amount;
        this.note = note;
        this.date = date;
        this.id = id;
        this.type = type;
    }

    // getters
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public int getId() { return id; }
    public String getType() { return type; }
}
