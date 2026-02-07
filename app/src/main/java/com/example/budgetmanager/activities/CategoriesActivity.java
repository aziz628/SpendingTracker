package com.example.budgetmanager.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.budgetmanager.R;
import com.example.budgetmanager.adapters.CategoriesAdapter;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.services.CategoryService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.NavigationHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.models.Category;


import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    // Dependencies
    private DatabaseHelper dbHelper;
    private CategoryDao categoryDao;
    private CategoryService categoryService;
    private UserManager userManager;

    private int currentUserId;
    private CategoriesAdapter categoriesAdapter;
    

    // Views
    private RecyclerView categoriesRecyclerView;

    private ImageView addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Edge-to-Edge
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories);   // <-- change from activity_main
        
        // Handle system bar insets
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));
        NavigationHelper.setupNavigation(this, NavigationHelper.NavigationPage.CATEGORIES);

        // Initialize 
        initializeDependencies();
        setupViews();

    }


    @Override
    protected void onResume() {
        super.onResume();

        loadCategories();
    }
     /**
     * Initialize DAOs and services
     */
    private void initializeDependencies() {
        // initialize the category dao with database helper
        dbHelper = DatabaseHelper.getInstance(this);
        categoryDao = new CategoryDao(dbHelper);
        UserDao userDao = new UserDao(dbHelper);


        // initialize the category service
        categoryService = new CategoryService(categoryDao, userDao, dbHelper, this);


        // initialize the shared preferences helper
        userManager = new UserManager(new SharedPreferencesHelper(this));
        
        // get the current user id from shared preferences
        currentUserId = userManager.getUserId();
    }
    
     /**
     * Setup view references and RecyclerView
     */
    private void setupViews() {
        try {
        // Find views
        categoriesRecyclerView = findViewById(R.id.categories_grid);
        addButton = findViewById(R.id.add_button);
        
        // Setup RecyclerView
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }catch (Exception e){
            e.printStackTrace();
        }
        
        // Initialize Adapter with Listener
        categoriesAdapter = new CategoriesAdapter(this, new ArrayList<>(), new CategoriesAdapter.CategoryClickListener() {
            @Override
            public void onEditClick(Category category) {
                // Navigate to UpdateCategoryActivity
                Intent intent = new Intent(CategoriesActivity.this, UpdateCategoryActivity.class);
                intent.putExtra("CATEGORY_ID", category.getId());
                intent.putExtra("CATEGORY_NAME", category.getName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Category category) {
                Result<String> result=categoryService.deleteCategory(category.getId());
                
                if (result.isSuccess()) {
                    loadCategories();
                } else {
                    Toast.makeText(CategoriesActivity.this, result.getError(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Set adapter
        categoriesRecyclerView.setAdapter(categoriesAdapter);

        // Setup click listeners
        setupClickListeners();
    }
    
    /**
     * Setup click listeners for navigation and buttons
     */
    private void setupClickListeners() {
        
        // Add Category button
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(CategoriesActivity.this, CreateCategoryActivity.class);
            startActivity(intent);
        });
    }

    
    private void loadCategories() {
        List<Category> categories = categoryService.getCategories(currentUserId);
        categoriesAdapter.updateCategories(categories);
    }


}