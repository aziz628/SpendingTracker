package com.example.budgetmanager.utils;

import android.content.Context;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



/**
 * EDGE-TO-EDGE HELPER - DEVELOPER GUIDE
 *
 * PURPOSE: add root padding aside from system bars
 *
 * WHAT IS EDGE-TO-EDGE :
 * Modern Android apps draw behind the system bars (Status Bar at top, Navigation Bar at bottom).
 * If we don't add padding, our content gets hidden behind the battery icon
 * or the home gesture line.
 *
 * LOGIC:
 * 1. We define a "Base Design Padding" (24dp) that we want on all screens.
 * 2. We check the device's specific System Bar sizes (which vary by phone).
 * 3. We calculate: Total Padding = Base Padding (24dp) + System Bar Size.
 * 4. We apply this to the Root View of the Activity.
 *
 * USAGE:
 * Call EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main)) in onCreate().
 */
public class EdgeToEdgeHelper {
    // run in every Activity's onCreate
    public static void handleWindowInsets(View rootView) {
        if (rootView == null) return;

        // Calculate 24dp (standard padding) in pixels
        float density = rootView.getContext().getResources().getDisplayMetrics().density;
        int basePadding = (int) (24 * density);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply 24dp + System Bar size
            v.setPadding(
                    basePadding + systemBars.left,
                    basePadding + systemBars.top,
                    basePadding + systemBars.right,
                    basePadding + systemBars.bottom
            );
            return insets;
        });
    }
}
