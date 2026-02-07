package com.example.budgetmanager.utils;

import android.content.Context;
import com.example.budgetmanager.R;
import java.util.HashMap;
import java.util.Map;

/**
 * VALIDATION HELPER - DEVELOPER GUIDE
 *
 * HIGH LEVEL EXPLANATION :
 * this validation helper take a validation error message containing tokens names
  and map the tokens to their right lang values and do concat them in a final string
 *
 * KEY DESIGN DECISIONS :
 * the helper build  map of string tokens names and their values
 * the map built at the runtime using literal strings and the tokens values
    made in compile time
 *
 * HOW IT WORKS:
 *  saripaar configured to return token name for specific errors that he return separated by newline
 *  this helper map receive the error string and split it , map the tokens name to int values
 *  use context to map to their right lang value and concat them into final string
 *
 * LOW LEVEL CONCEPTS :
 *  Resource mapping : context.getString() use the token resource id  with the current lang config
    and return the right string from a map it build with parsed string.xml at compile time
 * R class : contains the tokens values which reference many resources simultaneously like strings
     and it's used alongside other config value like language for getting final string
 */
public class ValidationHelper {

    /**
     * Static mapping of validation token names to resource IDs
     */
    private static final Map<String, Integer> TOKEN_MAP = new HashMap<>();

    // Initialize all validation token mappings
    static {
        // Email validation tokens
        TOKEN_MAP.put("saripaar_email_empty", R.string.saripaar_email_empty);
        TOKEN_MAP.put("saripaar_email_invalid", R.string.saripaar_email_invalid);
        
        // Password validation tokens
        TOKEN_MAP.put("saripaar_password_empty", R.string.saripaar_password_empty);
        TOKEN_MAP.put("saripaar_password_min", R.string.saripaar_password_min);
        
        // Name validation tokens
        TOKEN_MAP.put("saripaar_name_required", R.string.saripaar_name_required);
        TOKEN_MAP.put("saripaar_name_length", R.string.saripaar_name_length);
        
        // Amount validation tokens
        TOKEN_MAP.put("saripaar_amount_required", R.string.saripaar_amount_required);
        TOKEN_MAP.put("saripaar_amount_min", R.string.saripaar_amount_min);
        
        // Category validation tokens
        TOKEN_MAP.put("saripaar_category_required", R.string.saripaar_category_required);
        
        // Description validation tokens
        TOKEN_MAP.put("saripaar_description_required", R.string.saripaar_description_required);
        
        // Date validation tokens
        TOKEN_MAP.put("saripaar_date_required", R.string.saripaar_date_required);
    }

    /**
     * Universal function to convert error message tokens to current language strings
     * Handles single or multiple errors separated by newlines
     * Only processes token names that exist in TOKEN_MAP
     *
     * @param context Android context for resource access
     * @param errorMessage String containing token name(s) - single or separated by newlines
     * @return Localized error message(s) joined by newlines
     *
     * EXAMPLES:
     * Input:  "saripaar_email_empty" → Output: "Email cannot be empty"
     * Input:  "saripaar_email_empty\nsaripaar_email_invalid" → Output: "Email cannot be empty\nPlease enter a valid email"
     */
    public static String getLocalizedMessage(Context context, String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return "";
        }

        // Split by newline to get individual tokens
        String[] tokens = errorMessage.split("\n");
        StringBuilder localizedMessages = new StringBuilder();

        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty()) {
                // Look up token name in the map
                Integer resId = TOKEN_MAP.get(token);
                
                if (resId != null) {
                    // Token found in map - fetch localized string
                    String localizedMsg = context.getString(resId);
                    
                    // Add newline separator between multiple messages
                    if (localizedMessages.length() > 0) {
                        localizedMessages.append("\n");
                    }
                    localizedMessages.append(localizedMsg);
                    
                } else {
                    // Token not in map - skip it (don't add to output)
                    // This prevents displaying invalid or unmapped tokens
                }
            }
        }

        return localizedMessages.toString();
    }
}
