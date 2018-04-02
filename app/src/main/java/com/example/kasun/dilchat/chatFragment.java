package com.example.kasun.dilchat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class chatFragment extends Fragment {
    private String mCurrent_user_id;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mConvDatabase;
    private View mMainView;
    private RecyclerView mConvList;
    public chatFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {// Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_chat, container, false);
        mConvList = (RecyclerView) mMainView.findViewById(R.id.mConvList);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUserDatabase.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);
        return mMainView;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onStart() {
        super.onStart();

        Query converstationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerAdapter<Conv,ConvViewHolder > firebaseRecyclerAdapter= new
                FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
                        Conv.class,R.layout.users_single_layout,ConvViewHolder.class,converstationQuery
                ) {
                    @Override
                    protected void populateViewHolder(final ConvViewHolder viewHolder, final Conv model, int position) {
                        final String list_user_id = getRef(position).getKey();
                        Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);
                        lastMessageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                // String data = dataSnapshot.child("messages").getValue().toString();
                                // viewHolder.setMessage(data,model.isSeen());
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
                        mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userName = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                if(dataSnapshot.hasChild("online")){
                                    String userOnline = dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setUserOnline(userOnline);

                                }
                                viewHolder.setName(userName);
                                viewHolder.setStatus(status);
                                viewHolder.setUserImage(userThumb,getContext());
                                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (i == 0) {
                                                    Intent profile = new Intent(getContext(), ProfileActivity.class);
                                                    profile.putExtra("user_id", list_user_id);
                                                    startActivity(profile);
                                                }
                                                if (i == 1) {
                                                    Intent profile1 = new Intent(getContext(), chatActivity.class);
                                                    profile1.putExtra("user_id", list_user_id);
                                                    // profile1.putExtra("username", userName);
                                                    startActivity(profile1);
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };
        mConvList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class ConvViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setMessage(String message,boolean isSeen){
            TextView userText = (TextView) mView.findViewById(R.id.usi_status);
            userText.setText(message);
            if(!isSeen){
                userText.setTypeface(userText.getTypeface(), Typeface.BOLD);
            }else{
                userText.setTypeface(userText.getTypeface(), Typeface.NORMAL);
            }
        }
        public void setName(String name){
            TextView userName1 = (TextView) mView.findViewById(R.id.usi_disname);
            userName1.setText(name);
        }
        public void setUserOnline(String userOnline) {
            ImageView useronline = (ImageView)mView.findViewById(R.id.user_online);
            if(userOnline == "true"){
                useronline.setVisibility(View.VISIBLE);
            }else{
                useronline.setVisibility(View.INVISIBLE);
            }
        }

        public void setStatus(String statusa) {
            TextView status = (TextView) mView.findViewById(R.id.usi_status);
            status.setText(statusa);
        }

        public void setUserImage(String userThumb, Context ctx) {
            ImageView img = (ImageView) mView.findViewById(R.id.user_imageid);
            Picasso.with(ctx).load(userThumb).placeholder(R.drawable.de).into(img);
        }
    }
}
