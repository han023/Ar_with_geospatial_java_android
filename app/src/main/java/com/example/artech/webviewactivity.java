package com.example.artech;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebViewClient;

import com.example.artech.databinding.ActivityMainscreenBinding;
import com.example.artech.databinding.ActivityWebviewactivityBinding;

public class webviewactivity extends AppCompatActivity {

    ActivityWebviewactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebviewactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.skin));

        Intent intent = getIntent();
        String myData = intent.getStringExtra("link");

        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.webview.getSettings().setSafeBrowsingEnabled(true);
        }
        binding.webview.loadUrl(myData);
        binding.webview.setWebViewClient(new WebViewClient());


    }


    @Override
    public void onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
}