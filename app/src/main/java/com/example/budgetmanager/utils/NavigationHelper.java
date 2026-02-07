package com.example.budgetmanager.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.budgetmanager.R;
import com.example.budgetmanager.activities.CategoriesActivity;
import com.example.budgetmanager.activities.CreateTransactionActivity;
import com.example.budgetmanager.activities.MainActivity;
import com.example.budgetmanager.activities.ProfileActivity;
import com.example.budgetmanager.activities.StatsActivity;

/**
 * NAVIGATION HELPER - DEVELOPER GUIDE
 *
 * High level explaining
 * a navigation helper that receive the current page string and allow to highlight its icon while keeping rest as gray
 */
public class NavigationHelper {

    // enum converted at runtime to class with final objects having the navigation keys as their names and values    
    public enum NavigationPage 
    {
        HOME,
        CATEGORIES,
        PROFILE,
        STATS
    }

    public static final int NAV_ITEM_BACKGROUND = R.drawable.circular_bg_light_green;
    public static final int NAV_ITEM_COLOR = R.color.nav_icon_active;

    public static void setupNavigation(AppCompatActivity activity, NavigationPage currentPage) {
        ImageView navHome = activity.findViewById(R.id.nav_home);
        ImageView navCategories = activity.findViewById(R.id.nav_categories);
        ImageView navProfile = activity.findViewById(R.id.nav_profile);
        ImageView navStats = activity.findViewById(R.id.nav_stats);
        FrameLayout addBtn = activity.findViewById(R.id.add_transaction_button);        // Highlighting the current page icon (enum can be used in switch directly)
        switch (currentPage) {
            case HOME:
                highlightIcon(activity, navHome);
                break;
            case CATEGORIES:
                highlightIcon(activity, navCategories);
                break;
            case PROFILE:
                highlightIcon(activity, navProfile);
                break;
            case STATS:
                highlightIcon(activity, navStats);
                break;
        }

        // Setup Click Listeners (use enum name for accessing keys)
        navHome.setOnClickListener(v -> navigate(activity, MainActivity.class, NavigationPage.HOME==currentPage));
        navCategories.setOnClickListener(v -> navigate(activity, CategoriesActivity.class, NavigationPage.CATEGORIES==currentPage));
        navProfile.setOnClickListener(v -> navigate(activity, ProfileActivity.class, NavigationPage.PROFILE==currentPage));
        navStats.setOnClickListener(v -> navigate(activity, StatsActivity.class, NavigationPage.STATS==currentPage));

        addBtn.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, CreateTransactionActivity.class));
        });
    }

    // Highlight the selected icon
    private static void highlightIcon(AppCompatActivity activity, ImageView icon) {
            icon.setBackgroundResource(NAV_ITEM_BACKGROUND);
            icon.setColorFilter(activity.getColor(NAV_ITEM_COLOR));
    }

    private static void navigate(Activity activity, Class<?> targetClass, boolean isCurrent) {
        if (isCurrent) return;

        Intent intent = new Intent(activity, targetClass);
        // this flag allows to reuse same activity instance instead of creating a new one saving memory
        // main activity only benefits from this
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);

        // finish any current activity different from MainActivity 
        if (!(activity instanceof MainActivity)) {
            activity.finish();
        }
    }
}