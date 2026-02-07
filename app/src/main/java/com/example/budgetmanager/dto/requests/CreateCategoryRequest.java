package com.example.budgetmanager.dto.requests;
/**
 * CREATE CATEGORY REQUEST DTO - DEVELOPER GUIDE
 * PURPOSE: Encapsulate data for category creation requests
 *
 * IMMUTABLE: Fields are final, only getters provided
 */
public class CreateCategoryRequest {
    // fields
    private final String name;
    private final String iconName;
    private final String type;
    private final int userId;

    // constructor
    public  CreateCategoryRequest(String name, String iconName, String type, int userId) {
        this.name = name;
        this.iconName = iconName;
        this.type = type;
        this.userId = userId;
    }

    // getters
    public String getName() { return name; }
    public String getIconName() { return iconName; }
    public String getType() { return type; }
    public int getUserId() { return userId; }

}
