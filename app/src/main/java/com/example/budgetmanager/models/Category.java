package com.example.budgetmanager.models;

public class Category {
    private int id;
    private String name;
    private String iconName;
    private String type; // "income" or "expense"
    private int userId;  // Which user owns this category

    public Category(String name, String iconName, String type, int userId) {
        this.name = name;
        this.iconName = iconName;
        this.type = type;
        this.userId = userId;
    }
    public Category(int id, String name, String iconName, String type, int userId) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.type = type;
        this.userId = userId;
    }
    public Category(String name, String type, int userId) {
        this.name = name;
        this.type = type;
        this.userId = userId;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getUserId() {  return userId; }
}