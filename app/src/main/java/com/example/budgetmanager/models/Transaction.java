
package com.example.budgetmanager.models;

/**
 * TRANSACTION MODEL - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This class represents a single financial transaction stored in the `transactions` table.
 * because those are derived via JOINs in the DAO layer (see TransactionDao).
 *  Keeping the model pure ensures a clear separation between persistence (DB) and presentation (DTO).
 *
 * KEY DESIGN DECISIONS:
 * - Immutable constructor for new transactions (ID is assigned by SQLite after insertion).
 * - Separate getters/setters allow the DAO to populate the ID after insert.
 
 */
public class Transaction {
    private int id;
    private double amount;
    private String type; // "income" or "expense"
    private String note;
    private String date;
    private int categoryId;
    private int userId;
    
    // Constructor for creating new transactions (without ID)
    public Transaction(double amount, String type, String note, String date, int categoryId, int userId) {
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.date = date;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    // Constructor for retrieving existing transactions (with ID)
    public Transaction(int id, double amount, String type, String note, String date, int categoryId, int userId) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.date = date;
        this.categoryId = categoryId;
        this.userId = userId;
    }
    // getter setter 
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; } 
}
