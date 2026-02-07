package com.example.budgetmanager.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.UserTable;
import com.example.budgetmanager.models.User;
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * USER DAO - DEVELOPER GUIDE
 *
 * RESPONSIBILITY: All User entity database operations
 * SINGLE RESPONSIBILITY: Only User CRUD, no business logic
 * PATTERN: Data Access Object (DAO) - pure database layer
 *
 * METHODS: Your existing functions migrated with contract references
 */
public class UserDao {
    private final DatabaseHelper dbHelper;

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
    
    // get user by id
    public User getUserById(int id) {
        // get readable database 
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // build the query for the user with the given id
        Cursor cursor = db.query(
                UserTable.TABLE_NAME,
                new String[]{
                        UserTable.ID,
                        UserTable.NAME_COL,
                        UserTable.EMAIL,
                        UserTable.PASSWORD,
                        UserTable.BALANCE
                },
                UserTable.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.NAME_COL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.PASSWORD)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(UserTable.BALANCE))
            );
        }
        cursor.close();
         
        return user;
    }
    /**
     * Get user by email - authentication use case
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // build the query for the user with the given email
        Cursor cursor = db.query(
                UserTable.TABLE_NAME,
                new String[]{
                        UserTable.ID,
                        UserTable.NAME_COL,
                        UserTable.EMAIL,
                        UserTable.PASSWORD,
                        UserTable.BALANCE
                },
                UserTable.EMAIL + "=?",
                new String[]{email}, null, null, null, null
        );

        // if user exit,  get with the cursor
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.NAME_COL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.PASSWORD)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(UserTable.BALANCE))
            );
        }
        cursor.close();
         
        return user;
    }

    /**
     * Get user by name - profile/search use case
     */
    public User getUserByName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // build the query for the user with the given name
        Cursor cursor = db.query(
                UserTable.TABLE_NAME,
                new String[]{
                        UserTable.ID,
                        UserTable.NAME_COL,
                        UserTable.EMAIL,
                        UserTable.PASSWORD,
                        UserTable.BALANCE
                },
                UserTable.NAME_COL + "=?",
                new String[]{name}, null, null, null, null
        );

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(UserTable.ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.NAME_COL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(UserTable.PASSWORD)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(UserTable.BALANCE))
            );
        }
        cursor.close();
         
        return user;
    }
    
    /**
     * Get user balance
     */
    public double getUserBalance(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // build the query for the user with the given id
        Cursor cursor = db.query(
                UserTable.TABLE_NAME,
                new String[]{UserTable.BALANCE},
                UserTable.ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null, null
        );

        double balance = 0.0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow(UserTable.BALANCE));
        }
        cursor.close();
         
        return balance;
    }
    /**
     * Create new user with password hashing
     */
    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.NAME_COL, user.getName());
        values.put(UserTable.EMAIL, user.getEmail());
        String password = user.getPassword();


        // 10 is the cost factor (equivalent to log_rounds)
        String hashedPassword = BCrypt.withDefaults().hashToString(10, password.toCharArray());
        values.put(UserTable.PASSWORD, hashedPassword);
        
        // Set creation timestamp
        values.put(UserTable.CREATED_AT,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date() ));

        long id = db.insert(UserTable.TABLE_NAME, null, values);
         
        return id;
    }

    /** update user **/
    public int updateUserProfile(User user) {
        // get writable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create content values to update the user
        ContentValues values = new ContentValues();
        values.put(UserTable.NAME_COL, user.getName());
        values.put(UserTable.EMAIL, user.getEmail());

        int rowsAffected = db.update(
                UserTable.TABLE_NAME,
                values,
                UserTable.ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );


        return rowsAffected;
    }




    /**
     * Update user password with hashing
     */
    public int updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Hash new password
        String hashedPassword = BCrypt.withDefaults().hashToString(10, newPassword.toCharArray());

        values.put(UserTable.PASSWORD, hashedPassword);

        int rowsAffected = db.update(
                UserTable.TABLE_NAME,
                values,
                UserTable.ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

         
        return rowsAffected;
    }

    /**
     * Update user balance
     */
    public int updateUserBalance(int userId, double newBalance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(UserTable.BALANCE, newBalance);

        int rowsAffected = db.update(
                UserTable.TABLE_NAME,
                values,
                UserTable.ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

         
        return rowsAffected;
    }



    // get db helper
    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }
}