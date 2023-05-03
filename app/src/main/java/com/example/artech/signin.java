package com.example.artech;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.artech.databinding.ActivityMainscreenBinding;
import com.example.artech.databinding.ActivitySigninBinding;

public class signin extends AppCompatActivity {

    ActivitySigninBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.skin));

        binding.backsigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signin.this, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.siginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.siginemail.getText();
                binding.siginpass.getText();
                Intent intent = new Intent(signin.this, mainscreen.class);
                startActivity(intent);
            }
        });

        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signin.this, signup.class);
                startActivity(intent);
            }
        });


    }
}