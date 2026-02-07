package com.example.budgetmanager.services;

import android.content.Context;

import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.UpdateUserInfoRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.User;
import com.example.budgetmanager.R;

public class UserService {
    private UserDao userDao;
    DatabaseHelper dbHelper;
    private Context context;


    public UserService(UserDao userDao, DatabaseHelper dbHelper, Context context) {
        this.userDao = userDao;
        this.dbHelper = dbHelper;
        this.context = context;
    }


    // update user name and email
    public Result<String> updateUserProfile(UpdateUserInfoRequest request) {
        return dbHelper.runInTransaction(() -> {
            // check if edited email exit
            User existingUser = userDao.getUserByEmail(request.getEmail());

            if (existingUser != null && existingUser.getId() != request.getId()) {
                return Result.error(context.getString(R.string.error_email_exists));
            }

            //update user
            existingUser.setEmail(request.getEmail());
            existingUser.setName(request.getName());

            // update user name
            userDao.updateUserProfile(existingUser);

            return Result.success(context.getString(R.string.msg_user_updated));
        });

    }
    
    // update user password
    public Result<String> updateUserPassword(int userId, String newPassword) {
        userDao.updateUserPassword(userId, newPassword);
        return Result.success(context.getString(R.string.msg_password_updated));
    }
}
