package com.example.budgetmanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.budgetmanager.R;
import com.example.budgetmanager.database.DatabaseHelper;
import com.example.budgetmanager.database.dao.CategoryDao;
import com.example.budgetmanager.database.dao.TransactionDao;
import com.example.budgetmanager.database.dao.UserDao;
import com.example.budgetmanager.dto.requests.CreateTransactionRequest;
import com.example.budgetmanager.dto.requests.UpdateTransactionRequest;
import com.example.budgetmanager.dto.results.Result;
import com.example.budgetmanager.models.Category;
import com.example.budgetmanager.models.Transaction;
import com.example.budgetmanager.services.TransactionService;
import com.example.budgetmanager.utils.EdgeToEdgeHelper;
import com.example.budgetmanager.utils.SharedPreferencesHelper;
import com.example.budgetmanager.utils.ValidationHelper;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.DecimalMin;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpdateTransactionActivity extends AppCompatActivity implements Validator.ValidationListener {

    //  DEPENDENCIES
    private TransactionService transactionService;
    private Validator validator;
    private int transactionId;
    private Transaction currentTransaction;
    private final Calendar calendar = Calendar.getInstance();

    // Validation token constants (must be compile-time constants for annotations)
    private static final String TOKEN_AMOUNT_REQUIRED = "saripaar_amount_required";
    private static final String TOKEN_AMOUNT_MIN = "saripaar_amount_min";

    //  UI ELEMENTS (with Saripaar annotations)
    @NotEmpty(message = TOKEN_AMOUNT_REQUIRED)
    @DecimalMin(value = 0.01, message = TOKEN_AMOUNT_MIN)
    private EditText amountInput;

    private EditText dateInput;
    private EditText noteInput;
    private AppCompatButton saveButton;
    private ImageView backButton;

    // Error TextViews
    private TextView amountError,update_transaction_form_error;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_transaction);
        // Handle system bar padding
        EdgeToEdgeHelper.handleWindowInsets(findViewById(R.id.main));

        // Initialize dependencies
        initializeDependencies();

        // Setup UI
        setupViews();

        // Setup listeners
        setupListeners();
    }

    /**
     * Initialize DAOs, Services, and Validator
     */
    private void initializeDependencies() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        TransactionDao transactionDao = new TransactionDao(dbHelper);
        UserDao userDao= new UserDao(dbHelper);
        transactionService = new TransactionService(transactionDao, dbHelper, userDao, this);

        // get transaction id from intent
        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);

        // get current transaction
        currentTransaction = transactionDao.getTransactionById(transactionId);

        // Initialize Saripaar validator
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    /**
     * Setup view references
     */
    private void setupViews() {
        // Find views by ID
        update_transaction_form_error=findViewById(R.id.update_transaction_form_error);
        amountInput = findViewById(R.id.amount_input);
        amountError = findViewById(R.id.amount_error);
        dateInput = findViewById(R.id.date_input);
        noteInput = findViewById(R.id.note_input);
        saveButton = findViewById(R.id.save_button);
        backButton = findViewById(R.id.back_button);


        // prefill the fields with the current transaction data
        amountInput.setText(String.valueOf(currentTransaction.getAmount()));
        dateInput.setText(currentTransaction.getDate());
        noteInput.setText(currentTransaction.getNote());
    }

    /**
     * Setup click listeners
     */
    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Date picker
        dateInput.setOnClickListener(v -> showDatePicker());

        // Save button - triggers validation
        saveButton.setOnClickListener(v -> {
            clearErrors();
            validator.validate();
        });
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        amountError.setText("");
        amountError.setVisibility(View.GONE);
        update_transaction_form_error.setVisibility(View.GONE);
    }


    /**
     * Setup and Show date picker
     * when input entered the listener callback will run, it receive the selected date and updates the input
     */
    private void showDatePicker() {
        // Get the current date text (YYYY-MM-DD)
        String currentDate = dateInput.getText().toString();

        // Split it
        String[] parts = currentDate.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1; // DatePicker uses 0-11 for months
        int day = Integer.parseInt(parts[2]);

        // setup date picker
        DatePickerDialog picker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format back to DD-MM-YYYY
            String newDate = String.format(Locale.US, "%04d-%02d-%02d",
                    selectedYear, selectedMonth + 1,selectedDay ); // m+1 since picker return month index 0-11

            // Update the input
            dateInput.setText(newDate);

        }, year, month, day);

        picker.show();
    }



    //  SARIPAAR VALIDATION CALLBACKS

    @Override
    public void onValidationSucceeded() {
        // Extract form data
        double amount = Double.parseDouble(amountInput.getText().toString());
        String note = noteInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();



        // Build request DTO
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                amount,
                note,
                date,
                transactionId,
                currentTransaction.getType()
        );

        // Call service
        Result<Transaction> result = transactionService.updateTransaction(request);

        if (result.isSuccess()) {
            Toast.makeText(this, getString(R.string.msg_transaction_saved), Toast.LENGTH_SHORT).show();
            finish(); // Return to MainActivity (which will refresh)
        } else {
            update_transaction_form_error.setVisibility(View.VISIBLE);
            update_transaction_form_error.setText(result.getError());
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        // Display validation errors in TextViews
        for (ValidationError error : errors) {
            View view = error.getView();
            String errorMessage = error.getCollatedErrorMessage(this);
            
            // Convert token IDs to localized strings
            String localizedMsg = ValidationHelper.getLocalizedMessage(this, errorMessage);

            // Show error in custom TextView
            if (view.getId() == R.id.amount_input) {
                amountError.setText(localizedMsg);
                amountError.setVisibility(View.VISIBLE);
            }
        }
    }

    }