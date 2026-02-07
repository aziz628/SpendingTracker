package com.example.budgetmanager.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.example.budgetmanager.R;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class to handle PopupMenu styling (forcing icons and applying colors).
 */
public class MenuHelper {

    public static void setupMenuIcons(Context context, PopupMenu popup) {
        //  Force icons to show using reflection (Required for API < 29)
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception e) {
            // Fallback: if reflection fails, menu will just show without icons
            e.printStackTrace();
        }

        //  Apply colors to icons based on their ID
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem item = popup.getMenu().getItem(i);
            Drawable icon = item.getIcon();

            if (icon != null) {
                // Map ID to color token
                int colorRes = (item.getItemId() == R.id.action_delete)
                        ? R.color.error_red
                        : R.color.primary_green;

                // Apply tint using DrawableCompat for backward compatibility
                Drawable wrappedIcon = DrawableCompat.wrap(icon);
                DrawableCompat.setTint(wrappedIcon, ContextCompat.getColor(context, colorRes));
                item.setIcon(wrappedIcon);
            }
        }
    }
}