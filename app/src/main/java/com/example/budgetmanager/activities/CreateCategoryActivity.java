package com.example.budgetmanager.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.CategoryName;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.CreateCategoryRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.services.CategoryService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.CategoryLocalizer;
import com.example.budgetmanager.utils.CategorySpinnerAdapter;
import com.example.budgetmanager.utils.ValidationHelper;

// Saripaar imports
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CREATE CATEGORY ACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This activity allows users to create unlimited categories by selecting an icon and entering a name.
 * Uses Saripaar validation like CreateTransactionActivity.
 * 
 * KEY FEATURES:
 * - Income/Expense toggle 
 * - Icon dropdown with all available icons
 * - Free text name input (only constraint: unique per user)
 * - Optional: Auto-fill suggested name when icon selected
 * - Saripaar validation for required fields
 */
public class CreateCategoryActivity extends AppCompatActivity implements Validator.ValidationListener {

    // Validation token constants
    private static final String TOKEN_NAME_REQUIRED = "saripaar_category_name_required";

    // DEPENDENCIES
    private CategoryService categoryService;
    private UserManager userManager;
    private Validator validator;
    private int currentUserId;

    // UI ELEMENTS (with Saripaar annotations)
    @NotEmpty(message = TOKEN_NAME_REQUIRED)
    private EditText nameInput;

    private Spinner categorySpinner;
    private AppCompatButton createButton;
    private ImageView backButton;
    private TextView categoryError;
    private TextView nameError;
    private TextView formServiceError;

    // Toggle buttons
    private TextView btnExpense;
    private TextView btnIncome;

    // STATE
    private String selectedType = CategoryType.EXPENSE; // Default to expense
    private List<Category> availableIcons = new ArrayList<>();
    private CategorySpinnerAdapter categorySpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_category);
        
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        // Initialize dependencies
        initializeDependencies();

        // Setup UI
        setupViews();

        // Load available icons
        loadAvailableIcons();
        updateIconDropdown();

        // Setup listeners
        setupListeners();
    }

    /**
     * Initialize DAOs, Services, and Validator
     */
    private void initializeDependencies() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        CategoryDao categoryDao = new CategoryDao(dbHelper);
        UserDao userDao = new UserDao(dbHelper);
        
        categoryService = new CategoryService(categoryDao, userDao, dbHelper, this);
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
        nameInput = findViewById(R.id.name_input);
        categorySpinner = findViewById(R.id.category_spinner);
        createButton = findViewById(R.id.create_button);
        backButton = findViewById(R.id.back_button);
        categoryError = findViewById(R.id.category_error);
        nameError = findViewById(R.id.name_error);
        formServiceError = findViewById(R.id.form_service_error);
        
        btnExpense = findViewById(R.id.btn_expense);
        btnIncome = findViewById(R.id.btn_income);

        // Initialize category spinner adapter once
        categorySpinnerAdapter = new CategorySpinnerAdapter(this, availableIcons);
        categorySpinner.setAdapter(categorySpinnerAdapter);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Toggle buttons (Income/Expense)
        btnExpense.setOnClickListener(v -> selectType(CategoryType.EXPENSE));
        btnIncome.setOnClickListener(v -> selectType(CategoryType.INCOME));

        // Category selection - auto-fill suggested name 
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableIcons.size()) {
                    Category selectedIcon = availableIcons.get(position);
                    // Auto-fill with localized suggestion
                    String suggestedName = CategoryLocalizer.getLocalizedName(CreateCategoryActivity.this, selectedIcon.getIconName());
                    nameInput.setText(suggestedName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Create button - triggers validation
        createButton.setOnClickListener(v -> {
            clearErrors();
            validator.validate();
        });
    }

    /**
     * Load all available icons (no filtering - unlimited creation)
     */
    private void loadAvailableIcons() {
        availableIcons.clear();
        
        // Create icon options from DEFAULT_CATEGORIES map
        for (Map.Entry<String, String> categoryEntry : DatabaseHelper.DEFAULT_CATEGORIES.entrySet()) {
            String iconName = categoryEntry.getKey();
            String categoryType = categoryEntry.getValue();
            // only save current type categories (keep "other")
            if(!categoryType.equals(selectedType) && !categoryType.equals(CategoryType.OTHER)){
                continue;
            }

            String TranslatedName = CategoryLocalizer.getLocalizedName(this,iconName);

            // save category with current lang name
            availableIcons.add(new Category(TranslatedName, iconName, selectedType, currentUserId));
        }
    }

    /**
     * Update dropdown with available icons (just refresh data, don't recreate adapter)
     */
    private void updateIconDropdown() {
        // Notify adapter that data has changed
        categorySpinnerAdapter.notifyDataSetChanged();
        
        // Auto-select first item if available
        if (!availableIcons.isEmpty()) {
            categorySpinner.setSelection(0);
        }
    }

    /**
     * Toggle between income/expense
     */
    private void selectType(String type) {
        if (type.equals(selectedType)) {
            return; // Already selected
        }

        clearErrors();
        selectedType = type;

        // Update toggle button appearance
        if (type.equals(CategoryType.EXPENSE)) {
            btnExpense.setBackgroundResource(R.drawable.bg_toggle_selected_expense);
            btnExpense.setTextColor(getColor(R.color.error_red));
            btnIncome.setBackgroundResource(android.R.color.transparent);
            btnIncome.setTextColor(getColor(R.color.text_secondary));
        } else {
            btnIncome.setBackgroundResource(R.drawable.bg_toggle_selected_income);
            btnIncome.setTextColor(getColor(R.color.primary_green));
            btnExpense.setBackgroundResource(android.R.color.transparent);
            btnExpense.setTextColor(getColor(R.color.text_secondary));
        }
        loadAvailableIcons();
        updateIconDropdown();
    }


    /**
     * Clear all error messages
     */
    private void clearErrors() {
        nameError.setText("");
        categoryError.setText("");
        formServiceError.setText("");
        nameError.setVisibility(View.GONE);
        categoryError.setVisibility(View.GONE);
        formServiceError.setVisibility(View.GONE);
    }

    //  SARIPAAR VALIDATION CALLBACKS

    @Override
    public void onValidationSucceeded() {
        // Extract form data
        String categoryName = nameInput.getText().toString().trim();

        // Get selected icon
        int selectedPosition = categorySpinner.getSelectedItemPosition();
        Category selectedIcon = availableIcons.get(selectedPosition);
        
        // Create request
        CreateCategoryRequest request = new CreateCategoryRequest(
            categoryName,
            selectedIcon.getIconName(),
            selectedType,
            currentUserId
        );

        // Call service
        Result<Category> result = categoryService.createCategory(request);

        if (result.isSuccess()) {
            Toast.makeText(this, getString(R.string.msg_category_created), Toast.LENGTH_SHORT).show();
            finish(); // Return to CategoriesActivity
        } else {
            formServiceError.setText(result.getError());
            formServiceError.setVisibility(View.VISIBLE);
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
            if (view.getId() == R.id.name_input) {
                nameError.setText(localizedMsg);
                nameError.setVisibility(View.VISIBLE);
            }
        }
    }
}
