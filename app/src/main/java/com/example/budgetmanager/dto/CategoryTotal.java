package com.example.budgetmanager.dto;

public class CategoryTotal {
    private String categoryName;
    private String iconName;
    private double total;
    private double percentage;

    public CategoryTotal(String categoryName, String iconName, double total) {
        this.categoryName = categoryName;
        this.iconName = iconName;
        this.total = total;
    }

    // Getters and setters
    public String getCategoryName() { return categoryName; }
    public String getIconName() { return iconName; }
    public double getTotal() { return total; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
