package com.example.kasun.dilchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private  Toolbar mtoolbar;
    private ProgressDialog mprogressbar;
    private EditText mStatus;
    private Button mButton;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //tool bar
        mtoolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get Extra value
        String status_value = getIntent().getStringExtra("status_value");

        //Real Time Database
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //Progress Bar
        mprogressbar = new ProgressDialog(this);

        //Set Previous Status on TextFile
        mStatus= (EditText)findViewById(R.id.status_update);
        mStatus.setText(status_value);

        //When click save changes
        mButton = (Button)findViewById(R.id.status_add);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress Dialog
                mprogressbar.setTitle("Registering User");
                mprogressbar.setMessage("Please Wait");
                mprogressbar.show();
                String status = mStatus.getText().toString();
                //Set Value
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mprogressbar.dismiss();
                            Intent newStatus = new Intent(StatusActivity.this,SettingActivity.class);
                            startActivity(newStatus);
                        }else{
                            Toast.makeText(getApplicationContext(),"There was some error in saving Changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }
}
