package com.example.budgetmanager.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetmanager.R;
import com.example.budgetmanager.dto.CategoryTotal;
import com.example.budgetmanager.utils.CategoryLocalizer;
import com.example.budgetmanager.utils.CategoryIconMapper;
import com.example.budgetmanager.utils.TransactionColors;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.LegendViewHolder> {
    private List<CategoryTotal> categoryTotals;
    private Context context;
    private double totalAmount;
    private DecimalFormat currencyFormatter;
    private String transactionType; // "income" or "expense"
    
    public LegendAdapter(Context context, List<CategoryTotal> categoryTotals, String transactionType) {
        this.context = context;
        this.categoryTotals = categoryTotals;
        this.transactionType = transactionType;
        
        // Calculate total for percentage calculation
        this.totalAmount = 0;
        for (CategoryTotal categoryTotal : categoryTotals) {
            this.totalAmount += categoryTotal.getTotal();
        }
          // Setup currency formatter - force US locale to avoid Arabic numerals
        this.currencyFormatter = new DecimalFormat("#,##0.00");
        this.currencyFormatter.setDecimalFormatSymbols(
                java.text.DecimalFormatSymbols.getInstance(Locale.US)
        );
    }

    @NonNull
    @Override
    public LegendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_legend, parent, false);
        return new LegendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LegendViewHolder holder, int position) {
        CategoryTotal categoryTotal = categoryTotals.get(position);
        

        holder.legendName.setText(categoryTotal.getCategoryName());
        
        // Set amount
        String formattedAmount = "$" + currencyFormatter.format(categoryTotal.getTotal());
        holder.legendAmount.setText(formattedAmount);
          // Set percentage - force US locale to avoid Arabic numerals
        double percentage = totalAmount > 0 ? (categoryTotal.getTotal() / totalAmount) * 100 : 0;
        holder.legendPercentage.setText(String.format(Locale.US, "%.1f%%", percentage));
        
        // Set icon
        setIconForCategory(holder.legendIcon, categoryTotal.getIconName());
    }    @Override
    public int getItemCount() {
        return categoryTotals.size();
    }    private void setIconForCategory(ImageView iconView, String iconName) {
        // Use CategoryIconMapper like other adapters
        int iconRes = CategoryIconMapper.getIconResource(iconName);
        iconView.setImageResource(iconRes);
        
        // Set icon color based on transaction type (proper color)
        int colorRes = TransactionColors.getIconColor(transactionType);
        iconView.setColorFilter(context.getResources().getColor(colorRes, context.getTheme()));
    }
    
    public void updateData(List<CategoryTotal> newCategoryTotals, String newTransactionType) {
        this.categoryTotals = newCategoryTotals;
        this.transactionType = newTransactionType;
        this.totalAmount = 0;
        for (CategoryTotal categoryTotal : newCategoryTotals) {
            this.totalAmount += categoryTotal.getTotal();
        }
        notifyDataSetChanged();
    }

    static class LegendViewHolder extends RecyclerView.ViewHolder {
        ImageView legendIcon;
        TextView legendName;
        TextView legendAmount;
        TextView legendPercentage;

        LegendViewHolder(View itemView) {
            super(itemView);
            legendIcon = itemView.findViewById(R.id.legend_icon);
            legendName = itemView.findViewById(R.id.legend_name);
            legendAmount = itemView.findViewById(R.id.legend_amount);
            legendPercentage = itemView.findViewById(R.id.legend_percentage);
        }
    }
}
