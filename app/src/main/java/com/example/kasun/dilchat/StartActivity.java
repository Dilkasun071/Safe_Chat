package com.example.kasun.dilchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class StartActivity extends AppCompatActivity {
    private Button mRegBtn,btnlog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        // Button Initialize
        mRegBtn = (Button)findViewById(R.id.start_reg_btn);
        //If Clicked Register Button
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reg_intent = new Intent(StartActivity.this,RegPhoneActivity.class);
                startActivity(reg_intent);
            }
        });
    }

    public void login(View view){
        Intent log = new Intent(StartActivity.this,LoginActivity.class);
        startActivity(log);
    }

}
