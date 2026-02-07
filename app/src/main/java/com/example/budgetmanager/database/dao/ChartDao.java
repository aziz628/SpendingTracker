package com.example.budgetmanager.database.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.dto.CategoryTotal;
import com.example.budgetmanager.dto.MonthlyTotal;

import java.util.ArrayList;
import java.util.List;

public class ChartDao {
    private DatabaseHelper dbHelper;

    public ChartDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Get category spending/income totals for pie chart
     */
    public List<CategoryTotal> getCategoryTotals(int userId, String type) {
        List<CategoryTotal> categoryTotals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c." + DatabaseHelper.CategoryTable.NAME_COL + ", " +
                "c." + DatabaseHelper.CategoryTable.ICON_NAME + ", " +
                "SUM(t." + DatabaseHelper.TransactionTable.AMOUNT + ") as total " +
                "FROM " + DatabaseHelper.TransactionTable.TABLE_NAME + " t " +
                "INNER JOIN " + DatabaseHelper.CategoryTable.TABLE_NAME + " c " +
                "ON t." + DatabaseHelper.TransactionTable.CATEGORY_ID + " = c." + DatabaseHelper.CategoryTable.ID + " " +
                "WHERE t." + DatabaseHelper.TransactionTable.USER_ID + " = ? " +
                "AND c." + DatabaseHelper.CategoryTable.TYPE + " = ? " +
                "GROUP BY c." + DatabaseHelper.CategoryTable.ID + " " +
                "ORDER BY total DESC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), type});

            while (cursor.moveToNext()) {
                String categoryName = cursor.getString(0);
                String iconName = cursor.getString(1);
                double total = cursor.getDouble(2);

                categoryTotals.add(new CategoryTotal(categoryName, iconName, total));
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return categoryTotals;
    }

    /**
     * Get daily transaction totals for current month for line chart
     */
    public List<MonthlyTotal> getDailyTotalsCurrentMonth(int userId) {
        List<MonthlyTotal> dailyTotals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // First, let's get all transactions and filter by date in Java to debug the issue
        String query = "SELECT date, amount, c.type " +
                "FROM " + DatabaseHelper.TransactionTable.TABLE_NAME + " t " +
                "INNER JOIN " + DatabaseHelper.CategoryTable.TABLE_NAME + " c " +
                "ON t." + DatabaseHelper.TransactionTable.CATEGORY_ID + " = c." + DatabaseHelper.CategoryTable.ID + " " +
                "WHERE t." + DatabaseHelper.TransactionTable.USER_ID + " = ? " +
                "ORDER BY date ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
              // Get current year-month for filtering - force US locale to avoid Arabic numerals
            java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US);
            String currentMonth = monthFormat.format(new java.util.Date());
            
            // Group transactions by date
            java.util.Map<String, Double> incomeByDate = new java.util.HashMap<>();
            java.util.Map<String, Double> expenseByDate = new java.util.HashMap<>();
            
            while (cursor.moveToNext()) {
                String date = cursor.getString(0);
                double amount = cursor.getDouble(1);
                String type = cursor.getString(2);
                
                // Handle both date formats: YYYY-MM-DD and DD-MM-YYYY
                String normalizedDate = null;
                try {
                    if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        // Already in YYYY-MM-DD format
                        normalizedDate = date;
                    } else if (date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                        // Convert DD-MM-YYYY to YYYY-MM-DD
                        String[] parts = date.split("-");
                        normalizedDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                    }
                    
                    // Only include transactions from current month
                    if (normalizedDate != null && normalizedDate.startsWith(currentMonth)) {
                        if ("income".equals(type)) {
                            incomeByDate.put(normalizedDate, incomeByDate.getOrDefault(normalizedDate, 0.0) + amount);
                        } else {
                            expenseByDate.put(normalizedDate, expenseByDate.getOrDefault(normalizedDate, 0.0) + amount);
                        }
                    }
                } catch (Exception e) {
                    // Skip malformed dates
                    continue;
                }
            }
            
            // Get all unique dates and create MonthlyTotal objects
            java.util.Set<String> allDates = new java.util.HashSet<>();
            allDates.addAll(incomeByDate.keySet());
            allDates.addAll(expenseByDate.keySet());
            
            java.util.List<String> sortedDates = new java.util.ArrayList<>(allDates);
            java.util.Collections.sort(sortedDates);
            
            for (String date : sortedDates) {
                double income = incomeByDate.getOrDefault(date, 0.0);
                double expense = expenseByDate.getOrDefault(date, 0.0);
                dailyTotals.add(new MonthlyTotal(date, income, expense));
            }
            
        } finally {
            if (cursor != null) cursor.close();
        }

        return dailyTotals;
    }
}