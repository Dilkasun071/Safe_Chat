package com.example.kasun.dilchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private EditText uname,pass;
    private Button log_btn;
    private ProgressDialog mprogressbar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //tool bar
        mtoolbar = (Toolbar)findViewById(R.id.log_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //text Field
        uname = (EditText)findViewById(R.id.log_user);
        pass = (EditText)findViewById(R.id.log_pass);
        log_btn =  (Button)findViewById(R.id.log_login_btn);

        //Database Ref
        dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //progress bar
        mprogressbar = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //onclick event
        log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = uname.getText().toString();
                String password = pass.getText().toString();
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    mprogressbar.setTitle("Login User");
                    mprogressbar.setMessage("Please Wait");
                    mprogressbar.setCanceledOnTouchOutside(false);
                    mprogressbar.show();
                    login_user(email,password);
                }
            }
        });

    }

    //login method
    private void login_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mprogressbar.dismiss();
                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    dbRef.child(current_user_id).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent logIntent = new Intent(LoginActivity.this,MainActivity.class);
                            //if back button in phone app not want to log
                            logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(logIntent);
                            finish();
                       }
                    });
                }else{
                    mprogressbar.hide();
                    Toast.makeText(LoginActivity.this,"Can't Sign",Toast.LENGTH_LONG);
                }
            }
        });
    }
}
