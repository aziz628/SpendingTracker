package com.example.budgetmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetmanager.R;
import com.example.budgetmanager.adapters.TransactionsAdapter;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.TransactionDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.TransactionWithCategory;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.User;
import com.example.budgetmanager.services.TransactionService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.NavigationHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.LanguageManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.ThemeManager;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MAIN ACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This is the dashboard screen showing:
 * - User greeting
 * - Balance summary (total balance, income, expenses)
 * - Recent transactions list with category icons and colors
 * - Bottom navigation
 *
 * KEY DESIGN DECISIONS:
 * - Refreshes data in onResume() so returning from other screens updates the UI
 * - Uses TransactionsAdapter with TransactionWithCategory DTOs
 *
 * DATA FLOW:
 * onCreate() → initialize dependencies → setup views → load data
 * onResume() → refresh dashboard (in case user added transactions elsewhere)
 *
 * LOW-LEVEL CONCEPTS REFERENCE:
 * - onResume(): Called when activity becomes visible (after onCreate, onStart, or returning from another activity)
 * - RecyclerView: Efficient list rendering with ViewHolder pattern
 * - SharedPreferences: xml file managed by the system we use it to store userId for session management
 */
public class MainActivity extends AppCompatActivity {
    
    // Dependencies
    private DatabaseHelper dbHelper;
    private UserDao userDao;
    private TransactionDao transactionDao;
    private TransactionService transactionService;
    private UserManager userManager;
    private LanguageManager languageManager;
    private ThemeManager themeManager;
    private int currentUserId;
    private String lastLanguage;
    private String lastTheme;
    
    // Views
    private TextView userNameTextView;
    private TextView balanceTextView;
    private TextView incomeTextView;
    private TextView expenseTextView;
    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private LinearLayout emptyStateContainer;
    

    private ImageView logoutButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable Edge-to-Edge
        EdgeToEdge.enable(this);
         // Initialize dependencies
        initializeDependencies();

        setContentView(R.layout.activity_main);
        
        // Handle system bar insets
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));
        NavigationHelper.setupNavigation(this, NavigationHelper.NavigationPage.HOME);

       
        
        // Setup views
        setupViews();
        
        // Load user data
        loadUserData();
        
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if language has changed while the app was in background
        String currentLanguage = languageManager.getLanguage();
        if (!currentLanguage.equals(lastLanguage)) {
            // Language changed - recreate activity to apply new language globally
            lastLanguage = currentLanguage;
            recreate();
            return;
        }
        
        // Check if theme has changed while the app was in background
        String currentTheme = themeManager.getTheme();
        if (!currentTheme.equals(lastTheme)) {
            // Theme changed - recreate activity to apply new theme globally
            lastTheme = currentTheme;
            recreate();
            return;
        }
        
        // Refresh data when returning from other screens (also run after oncreate on boot)
        refreshDashboardData();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    /**
     * Initialize DAOs and services
     */
    private void initializeDependencies() {
        dbHelper = DatabaseHelper.getInstance(this); 

        userDao = new UserDao(dbHelper);
        transactionDao = new TransactionDao(dbHelper);

        // Initialize service with DAOs
        transactionService = new TransactionService(transactionDao, dbHelper, userDao, this);

        userManager = new UserManager(new SharedPreferencesHelper(this));
        languageManager = new LanguageManager(new SharedPreferencesHelper(this));
        themeManager = new ThemeManager(new SharedPreferencesHelper(this));
        currentUserId = userManager.getUserId();
        
        // Check if user is logged in
        if (currentUserId == -1) {
            // Not logged in - redirect to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // Store initial language and theme
        lastLanguage = languageManager.getLanguage();
        languageManager.applyLanguageOnStartup(this);

        lastTheme = themeManager.getTheme();
    }
    
    /**
     * Setup view references and RecyclerView
     */
    private void setupViews() {
        // Find views
        userNameTextView = findViewById(R.id.main_title);
        balanceTextView = findViewById(R.id.total_balance_value);
        incomeTextView = findViewById(R.id.income_value);
        expenseTextView = findViewById(R.id.expense_value);
        logoutButton = findViewById(R.id.logout_button);
        transactionsRecyclerView = findViewById(R.id.transactions_recycler);
        emptyStateContainer = findViewById(R.id.empty_state_container);

        // Setup RecyclerView
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsAdapter = new TransactionsAdapter(this, new ArrayList<>(), new TransactionsAdapter.TransactionClickListener() {
            @Override
            public void onEditClick(TransactionWithCategory transaction) {
                // open update transaction activity passing the transaction id , user_id and type
                Intent intent = new Intent(MainActivity.this, UpdateTransactionActivity.class);
                intent.putExtra("TRANSACTION_ID", transaction.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(TransactionWithCategory transaction) {
                // Handle Delete
                Result<String> result= transactionService.deleteTransaction(transaction,currentUserId);

                // check result and show message
                if (result.isSuccess()) {
                    // refresh the dashboard after deleting a transaction
                    refreshDashboardData();
                    Toast.makeText(MainActivity.this, getString(R.string.msg_transaction_deleted_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.error_delete_transaction_prefix, result.getError()), Toast.LENGTH_SHORT).show();
                }

            }
        });

        transactionsRecyclerView.setAdapter(transactionsAdapter);
        
        // logout button click listener
        logoutButton.setOnClickListener(v -> {
            userManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Load and display user name
     */
    private void loadUserData() {
        User user = userDao.getUserById(currentUserId);
        // if user exist display user name 
        if (user != null) {
            // make first chat capitalized
            String name = user.getName();
            userNameTextView.setText(name.substring(0, 1).toUpperCase() + name.substring(1));
        } 
        // in case the id was invalid redirect to login 
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    /**
     * Refresh balance summary and recent transactions
     */
    private void refreshDashboardData() {
        displayBalanceSummary();
        loadRecentTransactions();
    }
    
    /**
     *  display balance summary
     */
    private void displayBalanceSummary() {
        // Get balance summary from service
        double[] summary = transactionService.getBalanceSummary(currentUserId);
        
        // set the summary values
        double totalIncome = summary[0];
        double totalExpenses = summary[1];
        double balance = summary[2];
        
        
    // Force US locale for number formatting to avoid Arabic numerals
    Locale numberLocale = Locale.US;
    
    // Update UI with balance summary (format to 2 decimal places)
    balanceTextView.setText(String.format(numberLocale, getString(R.string.currency_format), balance));
    incomeTextView.setText(String.format(numberLocale, getString(R.string.currency_format), totalIncome));
    expenseTextView.setText(String.format(numberLocale, getString(R.string.currency_format), totalExpenses));
}
    
    /**
     * Load recent transactions with category names 
     * seperated from adapter setup because it's async db call when adapter need to be rendered instantly  
     */
    private void loadRecentTransactions() {
        // Get recent transactions (limit to 5 for dashboard)
        List<TransactionWithCategory> transactions = 
            transactionDao.getTransactionsWithCategory(currentUserId, 30);
        
        // Update adapter with recent transactions
        transactionsAdapter.updateTransactions(transactions);

        if (transactions.isEmpty()) {
            transactionsRecyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            transactionsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }


    }
   
}