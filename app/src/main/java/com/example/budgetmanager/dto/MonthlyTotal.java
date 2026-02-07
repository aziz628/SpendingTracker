package com.example.budgetmanager.dto;


public class MonthlyTotal {
    private String date;
    private double income;
    private double expense;

    public MonthlyTotal(String date, double income, double expense) {
        this.date = date;
        this.income = income;
        this.expense = expense;
    }

    // Getters
    public String getDate() { return date; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
}
