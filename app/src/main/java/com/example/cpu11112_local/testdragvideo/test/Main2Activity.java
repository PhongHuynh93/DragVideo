package com.example.cpu11112_local.testdragvideo.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.cpu11112_local.testdragvideo.R;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void OpenTest(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
