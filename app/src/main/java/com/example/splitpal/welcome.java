package com.example.splitpal;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class welcome extends AppCompatActivity {

    private static final int WELCOME_DISPLAY_LENGTH = 5600;
    private static final int FADE_DURATION = 1400; // Duration for fade in/out animations

    private TextView welcomeText2;
    private ViewSwitcher viewSwitcher;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome);

        mainHandler = new Handler(Looper.getMainLooper()); // Use the main looper

        viewSwitcher = findViewById(R.id.viewSwitcher);
        TextView welcomeText1 = findViewById(R.id.welcome_text);
        welcomeText2 = findViewById(R.id.welcome_text2);

        // Set initial visibility and alpha values
        welcomeText1.setVisibility(View.VISIBLE);
        welcomeText2.setVisibility(View.INVISIBLE);
        welcomeText2.setAlpha(0f);

        // Create fade-in and fade-out animations using ObjectAnimator
        ObjectAnimator fadeIn1 = ObjectAnimator.ofFloat(welcomeText1, "alpha", 0f, 1f);
        fadeIn1.setDuration(FADE_DURATION);

        ObjectAnimator fadeOut1 = ObjectAnimator.ofFloat(welcomeText1, "alpha", 1f, 0f);
        fadeOut1.setDuration(FADE_DURATION);

        ObjectAnimator fadeIn2 = ObjectAnimator.ofFloat(welcomeText2, "alpha", 0f, 1f);
        fadeIn2.setDuration(FADE_DURATION);

        ObjectAnimator fadeOut2 = ObjectAnimator.ofFloat(welcomeText2, "alpha", 1f, 0f);
        fadeOut2.setDuration(FADE_DURATION);

        // Start the fade-in animation for the first TextView
        fadeIn1.start();

        // Chain animations to switch views and fade out the first view
        mainHandler.postDelayed(() -> {
            fadeOut1.start();

            mainHandler.postDelayed(() -> {
                viewSwitcher.showNext(); // Switch to the second view
                welcomeText2.setVisibility(View.VISIBLE);
                fadeIn2.start();

                // Schedule fade-out for the second view
                mainHandler.postDelayed(() -> {
                    fadeOut2.start();
                }, FADE_DURATION);

            }, FADE_DURATION);
        }, FADE_DURATION);

        // Move to the next activity after the total duration
        mainHandler.postDelayed(() -> {
            Intent intent = new Intent(welcome.this, loginpage.class);
            startActivity(intent);
            finish();
        }, WELCOME_DISPLAY_LENGTH);
    }
}
