
package com.example.splitpal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class groupdetails extends AppCompatActivity {

    private static final String TAG = "GroupDetailsActivity";
    private TextView txtgroupname, txtgroupdcreatedDate, totalAmountTextView, btnAddGroupExpense, btnDeleteGroup, txtimgntfnd;
    private ImageView datanotfoundimg;
    private TableLayout tableLayout;
    private ListView displayans;
    private CardView cardviewresult;

    private FirebaseFirestore db;
    private String groupName, groupId, createdAt;
    private ArrayList<String> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupdetails);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve Intent extras
        groupName = getIntent().getStringExtra("groupName");
        members = getIntent().getStringArrayListExtra("members");
        groupId = getIntent().getStringExtra("groupId");
        createdAt = getIntent().getStringExtra("createdAt");

        // Initialize views
        txtgroupname = findViewById(R.id.txtgroupname);
        txtgroupdcreatedDate = findViewById(R.id.txtgroupdcreatedDate);
        datanotfoundimg = findViewById(R.id.datanotfoundimg);
        tableLayout = findViewById(R.id.tableLayout);
        btnAddGroupExpense = findViewById(R.id.btnAddGroupExpense);
        btnDeleteGroup = findViewById(R.id.btnDeleteGroup);
        datanotfoundimg = findViewById(R.id.datanotfoundimg);
        txtimgntfnd = findViewById(R.id.txtimgntfnd);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        cardviewresult = findViewById(R.id.cardviewresult);
        displayans = findViewById(R.id.displayinfo); // ListView to display the balances

        // Set data to views
        txtgroupname.setText(groupName);
        txtgroupdcreatedDate.setText("Created At: " + createdAt);

        // Load expenses for the group
        loadGroupExpenses(groupId, totalAmountTextView);

        // Set up buttons
        btnAddGroupExpense.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), addGroupExpense.class);
            intent1.putExtra("groupName", groupName);
            intent1.putStringArrayListExtra("members", members);
            intent1.putExtra("groupId", groupId);
            startActivity(intent1);
        });

        btnDeleteGroup.setOnClickListener(v -> deleteGroup(groupId));
    }

    private void loadGroupExpenses(String groupId, TextView totalAmountTextView) {
        // Check authentication
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        // Get userId for passing as a query
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        CollectionReference expensesRef = db.collection("users")
                .document(userId)
                .collection("groups")
                .document(groupId)
                .collection("expenses");

        // Snapshot listener for real-time data retrieval
        expensesRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Failed to load data.", e);
                return;
            }

            tableLayout.removeAllViews(); // Clear existing rows

            int count = 0;
            double totalAmount = 0.0;

            TableRow headerRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.rowheader, tableLayout, false);
            tableLayout.addView(headerRow);

            ArrayList<DocumentSnapshot> expenseList = new ArrayList<>();

            if (snapshots != null && !snapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : snapshots) {
                    expenseList.add(document);

                    GroupExpense expense = document.toObject(GroupExpense.class);

                    if (expense != null) {
                        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row, tableLayout, false);

                        TextView amountTextView = row.findViewById(R.id.amount_text_view);
                        TextView dateTextView = row.findViewById(R.id.date_text_view);
                        TextView categoryTextView = row.findViewById(R.id.category_text_view);
                        TextView paymentMethodTextView = row.findViewById(R.id.payment_method_text_view);

                        amountTextView.setText(expense.getAmount());
                        dateTextView.setText(expense.getDate());
                        categoryTextView.setText(expense.getCategory());
                        paymentMethodTextView.setText(expense.getPaidBy());

                        tableLayout.addView(row);
                        row.setOnClickListener(v -> showUpdateDeleteDialog(document.getId(), expense, expensesRef));

                        totalAmount += Double.parseDouble(expense.getAmount());
                        count++;
                    }
                }

                // Calculate and display expenses after loading them
                calculateAndSplitExpenses(expenseList);
            }

            // Handle data not found image
            if (count == 0) {
                datanotfoundimg.setVisibility(View.VISIBLE);
                txtimgntfnd.setVisibility(View.VISIBLE);
                headerRow.setVisibility(View.GONE);
                totalAmountTextView.setVisibility(View.GONE);
                cardviewresult.setVisibility(View.GONE);
                totalAmountTextView.setText("");
            } else {
                totalAmountTextView.setText("Total: ₹ " + String.format("%.2f", totalAmount));
                datanotfoundimg.setVisibility(View.GONE);
                txtimgntfnd.setVisibility(View.GONE);
                headerRow.setVisibility(View.VISIBLE);
            }

            tableLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
            totalAmountTextView.setVisibility(View.VISIBLE);
        });
    }

    private void showUpdateDeleteDialog(String expenseId, GroupExpense expense, CollectionReference expensesRef) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_delete, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(true);

        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        EditText editTextDate = dialogView.findViewById(R.id.editTextDate);
        EditText editTextCategory = dialogView.findViewById(R.id.editTextCategory);
        Spinner paidBy = dialogView.findViewById(R.id.spinnerPaymentMethod);

        Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        editTextAmount.setText(expense.getAmount());
        editTextDate.setText(expense.getDate());
        editTextCategory.setText(expense.getCategory());

        ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, members) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_item);
        paidBy.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(expense.getPaidBy());
        paidBy.setSelection(spinnerPosition);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(v -> {
            String updatedAmount = editTextAmount.getText().toString();
            String updatedDate = editTextDate.getText().toString();
            String updatedCategory = editTextCategory.getText().toString();
            String paidby = paidBy.getSelectedItem().toString();

            if (updatedDate.isEmpty()) {
                Utils.showToast(this, "Please enter a date");
                return;
            }

            if (updatedAmount.isEmpty()) {
                Utils.showToast(this, "Please enter an amount");
                return;
            }

            if (updatedCategory.isEmpty()) {
                Utils.showToast(this, "Please enter a category");
                return;
            }

            GroupExpense updatedExpense = new GroupExpense(updatedAmount, updatedDate, updatedCategory, paidby);
            expensesRef.document(expenseId).set(updatedExpense)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Expense updated!", Toast.LENGTH_SHORT).show();
                        loadGroupExpenses(groupId, totalAmountTextView); // Reload expenses
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error updating expense!", Toast.LENGTH_SHORT).show();
                    });
        });

        buttonDelete.setOnClickListener(v -> {
            expensesRef.document(expenseId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getApplicationContext(), "Expense deleted!", Toast.LENGTH_SHORT).show();
                        loadGroupExpenses(groupId, totalAmountTextView); // Reload expenses
                        alertDialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error deleting expense!", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    private void deleteGroup(String groupId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Delete '" + groupName + "' Group?")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton("Yes, Delete this group", (dialog, which) -> {

                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    // Reference to the group document
                    DocumentReference groupRef = FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .collection("groups")
                            .document(groupId);

                    //   Delete all documents in the "expenses" subcollection
                    groupRef.collection("expenses").get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot expenseDoc : task.getResult()) {
                                // Delete each document in the "expenses" subcollection
                                groupRef.collection("expenses").document(expenseDoc.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firebase", "Expense document successfully deleted!");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("Firebase", "Error deleting expense document", e);
                                        });
                            }

                            // Step 2: After deleting the subcollection, delete the group document
                            groupRef.delete().addOnSuccessListener(aVoid -> {
                                String temp = groupName + " Deleted Successfully";
                                Utils.showToast(this, temp);
                                finish();
                            }).addOnFailureListener(e -> {
                                Utils.showToast(this, e.toString());
                            });

                        } else {
                            Utils.showToast(this, "Error getting expenses documents");
                            Log.w("Firebase", "Error getting expenses documents", task.getException());
                        }
                    });
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





    // New method to calculate and split expenses
    private void calculateAndSplitExpenses(ArrayList<DocumentSnapshot> expenses) {
        HashMap<String, Double> balances = new HashMap<>();

        // Initialize balances for each member
        for (String member : members) {
            balances.put(member, 0.0);
        }

        // Calculate each member's balance
        for (DocumentSnapshot document : expenses) {
            Double amount = null;
            try {
                String amountStr = document.getString("amount");
                if (amountStr != null) {
                    amount = Double.parseDouble(amountStr);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing amount for document: " + document.getId(), e);
                continue;
            }

            String paidBy = document.getString("paidBy");
            if (paidBy != null && amount != null) {
                double splitAmount = amount / members.size();

                for (String member : members) {
                    if (member.equals(paidBy)) {
                        balances.put(member, balances.get(member) + (amount - splitAmount));
                    } else {
                        balances.put(member, balances.get(member) - splitAmount);
                    }
                }
            }
        }

        // Calculate and display who pays whom
        settleDebts(balances);
    }

    // New method to settle debts
    private void settleDebts(HashMap<String, Double> balances) {
        ArrayList<String> owes = new ArrayList<>();
        ArrayList<String> getsPaid = new ArrayList<>();

        // Split balances into debtors (negative) and creditors (positive)
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0) {
                getsPaid.add(entry.getKey());
            } else if (entry.getValue() < 0) {
                owes.add(entry.getKey());
            }
        }

        ArrayList<String> transactions = new ArrayList<>();

        int i = 0, j = 0;
        while (i < owes.size() && j < getsPaid.size()) {
            String debtor = owes.get(i);
            String creditor = getsPaid.get(j);

            double debtAmount = Math.abs(balances.get(debtor));
            double creditAmount = balances.get(creditor);

            double minAmount = Math.min(debtAmount, creditAmount);

            // Record transaction
            String transaction = debtor + " pays " + creditor + " ₹ " + String.format("%.2f", minAmount);
            transactions.add(transaction);

            // Adjust balances
            balances.put(debtor, balances.get(debtor) + minAmount);
            balances.put(creditor, balances.get(creditor) - minAmount);

            // Move to next debtor/creditor
            if (balances.get(debtor) == 0) i++;
            if (balances.get(creditor) == 0) j++;
        }

        // Display transactions in ListView
        displayTransactions(transactions);
    }

    // New method to display the transactions in ListView
    private void displayTransactions(ArrayList<String> transactions) {
        cardviewresult.setVisibility(View.VISIBLE);

        // Custom ArrayAdapter to display each transaction
        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.list_item, transactions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                }

                TextView textView = convertView.findViewById(R.id.groupName);
                textView.setText((CharSequence) getItem(position));
                textView.setTextColor(0xFF8C1313);

                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextSize(22);

                return convertView;
            }
        };

        displayans.setAdapter(adapter);
    }
    //something is missing for delete group
}
