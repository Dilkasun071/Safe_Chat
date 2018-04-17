package com.example.kasun.dilchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class RegPhoneActivity extends AppCompatActivity {

    private LinearLayout mLayUser1,mLayPhone1;
    private EditText et_user1,et_phone1,et_verify1;
    private ProgressBar pro_verify1,pro_phone1;
    private Button mSend1,logPhone;
    private TextView txt;
    private CountryCodePicker ccp;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mcallback;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String mVerificationId,name,cc,uid;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int bytecode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regphone);
        et_user1 = (EditText)findViewById(R.id.et_user);
        et_phone1 = (EditText)findViewById(R.id.et_phone);

        et_verify1 = (EditText)findViewById(R.id.et_verify);
        pro_verify1 = (ProgressBar)findViewById(R.id.pro_verify);

        mSend1 = (Button)findViewById(R.id.mSend);
        logPhone = (Button)findViewById(R.id.logPhone);

        txt = (TextView)findViewById(R.id.textView2);

        et_verify1.setVisibility(View.INVISIBLE);
        pro_verify1.setVisibility(View.INVISIBLE);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);

        mAuth = FirebaseAuth.getInstance();

        mSend1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bytecode == 0){
                    et_verify1.setVisibility(View.VISIBLE);
                    pro_verify1.setVisibility(View.VISIBLE);
                    ccp.registerCarrierNumberEditText(et_phone1);
                    cc = "+"+ccp.getFullNumber();
                    txt.setText(cc);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            cc, 60, TimeUnit.SECONDS, RegPhoneActivity.this, mcallback
                    );
                }else{
                    mSend1.setEnabled(false);
                    et_phone1.setEnabled(false);
                    ccp.setEnabled(false);

                    String verficationcode = et_verify1.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verficationcode);
                    signInWithPhoneAuthCredential(credential);
                }
                name = et_user1.getText().toString();
            }
        });

        mcallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                txt.setText("There was some error in verification");
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }

        };

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();
                            FirebaseUser curretn_user = FirebaseAuth.getInstance().getCurrentUser();
                            uid = curretn_user.getUid();
                            String device_token = FirebaseInstanceId.getInstance().getToken();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("name",name);
                            userMap.put("status","Hi There,I am using Dil Chat App");
                            userMap.put("image","Default Image");
                            userMap.put("thumb_image","Default Image");
                            userMap.put("device_token",device_token);
                            txt.setText(uid);
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //Start Intent
                                        Intent newUser = new Intent(RegPhoneActivity.this,MainActivity.class);
                                        startActivity(newUser);
                                    }
                                }
                            });
                        }else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                txt.setText("There was some error in verification");
                            }
                        }
                    }
                });
    }
    //onclick ->register user method
    private void register_user(String name, String cc) {
        }
}
