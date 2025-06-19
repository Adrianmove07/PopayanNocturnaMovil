package com.example.popayan_noc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.popayan_noc.R;

public class LandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        final ImageView logo = findViewById(R.id.logoImageView);
        final TextView title = findViewById(R.id.titleText);
        final TextView desc = findViewById(R.id.descText);
        final Button btnLogin = findViewById(R.id.btnLogin);
        final Button btnRegister = findViewById(R.id.btnRegister);

        // Eliminar referencias a estrellas eliminadas del layout
        // ImageView star1 = findViewById(R.id.star1);
        // ImageView star2 = findViewById(R.id.star2);
        // ImageView star3 = findViewById(R.id.star3);

        // Animación de entrada para logo, título, descripción y botones
        logo.setAlpha(0f);
        title.setAlpha(0f);
        desc.setAlpha(0f);
        btnLogin.setAlpha(0f);
        btnRegister.setAlpha(0f);

        
        logo.animate().alpha(1f).setDuration(800).withEndAction(new Runnable() {
            @Override
            public void run() {
                title.startAnimation(AnimationUtils.loadAnimation(LandingActivity.this, R.anim.slide_up));
                title.animate().alpha(1f).setDuration(600).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Animación dinámica para el texto descriptivo
                        desc.startAnimation(AnimationUtils.loadAnimation(LandingActivity.this, R.anim.text_dynamic));
                        desc.animate().alpha(1f).setDuration(600).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                btnLogin.startAnimation(AnimationUtils.loadAnimation(LandingActivity.this, R.anim.slide_up));
                                btnLogin.animate().alpha(1f).setDuration(400);
                                btnRegister.startAnimation(AnimationUtils.loadAnimation(LandingActivity.this, R.anim.slide_up));
                                btnRegister.animate().alpha(1f).setDuration(400);
                            }
                        });
                    }
                });
            }
        });

        // PushDownAnim en botones
//        PushDownAnim.setPushDownAnimTo(btnLogin)
//                .setScale(PushDownAnim.MODE_STATIC_DP, 6f)
//                .setDurationPush(100)
//                .setDurationRelease(100)
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });

//        PushDownAnim.setPushDownAnimTo(btnRegister)
//                .setScale(PushDownAnim.MODE_STATIC_DP, 6f)
//                .setDurationPush(100)
//                .setDurationRelease(100)
                btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(LandingActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
