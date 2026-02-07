package com.example.budgetmanager.utils;

import java.util.HashMap;
import java.util.Map;
import com.example.budgetmanager.R;
import  com.example.budgetmanager.database.DatabaseHelper.CategoryName;

/**
 * Utility class for mapping category icons names to their corresponding icon resources.
 * This class cannot be instantiated.
 */
public final class CategoryIconMapper {

    private static final Map<String, Integer> ICON_MAP = new HashMap<>();

    static {
        // Expenses
        ICON_MAP.put(CategoryName.FOOD, R.drawable.ic_category_food);
        ICON_MAP.put(CategoryName.TRANSPORT, R.drawable.ic_category_transport);
        ICON_MAP.put(CategoryName.SHOPPING, R.drawable.ic_category_shopping);

        // Income
        ICON_MAP.put(CategoryName.SALARY, R.drawable.ic_category_salary);
        ICON_MAP.put(CategoryName.FREELANCE, R.drawable.ic_category_freelance);

        // Default
        ICON_MAP.put(CategoryName.OTHER, R.drawable.ic_category_other);
    }
    
    // Private constructor prevents instantiation
    private CategoryIconMapper() {
    }

    public static int getIconResource(String iconName) {
        return ICON_MAP.getOrDefault(iconName, R.drawable.ic_category_other);
    }
}
