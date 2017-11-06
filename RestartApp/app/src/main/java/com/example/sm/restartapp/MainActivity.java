package com.example.sm.restartapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        count=getIntent().getIntExtra("count", 0);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,count));

        if (getIntent().getBooleanExtra("crash", false)) {
            Toast.makeText(this, "App restarted after crash"+count, Toast.LENGTH_SHORT).show();
        }
    }
    public void crashMe(View v) {
        throw new NullPointerException();
    }
}
