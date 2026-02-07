package com.example.budgetmanager.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.budgetmanager.R;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.utils.CategoryIconMapper;
import com.example.budgetmanager.utils.TransactionColors;

import java.util.List;

/**
 * Custom adapter for displaying categories with icons in a Spinner
 * 
 * HIGH-LEVEL EXPLANATION:
 * This adapter displays each category with its icon and name in the spinner dropdown.
 * It uses CategoryIconMapper to get the correct icon for each category.
 * 
 * KEY DESIGN DECISIONS:
 * - Extends ArrayAdapter<Category> for compatibility with Spinner
 * - Overrides both getView() and getDropDownView() for consistent rendering
 * - Uses semantic color tokens (text_primary) for theme support
 * - Reuses spinner_category_item.xml layout for each item
 * 
 * LOW-LEVEL CONCEPTS:
 * - getView(): Called when displaying the selected item in the closed spinner
 * - getDropDownView(): Called for each item in the open dropdown list
 * - convertView: Recycled view for performance (ViewHolder pattern)
 */
public class CategorySpinnerAdapter extends ArrayAdapter<Category> {
    private final Context context;
    private final List<Category> categories;
    private final LayoutInflater inflater;

    public CategorySpinnerAdapter(@NonNull Context context, List<Category> categories) {
        super(context, 0, categories);
        this.context = context;
        this.categories = categories;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Called for the selected item display
        return createCategoryView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        // Called for each item in the dropdown list
        return createCategoryView(position, convertView, parent);
    }

    /**
     * Creates or reuses a view for displaying a category item
     */
    private View createCategoryView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_category_item, parent, false);
        }

        Category category = categories.get(position);

        // Set icon using CategoryIconMapper
        ImageView iconView = convertView.findViewById(R.id.category_icon);
        int iconRes = CategoryIconMapper.getIconResource(category.getIconName());
        iconView.setImageResource(iconRes);

        // get category icon color token and its resolved value
        int iconColorRes = TransactionColors.getIconColor(category.getType());
        int resolvedIconColor = ContextCompat.getColor(context, iconColorRes);
        // apply color
        iconView.setColorFilter(resolvedIconColor);

        // Set category name
        TextView nameView = convertView.findViewById(R.id.category_name);
        nameView.setText(category.getName());

        return convertView;
    }
}
