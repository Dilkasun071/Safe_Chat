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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegActivity extends AppCompatActivity {
    //Firebase Auth
    private FirebaseAuth mAuth;
    //EditText And Button => Register
    private Button mCreate;
    private EditText muser,memail,mpass;
    //Toolbar
    private Toolbar mtoolbar;
    //Progress Dialog
    private ProgressDialog mprogressbar;
    //Realtime Firebase Database
    private DatabaseReference mDatabase;
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //tool bar
        mtoolbar = (Toolbar)findViewById(R.id.reg_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Edit Text Data Entered
        muser = (EditText) findViewById(R.id.reg_display);
        memail = (EditText) findViewById(R.id.reg_email);
        mpass = (EditText) findViewById(R.id.reg_pass);
        //Button Initializing
        mCreate = (Button)findViewById(R.id.reg_create_btn);
        //Progressbar
        mprogressbar = new ProgressDialog(this);

        //Onclick => Register
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = muser.getText().toString();
                String email = memail.getText().toString();
                String pass = mpass.getText().toString();

                mpass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            if (mpass.getText().toString().trim().length() < 6) {
                                mpass.setError("Password Should Be More Than 6 Characters");
                                //Toast.makeText(RegActivity.this, "Password Should Be More Than 6 Characters", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                });

                //progressbar
                if(!TextUtils.isEmpty(username) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(pass)){
                    if(mpass.length() < 6){
                        mpass.setError("Password Should Be More Than 6 Characters");
                    }else if(!isEmailValid(memail.toString())){
                        memail.setError("Enter The Vaild Email");
                    }else{
                        mprogressbar.setTitle("Registering User");
                        mprogressbar.setMessage("Please Wait");
                        mprogressbar.setCanceledOnTouchOutside(false);
                        mprogressbar.show();
                    }
                    //pass Data into register_user method
                    register_user(username,email,pass);
                }
            }
        });
    }
    //onclick ->register user method
    private void register_user(final String username, String email, String pass) {
        //Verify Email and Email and Password
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Current Firebase user
                    FirebaseUser curretn_user = FirebaseAuth.getInstance().getCurrentUser();
                    //Current User's ID
                    String uid = curretn_user.getUid();
                    //Current Device's ID
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    //Database Reference
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    //Do Hash Map<String,String>
                    HashMap<String,String> userMap = new HashMap<>();
                    //Send Data
                    userMap.put("name",username);
                    userMap.put("status","Hi There,I am using Dil Chat App");
                    userMap.put("image","Default Image");
                    userMap.put("thumb_image","Default Image");
                    userMap.put("device_token",device_token);
                    //If Data is seted
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Then Progress Bar dismissed
                                mprogressbar.dismiss();
                                Intent mainIntent = new Intent(RegActivity.this,MainActivity.class);
                                //if back button in phone app not want to log
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });
                }else{

                    //Then Progress Bar Hide
                    mprogressbar.hide();
                    Toast.makeText(RegActivity.this,"You Got Some Error",Toast.LENGTH_LONG);
                }
            }
        });
    }
}
