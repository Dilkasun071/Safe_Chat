package com.example.kasun.dilchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private TextView profile_total,profile_name,profile_status;
    private Button sendReq,delReq;
    private ImageView mProfileImage;

    //for request
    private String currentState;
    //Database Reference
    private DatabaseReference mDatabaseRefernece;
    private DatabaseReference mFriendRefernece;
    private DatabaseReference mNotificationRefernece;
    //Firebase Users
    private FirebaseUser mFirebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Get Intent Data
        final String user_id = getIntent().getStringExtra("user_id");
        //Database Reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        //TextView,ImageView,Button
        profile_total = (TextView)findViewById(R.id.profile_total);
        profile_name = (TextView)findViewById(R.id.profile_disname);
        profile_status = (TextView)findViewById(R.id.profile_status);
        mProfileImage = (ImageView)findViewById(R.id.mProfileImage);
        sendReq = (Button)findViewById(R.id.profile_request);
        delReq = (Button)findViewById(R.id.profile_decline);
        //for request
        currentState = "not_friend";
        //Database Reference
        mDatabaseRefernece = FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendRefernece = FirebaseDatabase.getInstance().getReference().child("Friend");
        mNotificationRefernece = FirebaseDatabase.getInstance().getReference().child("Notification");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //Progress Bar
        final ProgressDialog mprogressbar = new ProgressDialog(this);
        mprogressbar.setTitle("Loading User Data");
        mprogressbar.setMessage("Please Wait");
        mprogressbar.show();
        //Add data
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String Sname = dataSnapshot.child("name").getValue().toString();
                String Sstatus = dataSnapshot.child("status").getValue().toString();
                final String Simg = dataSnapshot.child("image").getValue().toString();
                //String Sthmub = dataSnapshot.child("thumb_image").getValue().toString();
                profile_name.setText(Sname);
                profile_status.setText(Sstatus);
                //Load Image
                Picasso.with(ProfileActivity.this).load(Simg).placeholder(R.drawable.de).into(mProfileImage);
                //Friend List
                mDatabaseRefernece.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("recieved")){
                                currentState = "req_recieved";
                                sendReq.setText("Accept Friend Request");
                                //delReq.setVisibility(View.VISIBLE);
                                //delReq.setEnabled(true);
                            }else if(req_type.equals("sent")){
                                currentState = "req_sent";
                                sendReq.setText("Cancel Friend Request");
                                //delReq.setVisibility(View.INVISIBLE);
                                //delReq.setEnabled(false);
                            }
                        }else{
                            mDatabaseRefernece.child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        currentState = "friend";
                                        sendReq.setText("Unfriend This Person");
                                        //delReq.setVisibility(View.INVISIBLE);
                                        //delReq.setEnabled(false);
                                    }
                                    mprogressbar.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mprogressbar.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mprogressbar.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            sendReq.setEnabled(false);
                if(currentState.equals("not_friend")){
                    mDatabaseRefernece.child(mFirebaseUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mDatabaseRefernece.child(user_id).child(mFirebaseUser.getUid()).child("request_type")
                                        .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Notification
                                        HashMap<String,String> notificationData = new HashMap<>();
                                        notificationData.put("from",mFirebaseUser.getUid());
                                        notificationData.put("type","request");
                                        mNotificationRefernece.child(user_id).push()
                                                .setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                currentState = "req_sent";
                                                sendReq.setText("Cancel Friend Request");
                                                //Toast.makeText(ProfileActivity.this,"Request Success",Toast.LENGTH_LONG).show();
                                                delReq.setVisibility(View.INVISIBLE);
                                                delReq.setEnabled(false);
                                            }
                                        });

                                    }
                                });
                            }
                            sendReq.setEnabled(true);
                        }
                    });
                }
                if(currentState.equals("req_sent")){
                    mDatabaseRefernece.child(mFirebaseUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabaseRefernece.child(user_id).child(mFirebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    sendReq.setEnabled(false);
                                    currentState = "not_friend";
                                    sendReq.setText("Send Friend Request");
                                    delReq.setVisibility(View.INVISIBLE);
                                    delReq.setEnabled(false);
                                }
                            });
                        }
                    });
                }
                if(currentState.equals("req_recieved")){
                    final String  currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendRefernece.child(mFirebaseUser.getUid()).child(user_id).child("date").setValue(currentDateTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRefernece.child(user_id).child(mFirebaseUser.getUid()).child("date").setValue(currentDateTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabaseRefernece.child(mFirebaseUser.getUid()).child(user_id).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDatabaseRefernece.child(user_id).child(mFirebaseUser.getUid()).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendReq.setEnabled(false);
                                                    currentState = "not_friend";
                                                    sendReq.setText("Send Friend Request");
                                                    delReq.setVisibility(View.INVISIBLE);
                                                    delReq.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }


}
