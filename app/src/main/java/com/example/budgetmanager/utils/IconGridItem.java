package com.example.budgetmanager.utils;

/**
 * Icon Grid Item Model
 * 
 * Data model for icon grid items in category selection.
 * Represents an icon with its name and selection state.
 */
public class IconGridItem {
    private String iconName;
    private boolean isSelected;

    public IconGridItem(String iconName, boolean isSelected) {
        this.iconName = iconName;
        this.isSelected = isSelected;
    }

    public String getIconName() { 
        return iconName; 
    }
    
    public boolean isSelected() { 
        return isSelected; 
    }
    
    public void setSelected(boolean selected) { 
        isSelected = selected; 
    }
}
