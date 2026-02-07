package com.example.budgetmanager.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.TransactionTable;
import com.example.budgetmanager.database.DatabaseHelper.CategoryTable;
import com.example.budgetmanager.database.DatabaseHelper.CategoryName;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.utils.CategoryLocalizer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CATEGORY DAO - Data Access Object for Category entity
 * 
 * Handles all database operations for categories.
 */
public class CategoryDao {
    private final DatabaseHelper dbHelper;
    
    public CategoryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    

    /**
     * Get all categories for a user
     */
    public List<Category> getCategories(int userId) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
                CategoryTable.TABLE_NAME,
                new String[]{
                        CategoryTable.ID,
                        CategoryTable.NAME_COL,
                        CategoryTable.ICON_NAME,
                        CategoryTable.TYPE,
                        CategoryTable.USER_ID
                },
                CategoryTable.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                CategoryTable.NAME_COL + " ASC"
        );
        
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow(CategoryTable.ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.NAME_COL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.ICON_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.TYPE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(CategoryTable.USER_ID))
            );
            categories.add(category);
        }

        cursor.close();
          
        return categories;
    }
      /**
     * Get category by ID
     */
    public Category getCategoryById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(
                CategoryTable.TABLE_NAME,
                new String[]{
                        CategoryTable.ID,
                        CategoryTable.NAME_COL,
                        CategoryTable.ICON_NAME,
                        CategoryTable.TYPE,
                        CategoryTable.USER_ID
                },
                CategoryTable.ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        
        Category category = null;
        if (cursor.moveToFirst()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndexOrThrow(CategoryTable.ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.NAME_COL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.ICON_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.TYPE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(CategoryTable.USER_ID))
            );
        }

        cursor.close();

        return category;
    }


    /* get sum of transactions per category
     */
    public double getTotalTransactionsPerCategory(int categoryId) {
        double total = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // calc sum of transactions per category
        Cursor cursor = db.query(
                TransactionTable.TABLE_NAME,
                new String[]{"SUM(" + TransactionTable.AMOUNT + ") AS " +TransactionTable.AMOUNT},
                TransactionTable.CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );
        // move the cursor to first row
        if (cursor.moveToFirst()) {
            // get the column index using col name , get total value using the index
            total = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(TransactionTable.AMOUNT)
            );
        }
        cursor.close();

        cursor.close();
          
        return total;
    }

    /**
     * Create a new category
     */
    public long createCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CategoryTable.NAME_COL, category.getName());
        values.put(CategoryTable.ICON_NAME, category.getIconName());
        values.put(CategoryTable.TYPE, category.getType());
        values.put(CategoryTable.USER_ID, category.getUserId());

        // set created at to current time using SimpleDateFormat
        values.put(CategoryTable.CREATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(new Date()));

        long id = db.insert(CategoryTable.TABLE_NAME, null, values);
          
        return id;
    }



    /**
     * Seed default categories for a new user
     *
     * Creates 6 essential categories (Food, Transport, Shopping, Other, Salary, Freelance)
     * so the user can immediately start creating transactions.
     */
    public void seedDefaultCategories(int userId, Context context) {

        // use the DEFAULT_CATEGORIES map to get names and types
        String[] names = DatabaseHelper.DEFAULT_CATEGORIES.keySet().toArray(new String[0]);
        String[] types = DatabaseHelper.DEFAULT_CATEGORIES.values().toArray(new String[0]);

        // Create each category for the user using the context to get localized names
        
        for (int i = 0; i < names.length; i++) {
            String iconName = names[i];
            // Skip "other" category - let users create it manually if needed
            if (CategoryName.OTHER.equals(iconName)) {
                continue;
            }

            String name = CategoryLocalizer.getLocalizedName(context, names[i]);
            String type = types[i];
            Category category = new Category(name, iconName, type, userId);
            createCategory(category);
        }
    }
    
    /**
     * update category by ID
     */
    public int updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // content value for the name 
        ContentValues values = new ContentValues();
        values.put(CategoryTable.NAME_COL, category.getName());
        values.put(CategoryTable.ICON_NAME, category.getIconName());
        

        int rowsAffected = db.update(
                CategoryTable.TABLE_NAME,
                values, 
                CategoryTable.ID + "=?",
                new String[]{String.valueOf(category.getId())}
        );
        
          
        return rowsAffected;
    }


    /**
     * Delete category by ID
     */
    public int deleteCategory(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int rowsAffected = db.delete(
                CategoryTable.TABLE_NAME,
                CategoryTable.ID + "=?",
                new String[]{String.valueOf(id)}
        );
        
          
        return rowsAffected;
    }
}