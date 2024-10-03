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
    import com.google.firebase.firestore.WriteBatch;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Locale;
    import java.util.Map;
    import java.util.Objects;

    public class groupdetails extends AppCompatActivity {

        private static final String TAG = "GroupDetailsActivity";
        private TextView txtgroupname, txtgroupdcreatedDate,totalAmountTextView, btnAddGroupExpense, btnDeleteGroup, txtimgntfnd;
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
            totalAmountTextView=findViewById(R.id.totalAmountTextView);
            cardviewresult=findViewById(R.id.cardviewresult);
            displayans = findViewById(R.id.displayinfo); // ListView to display the balances

            // Set data to views
            txtgroupname.setText(groupName);
            txtgroupdcreatedDate.setText("Created At: " + createdAt);

            // Load expenses for the group
            loadGroupExpenses(groupId,totalAmountTextView);

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
    //checking authentication
    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
        return;
    }

    //getting userid for passing as a query
    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference expensesRef = db.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .collection("expenses");

//    snapshot listener for realtime data retrieval
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
                String tem = "total count found : "+count;

                Utils.showToast(this,tem);

                calculateAndSplitExpenses(expenseList);
            }
        }

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
            androidx.appcompat.app.AlertDialog.Builder dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
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

            androidx.appcompat.app.AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            buttonUpdate.setOnClickListener(v -> {
                String updatedAmount = editTextAmount.getText().toString();
                String updatedDate = editTextDate.getText().toString();
                String updatedCategory = editTextCategory.getText().toString();
                String paidby = paidBy.getSelectedItem().toString();

                if (updatedDate.isEmpty()) {
                    Utils.showToast(this, "Please fill in all fields");
                    return;
                }

                if (updatedCategory.isEmpty()) {
                    updatedCategory = "-";
                }

                if (!updatedAmount.contains(".")) {
                    updatedAmount = String.format("%s.00", updatedAmount);
                }
                if(!Utils.isValidAmount(updatedAmount)){
                    editTextAmount.setError("Please Enter Valid Amount!");
                    editTextAmount.requestFocus();
                    return;
                }

                if (!isDateValid(updatedDate)) {
                    editTextDate.setError("Use dd-MM-yy or you can't add Future Date!");
                    return;
                }



                GroupExpense updatedGroupExpense = new GroupExpense(updatedAmount, updatedDate, updatedCategory, paidby);

                expensesRef.document(expenseId).set(updatedGroupExpense).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Expense updated successfully");
                        alertDialog.dismiss();
                    } else {
                        Utils.showToast(this, "Failed to update expense");
                        Log.d(TAG, "Failed to update expense");
                    }
                });
            });

            buttonDelete.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog.Builder alertdelete = new androidx.appcompat.app.AlertDialog.Builder(this);
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

                androidx.appcompat.app.AlertDialog alert = alertdelete.create();

                // Set the custom background
                if (alert.getWindow() != null) {
                    alert.getWindow().setBackgroundDrawableResource(R.drawable.customheader);
                }

                alert.show();
            });

        }

        private void calculateAndSplitExpenses(ArrayList<DocumentSnapshot> expenses) {
            // Create a map to hold the balance for each member
            HashMap<String, Double> balances = new HashMap<>();

            // Initialize balances for each member to 0.0
            for (String member : members) {
                balances.put(member, 0.0);
            }


            // Iterate over each expense and update balances
            for (DocumentSnapshot document : expenses) {
                Double amount = null;

                try {
                    // Get the amount as a string and parse it to double
                    String amountStr = document.getString("amount");
                    if (amountStr != null) {
                        amount = Double.parseDouble(amountStr);
                    } else {
                        throw new IllegalArgumentException("Amount is null for document: " + document.getId());
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing amount for document: " + document.getId(), e);
                    continue; // Skip this expense if there's an error
                }

                String paidBy = document.getString("paidBy");

                if (paidBy != null && amount != null) {
                    // Split the amount equally among all members
                    double splitAmount = amount / members.size();

                    // Deduct split amount from each member's balance and add the full amount to the payer's balance
                    for (String member : members) {
                        if (member.equals(paidBy)) {
                            balances.put(member, balances.get(member) + (amount - splitAmount));
                        } else {
                            balances.put(member, balances.get(member) - splitAmount);
                        }
                    }
                }
            }

            // Update ListView with balances
            updateListViewWithBalances(balances);
        }



        private void updateListViewWithBalances(HashMap<String, Double> balanceMap) {
            cardviewresult.setVisibility(View.VISIBLE);

            // Convert the balance map to a list of entries
            ArrayList<Map.Entry<String, Double>> balanceList = new ArrayList<>(balanceMap.entrySet());

            // Sort the list to have positive balances (should be paid) at the top
            balanceList.sort((entry1, entry2) -> {
                double balance1 = entry1.getValue();
                double balance2 = entry2.getValue();

                // Positive balances should come before negative balances
                if (balance1 > 0 && balance2 <= 0) return -1;
                if (balance1 <= 0 && balance2 > 0) return 1;

                // Sort by balance value in descending order
                return Double.compare(balance2, balance1);
            });

            // Custom ArrayAdapter
            ArrayAdapter<Map.Entry<String, Double>> adapter = new ArrayAdapter<Map.Entry<String, Double>>(this, R.layout.list_item, balanceList) {
                @SuppressLint("DefaultLocale")
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // Get the current entry
                    Map.Entry<String, Double> entry = getItem(position);

                    // Inflate the custom layout if necessary
                    if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
                    }

                    // Get the TextView from the custom layout
                    TextView textView = convertView.findViewById(R.id.groupName);

                    if (entry != null) {
                        String member = entry.getKey();
                        double balance = entry.getValue();
                        String displayText = "";

                        if (balance > 0.01) {
                            displayText = member + " get Paid : " + String.format("%.2f", balance);
                            textView.setTextColor(0xFF1F4E05); // Set text color to green for positive balance
                        } else if (balance < -0.01) {
                            displayText = member + " Owes : " + String.format("%.2f", Math.abs(balance));
                            textView.setTextColor(0xFF8C1313); // Set text color to red for negative balance
                        } else { // Handle zero balance
                            displayText = member + " is settled up";
                            textView.setTextColor(Color.BLACK); // Set text color to black for zero balance
                        }

                        // Set the display text
                        textView.setText(displayText);
                        textView.setTypeface(Typeface.DEFAULT_BOLD);
                        textView.setTextSize(22);
                    }

                    return convertView;
                }
            };

            // Set the adapter to the ListView
            displayans.setAdapter(adapter);
        }


private void deleteGroup(String groupId) {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle("Delete '" + groupName + "' Group?")
            .setMessage("Are you sure you want to delete this group?")
            .setPositiveButton("Yes, Delete this group", (dialog, which) -> {

                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                DocumentReference groupRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .collection("groups")
                        .document(groupId);

                deleteGroupWithSubcollections(groupRef);

                finish();
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

        private void deleteGroupWithSubcollections(DocumentReference groupRef) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Recursively delete all subcollections
            groupRef.collection("expenses").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        batch.delete(document.getReference());
                    }
                    batch.commit().addOnCompleteListener(expenseDeleteTask -> {
                        if (expenseDeleteTask.isSuccessful()) {
                            // Delete the group document after deleting all expenses
                            groupRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("TAG", "Group and its expenses successfully deleted!");
                                        Utils.showToast(this, "Group deleted successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("TAG", "Error deleting group document", e);
                                        Utils.showToast(this, "Failed to delete group");
                                    });
                        } else {
                            Log.w("TAG", "Error deleting expenses", expenseDeleteTask.getException());
                            Utils.showToast(this, "Failed to delete expenses");
                        }
                    });
                } else {
                    Log.w("TAG", "Error retrieving expenses", task.getException());
                    Utils.showToast(this, "Failed to retrieve expenses");
                }
            });
        }

//    Validation for Date.
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

    }
