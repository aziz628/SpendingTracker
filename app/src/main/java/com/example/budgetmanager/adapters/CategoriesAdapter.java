package com.example.budgetmanager.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetmanager.R;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.utils.CategoryIconMapper;
import com.example.budgetmanager.utils.MenuHelper;
import com.example.budgetmanager.utils.TransactionColors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {
    // dependencies
    private List<Category> categories = new ArrayList<>();
    private  final  CategoryClickListener listener;
    private final  Context context;

    // constructor
    public CategoriesAdapter( Context context,List<Category> categories,  CategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
        this.context = context;
    }

    // listener interface for category actions callbacks
    public interface CategoryClickListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    // create the view and wrap it in a view holder
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    // bind the data to the view holder elements and set the listeners 
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // get the category from list
        Category category = categories.get(position);
        
        // Bind Name
        holder.categoryName.setText(category.getName());

        // Bind Icon
        int iconRes = CategoryIconMapper.getIconResource(category.getIconName());
        holder.categoryIcon.setImageResource(iconRes);

        // Bind Colors (Income/Expense styling)
        // Map transaction type -> ids of colors resources
        int iconColorRes = TransactionColors.getIconColor(category.getType());
        int bgColorRes = TransactionColors.getBgColor(category.getType());

        // Resolve them to raw color values
        int resolvedIconColor = ContextCompat.getColor(context, iconColorRes);
        int resolvedBgColor = ContextCompat.getColor(context, bgColorRes);
        
        // apply colors
        holder.categoryIcon.setColorFilter(resolvedIconColor);
        holder.iconContainer.setCardBackgroundColor(resolvedBgColor);

        // Setup Menu Click
        holder.category_menu.setOnClickListener(v -> showPopupMenu(v, category));
    }

    
    // Popup Menu Logic
    private void showPopupMenu(View view, Category category) {
        // set the popup menu to create under the view
        PopupMenu popup = new PopupMenu(context, view);

        // Inflate the menu (create the views)
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        // setup the menu (enable icons , color them)
        MenuHelper.setupMenuIcons(context, popup);

        // set the click listener
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                listener.onEditClick(category);
                return true;

            } else if (itemId == R.id.action_delete) {
                listener.onDeleteClick(category);
                return true;
            }

            return false;
        });


        // show the popup
        popup.show();
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    // Helper to update list
    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryIcon;
        CardView iconContainer;
        ImageView category_menu;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            category_menu = itemView.findViewById(R.id.category_menu);
            categoryName = itemView.findViewById(R.id.category_name);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            iconContainer = itemView.findViewById(R.id.icon_container);
        }
    }

}
