package com.example.budgetmanager.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.TransactionTable;
import com.example.budgetmanager.database.DatabaseHelper.CategoryTable;
import com.example.budgetmanager.dto.TransactionWithCategory;
import com.example.budgetmanager.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TRANSACTION DAO - Data Access Object for Transaction entity
 * 
 * Handles all database operations for transactions, including JOIN queries.
 */
public class TransactionDao {
    private final DatabaseHelper dbHelper;
    
    public TransactionDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Transaction getTransactionById(int id) {
        // get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // query
        String query = "SELECT * FROM " + TransactionTable.TABLE_NAME + " WHERE "
                + TransactionTable.ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        // check if there is a result
        if (cursor.moveToFirst()) {
            // order is amount type note date category_id user_id
            Transaction transaction = new Transaction(
                    cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionTable.AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.NOTE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.CATEGORY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.USER_ID))
                    );
            cursor.close();
              
            return transaction;
        } else {
            cursor.close();

            return null;
        }
    }



    /**
     * Get recent transactions with category names (JOIN query)
     * This is the key method for displaying transactions in the UI
     */
    public List<TransactionWithCategory> getTransactionsWithCategory(int userId, int limit) {
        // initialize list to store transactions
        List<TransactionWithCategory> transactions = new ArrayList<>();

        // get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // JOIN query to get transaction data + category name 
        String query = "SELECT " +
                "t." + TransactionTable.ID + ", " +
                "t." + TransactionTable.AMOUNT + ", " +
                "t." + TransactionTable.TYPE + ", " +
                "t." + TransactionTable.NOTE + ", " +
                "t." + TransactionTable.DATE + ", " +
                "t." + TransactionTable.CATEGORY_ID + ", " +
                "c." + CategoryTable.NAME_COL + " as category_name, " +
                "c." + CategoryTable.ICON_NAME + " as category_icon " +
                "FROM " + TransactionTable.TABLE_NAME + " t " +
                "JOIN " + CategoryTable.TABLE_NAME + " c " +
                "ON t." + TransactionTable.CATEGORY_ID + " = c." + CategoryTable.ID + " " +
                "WHERE t." + TransactionTable.USER_ID + " = ? " +
                "ORDER BY t." + TransactionTable.DATE + " DESC ";

        String[] values = new String[]{String.valueOf(userId)};

        // add limit if needed
        if(limit != -1 ){
            query += "LIMIT ?";
            values=new String[]{String.valueOf(userId),String.valueOf(limit)};
        }


        Cursor cursor = db.rawQuery(query, values);

        while (cursor.moveToNext()) {
            TransactionWithCategory transaction = new TransactionWithCategory(
                    cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.ID)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionTable.AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.NOTE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.CATEGORY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow("category_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category_icon"))
            );
            transactions.add(transaction);
        }

        cursor.close();
          
        return transactions;
    }


    /**
     * Get total income for a user
     */
    public double getTotalIncome(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT SUM(" + TransactionTable.AMOUNT + ") " +
                "FROM " + TransactionTable.TABLE_NAME + " " +
                "WHERE " + TransactionTable.USER_ID + " = ? " +
                "AND " + TransactionTable.TYPE + " = 'income'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        
        cursor.close();
          
        return total;
    }
    
    /**
     * Get total expenses for a user
     */
    public double getTotalExpenses(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String query = "SELECT SUM(" + TransactionTable.AMOUNT + ") " +
                "FROM " + TransactionTable.TABLE_NAME + " " +
                "WHERE " + TransactionTable.USER_ID + " = ? " +
                "AND " + TransactionTable.TYPE + " = 'expense'";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        
        cursor.close();
          
        return total;
    }
    /**
     * Create a new transaction
     */
    public long createTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // use content values for formatting data to be inserted into the database
        ContentValues values = new ContentValues();
        values.put(TransactionTable.AMOUNT, transaction.getAmount());
        values.put(TransactionTable.TYPE, transaction.getType());
        values.put(TransactionTable.NOTE, transaction.getNote());
        values.put(TransactionTable.DATE, transaction.getDate());
        values.put(TransactionTable.CATEGORY_ID, transaction.getCategoryId());
        values.put(TransactionTable.USER_ID, transaction.getUserId());        values.put(TransactionTable.CREATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date()));

        long id = db.insert(TransactionTable.TABLE_NAME, null, values);
          
        return id;
    }


    /**
     * Update an existing transaction
     * the updated fields are amount, note, and date
     */
    public long updateTransaction(Transaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // get writable database

        // use content values for formatting data
        ContentValues values = new ContentValues();
        values.put(TransactionTable.AMOUNT, transaction.getAmount());
        values.put(TransactionTable.NOTE, transaction.getNote());
        values.put(TransactionTable.DATE, transaction.getDate());
        values.put(TransactionTable.USER_ID, transaction.getUserId());

        // update the database
        long id = db.update(
                TransactionTable.TABLE_NAME,
                values,
                TransactionTable.ID + "=?",
                new String[]{String.valueOf(transaction.getId())}
        );

          
        return id;
    }

    /**
     * Delete transaction by ID
     */
    public int deleteTransaction(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int rowsAffected = db.delete(
                TransactionTable.TABLE_NAME,
                TransactionTable.ID + "=?",
                new String[]{String.valueOf(id)}
        );
        
          
        return rowsAffected;
    }
}