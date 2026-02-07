package com.example.budgetmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.budgetmanager.R;
import com.example.budgetmanager.models.User;

import java.util.Locale;

/**
 * SHARED PREFERENCES HELPER - DEVELOPER GUIDE
 *
 * HIGH LEVEL EXPLAINING :
 * this class allow saving data in android shared preference file
 * it allow fast retrieve on startup for saved values like :
      _ current logged-in user id
      _ selected language
      _ selected theme
 *
 * KEY DESIGN DECISIONS:
 * keep only one file for the shared preferences
 * main helper act as manager for that file read and write exposing gets and sets
    and be a container for keys names saved as constants (for safety)
 * implement separation of concerns via making separate internal classes to handle logic of each feature
 *
 * LOW LEVEL CONCEPTS :
 * SharedPreferences: xml file managed by the system we use it to store data
 * Locale : represent a specific geographical, political, or cultural region for language settings 
   and it contains language/country/variant information 
 * example : new Locale("en") this object represent english language and used in changing app language by updating configuration
 * Configuration : class describe all device configuration information
   we use it to update the app resources with new locale to change app language
 */

/**
 * MAIN  HELPER
 * Pure DAO exposing preference storage functions
 */
public class SharedPreferencesHelper {
    // SharedPreferences instance
    private final SharedPreferences preferences;

    // Preference file name
    private static final String PREF_NAME = "BudgetAppPrefs";

    // Keys for stored values
    private static final String KEY_USER_ID = "user_id";
    private static final String LANGUAGE_KEY = "selected_language";
    private static final String THEME_KEY = "theme_mode";

    // Language constants
    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String ARABIC = "ar";
    public static final String NOT_SET = "NOT_SET";

    // Theme constants
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    public SharedPreferencesHelper(Context context) {
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // RAW DAO OPERATIONS - Used by managers below
    private void setInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }
    
    private int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }
    
    private void setString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
    
    private String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
    
    private void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * USER SESSION MANAGER - Handles all user session logic
     */
    public static class UserManager {
        private SharedPreferencesHelper prefs;
        
        public UserManager(SharedPreferencesHelper prefs) {
            this.prefs = prefs;
        }
        
        public void login(User user) {
            prefs.setInt(KEY_USER_ID, user.getId());
        }
        
        public int getUserId() {
            return prefs.getInt(KEY_USER_ID, -1);
        }
        
        public boolean isLoggedIn() {
            return getUserId() != -1;
        }
        
        public void logout() {
            prefs.remove(KEY_USER_ID);
        }
    }

    /**
     * LANGUAGE MANAGER - Handles app language switching
     */
    public static class LanguageManager {
        private SharedPreferencesHelper prefs;
        
        public LanguageManager(SharedPreferencesHelper prefs) {
            this.prefs = prefs;
        }
        
        /**
         * Change app language and apply it immediately
         * used in language switcher
         */
        public void setLanguage(Context context, String language) {
            prefs.setString(LANGUAGE_KEY, language);
            applyLanguageChange(context, language);
        }
        
        /**
         * Get currently saved language
         */
        public String getLanguage() {
            return prefs.getString(LANGUAGE_KEY, NOT_SET);
        }
        
        /**
         * Apply saved language on app startup
         */
        public void applyLanguageOnStartup(Context context) {
            // set lang as sys default if not saved and get current value
            String language = initializeLanguageOnFirstLaunch();

            // set current language in config and apply it
            applyLanguageChange(context, language);
        }

        /**
         * Detect and set language on first app launch
         * Uses device language if supported, otherwise defaults to English
         */
        public String initializeLanguageOnFirstLaunch() {
            String language = getLanguage();

            // pass if language is already saved
            if (!language.equals(NOT_SET)) {
                return language;
            }
            // First launch - detect device language
            String deviceLanguage = getDeviceLanguage();
            String appLanguage = getSupportedLanguageOrDefault(deviceLanguage);

            // edit the preference to save detected language
            prefs.setString(LANGUAGE_KEY, appLanguage);
            return appLanguage;
        }
    
        
        /**
         * Internal method - actually changes Android's language configuration
         */
        private void applyLanguageChange(Context context, String language) {
            // Create locale obj with saved lang and set default for current process
            Locale locale = new Locale(language);
            Locale.setDefault(locale);

            // create configuration obj and update resources
            Configuration config = context.getResources().getConfiguration();

            // Set locale using modern API
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
                
                //  FORCE layout direction
                config.setLayoutDirection(Locale.forLanguageTag(ENGLISH)); // Force LTR
                
            } else {
                config.locale = locale;
            }
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }

        
    
    /**
     * Get the device's current language code
     */
    private String getDeviceLanguage() {
        return Locale.getDefault().getLanguage().toLowerCase();
    }
    
    /**
     * Check if device language is supported, return default if not
     */
    private String getSupportedLanguageOrDefault(String deviceLanguage) {
        switch (deviceLanguage) {
            case "en":
                return ENGLISH;
            case "fr":
                return FRENCH;
            case "ar":
                return ARABIC;
            default:
                // Device language not supported, default to English
                return ENGLISH;
        }
    }
    



    }   
    
    /**
     * THEME MANAGER - Handles theme configuration
     */
    public static class ThemeManager {
        private SharedPreferencesHelper prefs;
        
        public ThemeManager(SharedPreferencesHelper prefs) {
            this.prefs = prefs;
        }
        
        /**
         * Save theme preference and apply it immediately
         */
        public void setTheme(Context context, String theme) {
            prefs.setString(THEME_KEY, theme);
            applyTheme(context);
        }
        
        /**
         * Get currently saved theme preference
         * @return "light", "dark", or "system"
         */
        public String getTheme() {
            return prefs.getString(THEME_KEY, THEME_SYSTEM);
        }
        
        /**
         * Apply the theme using AppCompatDelegate
         * Called on startup or when theme changes
         */
        public void applyTheme(Context context) {
            String theme = getTheme();
            
            // Determine night mode based on preference
            int nightMode;
            if (theme.equals(THEME_LIGHT)) {
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (theme.equals(THEME_DARK)) {
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                // THEME_SYSTEM - follow device settings
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            
            AppCompatDelegate.setDefaultNightMode(nightMode);
        }
        
        /**
         * Get display name for the current theme (localized)
         */
        public String getCurrentThemeDisplayName(Context context) {
            String theme = getTheme();
            if (theme.equals(THEME_LIGHT)) {
                return context.getString(R.string.theme_light);
            } else if (theme.equals(THEME_DARK)) {
                return context.getString(R.string.theme_dark);
            } else {
                return context.getString(R.string.theme_system);
            }
        }
    }

    /**
     * UTILITY METHODS
     */
    public void clearAll() {
        preferences.edit().clear().apply();
    }
}

