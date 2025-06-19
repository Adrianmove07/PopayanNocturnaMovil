package com.example.popayan_noc.activity;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.popayan_noc.R;
import com.example.popayan_noc.util.AuthUtils;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

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