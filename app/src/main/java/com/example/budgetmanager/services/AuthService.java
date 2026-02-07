package com.example.budgetmanager.services;

import android.content.Context;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.LoginRequest;
import com.example.budgetmanager.dto.requests.RegisterRequest;

import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.models.User;

import at.favre.lib.crypto.bcrypt.BCrypt;
// Your class code here...


/* AUTHENTICATION SERVICE - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This class contains BUSINESS LOGIC for user authentication. It sits between UI (Activities)
 * and Data (DatabaseHelper) layers.
 * it's seperated so authentication rules can be tested and
 * modified without touching UI or database code.
 *
 * SINGLE RESPONSIBILITY:
 * - Validates login credentials
 * - Handles registration business rules
 * - Manages password security
 * - NEVER touches UI directly (returns results for Activities to handle)
 *
 * DEPENDENCY INJECTION:
 * - Receives DatabaseHelper in constructor (not created internally)
 * - This allows mocking DatabaseHelper for unit testing
 *
 * BUSINESS RULES ENCAPSULATED:
 * - Password strength requirements
 * - Email validation format
 * - Login attempt limits (future)
 * - Session management
 */
public class AuthService {

    private UserDao userDao;
    private Context context;
    
    public AuthService(UserDao userDao, Context context) {
        this.userDao = userDao;
        this.context = context;
    }
    /**
     * LOGIN BUSINESS LOGIC
     *
     * ASSUMPTION: LoginRequest is already validated by FormValidator
     * SECURITY: Generic error message prevents user enumeration
     *
     * @param request Validated login credentials
     * @return Result with User on success, error message on failure
     */    public Result<User> login(LoginRequest request) {
        // Business logic - check user existence
        User user = userDao.getUserByEmail(request.getEmail());
        if (user == null) {
            return Result.error(context.getString(R.string.error_invalid_credentials));
        }

        // Business logic - password verification
        if (!BCrypt.verifyer().verify(request.getPassword().toCharArray(), user.getPassword()).verified) {
            return Result.error(context.getString(R.string.error_invalid_credentials));
        }

        return Result.success(user);
    }


    public Result<User> register(RegisterRequest request) {
        // Business logic - check if user already exists
        User existingUser = userDao.getUserByEmail(request.getEmail());

        if (existingUser != null) {
            return Result.error(context.getString(R.string.error_user_exists));
        }

        // Business logic - create user (hashing handled in DatabaseHelper)
        User newUser = new User(request.getName(), request.getEmail(), request.getPassword());
        long userId = userDao.createUser(newUser);

        if (userId == -1) {
            return Result.error(context.getString(R.string.error_registration_failed));
        }

        newUser.setId((int) userId);

        // Seed default categories for the new user
        CategoryDao categoryDao = new CategoryDao(userDao.getDbHelper());
        categoryDao.seedDefaultCategories((int) userId, context);

        return Result.success(newUser);
    }


}
