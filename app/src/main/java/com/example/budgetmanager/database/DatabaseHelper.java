package com.example.budgetmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Map;

import com.example.budgetmanager.models.User;

/**
 * DATABASE HELPER - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This class manages all database operations for the Budget App.
 * It extends SQLiteOpenHelper which handles database creation and version management.
 * The class follows the Repository pattern -
 * it's the single source of truth for all data operations.
 *
 * KEY DESIGN DECISIONS:
 * - Singleton-like pattern: Each Activity creates its own DB instance (Android manages connections)
 * - Separation of concerns: Only this class touches raw SQL
 * - Foreign key relationships: Users → Categories → Transactions (cascade integrity)
 * - ContentValues usage: Prevents SQL injection vs raw query concatenation
 *
 * DATA FLOW:
 * Activities → DatabaseHelper (CRUD operations) → SQLiteDatabase → Disk storage
 *
 * LOW-LEVEL CONCEPTS REFERENCE:
 *
 * SQLiteOpenHelper: Android wrapper that manages database creation/upgrades
 * - onCreate(): Called when app first installs (database doesn't exist)
 * - onUpgrade(): Called when DATABASE_VERSION increases (migration logic)
 *
 * Cursor:
 *  - Concept: A pointer to the result set of a query,
 *  - Cursor Window :  a buffer contains the chunks of pages readen by the cursor from the file, it have fixed size
 *  -  SQLite stores data in fixed-size units called "pages" (typically 4KB).
 *       pages parsed by cursor  into individual rows organized in  a structured format  in the buffer (array of row references)
 *       Rows data have header contains type/size metadata used for finding a  row's column index (pointer)
 * - Cursor Must always be closed to prevent memory leaks
 * - moveToFirst()/moveToNext(): Navigate through result rows , (move a pointer , and load from file if next row isn't in memory)
 * - getColumnIndexOrThrow(): Safe column access (throws exception if column missing)
 *
 * ContentValues:
 * - Key-value store for database operations
 * - Automatically escapes values to prevent SQL injection
 * - Required for insert()/update() methods
 *
 * SQLiteDatabase:
 * - getReadableDatabase(): For queries (SELECT)
 * - getWritableDatabase(): For mutations (INSERT, UPDATE, DELETE)
 * - Transactions: beginTransaction(), setTransactionSuccessful(), endTransaction()
 *
 * Foreign Keys:
 * - user_id in categories/transactions ensures data isolation
 * - category_id in transactions maintains referential integrity
 * - Manual cascade deletes (SQLite doesn't auto-cascade by default)
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "budget.db";
    private static final int DATABASE_VERSION = 1;

    // === USER TABLE CONTRACT ===
    public static class UserTable {
        public static final String TABLE_NAME = "users";
        public static final String ID = "id";
        public static final String NAME_COL = "name";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String BALANCE = "balance";
        public static final String CREATED_AT = "created_at";
    }

    // === CATEGORY TABLE CONTRACT ===
    public static class CategoryTable {
        public static final String TABLE_NAME = "categories";
        public static final String ID = "id";
        public static final String NAME_COL = "name";
        public static final String ICON_NAME = "icon_name";
        public static final String TYPE = "type";
        public static final String USER_ID = "user_id";
        public static final String CREATED_AT = "created_at";
    }

    // category type values
    public static class CategoryType {
        public static final String INCOME = "income";
        public static final String EXPENSE = "expense";
        public static final String OTHER = "other";
    }

    // category names
    public static class CategoryName {
        public static final String FOOD = "food";
        public static final String TRANSPORT = "transport";
        public static final String SHOPPING = "shopping";
        public static final String OTHER = "other";
        public static final String SALARY = "salary";
        public static final String FREELANCE = "freelance";
    }    

    // map of default categories and their types
    public static final Map<String, String> DEFAULT_CATEGORIES = Map.of(
        CategoryName.FOOD, CategoryType.EXPENSE,
        CategoryName.TRANSPORT, CategoryType.EXPENSE,
        CategoryName.SHOPPING, CategoryType.EXPENSE,
        CategoryName.SALARY, CategoryType.INCOME,
        CategoryName.FREELANCE, CategoryType.INCOME,
        CategoryName.OTHER, CategoryType.OTHER
    );

    // === TRANSACTION TABLE CONTRACT ===
    public static class TransactionTable {
        public static final String TABLE_NAME = "transactions";
        public static final String ID = "id";
        public static final String AMOUNT = "amount";
        public static final String TYPE = "type";
        public static final String NOTE = "note";
        public static final String DATE = "date";
        public static final String CATEGORY_ID = "category_id";
        public static final String USER_ID = "user_id";
        public static final String CREATED_AT = "created_at";
    }

    // singleton pattern
    private static DatabaseHelper instance;
    
    // synchronized access to prevent multiple instances
    public static synchronized DatabaseHelper getInstance(Context context) {
        // create the instance if it doesn't exist
        if (instance == null) {
            // Use application context to prevent memory leaks
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    // Make constructor private
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enable foreign key constraints
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + UserTable.TABLE_NAME + " (" +
            UserTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            UserTable.NAME_COL + " TEXT NOT NULL," +
            UserTable.EMAIL + " TEXT UNIQUE NOT NULL," +
            UserTable.PASSWORD + " TEXT NOT NULL," +
            UserTable.BALANCE + " REAL NOT NULL DEFAULT 0.0," +
            UserTable.CREATED_AT + " TEXT NOT NULL" +
            ");";

        //  categories table query
        // categories  unique in the couple (name,user_id)
        // types are forced to be either "income" or "expense" by the  check constraint
        String createCategoriesTable = "CREATE TABLE IF NOT EXISTS " + CategoryTable.TABLE_NAME + " (" +
            CategoryTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CategoryTable.NAME_COL + " TEXT NOT NULL," +
            CategoryTable.TYPE + " TEXT NOT NULL CHECK (" + CategoryTable.TYPE + " IN ('" + CategoryType.INCOME + "', '" + CategoryType.EXPENSE + "'))," +
            CategoryTable.USER_ID + " INTEGER NOT NULL," +
            CategoryTable.ICON_NAME + " TEXT NOT NULL DEFAULT 'other'," + 
            CategoryTable.CREATED_AT + " TEXT NOT NULL," +
            "UNIQUE (" + CategoryTable.NAME_COL + "," + CategoryTable.USER_ID + ")," +
            "FOREIGN KEY(" + CategoryTable.USER_ID + ") REFERENCES " +
            UserTable.TABLE_NAME + "(" + UserTable.ID + ") ON DELETE CASCADE" +
            ");";

        // Create transactions table
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS " + TransactionTable.TABLE_NAME + " (" +
            TransactionTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            TransactionTable.AMOUNT + " REAL NOT NULL," +
            TransactionTable.TYPE + " TEXT NOT NULL CHECK (" + TransactionTable.TYPE + " IN ('" + CategoryType.INCOME + "', '" + CategoryType.EXPENSE + "'))," +
            TransactionTable.NOTE + " TEXT," +
            TransactionTable.DATE + " TEXT NOT NULL," +
            TransactionTable.CATEGORY_ID + " INTEGER NOT NULL," +
            TransactionTable.USER_ID + " INTEGER NOT NULL," +
            TransactionTable.CREATED_AT + " TEXT NOT NULL," +
            "FOREIGN KEY(" + TransactionTable.CATEGORY_ID + ") REFERENCES " +
            CategoryTable.TABLE_NAME + "(" + CategoryTable.ID + ") ON DELETE CASCADE," +
            "FOREIGN KEY(" + TransactionTable.USER_ID + ") REFERENCES " +
            UserTable.TABLE_NAME + "(" + UserTable.ID + ") ON DELETE CASCADE" +
            ");";


        db.execSQL(createUsersTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createTransactionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TransactionTable.TABLE_NAME);

        onCreate(db);
    }

    /**
     * Interface for database transaction work.
     */
    @FunctionalInterface
    public interface TransactionWork<T> {
        T doWork();
    }

    /**
     * Executes a block of code inside a database transaction.
     * Automatically handles begin, commit, and rollback on error.
     */
    public <T> T runInTransaction(TransactionWork<T> work) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            T result = work.doWork();
            db.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Or throw a custom DatabaseException
        } finally {
            // end the transaction
            db.endTransaction();
        }
    }
}