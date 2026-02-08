package com.example.budgetmanager.services;

import android.content.Context;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.CreateCategoryRequest;
import com.example.budgetmanager.dto.requests.UpdateCategoryRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;

import java.util.List;

/**
 * CATEGORY SERVICE - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This service contains all business logic for category operations.
 * It pass the data from the UI to the DAO, enforcing business rules .
 *
 * KEY DESIGN DECISIONS:
 * - Returns Result<T> wrapper to handle success/failure uniformly.
 * - handle any database exceptions
 * - limit the fields that can be updated
 *
 * SINGLE RESPONSIBILITY:
 * - Coordinates between CategoryDao
 */
public class CategoryService {
    // Dependencies
    private final CategoryDao categoryDao;
    private final UserDao userDao;
    private final DatabaseHelper dbHelper;
    private final Context context;
    
    // constructor
    public CategoryService(CategoryDao categoryDao, UserDao userDao, DatabaseHelper dbHelper, Context context) {
        this.categoryDao = categoryDao;
        this.userDao = userDao;
        this.dbHelper = dbHelper;
        this.context = context;
    }
    // crud 

    // read categories
    public List<Category> getCategories(int userId) {
        return categoryDao.getCategories(userId);
    }

    // create category
    public Result<Category> createCategory(CreateCategoryRequest request) {
        // convert dto to category model
        Category category = new Category(
            request.getName(),
            request.getIconName(),
            request.getType(),
            request.getUserId()
        );
        
        // save to database
        long id = categoryDao.createCategory(category);
        if (id == -1) {
            return Result.error(context.getString(R.string.error_create_category_failed));
        }
        category.setId((int) id);
        return Result.success(category);
    }
    

        // update category
    public Result<Category> updateCategory(UpdateCategoryRequest request) {
        // Find the category by ID
        Category category = categoryDao.getCategoryById(request.getId());
        if (category == null) {
            return Result.error(context.getString(R.string.error_category_not_found));
        }
        // set the new values
        category.setName(request.getName());
        category.setIconName(request.getIconName());

        // save to database
        categoryDao.updateCategory(category);
        return Result.success(category);
    }    
    
    // delete category
    public Result<String> deleteCategory(int id) {
        // Find the category by ID
        Category category = categoryDao.getCategoryById(id);
        if (category == null) {
            return Result.error(context.getString(R.string.error_category_not_found));
        }
        double balance = userDao.getUserBalance(category.getUserId());
        double total = categoryDao.getTotalTransactionsPerCategory(id);

        // check if removing income category make  balance negative
        if (category.getType().equals(CategoryType.INCOME)) {
            if(balance - total < 0){
                return Result.error(context.getString(R.string.error_delete_category_balance));
            }
            balance -= total;
        }else{
            balance += total;
        }        
        
        // Delete the category and update balance
        double finalBalance = balance;
        return dbHelper.runInTransaction(() -> {
            userDao.updateUserBalance(category.getUserId(), finalBalance);
            
            int rowsAffected = categoryDao.deleteCategory(id);
            if (rowsAffected == 0) {
                return Result.error(context.getString(R.string.error_delete_category_failed));
            }

            return Result.success(context.getString(R.string.msg_category_deleted));
        });
        
    }

}
