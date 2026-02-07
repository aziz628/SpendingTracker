package com.example.budgetmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.UpdateCategoryRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.services.CategoryService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.ValidationHelper;
import com.example.budgetmanager.utils.CategoryIconMapper;
import com.example.budgetmanager.utils.IconGridAdapter;
import com.example.budgetmanager.utils.IconGridItem;

// Saripaar imports
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UPDATE CATEGORY ACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This activity allows users to update category name and icon.
 * Uses a 3-column icon grid for visual icon selection.
 * 
 * KEY FEATURES:
 * - Icon grid (3 columns) for visual selection
 * - Name input with current category name pre-filled
 * - Saripaar validation for required fields
 * - Highlights currently selected icon
 */
public class UpdateCategoryActivity extends AppCompatActivity implements Validator.ValidationListener, IconGridAdapter.OnIconClickListener {

    // Validation token constants
    private static final String TOKEN_NAME_REQUIRED = "saripaar_category_name_required";

    // DEPENDENCIES
    private CategoryService categoryService;
    private UserManager userManager;
    private Validator validator;
    private CategoryDao categoryDao;
    private int currentUserId;

    // UI ELEMENTS (with Saripaar annotations)
    @NotEmpty(message = TOKEN_NAME_REQUIRED)
    private EditText nameInput;

    private RecyclerView iconsGrid;
    private AppCompatButton updateButton;
    private ImageView backButton;
    private TextView iconError;
    private TextView nameError;
    private TextView formServiceError;

    // STATE
    private int categoryId;
    private String selectedIconName;
    private String categoryType;
    private List<IconGridItem> iconItems = new ArrayList<>();
    private IconGridAdapter iconsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_category);
        
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        // Get category data from intent
        getIntentData();

        // Initialize dependencies
        initializeDependencies();

        // Setup UI
        setupViews();

        // Load category data
        loadCategoryData();

        // Setup icon grid
        setupIconGrid();

        // Setup listeners
        setupListeners();
    }

    /**
     * Get category data passed from CategoriesActivity
     */
    private void getIntentData() {
        Intent intent = getIntent();
        categoryId = intent.getIntExtra("CATEGORY_ID", -1);
        
        if (categoryId == -1) {
            Toast.makeText(this, "Error: Category not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Initialize DAOs, Services, and Validator
     */
    private void initializeDependencies() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        categoryDao = new CategoryDao(dbHelper);
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
        iconsGrid = findViewById(R.id.icons_grid);
        updateButton = findViewById(R.id.update_button);
        backButton = findViewById(R.id.back_button);
        iconError = findViewById(R.id.icon_error);
        nameError = findViewById(R.id.name_error);
        formServiceError = findViewById(R.id.form_service_error);
    }

    /**
     * Load category data and pre-fill form
     */
    private void loadCategoryData() {
        Category category = categoryDao.getCategoryById(categoryId);
        if (category != null) {
            nameInput.setText(category.getName());
            selectedIconName  = category.getIconName(); // set current icon name
            categoryType = category.getType(); // set category type
        }
    }

    /**
     * Setup icon grid with 3 columns
     */
    private void setupIconGrid() {
        // Create icon items from DEFAULT_CATEGORIES
        for (Map.Entry<String, String> entry : DatabaseHelper.DEFAULT_CATEGORIES.entrySet()) {
            String iconName = entry.getKey();
            boolean isSelected = iconName.equals(selectedIconName);
            iconItems.add(new IconGridItem(iconName, isSelected));
        }

        // Setup RecyclerView
        iconsGrid.setLayoutManager(new GridLayoutManager(this, 3));
        iconsAdapter = new IconGridAdapter(iconItems, this, categoryType);
        iconsGrid.setAdapter(iconsAdapter);
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Update button - triggers validation
        updateButton.setOnClickListener(v -> {
            clearErrors();
            validator.validate();
        });
    }

    /**
     * Handle icon selection from grid
     */
    @Override
    public void onIconClick(String iconName) {
        // Update selection
        selectedIconName = iconName;
        
        // Update adapter to show new selection
        for (IconGridItem item : iconItems) {
            item.setSelected(item.getIconName().equals(iconName));
        }
        iconsAdapter.notifyDataSetChanged();
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        nameError.setText("");
        iconError.setText("");
        formServiceError.setText("");
        nameError.setVisibility(View.GONE);
        iconError.setVisibility(View.GONE);
        formServiceError.setVisibility(View.GONE);
    }

    //  SARIPAAR VALIDATION CALLBACKS

    @Override
    public void onValidationSucceeded() {
        // Extract form data
        String categoryName = nameInput.getText().toString().trim();
        
        // Create request
        UpdateCategoryRequest request = new UpdateCategoryRequest(
            categoryName,
            selectedIconName,
                categoryId
        );

        // Call service
        Result<Category> result = categoryService.updateCategory(request);

        if (result.isSuccess()) {
            Toast.makeText(this, getString(R.string.msg_category_updated), Toast.LENGTH_SHORT).show();
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