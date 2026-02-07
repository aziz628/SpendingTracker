package com.example.budgetmanager.utils;
import com.example.budgetmanager.R;

/**
 * Utility class for transaction color constants and helpers.
 * This class cannot be instantiated.
 */
public final class TransactionColors {
    public static final int INCOME_COLOR = R.color.income_text;
    public static final int EXPENSE_COLOR = R.color.expense_text;
    public static final int INCOME_BG = R.color.income_bg;
    public static final int EXPENSE_BG = R.color.expense_bg;
    
    // Private constructor prevents instantiation
    private TransactionColors() {
    }
    
    // Return the Resource ID for the icon color based on the transaction type
    public static int getIconColor(String type) {
        return type.equalsIgnoreCase("income") ? INCOME_COLOR : EXPENSE_COLOR;
    }
    
    // Return the Resource ID for the background color based on the transaction type
    public static int getBgColor(String type) {
        return type.equalsIgnoreCase("income") ? INCOME_BG : EXPENSE_BG;
    }
}
