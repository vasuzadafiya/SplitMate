package com.example.splitpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class loginpage extends AppCompatActivity {

    private static final String TAG = "PhoneAuth";

    private Button button, btnSendOtp, btnVerifyOtp, btnreg;
    private TextView tvsignup, tvphoneLogin, tvlogintrigger, tvheadlogin, tvheadreg,tvheadphone;
    private EditText edtemail, edtpassword, edtPhoneNumber, edtOtp, edtregemail, edtregpassword, confirmregpassword;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHomepage();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        initializeFirebaseAuth();
        setupCallbacks();
        setupViewListeners();
    }

    private void initializeViews() {
        tvheadphone = findViewById(R.id.tvheadloginwithphone);
        button = findViewById(R.id.btnlogin);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvsignup = findViewById(R.id.tvsignup);
        edtemail = findViewById(R.id.edtemail);
        edtpassword = findViewById(R.id.edtpassword);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtOtp = findViewById(R.id.edtOtp);
        tvphoneLogin = findViewById(R.id.tvphoneLogin);
        progressBar = findViewById(R.id.progressbar);
        tvlogintrigger = findViewById(R.id.tvlogintrigger);
        tvheadlogin = findViewById(R.id.tvheadlogin);
        tvheadreg = findViewById(R.id.tvheadreg);
        edtregemail = findViewById(R.id.edtregemail);
        edtregpassword = findViewById(R.id.edtregpassword);
        confirmregpassword = findViewById(R.id.confirmregpassword);
        btnreg = findViewById(R.id.btnregester);


        // Initially hide phone number input, OTP input, and OTP button
        edtPhoneNumber.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        edtOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvphoneLogin.setVisibility(View.VISIBLE);
    }

    private void initializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupCallbacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                Log.w(TAG, "onVerificationFailed", e);
                handleVerificationFailure(e);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                loginpage.this.verificationId = verificationId;
                resendToken = token;
                progressBar.setVisibility(View.GONE);
                edtOtp.setVisibility(View.VISIBLE);
                btnVerifyOtp.setVisibility(View.VISIBLE);
                btnSendOtp.setVisibility(View.GONE);
                Toast.makeText(loginpage.this, "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupViewListeners() {
        btnreg.setOnClickListener(v -> registerUser());

        tvlogintrigger.setOnClickListener(v -> showLoginForm());

        button.setOnClickListener(v -> loginUser());

        tvsignup.setOnClickListener(v -> showRegistrationForm());

        tvphoneLogin.setOnClickListener(v -> showPhoneLoginForm());

        btnSendOtp.setOnClickListener(v -> sendOtp());

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    private void registerUser() {
        String email = edtregemail.getText().toString().trim();
        String password = edtregpassword.getText().toString().trim();
        String confirmPass = confirmregpassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            showToast("Please Fill all fields");
            return;
        }

        if (!password.equals(confirmPass)) {
            showToast("Password Does Not Match!");
            return;
        }
        if (password.length() < 6) {
            edtregpassword.setError("Password must be at least 6 characters long");
            edtregpassword.requestFocus();
            return;
        }
        if (!password.matches(".*[A-Z].*")) {
            edtregpassword.setError("Password must contain at least one uppercase letter");
            edtregpassword.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            edtregpassword.setError("Password must contain at least one lowercase letter");
            edtregpassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                                              addUserToFirestore(user);

                        showToast("Welcome to SplitMate Family");
                        navigateToHomepage();
                    } else {
                        showToast("Authentication failed. Maybe user already exists.");
                    }
                });
    }

    private void loginUser() {
        String email = edtemail.getText().toString().trim();
        String password = edtpassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtemail.setError("Please enter your email address");
            edtemail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtemail.setError("Please enter a valid email address");
            edtemail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtpassword.setError("Please enter your password");
            edtpassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edtpassword.setError("Password must be at least 6 characters long");
            edtpassword.requestFocus();
            return;
        }



        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showToast("Welcome back!");

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            addUserToFirestore(user);
                        }

                        navigateToHomepage();
                    } else {
                        showToast("Authentication Failed");
                    }
                });
    }

    private void showLoginForm() {
        tvheadlogin.setVisibility(View.VISIBLE);
        edtemail.setVisibility(View.VISIBLE);
        edtpassword.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        tvphoneLogin.setVisibility(View.VISIBLE);
        tvsignup.setVisibility(View.VISIBLE);

        // Hide registration and phone login form
        tvlogintrigger.setVisibility(View.GONE);
        tvheadphone.setVisibility(View.GONE);
        edtPhoneNumber.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        edtOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvheadreg.setVisibility(View.GONE);
        edtregemail.setVisibility(View.GONE);
        edtregpassword.setVisibility(View.GONE);
        confirmregpassword.setVisibility(View.GONE);
        btnreg.setVisibility(View.GONE);
    }

    private void showRegistrationForm() {
        tvheadphone.setVisibility(View.GONE);
        tvheadlogin.setVisibility(View.GONE);
        edtemail.setVisibility(View.GONE);
        edtpassword.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        tvsignup.setVisibility(View.GONE);
        tvphoneLogin.setVisibility(View.VISIBLE);
        tvlogintrigger.setVisibility(View.VISIBLE);
        tvheadreg.setVisibility(View.VISIBLE);
        edtregemail.setVisibility(View.VISIBLE);
        edtregpassword.setVisibility(View.VISIBLE);
        confirmregpassword.setVisibility(View.VISIBLE);
        btnreg.setVisibility(View.VISIBLE);

        // Hide phone login form
        edtPhoneNumber.setVisibility(View.GONE);
        btnSendOtp.setVisibility(View.GONE);
        edtOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
    }

    private void showPhoneLoginForm() {
       tvheadphone.setVisibility(View.VISIBLE);
        tvheadlogin.setVisibility(View.GONE);
        edtemail.setVisibility(View.GONE);
        edtpassword.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        tvsignup.setVisibility(View.VISIBLE);
        tvlogintrigger.setVisibility(View.VISIBLE);
        edtPhoneNumber.setVisibility(View.VISIBLE);
        btnSendOtp.setVisibility(View.VISIBLE);
        edtOtp.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvheadreg.setVisibility(View.GONE);
        edtregemail.setVisibility(View.GONE);
        edtregpassword.setVisibility(View.GONE);
        tvphoneLogin.setVisibility(View.GONE);
        confirmregpassword.setVisibility(View.GONE);
        btnreg.setVisibility(View.GONE);
    }

private void sendOtp() {
    String phoneNumber = edtPhoneNumber.getText().toString().trim();

    if (phoneNumber.isEmpty()) {
        edtPhoneNumber.setError("Please enter phone number");
        edtPhoneNumber.requestFocus();
        return;
    }

    if (phoneNumber.length() != 10) {
        edtPhoneNumber.setError("Please enter a valid 10-digit phone number");
        edtPhoneNumber.requestFocus();
        return;
    }

    // Adding +91 country code if not already present
    if (!phoneNumber.startsWith("+91")) {
        phoneNumber = "+91" + phoneNumber;
    }

    progressBar.setVisibility(View.VISIBLE);

    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)         // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS)   // Timeout and unit
            .setActivity(this)                   // Activity (for callback binding)
            .setCallbacks(callbacks)             // OnVerificationStateChangedCallbacks
            .build();

    PhoneAuthProvider.verifyPhoneNumber(options);
    btnSendOtp.setVisibility(View.GONE);
}


    private void verifyOtp() {
        String otp = edtOtp.getText().toString().trim();

        if (otp.isEmpty()) {
            edtOtp.setError("Please enter OTP");
            edtOtp.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        showToast("Login Successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            addUserToFirestore(user);
                        }
                        navigateToHomepage();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            showToast("Invalid OTP");
                        } else {
                            showToast("Authentication Failed");

                        }
                    }
                });
    }

    private void navigateToHomepage() {
        Intent intent = new Intent(loginpage.this, homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }

    private void handleVerificationFailure(FirebaseException e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Invalid Phone Number");
        } else {
            showToast("Verification Failed: " + e.getMessage());
            Log.e(TAG, "Verification Failed", e);
        }
    }


    private void showToast(String message) {
        Toast.makeText(loginpage.this, message, Toast.LENGTH_SHORT).show();
    }

private void addUserToFirestore(FirebaseUser user) {
    String uid = user.getUid();
    String email = user.getEmail();
    String phoneNumber = user.getPhoneNumber();

    Map<String, Object> userMap = new HashMap<>();
    userMap.put("UID", uid);
    userMap.put("Email", email);
    userMap.put("Phone Number", phoneNumber);

    db.collection("users").document(uid).get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d(TAG, "User already exists in Firestore");
                // Optionally update specific fields
                Log.d("TAG", "User already exists in Firestore");
                db.collection("users").document(uid).set(userMap, SetOptions.merge());
            } else {
                // If the user doesn't exist, create a new document
                db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User added to Firestore with UID: " + uid))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding user to Firestore", e));
            }
        } else {
            Log.d(TAG, "Failed to check if user exists: ", task.getException());
        }
    });
}

}
