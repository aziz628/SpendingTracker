package com.example.budgetmanager.utils;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;
import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper.CategoryName;

/**
 * Provides localized names for default category icon names
 */
public class CategoryLocalizer {
    private static final Map<String, Integer> CATEGORY_NAME_MAP = new HashMap<>();
    
    static {
        // Map icon names to string resource IDs
        CATEGORY_NAME_MAP.put(CategoryName.FOOD, R.string.default_category_food);
        CATEGORY_NAME_MAP.put(CategoryName.TRANSPORT, R.string.default_category_transport);
        CATEGORY_NAME_MAP.put(CategoryName.SHOPPING, R.string.default_category_shopping);
        CATEGORY_NAME_MAP.put(CategoryName.SALARY, R.string.default_category_salary);
        CATEGORY_NAME_MAP.put(CategoryName.FREELANCE, R.string.default_category_freelance);
        CATEGORY_NAME_MAP.put(CategoryName.OTHER, R.string.default_category_other);
    }
    
    /**
     * Get localized category name using the icon Name
     * Returns the original name if not a default category.
     */
    public static String getLocalizedName(Context context, String Name) {
        Integer stringRes = CATEGORY_NAME_MAP.get(Name.toLowerCase());
        if (stringRes != null) {
            return context.getString(stringRes);
        }
        // Not a default category - return original user-defined name
        return Name;
    }
}