package com.example.budgetmanager.services;

import android.content.Context;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.DatabaseHelper.CategoryType;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.TransactionDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.TransactionWithCategory;
import com.example.budgetmanager.dto.requests.CreateTransactionRequest;
import com.example.budgetmanager.dto.requests.UpdateTransactionRequest;
import com.example.budgetmanager.dto.results.Result;


import com.example.budgetmanager.models.Transaction;

/**
 * TRANSACTION SERVICE - DEVELOPER GUIDE
 *
 * HIGH-LEVEL EXPLANATION:
 * This service contains all business logic for transaction operations. 
 * It sits between the UI (Activities) and the data layer (DAOs),
 *  enforcing business rules 
 *
 * KEY DESIGN DECISIONS:
 * - Returns Result<T> wrapper to handle success/failure uniformly.
 * - Never exposes raw database exceptions to the UI.
 *
 * SINGLE RESPONSIBILITY:
 * - Coordinates between TransactionDao and CategoryDao
 * - Transforms DTOs ↔ Models
 *
 * LOW-LEVEL CONCEPTS REFERENCE:
 * - Result Pattern: Wraps success/failure to avoid throwing exceptions up to UI.
 * - Dependency Injection: DAOs are passed in constructor .
 */
public class TransactionService {
    private final TransactionDao transactionDao;
    private final UserDao userDao;
    private final DatabaseHelper dbHelper;
    private final Context context;
    
    public TransactionService(TransactionDao transactionDao, DatabaseHelper dbHelper, UserDao userDao, Context context) {
        this.transactionDao = transactionDao;
        this.dbHelper = dbHelper;
        this.userDao = userDao;
        this.context = context;
    }
      /**
     * Create a new transaction with validation
     */
    public Result<Transaction> createTransaction(CreateTransactionRequest request, int userId) {
        double balance = userDao.getUserBalance(userId);

        // Check if the transaction is an expense and if the user has enough balance
        if (request.getType().equals(CategoryType.EXPENSE)) {
            if (request.getAmount() > balance) {
                String message = context.getString(R.string.error_insufficient_balance_amount, balance);
                return Result.error(message);
            }
            balance -= request.getAmount();
        }else{
            balance += request.getAmount();
        }
        // convert dto to transaction model
        Transaction transaction = new Transaction(
            request.getAmount(),
            request.getType(),
            request.getNote(),
            request.getDate(),
            request.getCategoryId(),
            userId
        );

        double finalBalance = balance;
        return dbHelper.runInTransaction(() -> {
            // update balance and create transaction
            userDao.updateUserBalance(userId, finalBalance);
            long id = transactionDao.createTransaction(transaction);
            
            if (id == -1) {
                return Result.error(context.getString(R.string.error_create_transaction_failed));
            }
            
            // Set the generated ID
            transaction.setId((int) id);
            return Result.success(transaction);
        });
        
    }    /**
     * update user transaction
     * the updated fields are amount, note, and date
     */
    public Result<Transaction> updateTransaction( UpdateTransactionRequest request) {
        // Find the transaction by ID
        Transaction transaction = transactionDao.getTransactionById(request.getId());
        if (transaction == null) {
            return Result.error(context.getString(R.string.error_transaction_not_found));
        }
        // balance 
        double amountDifference  = request.getAmount() - transaction.getAmount();
        double balance = userDao.getUserBalance(transaction.getUserId());
        
        // if the type is income check if reduce is bigger than balance
        if (request.getType().equals(CategoryType.INCOME)){
            if(amountDifference > balance) {
                String message = context.getString(R.string.error_insufficient_balance_amount, balance);
                return Result.error(message);
            }
            balance += amountDifference;
        } 
        // if it's expense check if the amount difference is bigger than balance
        else {
            if(amountDifference > balance) {
                String message = context.getString(R.string.error_insufficient_balance_amount, balance);
                return Result.error(message);
            }
            balance -= amountDifference;
        }
         
        // set updated values
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setDate(request.getDate());

        // Persist to database
        double finalBalance = balance;
        return dbHelper.runInTransaction(() -> {
            userDao.updateUserBalance(transaction.getUserId(), finalBalance);
            long updatedId = transactionDao.updateTransaction(transaction);
            
            if (updatedId == -1) {
                return Result.error(context.getString(R.string.error_update_transaction_failed));
            }
            
            // Set the updated ID
            transaction.setId((int) updatedId);
            return Result.success(transaction);
        });

       
    }
    /**
     * Get balance summary for a user
     * 
     * Returns a simple array: [totalIncome, totalExpenses, balance]
     */
    public double[] getBalanceSummary(int userId) {
        double totalIncome = transactionDao.getTotalIncome(userId);
        double totalExpenses = transactionDao.getTotalExpenses(userId);
        double balance = userDao.getUserBalance(userId);
        
        return new double[]{totalIncome, totalExpenses, balance};
    }
     // delete transaction
    public Result<String> deleteTransaction(TransactionWithCategory transaction, int userId) {
        // get the balance
        double balance = userDao.getUserBalance(userId);

        //check type if it's a income and if reducing this the balance become negative
        // then show a proper message telling user he can't delete because
        // the balance  become negative

        if (transaction.getType().equals(CategoryType.INCOME)) {
            if(transaction.getAmount() > balance) {
                String message = context.getString(R.string.error_insufficient_balance_amount, balance);
                return Result.error(message);
            }
            balance -= transaction.getAmount();
        }else{
            balance += transaction.getAmount();
        }

        double finalBalance = balance;
        return dbHelper.runInTransaction(() -> {
            userDao.updateUserBalance(userId, finalBalance);
            int rowsAffected =  transactionDao.deleteTransaction(transaction.getId());
            if (rowsAffected == 0) {
                return Result.error(context.getString(R.string.error_delete_transaction_failed));
            }
            return Result.success(context.getString(R.string.msg_transaction_deleted));
        });
        
    }
    
   
}