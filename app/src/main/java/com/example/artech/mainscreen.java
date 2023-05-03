package com.example.artech;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.artech.databinding.ActivityMainscreenBinding;
import com.example.artech.geoapi.hellogeospatial.hellogeoactivity;

public class mainscreen extends AppCompatActivity {

    ActivityMainscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // changing the color of the status bar and hiding the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.skin));



        binding.screenar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, com.example.artech.geoapi.hellogeospatial.hellogeoactivity.class);
                startActivity(intent);
            }
        });


        binding.screencalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://www.auk.edu.kw/academics/academic-calendar");
                startActivity(intent);
            }
        });

        binding.screenschedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://ssb-prod.ec.auk.edu.kw/PROD/bwskfshd.P_CrseSchd");
                startActivity(intent);
            }
        });

        binding.screenweb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://www.auk.edu.kw");
                startActivity(intent);
            }
        });

        binding.screenservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://ssb-prod.ec.auk.edu.kw/PROD/twbkwbis.P_GenMenu?name=bmenu.P_MainMnu");
                startActivity(intent);
            }
        });

        binding.screendegree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://dw-prod.ec.auk.edu.kw/responsiveDashboard/worksheets/WEB31");
                startActivity(intent);
            }
        });


        binding.screenadver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainscreen.this, webviewactivity.class);
                intent.putExtra("link", "https://lms.auk.edu.kw/course/view.php?id=3321&section=1");
                startActivity(intent);
            }
        });


    }

}