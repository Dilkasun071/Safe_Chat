package com.example.kasun.dilchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar toolbar1;
    //Database Reference
    private DatabaseReference mRoofRef,mR,mMsg;
    //Firebase Auth
    private FirebaseAuth mAuth;
    //TextView,ImageView
    private TextView mTitleview,mLastseen;
    private TextView chat_chat;
    private ImageView mProfile;
    private ImageView chat_add;
    private ImageView chat_send;
    //Recycle View
    private RecyclerView message;
    //Chat User
    private String mChatUser;
    //userName
    private String userName;
    //User ID
    private String mCurrentUserID;
    //Pagnation
    private int mCurrentPage = 0;
    private int itempos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private TextView message_time_layout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mlinerlayout;
    private MessageAdapter msgadpter;

    private SwipeRefreshLayout mRefreshLayout;
    private StorageReference mImageStorage;

    private static final int GALLERY_PICK = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Tool Bar
        toolbar1 = (Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(toolbar1);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //Firebase Database Reference
        mRoofRef = FirebaseDatabase.getInstance().getReference();
        mR = FirebaseDatabase.getInstance().getReference().child("Users");
        mMsg = FirebaseDatabase.getInstance().getReference().child("messages");
        //Firebase Author
        mAuth = FirebaseAuth.getInstance();
        //Get Intent
        mChatUser = getIntent().getStringExtra("user_id");
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        userName = getIntent().getStringExtra("username");

        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.mRefreshLayout);

        msgadpter = new MessageAdapter(messagesList);

        message = (RecyclerView)findViewById(R.id.message_list);

        mlinerlayout = new LinearLayoutManager(this);

        message.setHasFixedSize(true);
        message.setLayoutManager(mlinerlayout);
        message.setAdapter(msgadpter);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);
        //TextView,ImageView
        mTitleview = (TextView)findViewById(R.id.chat_disname);
        mLastseen = (TextView)findViewById(R.id.chat_lastseen);
        mProfile = (ImageView)findViewById(R.id.chat_profile);
        chat_add = (ImageView)findViewById(R.id.chat_add);
        chat_chat = (TextView)findViewById(R.id.chat_chat);
        chat_send = (ImageView)findViewById(R.id.chat_send);
        message_time_layout = (TextView)findViewById(R.id.message_time_layout);
        //Set Text
        mTitleview.setText(userName);
        mR.child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeerrrrrrrrrrr
                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                mTitleview.setText(name);
                Picasso.with(chatActivity.this).load(userThumb).placeholder(R.drawable.de).into(mProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

                //mProfile
        //Load Message
        loadMessage();
        //start

        //end
        mRoofRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                //set Data
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                //If is Online
                if(online.equals("true")){
                    mLastseen.setText("online");
                }else{
                    //Get Time
                    GetTimeAgo gtago = new GetTimeAgo();
                    long lasttime = Long.parseLong(online);
                    String lastlog = gtago.getTimeAgo(lasttime,getApplicationContext());
                    //set Last Time
                    mLastseen.setText(lastlog);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRoofRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserID+"/"+mChatUser,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserID,chatAddMap);

                    mRoofRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                chat_chat.setText("");
            }
        });
        chat_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                itempos = 0;
                loadMoreMessage();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && requestCode == RESULT_OK){
            Uri imageuri = data.getData();
            final String current_user_ref = "messages/"+mCurrentUserID+"/"+mChatUser;
            final String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserID;

            DatabaseReference user_message_push = mRoofRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id+".jpg");
            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String download_url = task.getResult().getDownloadUrl().toString();
                        Map messageMap =    new HashMap();
                        messageMap.put("message",message);
                        messageMap.put("seen",false);
                        messageMap.put("type","text");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserID);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);


                        mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                }
                            }
                        });

                    }
                }
            });

        }
    }

    private void loadMoreMessage() {
        DatabaseReference messageRef = mRoofRef.child("messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(itempos++,messages);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itempos++,messages);
                }else{
                    mPrevKey = mLastKey;
                }
                if(itempos == 1){
                    mLastKey = messageKey;
                }
                Log.d("TOTALKEYS","LAST Key"+mLastKey + "] Prev Key"+mPrevKey+"] Message Key " + messageKey);
                msgadpter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mlinerlayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessage() {
        mRoofRef.child("messages").child(mCurrentUserID).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                itempos++;

                if(itempos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(messages);
                msgadpter.notifyDataSetChanged();

                message.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = chat_chat.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/"+mCurrentUserID+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserID;

            DatabaseReference user_message_push = mRoofRef.child("messages")
                    .child(mCurrentUserID).child(mChatUser).push();
            String push_id = user_message_push.getKey();
            Map messageMap =    new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);


            mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}
