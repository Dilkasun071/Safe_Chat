package com.example.kasun.dilchat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.AccessControlContext;

import static java.security.AccessController.getContext;

public class UserActivity extends AppCompatActivity {
    //Toolbar
    private Toolbar mtoolbar;
    //RecycleView
    private RecyclerView mUserList;
    //Database reference
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDatabase1;
    //Firebase Author
    private FirebaseAuth fb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        fb = FirebaseAuth.getInstance();
        //Current ID
        String mcurrent = fb.getCurrentUser().getUid();
        //Database Reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users");
        //Recycle View
        mUserList = (RecyclerView)findViewById(R.id.user_app_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        //Tool bar
        mtoolbar = (Toolbar)findViewById(R.id.alluser_app_bar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Adapter
        FirebaseRecyclerAdapter<Users,UsersViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUserDatabase1
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());

                viewHolder.setStatus(model.getStatus());
                final String list_user_id = getRef(position).getKey();
                mUserDatabase1.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        viewHolder.setUserImage(userThumb,getApplicationContext());
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        viewHolder.setName(userName);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                //viewHolder.setThumb(model.getThumb(),getApplicationContext());

                final String user_id = getRef(position).getKey();
                //Click and Go Profile
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profile = new Intent(UserActivity.this,ProfileActivity.class);
                        profile.putExtra("user_id",user_id);
                        startActivity(profile);
                    }
                });
            }
        };
        //Set Adapter
        mUserList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView mUserNameView = mView.findViewById(R.id.usi_disname);
            mUserNameView.setText(name);
        }
        public void setStatus(String status){
            TextView mUserNameView = mView.findViewById(R.id.usi_status);
            mUserNameView.setText(status);
        }

        public void setUserImage(String userThumb, Context ctx) {
            ImageView im = (ImageView)mView.findViewById(R.id.user_imageid);
            Picasso.with(ctx).load(userThumb).placeholder(R.drawable.de).into(im);
        }
    }
}
