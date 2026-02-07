package com.example.budgetmanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.TransactionDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.CreateTransactionRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.models.Transaction;
import com.example.budgetmanager.services.TransactionService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.ValidationHelper;
import com.example.budgetmanager.utils.CategorySpinnerAdapter;

// Saripaar imports
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * CREATE TRANSACTION ACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This activity allows users to create new income/expense transactions.
 * architectural pattern :  UI → Validation (Saripaar) → Service → DAO → DB.
 *
 * KEY DESIGN DECISIONS:
 * - Uses Saripaar for client-side validation (amount > 0, fields not empty)
 * - Uses TransactionService for business logic (category ownership verification)
 * - Dynamically loads categories based on selected type (income/expense)
 * - Stores categoryId (not name) in the transaction - follows DTO pattern
 * - Date picker for user-friendly date selection, converts to DB format (yyyy-MM-dd)
 *
 * DATA FLOW:
 * 1. User fills form → clicks Save
 * 2. Saripaar validates input fields
 * 3. CreateTransactionRequest DTO is built
 * 4. TransactionService validates business rules (category ownership, etc.)
 * 5. TransactionDao persists to database
 * 6. User returns to MainActivity (which refreshes to show new transaction)
 *
 * ARCHITECTURE LAYERS:
 * UI (this) → Validator (Saripaar) → Service (TransactionService) → DAO (TransactionDao) → DB
 *
 * LOW-LEVEL CONCEPTS REFERENCE:
 * - Saripaar: Annotation-based validation library (similar to JSR-303)
 * - Spinner: Dropdown for category selection that uses ArrayAdapter
 *   arrayAdapter is a class that provides a binding between an array and a Spinner
 * - DatePickerDialog: Android's built-in date picker
 * - SimpleDateFormat: Converts between display format (dd/MM/yyyy) and DB format (yyyy-MM-dd)
 * - Result Pattern: Uniform success/failure handling
 */
public class CreateTransactionActivity extends AppCompatActivity implements Validator.ValidationListener {

    //  DEPENDENCIES
    private TransactionService transactionService;
    private CategoryDao categoryDao;
    private UserManager userManager;
    private Validator validator;
    private int currentUserId;

    // Validation token constants (compile-time constants for annotations)
    private static final String TOKEN_AMOUNT_REQUIRED = "saripaar_amount_required";
    private static final String TOKEN_AMOUNT_MIN = "saripaar_amount_min";

    //  UI ELEMENTS (with Saripaar annotations)
    @NotEmpty(message = TOKEN_AMOUNT_REQUIRED)
    @DecimalMin(value = 0.01, message = TOKEN_AMOUNT_MIN)
    private EditText amountInput;

    private EditText dateInput;
    private EditText noteInput;
    private Spinner categorySpinner;
    private AppCompatButton saveButton;
    private ImageView backButton;

    // Error TextViews
    private TextView amountError;
    private TextView categoryError;

    // Toggle buttons
    private TextView btnExpense;
    private TextView btnIncome;

    //  STATE
    private String selectedType = DatabaseHelper.CategoryType.EXPENSE; // Default to expense
    private final Calendar calendar = Calendar.getInstance();
    private List<Category> currentCategories = new ArrayList<>();
    private List<Category> allCategories ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_transaction);
        // Handle system bar padding
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        // Initialize dependencies
        initializeDependencies();

        // Setup UI
        setupViews();

        // Load initial categories (expense by default)
        loadCategories();
        fillCategoryDropdown();


        // Setup listeners
        setupListeners();
    }

    /**
     * Initialize DAOs, Services, and Validator
     */
    private void initializeDependencies() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        TransactionDao transactionDao = new TransactionDao(dbHelper);
        categoryDao = new CategoryDao(dbHelper);
        UserDao userDao= new UserDao(dbHelper);


        transactionService = new TransactionService(transactionDao, dbHelper, userDao, this);
        userManager = new UserManager(new SharedPreferencesHelper(this));
        currentUserId = userManager.getUserId();

        // Initialize Saripaar validator
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    /**
     * Setup view references
     */
    private void setupViews() {
        // Find views by ID 
        amountInput = findViewById(R.id.amount_input);
        amountError = findViewById(R.id.amount_error);
        dateInput = findViewById(R.id.date_input);
        noteInput = findViewById(R.id.note_input);
        categorySpinner = findViewById(R.id.category_spinner);
        categoryError = findViewById(R.id.category_error);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);
        btnExpense = findViewById(R.id.btn_expense);
        btnIncome = findViewById(R.id.btn_income);

        // Set current date as default - force US locale to avoid Arabic numerals
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateInput.setText(dateFormat.format(calendar.getTime()));
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Date picker
        dateInput.setOnClickListener(v -> showDatePicker());

        // Toggle buttons (Income/Expense)
        btnExpense.setOnClickListener(v -> selectType(CategoryType.EXPENSE));
        btnIncome.setOnClickListener(v -> selectType(CategoryType.INCOME));

        // Save button - triggers validation
        saveButton.setOnClickListener(v -> {
            clearErrors();
            validator.validate();
        });
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        amountError.setText("");
        categoryError.setText("");
        amountError.setVisibility(View.GONE);
        categoryError.setVisibility(View.GONE);
    }

    /**
     * Load categories for the selected type (income/expense)
     */
    /**
     * Load categories for the selected type and display with icons in spinner
     */
    private void loadCategories() {
        // Get all categories for the user
        allCategories = categoryDao.getCategories(currentUserId);
    }

    public void fillCategoryDropdown(){
        // Filter by type
        currentCategories.clear();
        for (Category category : allCategories) {
            if (category.getType().equalsIgnoreCase(selectedType)) {
                currentCategories.add(category);
            }
        }

        // Use custom adapter that displays icons + category names
        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, currentCategories);
        categorySpinner.setAdapter(adapter);
    }

    /**
     * Toggle between income/expense
     */
    private void selectType(String type) {
        // If same type selected, exit
        if(type.equals(selectedType)){
            return;
        }
        clearErrors();
        selectedType = type;

        if (type.equals(CategoryType.EXPENSE)) {
            // Highlight expense
            btnExpense.setBackgroundResource(R.drawable.bg_toggle_selected_expense);
            btnExpense.setTextColor(getColor(R.color.error_red));

            btnIncome.setBackgroundResource(android.R.color.transparent);
            btnIncome.setTextColor(getColor(R.color.text_secondary));
        } else {
            // Highlight income
            btnIncome.setBackgroundResource(R.drawable.bg_toggle_selected_income);
            btnIncome.setTextColor(getColor(R.color.primary_green));

            btnExpense.setBackgroundResource(android.R.color.transparent);
            btnExpense.setTextColor(getColor(R.color.text_secondary));
        }

        // Reload categories for the new type
        fillCategoryDropdown();
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        // Get the current date text (DD-MM-YYYY)
        String currentDate = dateInput.getText().toString();

        // Split it
        String[] parts = currentDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1; // DatePicker uses 0-11 for months
        int day = Integer.parseInt(parts[2]);

        // setup date picker
        DatePickerDialog picker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format back to DD-MM-YYYY
            String newDate = String.format(Locale.US, "%04d-%02d-%02d",
                    selectedYear, selectedMonth + 1, selectedDay); // m+1 since picker return month index 0-11

            // Update the input
            dateInput.setText(newDate);

        }, year, month, day);

        picker.show();
    }



    //  SARIPAAR VALIDATION CALLBACKS

    @Override
    public void onValidationSucceeded() {
        // Extract form data
        double amount = Double.parseDouble(amountInput.getText().toString());
        String note = noteInput.getText().toString().trim();
        String date = dateInput.getText().toString(); // DD-MM-YYYY format
        

        // Get selected category
        int selectedPosition = categorySpinner.getSelectedItemPosition();
        if (currentCategories.isEmpty()) {
            categoryError.setText(getString(R.string.error_no_categories_available, selectedType));
            categoryError.setVisibility(View.VISIBLE);
            return;
        }
        Category selectedCategory = currentCategories.get(selectedPosition);

        // Build request DTO with converted date
        CreateTransactionRequest request = new CreateTransactionRequest(
                amount,
                selectedType,
                note,
                date, // Use converted date format
                selectedCategory.getId()
        );

        // Call service
        Result<Transaction> result = transactionService.createTransaction(request, currentUserId);

        if (result.isSuccess()) {
            Toast.makeText(this, getString(R.string.msg_transaction_saved), Toast.LENGTH_SHORT).show();
            finish(); // Return to MainActivity (which will refresh)
        } else {
            Toast.makeText(this, getString(R.string.error_prefix, result.getError()), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        // Display validation errors in TextViews
        for (ValidationError error : errors) {
            View view = error.getView();
            String errorMessage = error.getCollatedErrorMessage(this);
            
            // Convert token IDs to localized strings
            String localizedMsg = ValidationHelper.getLocalizedMessage(this, errorMessage);

            // Show error in custom TextView
            if (view.getId() == R.id.amount_input) {
                amountError.setText(localizedMsg);
                amountError.setVisibility(View.VISIBLE);
            }
        }
    }
}
