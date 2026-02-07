package com.example.budgetmanager.dto;

/**
 * VIEW DTO - For displaying transactions with category names
 * 
 * This DTO combines data from transactions and categories tables (JOIN query).
 * Used by the adapter to display transaction list with category information.
 */
public class TransactionWithCategory {
    private int id;
    private double amount;
    private String type;
    private String note;
    private String date;
    private int categoryId;
    private String categoryName;  // From JOIN with categories table
    private String categoryIcon;  // From JOIN with categories table
    
    // Constructor for JOIN query results
    public TransactionWithCategory(int id, double amount, String type, String note,
                                   String date, int categoryId, String categoryName, String categoryIcon) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.note = note;
        this.date = date;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
}