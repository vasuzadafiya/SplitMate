// src/com/example/splitpal/splash.java
package com.example.splitpal;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splash extends AppCompatActivity {
    private static final int WELCOME_DISPLAY_LENGTH = 2000;

    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1000);//for show splash screen
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);


        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), homepage.class);
            startActivity(intent);
            finishAffinity();
        }else {
            Intent intent = new Intent(splash.this, welcome.class);
            startActivity(intent);
            finish();
        }

    }

}
