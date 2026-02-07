package com.example.budgetmanager.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.utils.CategoryIconMapper;

import java.util.List;
import java.util.Locale.Category;

/**
 * Icon Grid Adapter
 * 
 * RecyclerView adapter for displaying category icons in a grid layout.
 * Supports icon selection with visual feedback.
 */
public class IconGridAdapter extends RecyclerView.Adapter<IconGridAdapter.IconViewHolder> {
    private List<IconGridItem> iconItems;
    private OnIconClickListener listener;
    private String categoryType;

    /**
     * Interface for handling icon click events
     */
    public interface OnIconClickListener {
        void onIconClick(String iconName);
    }

    public IconGridAdapter(List<IconGridItem> iconItems, OnIconClickListener listener, String categoryType) {
        this.iconItems = iconItems;
        this.listener = listener;
        this.categoryType = categoryType;
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_grid, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        IconGridItem item = iconItems.get(position);
        holder.bind(item, listener, categoryType);
    }

    @Override
    public int getItemCount() {
        return iconItems.size();
    }

    /**
     * ViewHolder for icon grid items
     */
    static class IconViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconView;
        private CardView iconContainer;

        public IconViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.category_icon);
            iconContainer = itemView.findViewById(R.id.icon_container);
        }

        public void bind(IconGridItem item, OnIconClickListener listener, String categoryType) {
            // Set icon
            int iconRes = CategoryIconMapper.getIconResource(item.getIconName());
            iconView.setImageResource(iconRes);
            int iconColor;
            int backgroundColor;

            // Set selection state with semantic colors
            if (item.isSelected()) {
                if(categoryType.equals(CategoryType.INCOME)){
                    backgroundColor = R.color.primary_green_light;
                    iconColor = R.color.primary_green;
                }else {
                    backgroundColor = R.color.expense_bg;
                    iconColor = R.color.expense_text;
                }   
            }else {
                backgroundColor = R.color.surface_white;
                iconColor = R.color.icon_default;
            }

            iconContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), backgroundColor));
            iconView.setColorFilter(ContextCompat.getColor(itemView.getContext(), iconColor));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconClick(item.getIconName());
                }
            });
        }
    }
}
