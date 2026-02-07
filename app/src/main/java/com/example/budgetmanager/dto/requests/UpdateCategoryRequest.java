package com.example.budgetmanager.dto.requests;

public class UpdateCategoryRequest {
    // fields
    private final String name;
    private final String iconName;
    private final int id;
    

    // constructor
    public UpdateCategoryRequest(String name, String iconName, int id) {
        this.name = name;
        this.iconName = iconName;
        this.id = id;
    }

    // getters
    public String getName() { return name; }
    public String getIconName() { return iconName; }
    public int getId() { return id; }


}
