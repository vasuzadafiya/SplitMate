//package com.example.splitpal;
//
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.os.Bundle;
//import android.text.InputType;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//public class createGroup extends AppCompatActivity {
//
//    private static final String UID = "uid";
//    private static final String GROUP_NAME = "groupname";
//    private static final String MEMBERS = "members";
//    private static final String CREATED_AT = "createdAt";
//    private static final String COLLECTIONPATH = "groups";
//
//    private EditText editTextGroupName;
//    private EditText editTextNumberOfMembers;
//    private LinearLayout membersContainer;
//    private Button buttonAddMembers,buttonSubmit;
//    private TextView clickadd;
//    private ArrayList<EditText> memberEditTexts = new ArrayList<>();
//    private ProgressBar progressBar;
//
//    private FirebaseFirestore db;
//    private FirebaseAuth auth;
//    private String user;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.creategroup);
//
//        editTextGroupName = findViewById(R.id.Groupname);
//        editTextNumberOfMembers = findViewById(R.id.groupmember);
//        membersContainer = findViewById(R.id.membersContainer);
//        buttonAddMembers = findViewById(R.id.btn_addmember);
//        buttonSubmit = findViewById(R.id.btn_submit);
//        clickadd = findViewById(R.id.clickadd);
//        progressBar = findViewById(R.id.progressbar);
//
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        user = auth.getUid();
//
//        buttonAddMembers.setOnClickListener(v -> addMemberFields());
//        buttonSubmit.setOnClickListener(v -> submitGroup());
//    }
//
//    private void addMemberFields() {
//        buttonAddMembers.setVisibility(View.GONE);
//        editTextNumberOfMembers.setVisibility(View.GONE);
//        clickadd.setVisibility(View.GONE);
//
//        membersContainer.removeAllViews(); // Clear existing views
//        memberEditTexts.clear(); // Clear existing EditText references
//
//        clickadd.setVisibility(View.GONE); // Hide the click to add text view
//
//        String numberOfMembersString = editTextNumberOfMembers.getText().toString();
//        if (TextUtils.isEmpty(numberOfMembersString)) {
//            maketoast("Please enter the number of members.");
//            return;
//        }
//
//        int numberOfMembers;
//        try {
//            numberOfMembers = Integer.parseInt(numberOfMembersString);
//            if (numberOfMembers <= 0) {
//                maketoast("Number of members must be greater than zero.");
//                buttonAddMembers.setVisibility(View.VISIBLE);
//                editTextNumberOfMembers.setVisibility(View.VISIBLE);
//                return;
//            }
//        } catch (NumberFormatException e) {
//            maketoast("Invalid number format. Please enter a valid number.");
//            buttonAddMembers.setVisibility(View.VISIBLE);
//            editTextNumberOfMembers.setVisibility(View.VISIBLE);
//            return;
//        }
//
//        // Add header TextView for dynamic fields
//        TextView headerTextView = new TextView(this);
//        headerTextView.setText("Enter Member Names");
//        headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
//        headerTextView.setTypeface(null, Typeface.BOLD);
//        headerTextView.setTextColor(Color.parseColor("#353C1D"));
//        membersContainer.addView(headerTextView);
//
//        // Create EditText fields for each member
//        for (int i = 0; i < numberOfMembers; i++) {
//            EditText editText = new EditText(this);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            params.setMargins(0, 0, 0, 50); // Margin of 50dp between fields
//            editText.setLayoutParams(params);
//
//            editText.setBackgroundResource(R.drawable.custum_input);
//            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user, 0, 0, 0);
//            editText.setCompoundDrawablePadding(10);
//            editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
//            editText.setHint("Member " + (i + 1) + " Name");
//            editText.setInputType(InputType.TYPE_CLASS_TEXT);
//            editText.setPadding(10, 10, 10, 10);
//            editText.setTextColor(Color.parseColor("#353C1D"));
//            editText.setHintTextColor(Color.parseColor("#698474"));
//            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
//            editText.setTypeface(null, Typeface.BOLD);
//
//            membersContainer.addView(editText);
//            memberEditTexts.add(editText);
//        }
//    }
//
//    private void submitGroup() {
//        String groupName = editTextGroupName.getText().toString().trim();
//        String userId = user ;
//
//        if (TextUtils.isEmpty(groupName)) {
//            editTextGroupName.setError("Please enter the group name");
//            editTextGroupName.requestFocus();
//            return;
//        }
//
//        String numberOfMembersString = editTextNumberOfMembers.getText().toString();
//        if (TextUtils.isEmpty(numberOfMembersString)) {
//            editTextNumberOfMembers.setError("Please enter the number of members");
//            return;
//        }
//
//        buttonAddMembers.setVisibility(View.GONE);
//        editTextNumberOfMembers.setVisibility(View.GONE);
//        clickadd.setVisibility(View.GONE);
//
//        progressBar.setVisibility(View.VISIBLE);
//
//        ArrayList<String> memberNames = new ArrayList<>();
//        for (EditText editText : memberEditTexts) {
//            String name = editText.getText().toString().trim();
//            if (!name.isEmpty()) {
//                memberNames.add(name);
//            } else {
//                editText.setError("Please enter member name");
//                editText.requestFocus();
//                return;
//            }
//        }
//
//        if (memberNames.isEmpty()) {
//            maketoast("Please enter at least one member name.");
//            return;
//        }
//
//        // Check if the group name already exists for the current user
//        if (userId != null) {
//            checkIfGroupNameExists(userId, groupName, memberNames);
//        } else {
//            maketoast("User not authenticated.");
//            progressBar.setVisibility(View.GONE);
//        }
//    }
//
//    private void checkIfGroupNameExists(String userId, String groupName, ArrayList<String> memberNames) {
//        db.collection(COLLECTIONPATH)
//                .whereEqualTo(UID, userId)
//                .whereEqualTo(GROUP_NAME, groupName)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        // Group name already exists for this user
//                        maketoast("You already have a group with this name. Please choose a different name.");
//                        progressBar.setVisibility(View.GONE);
//                    } else {
//                        // Group name does not exist for this user, proceed to create
//                        createGroupInFirestore(userId, groupName, memberNames);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    maketoast("Error checking group name: " + e.getMessage());
//                    progressBar.setVisibility(View.GONE);
//                });
//    }
//
//    private void createGroupInFirestore(String userId, String groupName, ArrayList<String> memberNames) {
//        Map<String, Object> groupData = new HashMap<>();
//        groupData.put(CREATED_AT, getCurrenttime());
//        groupData.put(MEMBERS, memberNames);
//        groupData.put(GROUP_NAME, groupName);
//        groupData.put(UID, userId);
//
//        db.collection(COLLECTIONPATH)
//                .add(groupData)
//                .addOnSuccessListener(documentReference -> {
//                    // Group created successfully
//                    maketoast("Group Created Successfully!");
//                    clearAllFields();
//                    progressBar.setVisibility(View.GONE);
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    maketoast("Failed to create group: " + e.getMessage());
//                    buttonAddMembers.setVisibility(View.VISIBLE);
//                    editTextNumberOfMembers.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.GONE);
//                });
//    }
//
//    private void maketoast(String msg) {
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//    }
//
//    private void clearAllFields() {
//        // Clear the group name EditText
//        editTextGroupName.setText("");
//
//        // Clear the number of members EditText
//        editTextNumberOfMembers.setText("");
//
//        // Clear the dynamic member EditText fields
//        for (EditText editText : memberEditTexts) {
//            editText.setText("");
//        }
//
//        // Optionally, reset the visibility of the click add TextView
//        clickadd.setVisibility(View.VISIBLE);
//        buttonAddMembers.setVisibility(View.VISIBLE);
//        editTextNumberOfMembers.setVisibility(View.VISIBLE);
//
//        // Optionally, remove all member EditText views
//        membersContainer.removeAllViews();
//    }
//
//    private static String getCurrenttime() {
//        LocalDateTime myDateObj = LocalDateTime.now();
//        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm");
//        return myDateObj.format(myFormatObj);
//    }
//}
package com.example.splitpal;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class createGroup extends AppCompatActivity {

    private static final String UID = "uid";
    private static final String GROUP_NAME = "groupname";
    private static final String MEMBERS = "members";
    private static final String CREATED_AT = "createdAt";
    private static final String USERS_COLLECTION = "users";  // Users collection name
    private static final String GROUPS_SUBCOLLECTION = "groups";  // Groups subcollection name

    private EditText editTextGroupName;
    private EditText editTextNumberOfMembers;
    private LinearLayout membersContainer;
    private TextView buttonAddMembers, buttonSubmit;
    private ArrayList<EditText> memberEditTexts = new ArrayList<>();
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creategroup);

        editTextGroupName = findViewById(R.id.Groupname);
        editTextNumberOfMembers = findViewById(R.id.groupmember);
        membersContainer = findViewById(R.id.membersContainer);
        buttonAddMembers = findViewById(R.id.btn_addmember);
        buttonSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progressbar);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        buttonAddMembers.setOnClickListener(v -> addMemberFields());
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(areMemberNamesUnique()){
                    submitGroup();
                }

            }
        });
    }

    private void addMemberFields() {


        if(editTextNumberOfMembers.getText().toString().isEmpty()){
            maketoast("Please enter the number of members.");
            editTextNumberOfMembers.requestFocus();
            return;
        }


        buttonAddMembers.setVisibility(View.GONE);
        editTextNumberOfMembers.setVisibility(View.GONE);


        membersContainer.removeAllViews(); // Clear existing views
        memberEditTexts.clear(); // Clear existing EditText references



        String numberOfMembersString = editTextNumberOfMembers.getText().toString();
        if (TextUtils.isEmpty(numberOfMembersString)) {
            maketoast("Please enter the number of members.");
            return;
        }

        int numberOfMembers;
        try {
            numberOfMembers = Integer.parseInt(numberOfMembersString);
            if (numberOfMembers < 0) {
                maketoast("Number of members must be greater than zero.");
                buttonAddMembers.setVisibility(View.VISIBLE);
                editTextNumberOfMembers.setVisibility(View.VISIBLE);
                buttonAddMembers.setVisibility(View.VISIBLE);
                return;
            }
            if(numberOfMembers >30){
                editTextNumberOfMembers.setError("Number of members must be less than 30.");
                editTextNumberOfMembers.requestFocus();
                buttonAddMembers.setVisibility(View.VISIBLE);
                editTextNumberOfMembers.setVisibility(View.VISIBLE);
                buttonAddMembers.setVisibility(View.VISIBLE);
                return;
            }
        } catch (NumberFormatException e) {
            maketoast("Invalid number format. Please enter a valid number.");
            buttonAddMembers.setVisibility(View.VISIBLE);
            editTextNumberOfMembers.setVisibility(View.VISIBLE);
            buttonAddMembers.setVisibility(View.VISIBLE);
            return;
        }
        Utils.hideKeyboard(this);

        // Add header TextView for dynamic fields
        TextView headerTextView = new TextView(this);
        headerTextView.setText("Enter Member Names");
        headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        headerTextView.setTypeface(null, Typeface.BOLD);
        headerTextView.setTextColor(Color.parseColor("#353C1D"));
        membersContainer.addView(headerTextView);

        // Create EditText fields for each member
        for (int i = 0; i < numberOfMembers; i++) {
            EditText editText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 50); // Margin of 50dp between fields
            editText.setLayoutParams(params);

            editText.setBackgroundResource(R.drawable.custum_input);
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_user, 0, 0, 0);
            editText.setCompoundDrawablePadding(10);
            editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            editText.setHint("Member " + (i + 1) + " Name");
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setPadding(10, 10, 10, 10);
            editText.setTextColor(Color.parseColor("#353C1D"));
            editText.setHintTextColor(Color.parseColor("#698474"));
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            editText.setTypeface(null, Typeface.BOLD);

            membersContainer.addView(editText);
            memberEditTexts.add(editText);
        }

        buttonSubmit.setVisibility(View.VISIBLE);
    }

    private boolean areMemberNamesUnique() {
        Set<String> memberNames = new HashSet<>();
        for (EditText editText : memberEditTexts) {
            String name = editText.getText().toString().trim();
            if (!memberNames.add(name)) {
                editText.setError("Please enter unique member name");
                editText.requestFocus();
                return false; // Duplicate found
            }
        }
        return true; // All names are unique
    }

    private void submitGroup() {
        String groupName = editTextGroupName.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            editTextGroupName.setError("Please enter the group name");
            editTextGroupName.requestFocus();
            return;
        }

        String numberOfMembersString = editTextNumberOfMembers.getText().toString();
        if (TextUtils.isEmpty(numberOfMembersString)) {
            editTextNumberOfMembers.setError("Please enter the number of members");
            return;
        }

        buttonAddMembers.setVisibility(View.GONE);
        editTextNumberOfMembers.setVisibility(View.GONE);


        progressBar.setVisibility(View.VISIBLE);

        ArrayList<String> memberNames = new ArrayList<>();
        for (EditText editText : memberEditTexts) {
            String name = editText.getText().toString().trim();
            if (!name.isEmpty()) {
                memberNames.add(name);
            } else {
                editText.setError("Please enter member name");
                editText.requestFocus();
                progressBar.setVisibility(View.GONE);
                return;
            }
        }

        if (memberNames.isEmpty()) {
            maketoast("Please enter at least one member name.");
            return;
        }

        // Check if the group name already exists for the current user
        if (userId != null) {
            checkIfGroupNameExists(userId, groupName, memberNames);
        } else {
            maketoast("User not authenticated.");
            progressBar.setVisibility(View.GONE);
        }
    }

    private void checkIfGroupNameExists(String userId, String groupName, ArrayList<String> memberNames) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(GROUPS_SUBCOLLECTION)
                .whereEqualTo(GROUP_NAME, groupName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Group name already exists for this user
                        maketoast("You already have a group with this name. Please choose a different name.");
                        progressBar.setVisibility(View.GONE);
                    } else {
                        // Group name does not exist for this user, proceed to create
                        createGroupInFirestore(userId, groupName, memberNames);
                    }
                })
                .addOnFailureListener(e -> {
                    maketoast("Error checking group name: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void createGroupInFirestore(String userId, String groupName, ArrayList<String> memberNames) {
        Map<String, Object> groupData = new HashMap<>();
        groupData.put(CREATED_AT, getCurrenttime());
        groupData.put(MEMBERS, memberNames);
        groupData.put(GROUP_NAME, groupName);
        groupData.put(UID, userId);

        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(GROUPS_SUBCOLLECTION)
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    // Group created successfully
                    maketoast("Group Created Successfully!");
                    clearAllFields();
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    maketoast("Failed to create group: " + e.getMessage());
                    buttonAddMembers.setVisibility(View.VISIBLE);
                    editTextNumberOfMembers.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void maketoast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void clearAllFields() {
        // Clear the group name EditText
        editTextGroupName.setText("");

        // Clear the number of members EditText
        editTextNumberOfMembers.setText("");

        // Clear the dynamic member EditText fields
        for (EditText editText : memberEditTexts) {
            editText.setText("");
        }

        // Optionally, reset the visibility of the click add TextView
        buttonAddMembers.setVisibility(View.VISIBLE);
        editTextNumberOfMembers.setVisibility(View.VISIBLE);

        // Optionally, remove all member EditText views
        membersContainer.removeAllViews();
    }

    private static String getCurrenttime() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy 'at' hh:mma");
        return myDateObj.format(myFormatObj);
    }
}
