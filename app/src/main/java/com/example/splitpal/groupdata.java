package com.example.splitpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class groupdata extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    ListView GroupListView;
    ProgressBar progressBar;
    ImageView datanotfound;
    TextView txtimgntfnd;

    private static final String USERS_COLLECTION = "users";
    private static final String GROUPS_SUBCOLLECTION = "groups";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.groupsdata);

        GroupListView = findViewById(R.id.groupdisplay);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressbar);
        datanotfound = findViewById(R.id.datanotfoundimg);
        txtimgntfnd = findViewById(R.id.txtimgntfnd);

        fetchAndDisplayGroupNames();
        clickCreateGroup();
        clickListView();
    }

    private void clickListView() {
        GroupListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGroupName = (String) parent.getItemAtPosition(position);

            if (user != null) {
                String userId = user.getUid(); // Get the current user's UID

                // Get a reference to the Firestore database
                CollectionReference groupsRef = firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(GROUPS_SUBCOLLECTION);

                // Query to find the document with the matching group name
                groupsRef.whereEqualTo("groupname", selectedGroupName).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming group names are unique)
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Retrieve the document ID (groupId)
                        String documentId = documentSnapshot.getId();
                        String uid = documentSnapshot.getString("uid");

                        // Retrieve fields from the document
                        String createdAt = documentSnapshot.getString("createdAt");
                        if (createdAt == null) {
                            createdAt = "N/A";
                        }

                        // Retrieve the "members" field
                        List<String> members = (List<String>) documentSnapshot.get("members");
                        if (members == null) {
                            members = new ArrayList<>();
                        }

                        // Create an intent to start the group details activity
                        Intent intent = new Intent(groupdata.this, groupdetails.class);
                        intent.putExtra("groupId", documentId); // Pass the document ID (groupId)
                        intent.putExtra("groupName", selectedGroupName);
                        intent.putExtra("createdAt", createdAt);
                        intent.putExtra("uid", uid);
                        intent.putStringArrayListExtra("members", new ArrayList<>(members));

                        // Start the group details activity
                        startActivity(intent);
                    } else {
                        // Handle the case where no document was found
                        Toast.makeText(groupdata.this, "Group not found", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    // Handle the failure case
                    Toast.makeText(groupdata.this, "Error fetching group data", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(groupdata.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            }
        });
    }

private void fetchAndDisplayGroupNames() {
    List<String> groupNames = new ArrayList<>();
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.groupName, groupNames);

    GroupListView.setAdapter(adapter);

    progressBar.setVisibility(View.GONE);

    if (user != null) {
        String userId = user.getUid(); // Get the current user's UID

        // Listen for real-time updates
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(GROUPS_SUBCOLLECTION)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.d("GroupFetch", "Listen failed: ", e);
                        Toast.makeText(getApplicationContext(), "Failed to fetch group names.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    groupNames.clear(); // Clear the list to avoid duplicates

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String groupName = document.getString("groupname"); // Ensure this is the correct field name
                            if (groupName != null) {
                                groupNames.add(groupName);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        GroupListView.setVisibility(View.VISIBLE);
                        datanotfound.setVisibility(View.GONE);
                        txtimgntfnd.setVisibility(View.GONE);

                    } else {
                        GroupListView.setVisibility(View.GONE);
                        datanotfound.setVisibility(View.VISIBLE);
                        txtimgntfnd.setVisibility(View.VISIBLE);

                        groupNames.clear(); // Clear the list if no groups are found
                        adapter.notifyDataSetChanged();
                    }
                });
    } else {
        Toast.makeText(getApplicationContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
    }
}
    private void clickCreateGroup() {
        findViewById(R.id.CreateGroup).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), createGroup.class);
            startActivity(intent);
        });
    }
}
