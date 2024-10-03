//package com.example.splitpal;
//
//import android.app.DatePickerDialog;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//public class addGroupExpense extends AppCompatActivity {
//
//    TextView head;
//    EditText amount, date, category;
//    Spinner PaidBy;
//    Button addExpense;
//    Calendar calendar;
//
//    FirebaseFirestore db;
//
//    ArrayList<String> arrayList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.addgroupexpense);
//
//        initializeViews();
//        setHeaderText();
//        setupSpinner();
//
//        date.setOnClickListener(v -> showDatePickerDialog());
//
//        addExpense.setOnClickListener(v -> addExpenseToGroup());
//    }
//
//    private void initializeViews() {
//        calendar = Calendar.getInstance();
//        head = findViewById(R.id.head);
//        amount = findViewById(R.id.amount);
//        date = findViewById(R.id.date);
//        category = findViewById(R.id.category);
//        PaidBy = findViewById(R.id.PaidBy);
//        addExpense = findViewById(R.id.btn_addexpense);
//
//        // Initialize Firestore
//        db = FirebaseFirestore.getInstance();
//    }
//
//    private void setHeaderText() {
//        String tem = head.getText().toString() + " " + getIntent().getStringExtra("groupName");
//        head.setText(tem);
//    }
//
//    private void setupSpinner() {
//        arrayList = getIntent().getStringArrayListExtra("members");
//        arrayList.add(0, "Who Pays ?");  // Add item for spinner hint
//
//        ArrayAdapter<String> paidByAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList) {
//            @Override
//            public boolean isEnabled(int position) {
//                // Hide keyboard
//                Utils.hideKeyboard(addGroupExpense.this);
//                return position != 0;
//            }
//
//            @Override
//            public View getDropDownView(int position, View convertView, ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
//                return view;
//            }
//
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                View view = super.getView(position, convertView, parent);
//                TextView tv = (TextView) view;
//                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
//                return view;
//            }
//        };
//        paidByAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
//        PaidBy.setAdapter(paidByAdapter);
//    }
//
//    private void showDatePickerDialog() {
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        // Hide keyboard
//        Utils.hideKeyboard(addGroupExpense.this);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    calendar.set(Calendar.YEAR, selectedYear);
//                    calendar.set(Calendar.MONTH, selectedMonth);
//                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
//                    updateDateEditText();
//                }, year, month, day);
//
//        // Set the maximum date to today's date
//        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
//
//        datePickerDialog.show();
//    }
//
//    private void updateDateEditText() {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
//        date.setText(sdf.format(calendar.getTime()));
//    }
//
//    private void addExpenseToGroup() {
//        String groupId = getIntent().getStringExtra("groupId");
//        String expenseAmount = amount.getText().toString().trim();
//        String expenseDate = date.getText().toString().trim();
//        String expenseCategory = category.getText().toString().trim();
//        String paidBy = PaidBy.getSelectedItem().toString().trim();
//
//        if (groupId != null && !expenseAmount.isEmpty() && !expenseDate.isEmpty() && !expenseCategory.isEmpty() && !paidBy.equals("Who Pays ?")) {
//            Map<String, Object> expenseData = new HashMap<>();
//            expenseData.put("amount", expenseAmount);
//            expenseData.put("date", expenseDate);
//            expenseData.put("category", expenseCategory);
//            expenseData.put("paidby", paidBy);
//
//            db.collection("groups")
//                    .document(groupId)
//                    .collection("expenses")
//                    .add(expenseData)
//                    .addOnSuccessListener(documentReference -> {
//                        // Expense added successfully
//                        Utils.showToast(addGroupExpense.this, "Expense added successfully");
//                        finish(); // Close the activity or reset the fields if needed
//                    })
//                    .addOnFailureListener(e -> {
//                        // Handle the error
//                        Utils.showToast(addGroupExpense.this, "Error adding expense: " + e.getMessage());
//                    });
//        } else {
//            Utils.showToast(addGroupExpense.this, "Please fill in all the details.");
//        }
//    }
//
//
//}
package com.example.splitpal;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class addGroupExpense extends AppCompatActivity {

    TextView head;
    EditText amount, date, category;
    Spinner PaidBy;
    Button addExpense;
    Calendar calendar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    String groupId;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.addgroupexpense);

        initializeViews();
        setHeaderText();
        setupSpinner();

         groupId = getIntent().getStringExtra("groupId");

        updateDateEditText();
        date.setOnClickListener(v -> showDatePickerDialog());

        addExpense.setOnClickListener(v -> addExpenseToGroup());
    }

    private void initializeViews() {
        calendar = Calendar.getInstance();
        head = findViewById(R.id.head);
        amount = findViewById(R.id.amount);
        date = findViewById(R.id.date);
        category = findViewById(R.id.category);
        PaidBy = findViewById(R.id.PaidBy);
        addExpense = findViewById(R.id.btn_addexpense);
        progressBar=findViewById(R.id.progressbar);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setHeaderText() {
        String tem = head.getText().toString() + " " + getIntent().getStringExtra("groupName");
        head.setText(tem);
    }

    private void setupSpinner() {
        arrayList = getIntent().getStringArrayListExtra("members");
        arrayList.add(0, "Who Pays ?");  // Add item for spinner hint

        ArrayAdapter<String> paidByAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, arrayList) {
            @Override
            public boolean isEnabled(int position) {
                // Hide keyboard
                Utils.hideKeyboard(addGroupExpense.this);
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
        paidByAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        PaidBy.setAdapter(paidByAdapter);
    }

    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Hide keyboard
        Utils.hideKeyboard(addGroupExpense.this);

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


    private boolean isClickable = true; // Flag to prevent multiple clicks

    private void addExpenseToGroup() {
        if (isClickable) {
            progressBar.setVisibility(View.VISIBLE);

            isClickable = false; // Disable further clicks

            String userId = mAuth.getCurrentUser().getUid(); // Get the current user's UID
            String expenseAmount = amount.getText().toString().trim();
            String expenseDate = date.getText().toString().trim();
            String expenseCategory = category.getText().toString().trim();
            String paidBy = PaidBy.getSelectedItem().toString().trim();

            if (expenseAmount.isEmpty() || expenseDate.isEmpty() || PaidBy.getSelectedItemPosition()==0) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                isClickable = true;
                return;
            }

            if (expenseCategory.isEmpty()) {
                expenseCategory = "-";
            }

            if(expenseAmount.equals("0")||expenseAmount.equals("0.00")||expenseAmount.equals("0.0")){
                amount.setError("Please Enter a Valid Number!");
                progressBar.setVisibility(View.GONE);
                amount.requestFocus();
                return;
            }


            if (!expenseAmount.contains(".")) {
                expenseAmount = String.format("%s.00", expenseAmount);
            }


                Map<String, Object> expenseData = new HashMap<>();
                expenseData.put("amount", expenseAmount);
                expenseData.put("date", expenseDate);
                expenseData.put("category", expenseCategory);
                expenseData.put("paidby", paidBy);

                GroupExpense expense = new GroupExpense(expenseAmount,expenseDate,expenseCategory,paidBy);

                db.collection("users")
                        .document(userId) // Path to the user's document
                        .collection("groups")
                        .document(groupId) // Path to the group's document
                        .collection("expenses")
                        .add(expense)
                        .addOnSuccessListener(documentReference -> {
                            // Expense added successfully
                            Utils.showToast(addGroupExpense.this, "Expense added successfully");
                            finish(); // Close the activity or reset the fields if needed
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                            Utils.showToast(addGroupExpense.this, "Error adding expense: " + e.getMessage());
                        })
                        .addOnCompleteListener(task -> {
                            // Re-enable the button after a delay
                            new Handler(Looper.getMainLooper()).postDelayed(() -> isClickable = true, 1000);
                        });


            }
        }
    }

