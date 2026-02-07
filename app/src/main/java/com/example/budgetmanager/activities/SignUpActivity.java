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

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.RegisterRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.User;
import com.example.budgetmanager.services.AuthService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.ValidationHelper;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.List;

/**
 * REGISTER ACTIVITY - DEVELOPER GUIDE
 *
 * RESPONSIBILITY: Handle user registration with validation
 * PATTERN: Same as LoginActivity - Saripaar for UI validation, AuthService for business logic
 *
 * VALIDATION FEATURES:
 * - @NotEmpty: Required fields
 * - @Email: Valid email format
 * - @Password: Password strength rules  
 * - @ConfirmPassword: Password confirmation match
 *
 * FLOW: Validation → Business Logic → Navigation/Error Handling
 */
public class SignUpActivity extends AppCompatActivity
        implements Validator.ValidationListener {

    // Validation token constants (must be compile-time constants for annotations)
    private static final String TOKEN_NAME_EMPTY = "saripaar_name_required";
    private static final String TOKEN_NAME_LENGTH = "saripaar_name_length";
    private static final String TOKEN_EMAIL_EMPTY = "saripaar_email_empty";
    private static final String TOKEN_EMAIL_INVALID = "saripaar_email_invalid";
    private static final String TOKEN_PASSWORD_EMPTY = "saripaar_password_empty";
    private static final String TOKEN_PASSWORD_MIN = "saripaar_password_min";

    // === UI ELEMENTS WITH VALIDATION ANNOTATIONS ===
    @NotEmpty(message = TOKEN_NAME_EMPTY)
    @com.mobsandgeeks.saripaar.annotation.Length(min = 2, max = 50, message = TOKEN_NAME_LENGTH)
    EditText nameEditText;

    @NotEmpty(message = TOKEN_EMAIL_EMPTY)
    @Email(message = TOKEN_EMAIL_INVALID)
    EditText emailEditText;

    @NotEmpty(message = TOKEN_PASSWORD_EMPTY)
    @Password(min = 6, scheme = Password.Scheme.ANY, message = TOKEN_PASSWORD_MIN)
    EditText passwordEditText;


    // Error displays
    TextView nameError, emailError, passwordError,formServiceError;
    // Buttons
    Button registerButton;
    TextView loginLink;

    // === DEPENDENCIES ===
    Validator validator;
    AuthService authService;
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // use edge to edge helper to handle system bar borders
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        initializeDependencies();
        setupUI();
    }

    /**
     * DEPENDENCY INITIALIZATION
     */
    private void initializeDependencies() {
        UserDao userDao = new UserDao(DatabaseHelper.getInstance(this));
        authService = new AuthService(userDao, this);
        userManager =  new UserManager(new SharedPreferencesHelper(this));

        // Saripaar validator
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    /**
     * UI SETUP
     * Bind views and set up event listeners
     */
    private void setupUI() {
        // Bind input fields
        nameEditText = findViewById(R.id.fullname_input);
        emailEditText = findViewById(R.id.signup_email_input);
        passwordEditText = findViewById(R.id.signup_password_input);

        // Bind error displays
        formServiceError=findViewById(R.id.signup_form_error);
        nameError = findViewById(R.id.signup_name_error);
        emailError = findViewById(R.id.signup_email_error);
        passwordError = findViewById(R.id.signup_password_error);

        // Bind buttons/links
        registerButton = findViewById(R.id.signup_button);
        loginLink = findViewById(R.id.login_link);

        // Register button click - triggers validation
        registerButton.setOnClickListener(v -> {
            clearErrors();
            validator.validate(); // Saripaar will call onValidationSucceeded() if valid
        });

        // Login link - navigate back to login
        loginLink.setOnClickListener(v -> {
            navigateToLogin();
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

    /**
     * CLEAR PREVIOUS ERRORS
     * Reset all error text views before new validation
     */
    private void clearErrors() {
        // Clear form service error message
        formServiceError.setText("");
        formServiceError.setVisibility(View.GONE);
        // Clear input error messages
        nameError.setText("");
        nameError.setVisibility(View.GONE);
        emailError.setText("");
        emailError.setVisibility(View.GONE);
        passwordError.setText("");
        passwordError.setVisibility(View.GONE);
    }

    /**
     * VALIDATION SUCCESS CALLBACK
     * Called by Saripaar when all fields pass validation
     * Now we can safely proceed with business logic
     */
    @Override
    public void onValidationSucceeded() {
        // Get input values (already validated by Saripaar)
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Create request DTO
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        // Delegate to AuthService for business logic
        Result<User> result = authService.register(registerRequest);

        // Handle business logic result
        if (result.isSuccess()) {
            handleRegistrationSuccess(result.getData());
        } else {
            handleRegistrationError(result.getError());
        }
    }

    /**
     * VALIDATION FAILED CALLBACK
     * Called by Saripaar when validation fails
     * Show appropriate error messages under each field
     */
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            // Get the view that failed validation
            View view = error.getView();
            // Extract the error message (contains token IDs)
            String errorMessage = error.getCollatedErrorMessage(this);
            
            // Convert token IDs to localized strings
            String localizedMsg = ValidationHelper.getLocalizedMessage(this, errorMessage);

            // Map validation errors to the correct error TextView
            if (view.getId() == R.id.fullname_input) {
                nameError.setText(localizedMsg);
                nameError.setVisibility(View.VISIBLE);
            } else if (view.getId() == R.id.signup_email_input) {
                emailError.setText(localizedMsg);
                emailError.setVisibility(View.VISIBLE);
            } else if (view.getId() == R.id.signup_password_input) {
                passwordError.setText(localizedMsg);
                passwordError.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * REGISTRATION SUCCESS HANDLER
     * Save user session and navigate to main app
     */
    private void handleRegistrationSuccess(User user) {
        // Save user session
        userManager.login(user);
        // Navigate to main app
        navigateToMainApp();
    }

    /**
     * REGISTRATION ERROR HANDLER
     * Show business logic errors (e.g., "user already exists")
     */
    private void handleRegistrationError(String error) {
        formServiceError.setText(error);
        formServiceError.setVisibility(View.VISIBLE);
    }

    /**
     * NAVIGATE TO MAIN APP
     */
    private void navigateToMainApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to register
    }

    /**
     * NAVIGATE TO LOGIN
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close register activity
    }
}