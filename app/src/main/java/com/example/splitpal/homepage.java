package com.example.splitpal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class homepage extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    ImageButton logout,profile;
    TextView greeting,tvheadexp,txtimgntfnd;
    TableLayout tableLayout;
    FirebaseDatabase database;
    DatabaseReference expensesRef;
    ProgressBar progressBar;
    ImageView datanotfoundimg;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        logout = findViewById(R.id.logout);
        greeting = findViewById(R.id.greetingmsg);
        tableLayout = findViewById(R.id.tableLayout);
        TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        tvheadexp = findViewById(R.id.tvheadexp);
        progressBar = findViewById(R.id.progressbar);
        profile=findViewById(R.id.profile);
        txtimgntfnd=findViewById(R.id.txtimgntfnd);
        datanotfoundimg = findViewById(R.id.datanotfoundimg);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();

        if (user != null) {
            // Set up the reference to the current user's expenses
            expensesRef = database.getReference("expenses").child(user.getUid());

            greeting.setText(getGreetingMessage());

            fetchExpensesData(totalAmountTextView);

        } else {
            // Redirect to login if user is null
            Intent intent = new Intent(getApplicationContext(), loginpage.class);
            startActivity(intent);
            finishAffinity();
        }
        clickgroupexpenses();
        clickLogout();
        clickAddExpense();
        clickprofile();
    }

    private void clickprofile() {
        profile.setOnClickListener(v -> {
            // Create an AlertDialog Builder
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());

            // Inflate the custom layout
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.profile, null);

            // Set the view for the dialog
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);

            TextView Tvemail = dialogView.findViewById(R.id.tvmail);
            TextView Tvphone = dialogView.findViewById(R.id.tvphone);
            TextView Tvuid = dialogView.findViewById(R.id.tvuid);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userid = user.getUid();

            DocumentReference docRef = db.collection("users").document(userid);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String email = documentSnapshot.getString("Email");
                    String phoneNumber = documentSnapshot.getString("Phone Number");
                    String uid = documentSnapshot.getString("UID");

                    // Check which one is null and set visibility accordingly
                    if (email != null && !email.isEmpty()) {
                        Tvemail.setText("Email: " + email);
                        Tvphone.setVisibility(View.GONE);  // Hide phone number field
                    } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        Tvphone.setText("Phone Number: " + phoneNumber);
                        Tvemail.setVisibility(View.GONE);  // Hide email field
                    }

                    // Set UID
                    Tvuid.setText("UID: " + uid);
                }

                // Create and show the dialog
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            });
        });
    }


    private void clickgroupexpenses() {
        findViewById(R.id.groupdetails).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), groupdata.class);
            startActivity(intent);
        });
    }

    private void fetchExpensesData(TextView totalAmountTextView) {
//        Validation that not require
        if (mAuth.getCurrentUser() == null) {
            finishAffinity();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expensesRef = db.collection("users").document(userId).collection("PersonalExpenses");

        expensesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(homepage.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                return;
            }

            tableLayout.removeAllViews(); // Clear existing rows
            int count = 0;
            double totalAmount = 0.0;

            TableRow headerRow = (TableRow) LayoutInflater.from(homepage.this).inflate(R.layout.table_row_header, tableLayout, false);
            tableLayout.addView(headerRow);

            progressBar.setVisibility(View.GONE);

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    Expense expense = document.toObject(Expense.class);



                    if (expense != null) {
                        TableRow row = (TableRow) LayoutInflater.from(homepage.this).inflate(R.layout.table_row, tableLayout, false);

                        TextView amountTextView = row.findViewById(R.id.amount_text_view);
                        TextView dateTextView = row.findViewById(R.id.date_text_view);
                        TextView categoryTextView = row.findViewById(R.id.category_text_view);
                        TextView paymentMethodTextView = row.findViewById(R.id.payment_method_text_view);

                        amountTextView.setText(expense.getAmount());
                        dateTextView.setText(expense.getDate());
                        categoryTextView.setText(expense.getCategory());
                        paymentMethodTextView.setText(expense.getPaymentMethod());

                        tableLayout.addView(row);
                        row.setOnClickListener(v -> showUpdateDeleteDialog(document.getId(), expense, expensesRef));

                        totalAmount += Double.parseDouble(expense.getAmount());
                        count++;
                    }
                }
            }

            if (count == 0) {
                datanotfoundimg.setVisibility(View.VISIBLE);
                txtimgntfnd.setVisibility(View.VISIBLE);
                headerRow.setVisibility(View.GONE);
                totalAmountTextView.setVisibility(View.GONE);
                totalAmountTextView.setText("");
            } else {
                totalAmountTextView.setText("Total: ₹ " + String.format("%.2f", totalAmount));
                tvheadexp.setVisibility(View.VISIBLE);
                datanotfoundimg.setVisibility(View.GONE);
                txtimgntfnd.setVisibility(View.GONE);
                headerRow.setVisibility(View.VISIBLE);
            }

            // Show views after data is loaded
            tableLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
            totalAmountTextView.setVisibility(View.VISIBLE);
        });
    }

    private void showUpdateDeleteDialog(String expenseId, Expense expense, CollectionReference expensesRef) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_delete, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(true);

        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        EditText editTextCategory = dialogView.findViewById(R.id.editTextCategory);
        Spinner spinnerPaymentMethod = dialogView.findViewById(R.id.spinnerPaymentMethod);
        Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        editTextAmount.setText(expense.getAmount());
        editTextDate.setText(expense.getDate());
        editTextCategory.setText(expense.getCategory());

        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.spinner_item, getResources().getStringArray(R.array.payment_methods)) {
            @Override
            public boolean isEnabled(int position) {
                Utils.hideKeyboard(homepage.this);
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
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerPaymentMethod.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(expense.getPaymentMethod());
        spinnerPaymentMethod.setSelection(spinnerPosition);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(v -> {
            String updatedAmount = editTextAmount.getText().toString();
            String updatedDate = editTextDate.getText().toString();
            String updatedCategory = editTextCategory.getText().toString();
            String updatedPaymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

            if ( updatedDate.isEmpty() || updatedPaymentMethod.equals("Select Payment Method")) {
                Toast.makeText(homepage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (updatedCategory.isEmpty()) {
                updatedCategory = "-";
            }

            if (!isDateValid(updatedDate)) {
                editTextDate.setError("Use dd-MM-yy or you can't add Future Date!");
                return;
            }

            if (!updatedAmount.contains(".")) {
                updatedAmount = String.format("%s.00", updatedAmount);
            }
            if(!Utils.isValidAmount(updatedAmount)){
                editTextAmount.setError("Please Enter Valid Amount! ");
                editTextAmount.requestFocus();
                return;
            }

            Expense updatedExpense = new Expense(expenseId, updatedAmount, updatedDate, updatedCategory, updatedPaymentMethod);

            expensesRef.document(expenseId).set(updatedExpense).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Utils.showToast(this, "Expense updated successfully");
                    alertDialog.dismiss();
                } else {
                    Utils.showToast(this, "Failed to update expense");
                }
            });
        });

        buttonDelete.setOnClickListener(v -> {
            AlertDialog.Builder alertdelete = new AlertDialog.Builder(this);
            alertdelete.setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Yes, Delete", (dialog, which) -> {
                        expensesRef.document(expenseId).delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Utils.showToast(this, "Expense deleted successfully");
                                alertDialog.dismiss();
                            } else {
                                Utils.showToast(this, "Failed to delete expense");
                            }
                        });
                    })
                    .setNegativeButton("No, Cancel", null)
                    .setIcon(R.drawable.applogo)
                    .setCancelable(true);

            AlertDialog alert = alertdelete.create();

            // Set the custom background
            if (alert.getWindow() != null) {
                alert.getWindow().setBackgroundDrawableResource(R.drawable.customheader);
            }

            alert.show();
        });

    }



    //    Validation for Date
    private boolean isDateValid(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            // Get the current date
            Date currentDate = new Date();

            // Check if the parsed date is before or equal to the current date
            return !date.after(currentDate);
        } catch (Exception e) {
            return false;
        }
    }





    private void clickAddExpense() {
        findViewById(R.id.addexpense).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), addexpense.class);
            startActivity(intent);
        });
    }

    private void clickLogout() {
        findViewById(R.id.logout).setOnClickListener(v -> showLogoutDialog());
    }


    private void showLogoutDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    Toast.makeText(getApplicationContext(), "Logout successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), loginpage.class);
                    mAuth.signOut();
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("No, I don't want", null)
                .setIcon(R.drawable.applogo)
                .setCancelable(true);

        AlertDialog alertDialog = dialogBuilder.create();

        // Set the custom background
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.customheader);
        }

        alertDialog.show();
    }


    // Greeting Message
    private String getGreetingMessage() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int currentHour = currentDateTime.getHour();
        if (currentHour >= 5 && currentHour < 12) {
            return "Good Morning";
        } else if (currentHour >= 12 && currentHour < 17) {
            return "Good Afternoon";
        } else if (currentHour >= 17 && currentHour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }




}
