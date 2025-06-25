package com.example.popayan_noc.activity;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.popayan_noc.R;
import com.example.popayan_noc.util.AuthUtils;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_splash);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    AuthUtils.saveTokenDevice(this, token);
                    Log.d(TAG, "Token actual: " + token);
                });

        String token = AuthUtils.getToken(this);
        Object user = AuthUtils.getUser(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Este método se ejecutará después del tiempo de SPLASH_TIME_OUT
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                Intent j = new Intent(SplashActivity.this, MainActivity.class);
                if(token == null){
                    startActivity(i);
                }else{
                startActivity(j);}
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}