package com.example.splitpal;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addexpense extends AppCompatActivity {

    private EditText date;
    private EditText categoryEditText, amountEditText;
    private Spinner paymentMethodSpinner;
    private Calendar calendar;
    private Button addExpenseButton;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addexpense);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            // Redirect to login if user is not authenticated
            Toast.makeText(this, "Please log in to add expenses.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Set up reference to user's expenses node
        databaseReference = FirebaseDatabase.getInstance().getReference("expenses").child(user.getUid());

        // Initialize UI elements
        date = findViewById(R.id.date);
        categoryEditText = findViewById(R.id.category);  // Changed Spinner to EditText
        paymentMethodSpinner = findViewById(R.id.payment_method);
        calendar = Calendar.getInstance();
        addExpenseButton = findViewById(R.id.btn_addexpense);
        amountEditText = findViewById(R.id.amount);
        progressBar = findViewById(R.id.progressbar);

        // Set up Payment Method Spinner with custom layout and hint
        final String[] paymentMethods = getResources().getStringArray(R.array.payment_methods);

        ArrayAdapter<String> paymentMethodAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, paymentMethods) {
            @Override
            public boolean isEnabled(int position) {
                //For hide keyboard
                Utils.hideKeyboard(addexpense.this);
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        paymentMethodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentMethodAdapter);

        // Set default date to today's date and set up date picker
        updateDateEditText();
        date.setOnClickListener(v -> showDatePickerDialog());

        addExpenseButton.setOnClickListener(v -> addExpenseToDatabase());



    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

//    For hide keyboard
        Utils.hideKeyboard(addexpense.this);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    updateDateEditText();
                }, year, month, day);

        // Set the maximum date to today's date
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }


    private void updateDateEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        date.setText(sdf.format(calendar.getTime()));
    }


    private void addExpenseToDatabase() {
        progressBar.setVisibility(View.VISIBLE);

        String amount = amountEditText.getText().toString();
        String date = this.date.getText().toString();
        String category = categoryEditText.getText().toString();
        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();

        if (amount.isEmpty() || date.isEmpty() || paymentMethodSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (category.isEmpty()) {
            category = "-";
        }


            String input = amountEditText.getText().toString();

            if (!input.contains(".")) {
                amount = String.format("%s.00", input);
            }

            if(!Utils.isValidAmount(input)){
                amountEditText.setError("Please Enter Valid Amount! ");
                progressBar.setVisibility(View.GONE);
                amountEditText.requestFocus();
                return;
            }



        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the 'expenses' subcollection within 'users/{userId}'
        DocumentReference userDocRef = db.collection("users").document(userId);
        CollectionReference expensesRef = userDocRef.collection("PersonalExpenses");

        // Create an Expense object
        Expense expense = new Expense(amount, date, category, paymentMethod);

        // Add the expense document to the 'expenses' subcollection
        expensesRef.add(expense).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // Expense Added
                clearFields();
            } else {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void clearFields() {
        amountEditText.setText("");
        date.setText("");
        categoryEditText.setText("");  // Clear EditText for category
        paymentMethodSpinner.setSelection(0);

        finish();
    }

}
