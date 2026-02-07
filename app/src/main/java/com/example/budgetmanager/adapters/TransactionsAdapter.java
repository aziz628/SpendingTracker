package com.example.budgetmanager.adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.budgetmanager.R;
import com.example.budgetmanager.dto.TransactionWithCategory;
import com.example.budgetmanager.models.Transaction;
import com.example.budgetmanager.utils.CategoryIconMapper;
import com.example.budgetmanager.utils.MenuHelper;
import com.example.budgetmanager.utils.TransactionColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TRANSACTIONS ADAPTER - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This adapter binds a list of TransactionWithCategory DTOs to RecyclerView items.
 * It uses CategoryIconMapper to display the correct icon and TransactionColors to
 * apply income/expense color schemes.
 *
 * KEY DESIGN DECISIONS:
 * - Works with TransactionWithCategory DTO (not pure Transaction model) because
 *   we need the categoryName for icon mapping.
 * - Uses utility classes (CategoryIconMapper, TransactionColors) for consistency.
 * - Immutable list updates via updateTransactions() method.
 *
 * DATA FLOW:
 * MainActivity → loads List<TransactionWithCategory> from DAO → passes to adapter
 * → adapter binds each item with icon/color mapping → displays in RecyclerView
 *
 * LOW-LEVEL CONCEPTS REFERENCE:
 * - ViewHolder pattern: Caches view references to avoid findViewById() on every bind.
 * - notifyDataSetChanged(): Tells RecyclerView to re-render all items (simple but not optimal).
 * - ColorFilter: Tints the icon drawable at runtime (allows white icons to be colored dynamically).
 * - Colors resources: the color tokens from colors.xml are parsed in compile time and stored in the R class 
    as static final integers. These IDs reference the color definitions in the resources folder
    and are used to retrieve the actual color values at runtime.
 * - ContextCompat is a "helper" that receive context and id and checks the user's Android version and decide to call right version of  id-color mapper 
 */
public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    
    private List<TransactionWithCategory> transactions = new ArrayList<>();
    private  final  TransactionClickListener listener;
    private final  Context context;


    // constructor
    public  TransactionsAdapter(Context context, List<TransactionWithCategory> transactions, TransactionClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
    }

    // listener interface for transaction actions callbacks
    public interface TransactionClickListener {
        void onEditClick(TransactionWithCategory transaction);
        void onDeleteClick(TransactionWithCategory transaction);
    }

    // create the view and wrap it in a view holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the transaction_item layout
        View view = LayoutInflater.from(context)
                .inflate(R.layout.transaction_item, parent, false);
        return new ViewHolder(view);
    }
    
    // bind the db data to the view holder which is a container of the xml parsed view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get transaction by its screen position (index in array)
        TransactionWithCategory transaction = transactions.get(position);
        
        // Map category name -> icon resource
        int iconRes = CategoryIconMapper.getIconResource(transaction.getCategoryIcon());
        holder.categoryIcon.setImageResource(iconRes); // set an image resource to a cardview component
        
        // Map transaction type -> ids of colors resources  
        String transactionType = transaction.getType();
        int iconColorRes = TransactionColors.getIconColor(transactionType);
        int bgColorRes = TransactionColors.getBgColor(transactionType);

        // Resolve them to raw color values (map the ids to the raw colors )
        int resolvedIconColor = ContextCompat.getColor(context, iconColorRes);
        int resolvedBgColor = ContextCompat.getColor(context, bgColorRes);

        // Apply colors
        holder.categoryIcon.setColorFilter(resolvedIconColor);
        holder.iconContainer.setCardBackgroundColor(resolvedBgColor);
        
        // Bind text data
        holder.transactionName.setText(transaction.getCategoryName());
        holder.transactionDate.setText(transaction.getDate());
          // Format amount with +/- prefix - force US locale to avoid Arabic numerals
        String prefix = transactionType.equalsIgnoreCase("income") ? "+ $" : "- $";
        holder.transactionAmount.setText(
            String.format(Locale.US, "%s%.2f", prefix, transaction.getAmount())
        );
        
        // Set amount color
        holder.transactionAmount.setTextColor(resolvedIconColor);
        
        // Bind note (if any)
        if (transaction.getNote() != null && !transaction.getNote().isEmpty()) {
            holder.transactionNote.setText(transaction.getNote());
            holder.transactionNote.setVisibility(View.VISIBLE);
        } else {
            holder.transactionNote.setVisibility(View.GONE);
        }

        // set click listener for the menu icon
        holder.menuIcon.setOnClickListener(v -> {
            // pass the clicked view and the transaction to the listener
            showPopupMenu(v, transaction);
        });
    }

    private void showPopupMenu(View view, TransactionWithCategory transaction) {
        // Create PopupMenu attached to the view (the 3 dots)
        PopupMenu popup = new PopupMenu(context, view);

        // Inflate the menu resource
        popup.inflate(R.menu.popup_menu);

        // setup the menu (enable icons , color them)
        MenuHelper.setupMenuIcons(context, popup);

        // apply event delegation on the menu item click
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // map the menu item id to the listener method
                if (itemId == R.id.action_edit) {
                    listener.onEditClick(transaction);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDeleteClick(transaction);
                    return true;
                }
                return false;

        });

        // Show the popup menu linked to the view  specified during construction
        popup.show();
    }
    
    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    /**
     * Update the adapter's data and refresh the UI
     */
    public void updateTransactions(List<TransactionWithCategory> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder - caches view references for performance
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView iconContainer;
        ImageView categoryIcon;
        ImageView menuIcon;

        TextView transactionName;
        TextView transactionDate;
        TextView transactionAmount;
        TextView transactionNote;
        
        ViewHolder(@NonNull View itemView) {
            // pass the view to the super constructor to be saved in the ViewHolder
            super(itemView);
            
            // Cache view references (IDs from transaction_item.xml)
            iconContainer = itemView.findViewById(R.id.icon_container);
            categoryIcon = itemView.findViewById(R.id.category_icon);
            transactionName = itemView.findViewById(R.id.transaction_name);
            transactionDate = itemView.findViewById(R.id.transaction_date);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
            transactionNote = itemView.findViewById(R.id.transaction_note);
            menuIcon = itemView.findViewById(R.id.transaction_menu);
        }
    }
}