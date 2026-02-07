package com.example.budgetmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.UpdateUserInfoRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.User;
import com.example.budgetmanager.services.UserService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.NavigationHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper.UserManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.LanguageManager;
import com.example.budgetmanager.utils.SharedPreferencesHelper.ThemeManager;
import com.example.budgetmanager.utils.ValidationHelper;

import com.google.android.material.bottomsheet.BottomSheetDialog;

// Saripaar imports
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.List;

/**
 * PROFILE ACTIVITY - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This activity allows users to edit their profile information.
 * architectural pattern :  UI → Validation (Saripaar) → Service → DAO → DB.
 *
 * KEY DESIGN DECISIONS:
 * - Uses Saripaar for client-side validation (name, email)
 * - Uses UserService for business logic (update user info)
 * - Uses SharedPreferencesHelper for user identification
 *
 * LOW-LEVEL CONCEPTS :
 * BottomSheetDialog: is a modal dialog that slides up from the bottom of the screen 
    and makes the rest of the screen darkened. It is commonly used to present a set of options or additional information,
   allowing users to interact with it without leaving the current activity.
   It uses a custom layout defined in XML to structure its content.

 */
public class ProfileActivity extends AppCompatActivity {

    // Validation token constants for edit info (must be compile-time constants for annotations)
    private static final String TOKEN_NAME_EMPTY = "saripaar_name_required";
    private static final String TOKEN_EMAIL_EMPTY = "saripaar_email_empty";
    private static final String TOKEN_EMAIL_INVALID = "saripaar_email_invalid";
    
    // Validation token constants for password change
    private static final String TOKEN_PASSWORD_EMPTY = "saripaar_password_empty";
    private static final String TOKEN_PASSWORD_MIN = "saripaar_password_min";
    private static final String TOKEN_CONFIRM_PASSWORD_EMPTY = "saripaar_confirm_password_empty";

    // Dependencies
    private UserManager userManager;
    private LanguageManager languageManager;
    private ThemeManager themeManager;
    private UserService userService;
    private User currentUser;

    // global page Views
    private TextView profileName, profileEmail, profileLanguageValue, profileThemeValue;
    private LinearLayout rowEditInfo, rowChangePassword, rowChangeLanguage, rowChangeTheme;
    private ImageView navHome, navCategories, navProfile;
    private FrameLayout addBtn;

    // BOTTOM SHEET VIEWS (not validated here - validation happens in inner form classes)
    private EditText nameInput;
    private EditText emailInput;

    // PASSWORD CHANGE VIEWS (not validated here - validation happens in inner form classes)
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Handle window borders to get edge-to-edge effect
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));
        NavigationHelper.setupNavigation(this, NavigationHelper.NavigationPage.PROFILE);

        initializeDependencies();
        setupViews();
        FillProfileUI();
    }

    private void initializeDependencies() {
        // one time use dependecies
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        UserDao userDao = new UserDao(dbHelper);

        // service dependencies
        userService = new UserService(userDao, dbHelper, this);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        userManager = new UserManager(sharedPreferencesHelper);
        languageManager = new LanguageManager(sharedPreferencesHelper);
        themeManager = new ThemeManager(sharedPreferencesHelper);

        // get current user
        int currentUserId = userManager.getUserId();
        currentUser = userDao.getUserById(currentUserId);
    }

    private void setupViews() {
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        rowEditInfo = findViewById(R.id.row_edit_info);
        rowChangePassword = findViewById(R.id.row_change_password);
        rowChangeLanguage = findViewById(R.id.row_change_language);
        rowChangeTheme = findViewById(R.id.row_change_theme);
        profileLanguageValue = findViewById(R.id.profile_language_value);
        profileThemeValue = findViewById(R.id.profile_theme_value);

        rowEditInfo.setOnClickListener(v -> showEditInfoSheet());
        rowChangePassword.setOnClickListener(v -> showChangePasswordSheet());
        rowChangeLanguage.setOnClickListener(v -> showLanguageSelectionSheet());
        rowChangeTheme.setOnClickListener(v -> showThemeSelectionSheet());
    }


    /*
     * Fill Profile UI with current user data
     */
    private void FillProfileUI() {
        profileName.setText(currentUser.getName());
        profileEmail.setText(currentUser.getEmail());
        profileLanguageValue.setText(languageManager.getLanguage());
        profileThemeValue.setText(themeManager.getCurrentThemeDisplayName(this));
    }

    /**
     * SHOW EDIT INFO SHEET WITH SARIPAAR VALIDATION
     */
    private void showEditInfoSheet() {
        // Create the dialog
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_edit_info, null);

        // View References inside the sheet
        TextView errorBanner = sheetView.findViewById(R.id.sheet_error_text);

        nameInput = sheetView.findViewById(R.id.edit_name);
        emailInput = sheetView.findViewById(R.id.edit_email);

        TextView nameError = sheetView.findViewById(R.id.name_error);
        TextView emailError = sheetView.findViewById(R.id.email_error);
        Button saveBtn = sheetView.findViewById(R.id.btn_save_info);

        // Pre-fill
        nameInput.setText(currentUser.getName());
        emailInput.setText(currentUser.getEmail());

        // Create the form object and its own validator for this dialog with a callback handlers
        EditInfoForm form = new EditInfoForm(nameInput, emailInput);
        Validator dialogValidator = new Validator(form);

        dialogValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                // Validation passed - call service
                String newName = nameInput.getText().toString().trim();
                String newEmail = emailInput.getText().toString().trim();

                Result<String> result = userService.updateUserProfile(
                        new UpdateUserInfoRequest(currentUser.getId(), newName, newEmail)
                );

                if (result.isSuccess()) {
                    currentUser.setName(newName);
                    currentUser.setEmail(newEmail);
                    FillProfileUI();
                    Toast.makeText(ProfileActivity.this, getString(R.string.msg_user_updated), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    errorBanner.setText(result.getError());
                    errorBanner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                // Display validation errors
                errorBanner.setVisibility(View.GONE);
                nameError.setText("");
                emailError.setText("");
                nameError.setVisibility(View.GONE);
                emailError.setVisibility(View.GONE);

                for (ValidationError error : errors) {
                    View view = error.getView();
                    String errorMessage = error.getCollatedErrorMessage(ProfileActivity.this);
                    
                    // Convert token IDs to localized strings
                    String localizedMsg = ValidationHelper.getLocalizedMessage(ProfileActivity.this, errorMessage);

                    if (view.getId() == R.id.edit_name) {
                        nameError.setText(localizedMsg);
                        nameError.setVisibility(View.VISIBLE);
                    } else if (view.getId() == R.id.edit_email) {
                        emailError.setText(localizedMsg);
                        emailError.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Save button triggers validation
        saveBtn.setOnClickListener(v -> dialogValidator.validate());

        dialog.setContentView(sheetView);
        dialog.show();
    }

    /**
     * SHOW CHANGE PASSWORD SHEET WITH SARIPAAR VALIDATION
     */
    private void showChangePasswordSheet() {
        // Create the dialog
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_edit_pass, null);

        // View References inside the sheet
        TextView passwordErrorBanner = sheetView.findViewById(R.id.sheet_error_text_password);
        newPasswordInput = sheetView.findViewById(R.id.new_password);
        confirmPasswordInput = sheetView.findViewById(R.id.confirm_password);
        TextView newPasswordError = sheetView.findViewById(R.id.new_password_error);
        TextView confirmPasswordError = sheetView.findViewById(R.id.confirm_password_error);
        Button savePasswordBtn = sheetView.findViewById(R.id.btn_save_password);
        
        // Create the form object and its own validator for this dialog with a callback handlers
        ChangePasswordForm form = new ChangePasswordForm(newPasswordInput, confirmPasswordInput);
        Validator dialogValidator = new Validator(form);
        
        dialogValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                // Validation passed - check if passwords match
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordError.setText(getString(R.string.saripaar_passwords_not_match));
                    confirmPasswordError.setVisibility(View.VISIBLE);
                    return;
                }

                // Call service to update password
                Result<String> result = userService.updateUserPassword(currentUser.getId(), newPassword);

                if (result.isSuccess()) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.msg_password_updated), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    passwordErrorBanner.setText(result.getError());
                    passwordErrorBanner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {

                for (ValidationError error : errors) {
                    View view = error.getView();
                    String errorMessage = error.getCollatedErrorMessage(ProfileActivity.this);
                    
                    // Convert token IDs to localized strings
                    String localizedMsg = ValidationHelper.getLocalizedMessage(ProfileActivity.this, errorMessage);

                    if (view.getId() == R.id.new_password) {
                        newPasswordError.setText(localizedMsg);
                        newPasswordError.setVisibility(View.VISIBLE);
                    } else if (view.getId() == R.id.confirm_password) {
                        confirmPasswordError.setText(localizedMsg);
                        confirmPasswordError.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Save button triggers validation
        savePasswordBtn.setOnClickListener(v -> {
            newPasswordError.setText("");
            confirmPasswordError.setText("");
            newPasswordError.setVisibility(View.GONE);
            confirmPasswordError.setVisibility(View.GONE);
            passwordErrorBanner.setVisibility(View.GONE);
            dialogValidator.validate();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    /**
     * SHOW LANGUAGE SELECTION SHEET
     */
    private void showLanguageSelectionSheet() {
        // Create the dialog and inflate the layout
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_lang_select, null);

        // setup views
        RadioButton radioEnglish = sheetView.findViewById(R.id.radio_english);
        RadioButton radioFrench = sheetView.findViewById(R.id.radio_french);
        RadioButton radioArabic = sheetView.findViewById(R.id.radio_arabic);
        Button applyBtn = sheetView.findViewById(R.id.btn_apply_language);

        // Get current language and pre-set the selection
        String currentLang = languageManager.getLanguage();
        switch (currentLang) {
            case SharedPreferencesHelper.ENGLISH:
                radioEnglish.setChecked(true);
                break;
            case SharedPreferencesHelper.FRENCH:
                radioFrench.setChecked(true);
                break;
            case SharedPreferencesHelper.ARABIC:
                radioArabic.setChecked(true);
                break;
        }
        // save the selected language
        applyBtn.setOnClickListener(v -> {
            // english as default
            String selectedLanguage = SharedPreferencesHelper.ENGLISH;

            // change if any other language is selected
            if (radioEnglish.isChecked()) {
                selectedLanguage = SharedPreferencesHelper.ENGLISH;
            } else
            if (radioFrench.isChecked()) {
                selectedLanguage = SharedPreferencesHelper.FRENCH;
            } else if (radioArabic.isChecked()) {
                selectedLanguage = SharedPreferencesHelper.ARABIC;
            }

            // Apply language change
            languageManager.setLanguage(this, selectedLanguage);


            // Recreate activity to apply new language
            recreate();
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    /**
     * SHOW THEME SELECTION SHEET
     */
    private void showThemeSelectionSheet() {
        // Create the dialog and inflate the layout
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_theme_select, null);

        // setup views
        RadioButton radioSystem = sheetView.findViewById(R.id.radio_system);
        RadioButton radioLight = sheetView.findViewById(R.id.radio_light);
        RadioButton radioDark = sheetView.findViewById(R.id.radio_dark);
        Button applyBtn = sheetView.findViewById(R.id.btn_apply_theme);

        // Get current theme and pre-set the selection
        String currentTheme = themeManager.getTheme();
        switch (currentTheme) {
            case SharedPreferencesHelper.THEME_LIGHT:
                radioLight.setChecked(true);
                break;
            case SharedPreferencesHelper.THEME_DARK:
                radioDark.setChecked(true);
                break;
            case SharedPreferencesHelper.THEME_SYSTEM:
            default:
                radioSystem.setChecked(true);
                break;
        }

        // save the selected theme
        applyBtn.setOnClickListener(v -> {
            // system as default
            String selectedTheme = SharedPreferencesHelper.THEME_SYSTEM;

            // change if any other theme is selected
            if (radioLight.isChecked()) {
                selectedTheme = SharedPreferencesHelper.THEME_LIGHT;
            } else if (radioDark.isChecked()) {
                selectedTheme = SharedPreferencesHelper.THEME_DARK;
            }

            // Apply theme change
            themeManager.setTheme(this, selectedTheme);

            // Update the theme display
            profileThemeValue.setText(themeManager.getCurrentThemeDisplayName(this));

            // Recreate activity to apply new theme globally
            recreate();
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }


    /**
     * Inner classes to segregate validation logic and
     * allow Saripaar to apply annotation validation to only one form fields at a time.
     */
    private static class EditInfoForm {
        @NotEmpty(message = "saripaar_name_required")
        EditText name;

        @NotEmpty(message = "saripaar_email_empty")
        @Email(message = "saripaar_email_invalid")
        EditText email;

        EditInfoForm(EditText name, EditText email) {
            this.name = name;
            this.email = email;
        }
    }

    private static class ChangePasswordForm {
        @NotEmpty(message = "saripaar_password_empty")
        @Password(min = 6, message = "saripaar_password_min")
        EditText newPassword;

        @NotEmpty(message = "saripaar_confirm_password_empty")
        EditText confirmPassword;

        ChangePasswordForm(EditText newPassword, EditText confirmPassword) {
            this.newPassword = newPassword;
            this.confirmPassword = confirmPassword;
        }
    }

}