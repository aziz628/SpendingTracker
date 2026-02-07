package com.example.budgetmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.LoginRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.models.User;
import com.example.budgetmanager.services.AuthService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.NavigationHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.LanguageManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.ThemeManager;;

import com.example.budgetmanager.utils.ValidationHelper;

// SARIPAAR IMPORTS
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;


import java.util.List;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener{

    // Validation token constants (must be compile-time constants for annotations)
    private static final String TOKEN_EMAIL_EMPTY = "saripaar_email_empty";
    private static final String TOKEN_EMAIL_INVALID = "saripaar_email_invalid";
    private static final String TOKEN_PASSWORD_EMPTY = "saripaar_password_empty";
    private static final String TOKEN_PASSWORD_MIN = "saripaar_password_min";

    // UI elements
    @NotEmpty(message = TOKEN_EMAIL_EMPTY)
    @Email(message = TOKEN_EMAIL_INVALID)
    EditText emailEditText;

    @NotEmpty(message = TOKEN_PASSWORD_EMPTY)
    @Password(min = 6, message = TOKEN_PASSWORD_MIN)
    EditText passwordEditText;

    TextView emailError, passwordError,formServiceError;
    Button loginButton;
    TextView signUpLink;

    // Saripaar Validator
    Validator validator;

    AuthService authService;
    UserDao userDao;
    private UserManager userManager;
    private LanguageManager languageManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // init the dependencies (lang config before set content view)
        initializeDependencies();

        setContentView(R.layout.activity_login);

        // Handle system bar borders
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        // check if user logged in
        checkExistingSession();

        // UI elements
        setupUI();
    }


    private void initializeDependencies() {
        // init the user db operations and the auth service
        userDao = new UserDao(DatabaseHelper.getInstance(this));
        authService = new AuthService(userDao, this);

        // get Saripaar Validator instance
        validator = new Validator(this);
        // set validation listener  for validation events
        validator.setValidationListener(this);

        // initialize the shared preferences helper
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        userManager = new UserManager(sharedPreferencesHelper);
        languageManager = new LanguageManager(sharedPreferencesHelper);

        // Apply saved language
        languageManager.applyLanguageOnStartup(this);

        // Initialize Theme Manager
        themeManager = new ThemeManager(sharedPreferencesHelper);
        // Apply saved theme
        themeManager.applyTheme(this);
    }

    private void setupUI() {
        // Matching IDs from your activity_login.xml
        emailEditText = findViewById(R.id.login_email_input);
        passwordEditText = findViewById(R.id.login_password_input);
        emailError = findViewById(R.id.login_email_error);
        passwordError = findViewById(R.id.login_password_error);
        formServiceError = findViewById(R.id.login_form_error);
        loginButton = findViewById(R.id.login_btn);
        signUpLink = findViewById(R.id.signup_link);

        loginButton.setOnClickListener(v -> {
            clearErrors(); // clear previous errors
            validator.validate(); // trigger the validation
        });

        // Setup the "Sign Up" link click listener
        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

        // set up listener for password input (enter button)
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            // check if enter button is pressed
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearErrors(); // clear previous errors
                validator.validate(); // trigger the validation
                return true;
            }
            return false;
            });



    }

    private void clearErrors() {
        // clear the input error messages
        emailError.setText("");
        emailError.setVisibility(View.GONE);
        passwordError.setText("");
        passwordError.setVisibility(View.GONE);

        // Clear the form service error message
        formServiceError.setText("");
        formServiceError.setVisibility(View.GONE);
    }


    @Override
    public void onValidationSucceeded() {
        // get the input values
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // create DTO and validate
        LoginRequest loginRequest = new LoginRequest(email, password);
        Result<User> result = authService.login(loginRequest);

        // handle results
        if (result.isSuccess()) {
            handleLoginSuccess(result.getData());
        } else {
            handleLoginError(result.getError());
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        //  If validation fails, loop through errors and show them
        for (ValidationError error : errors) {
            // get the view that failed validation
            View view = error.getView();

            // extract the error message (contains token IDs)
            String errorMessage = error.getCollatedErrorMessage(this);
            
            // Convert token IDs to localized strings
            String localizedMsg = ValidationHelper.getLocalizedMessage(this, errorMessage);

            // Show error in your custom TextViews
            if (view.getId() == R.id.login_email_input) {
                emailError.setText(localizedMsg);
                emailError.setVisibility(View.VISIBLE);
            } else if (view.getId() == R.id.login_password_input) {
                passwordError.setText(localizedMsg);
                passwordError.setVisibility(View.VISIBLE);
            }
        }
    }



    // check if user logged in
    private void checkExistingSession() {
        if (userManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }


    private void handleLoginError(String error) {
        // Show business logic error message
        formServiceError.setText(error);
        // set visibility to visible
        formServiceError.setVisibility(View.VISIBLE);
    }

    // open home page activity
    private void handleLoginSuccess(User user){
        // Save user ID to SharedPreferences
        userManager.login(user);

        // Navigate to MainActivity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    
}